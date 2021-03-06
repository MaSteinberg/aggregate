/*
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.format.structure.flexibleExport;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.lang3.StringUtils;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.ExportTemplateConfig;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.structure.flexibleExport.models.*;
import org.opendatakit.aggregate.odktables.flexibleExport.SemanticsTable;
import org.opendatakit.aggregate.server.GenerateHeaderInfo;
import org.opendatakit.aggregate.server.ExportTemplateConfigManager;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.IntStream;

import static org.opendatakit.aggregate.datamodel.FormElementModel.ElementType.*;

/**
 * @author Markus Steinberg
 */
public class TemplateFormatterWithFilters implements SubmissionFormatter {
    public static final String ONTOLOGY_REF_PREFIX = "_onto_";
    public static final String COLUMN_REF_PREFIX = "_col_";
    //Relative to src/main/resources
    private static String TEMPLATE_ROOT_DIR = "templateExport/mustache_templates";

    //Can be overwritten by the selected template
    public String filetype = ServletConsts.TEMPLATE_FILENAME_TYPE_FALLBACK;

    private ElementFormatter elemFormatter;
    private List<FormElementModel> columnFormElementModelsFiltered;
    private List<FormElementModel> columnFormElementModelsUnfiltered;
    private final IForm form;
    private final PrintWriter output;
    private List<FormElementNamespace> namespaces;
    private String templateGroup;
    private Map<String, Map<String, String>> semantics; //(fieldName -> (propertyName -> propertyValue))

    private String baseURI;
    private boolean requireRowUUIDs;
    private int rowCounter = 1;

    private MustacheFactory mf;
    private Mustache toplevelIdentifierMustache;
    private Mustache columnIdentifierMustache;
    private Mustache rowIdentifierMustache;
    private Mustache cellIdentifierMustache;

    private Mustache namespacesMustache;
    private Mustache toplevelMustache;
    private Mustache columnMustache;
    private Mustache rowMustache;
    private Mustache genericCellMustache;
    private Mustache terminationMustache;

    private Map<FormElementModel.ElementType, Mustache> elementTypeToCellMustacheMap;

    private boolean firstRow = true;
    private ModelBuilder modelBuilder = new ModelBuilder();
    private TopLevelModel toplevelModel;
    private List<ColumnModel> columnModels = new ArrayList<>();


    public TemplateFormatterWithFilters(IForm xform, String webServerUrl, PrintWriter printWriter,
                                        FilterGroup filterGroup, String baseURI, Boolean requireRowUUID, String templateGroup) {
        form = xform;
        output = printWriter;

        this.baseURI = baseURI;
        this.requireRowUUIDs = requireRowUUID;
        this.templateGroup = templateGroup;

        //Extract information from the form
        SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());
        GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
        headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
        columnFormElementModelsFiltered = headerGenerator.getIncludedElements();
        namespaces = headerGenerator.includedFormElementNamespaces();
        elemFormatter = new BasicElementFormatter(false, true, true, false);

        //We have to store the unfiltered FormElementModels because the semantics might reference a column that
        //is removed by the filters
        FilterGroup noFilter = new FilterGroup(UIConsts.FILTER_NONE, this.form.getFormId(), null);
        GenerateHeaderInfo headerGeneratorUnfiltered = new GenerateHeaderInfo(noFilter, summary, form);
        headerGeneratorUnfiltered.processForHeaderInfo(form.getTopLevelGroupElement());
        columnFormElementModelsUnfiltered = headerGeneratorUnfiltered.getIncludedElements();

        //Initialize Mustache & compile the templates
        String templateGroupRoot = TEMPLATE_ROOT_DIR + "/" + this.templateGroup;
        mf = new DefaultMustacheFactory();
        //Identifier templates
        this.toplevelIdentifierMustache = mf.compile(TEMPLATE_ROOT_DIR +"/common/toplevelIdentifier.mustache");
        this.columnIdentifierMustache = mf.compile(TEMPLATE_ROOT_DIR + "/common/columnIdentifier.mustache");
        this.rowIdentifierMustache = mf.compile(TEMPLATE_ROOT_DIR + "/common/rowIdentifier.mustache");
        this.cellIdentifierMustache = mf.compile(TEMPLATE_ROOT_DIR + "/common/cellIdentifier.mustache");
        //Turtle templates
        this.namespacesMustache = mf.compile(templateGroupRoot + "/namespaces.mustache");
        this.toplevelMustache = mf.compile(templateGroupRoot + "/toplevel.mustache");
        this.columnMustache = mf.compile(templateGroupRoot + "/column.mustache");
        this.rowMustache = mf.compile(templateGroupRoot + "/row.mustache");
        this.genericCellMustache = mf.compile(templateGroupRoot + "/cell.mustache");
        this.terminationMustache = mf.compile(templateGroupRoot + "/termination.mustache");
        //Assign and compile the cell templates
        elementTypeToCellMustacheMap = new HashMap();
        String cellTemplateRoot = templateGroupRoot + "/elementTypeCells/";
        elementTypeToCellMustacheMap.put(DECIMAL, mf.compile(cellTemplateRoot + "decimalCell.mustache"));
        elementTypeToCellMustacheMap.put(INTEGER, mf.compile(cellTemplateRoot + "integerCell.mustache"));
        elementTypeToCellMustacheMap.put(STRING, mf.compile(cellTemplateRoot + "stringCell.mustache"));
        elementTypeToCellMustacheMap.put(SELECT1, mf.compile(cellTemplateRoot + "singleChoiceCell.mustache"));
        elementTypeToCellMustacheMap.put(BOOLEAN, mf.compile(cellTemplateRoot + "booleanCell.mustache"));
        elementTypeToCellMustacheMap.put(JRDATE, mf.compile(cellTemplateRoot + "dateCell.mustache"));
        elementTypeToCellMustacheMap.put(JRTIME, mf.compile(cellTemplateRoot + "timeCell.mustache"));
        elementTypeToCellMustacheMap.put(JRDATETIME, mf.compile(cellTemplateRoot + "dateTimeCell.mustache"));
        elementTypeToCellMustacheMap.put(GEOPOINT, mf.compile(cellTemplateRoot + "geolocationCell.mustache"));
        elementTypeToCellMustacheMap.put(GEOTRACE, mf.compile(cellTemplateRoot + "geotraceCell.mustache"));
        elementTypeToCellMustacheMap.put(GEOSHAPE, mf.compile(cellTemplateRoot + "geoshapeCell.mustache"));
        elementTypeToCellMustacheMap.put(SELECTN, mf.compile(cellTemplateRoot + "multipleChoiceCell.mustache"));
    }

    @Override
    public void beforeProcessSubmissions(CallingContext cc) {
        //Gather the semantic information that was submitted during form upload
        List<SemanticsTable> sem = SemanticsTable.findEntriesByFormId(this.form.getFormId(), cc);
        semantics = new HashMap<>();
        //Transform semantic information for easier access
        for(SemanticsTable t : sem){
            Map<String, String> tmp;
            String fieldName = t.getFieldName();
            if(!semantics.containsKey(t.getFieldName())){
                tmp = new HashMap<>();
            } else{
                tmp = semantics.get(fieldName);
            }
            tmp.put(t.getPropertyName(), t.getPropertyValue());
            semantics.put(fieldName, tmp);
        }

        //Grab the template config
        ExportTemplateConfig templateConfig = ExportTemplateConfigManager.getExportTemplateConfig(this.templateGroup);
        
        //Let the template config overwrite the default filetype
        if(templateConfig.getFiletype() != null && !StringUtils.isBlank(templateConfig.getFiletype())){
            this.filetype = templateConfig.getFiletype();
        }

        //Namespaces
        namespacesMustache.execute(output, this.baseURI);

        //Toplevel
        //Generate toplevel identifier via template
        ByteArrayOutputStream identifierStream = new ByteArrayOutputStream();
        PrintWriter identifierWriter;
        String toplevelEntityIdentifier = "";
        try {
            identifierWriter = new PrintWriter(new OutputStreamWriter(identifierStream, HtmlConsts.UTF8_ENCODE));
            toplevelIdentifierMustache.execute(identifierWriter, this.form.getFormId());
            identifierWriter.close();
            toplevelEntityIdentifier = identifierStream.toString();
            identifierStream.reset();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        toplevelModel = modelBuilder.buildTopLevelModel(this.form, toplevelEntityIdentifier);
        toplevelMustache.execute(output, toplevelModel);

        //Columns ~= Questions of the form
        boolean firstColumn = true;
        for(int col = 0; col < columnFormElementModelsFiltered.size(); col++){
            String colName = columnFormElementModelsFiltered.get(col).getElementName();

            //InstanceID is a special case - it's not to be considered a field for the template-based export
            if(colName.equals("instanceID")){
                //Adding a null-value to the list of column models makes indexing easier in the cells-section
                columnModels.add(null);
            } else{
                //Generate column identifier via template
                String columnEntityIdentifier = "";
                try {
                    identifierWriter = new PrintWriter(new OutputStreamWriter(identifierStream, HtmlConsts.UTF8_ENCODE));
                    columnIdentifierMustache.execute(identifierWriter, colName);
                    identifierWriter.close();
                    columnEntityIdentifier = identifierStream.toString();
                    identifierStream.reset();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                //For each column create the ColumnModel and fill the template
                ColumnModel columnModel = modelBuilder.buildColumnModel(toplevelModel, colName, columnEntityIdentifier,
                        firstColumn, col == (columnFormElementModelsFiltered.size()-1));
                columnModels.add(columnModel);
                columnMustache.execute(output, columnModel);
                firstColumn = false;
            }
        }
    }

    @Override
    public void processSubmissions(List<Submission> submissions, CallingContext cc) throws ODKDatastoreException {
        //Function currently not in use, the functions are executed separately
        beforeProcessSubmissions(cc);
        processSubmissionSegment(submissions, cc);
        afterProcessSubmissions(cc);
    }

    @Override
    public void processSubmissionSegment(List<Submission> submissions, CallingContext cc) throws ODKDatastoreException {
        //Rows
        for (Submission sub : submissions) {
            //Get the values that we want to export
            Row rowFiltered = sub.getFormattedValuesAsRow(namespaces, columnFormElementModelsFiltered, elemFormatter, false, cc);
            List<String> formattedValuesFiltered = rowFiltered.getFormattedValues();

            //We also need the unfiltered values to correctly process column-references in the semantics
            Row rowUnfiltered = sub.getFormattedValuesAsRow(namespaces, columnFormElementModelsUnfiltered, elemFormatter, false, cc);
            List<String> formattedValuesUnfiltered = rowUnfiltered.getFormattedValues();

            //Generate row identifier via template
            String rowId = "";
            if(this.requireRowUUIDs){
                //Use the header name to identify the field that contains the unique row ID
                for(int i = 0; i < columnFormElementModelsFiltered.size(); i++){
                    String header = columnFormElementModelsFiltered.get(i).getElementName();
                    if (header.equals("instanceID"))
                        rowId = formattedValuesFiltered.get(i);
                }
            } else{
                //If we don't require globally unique row IDs we can use simple numbers
                rowId = String.valueOf(rowCounter);
                rowCounter++;
            }
            //Generate row identifier via template
            ByteArrayOutputStream identifierStream = new ByteArrayOutputStream();
            PrintWriter identifierWriter;
            String rowEntityIdentifier = "";
            try {
                identifierWriter = new PrintWriter(new OutputStreamWriter(identifierStream, HtmlConsts.UTF8_ENCODE));
                rowIdentifierMustache.execute(identifierWriter, rowId);
                identifierWriter.close();
                rowEntityIdentifier = identifierStream.toString();
                identifierStream.reset();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //For each row create the RowModel and fill the template
            RowModel rowModel = modelBuilder.buildRowModel(toplevelModel, rowId, rowEntityIdentifier, firstRow);
            rowMustache.execute(output, rowModel);
            firstRow = false;

            //Cells
            int columnNumber = 0;
            Iterator<String> cellIt = formattedValuesFiltered.iterator();
            while(cellIt.hasNext()){
                String cellValue = cellIt.next();
                String columnName = columnFormElementModelsFiltered.get(columnNumber).getElementName();
                //InstanceID is a special case - it's not to be considered a field for the template-based export
                if(!columnName.equals("instanceID")) {
                    //Generate cell identifier via template
                    CellIdentifierModel cellIdModel = modelBuilder.buildCellIdentifierModel(columnModels.get(columnNumber), rowModel);
                    String cellEntityIdentifier = "";
                    try {
                        identifierWriter = new PrintWriter(new OutputStreamWriter(identifierStream, HtmlConsts.UTF8_ENCODE));
                        cellIdentifierMustache.execute(identifierWriter, cellIdModel);
                        identifierWriter.close();
                        cellEntityIdentifier = identifierStream.toString();
                        identifierStream.reset();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    //For each semantic property that is referencing another column we have to replace the _col_<columnName>
                    //with the respective value of the column
                    Map<String, String> columnSemantics = semantics.get(columnName);
                    //We need a copy (shallow suffices here) of the semantics to adapt the values for the given row
                    Map<String, String> semanticsForGivenRow;
                    if(columnSemantics != null){
                      semanticsForGivenRow = new HashMap<>(columnSemantics);
                    } else{
                        semanticsForGivenRow = new HashMap<>();
                    }
                    for(Map.Entry<String, String> entry : semanticsForGivenRow.entrySet()){
                        if(entry.getValue().startsWith(TemplateFormatterWithFilters.COLUMN_REF_PREFIX)){
                            String referenceColumn = StringUtils.removeStart(entry.getValue(), TemplateFormatterWithFilters.COLUMN_REF_PREFIX);
                            //Find index of the referenced column (using the unfiltered FormElementModels here)
                            int index = IntStream.range(0, columnFormElementModelsUnfiltered.size())
                                    .filter(i -> columnFormElementModelsUnfiltered.get(i).getElementName().equals(referenceColumn))
                                    .findFirst().orElseThrow(
                                            () -> new ODKDatastoreException("Semantic information is referencing the non-existing column " + referenceColumn)
                                    );
                            String referenceValue = formattedValuesUnfiltered.get(index);
                            entry.setValue(referenceValue);
                        }
                    }
                    
                    FormElementModel.ElementType elementType = columnFormElementModelsFiltered.get(columnNumber).getElementType();
                    AbstractCellModel cellModel = modelBuilder.buildCellModel(
                            columnModels.get(columnNumber),
                            rowModel,
                            cellValue,
                            cellEntityIdentifier,
                            elementType,
                            semanticsForGivenRow
                    );
                    //Use the generic cell-template
                    genericCellMustache.execute(output, cellModel);

                    //Grab the suitable elementType-specific template, defaulting to the String-template
                    Mustache cellMustache;
                    if (elementTypeToCellMustacheMap.containsKey(elementType)) {
                        cellMustache = elementTypeToCellMustacheMap.get(elementType);
                    } else {
                        cellMustache = elementTypeToCellMustacheMap.get(STRING);
                    }
                    cellMustache.execute(output, cellModel);
                }
                columnNumber++;
            }
        }
    }

    @Override
    public void afterProcessSubmissions(CallingContext cc) {
        //Execute termination template with the same Model as the Toplevel template
        ByteArrayOutputStream identifierStream = new ByteArrayOutputStream();
        PrintWriter identifierWriter;
        String toplevelEntityIdentifier = "";
        try {
            identifierWriter = new PrintWriter(new OutputStreamWriter(identifierStream, HtmlConsts.UTF8_ENCODE));
            toplevelIdentifierMustache.execute(identifierWriter, this.form.getFormId());
            identifierWriter.close();
            toplevelEntityIdentifier = identifierStream.toString();
            identifierStream.reset();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        toplevelModel = modelBuilder.buildTopLevelModel(this.form, toplevelEntityIdentifier);
        terminationMustache.execute(output, toplevelModel);
    }
}
