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
package org.opendatakit.aggregate.format.structure.rdf;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.aggregate.format.structure.rdf.models.*;
import org.opendatakit.aggregate.server.GenerateHeaderInfo;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opendatakit.aggregate.datamodel.FormElementModel.ElementType.*;

public class RdfFormatterWithFilters implements SubmissionFormatter {
    private final Logger logger = LoggerFactory.getLogger(RdfFormatterWithFilters.class);

    private ElementFormatter elemFormatter;
    private List<FormElementModel> columnFormElementModels;
    private List<String> headerNames;
    List<FormElementModel.ElementType> headerTypes;
    private final IForm form;
    private final PrintWriter output;
    private List<FormElementNamespace> namespaces;

    private boolean requireRowGuid = false;
    private String templateGroup = "oboe";

    private MustacheFactory mf;
    private Mustache toplevelMustache;
    private Mustache columnMustache;
    private Mustache rowMustache;
    private Map<FormElementModel.ElementType, Mustache> elementTypeToCellMustacheMap;

    private ModelBuilder modelBuilder = new ModelBuilder();
    private TopLevelModel toplevelModel;
    private List<ColumnModel> columnModels = new ArrayList<>();

    public RdfFormatterWithFilters(IForm xform, String webServerUrl, PrintWriter printWriter,
                                   FilterGroup filterGroup) {
        form = xform;
        output = printWriter;

        SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());
        HeaderFormatter headerFormatter = new BasicHeaderFormatter(false, true, true);

        GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
        headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
        columnFormElementModels = headerGenerator.getIncludedElements();
        namespaces = headerGenerator.includedFormElementNamespaces();
        headerNames = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(), columnFormElementModels);
        headerTypes = headerFormatter.getHeaderTypes(); //TODO this needs the same size as headerNames or we get a problem
        elemFormatter = new BasicElementFormatter(false, true, true, false);

        //Workaround: headerNames and headerTypes have extra columns for GEOPOINTs altitude and accuracy while our elementFormatter just
        //includes them in a single String, split by ", " (which is easier to process)
        //So we remove the additional entries from headerNames and headerTypes
        int col = 0;
        while(col < headerNames.size()){
            if(headerTypes.get(col) == GEOPOINT){
                headerTypes.remove(col + 2);
                headerTypes.remove(col + 1);
                headerNames.remove(col + 2);
                headerNames.remove(col + 1);
            }
            col++;
        }

        //Initialize Mustache & compile the templates
        mf = new DefaultMustacheFactory();
        this.toplevelMustache = mf.compile("mustache_templates/oboe/toplevel.ttl.mustache");
        this.columnMustache = mf.compile("mustache_templates/oboe/column.ttl.mustache");
        this.rowMustache = mf.compile("mustache_templates/oboe/row.ttl.mustache");

        //Assign and compile the cell templates
        elementTypeToCellMustacheMap = new HashMap();
        String cellTemplateRoot = "mustache_templates/" + templateGroup + "/cell/";
        elementTypeToCellMustacheMap.put(DECIMAL, mf.compile(cellTemplateRoot + "decimalCell.ttl.mustache"));
        elementTypeToCellMustacheMap.put(INTEGER, mf.compile(cellTemplateRoot + "integerCell.ttl.mustache"));
        elementTypeToCellMustacheMap.put(STRING, mf.compile(cellTemplateRoot + "stringCell.ttl.mustache"));
        elementTypeToCellMustacheMap.put(SELECT1, mf.compile(cellTemplateRoot + "select1Cell.ttl.mustache"));
        elementTypeToCellMustacheMap.put(BOOLEAN, mf.compile(cellTemplateRoot + "booleanCell.ttl.mustache"));
        elementTypeToCellMustacheMap.put(JRDATE, mf.compile(cellTemplateRoot + "dateCell.ttl.mustache"));
        elementTypeToCellMustacheMap.put(JRTIME, mf.compile(cellTemplateRoot + "timeCell.ttl.mustache"));
        elementTypeToCellMustacheMap.put(JRDATETIME, mf.compile(cellTemplateRoot + "dateTimeCell.ttl.mustache"));
        elementTypeToCellMustacheMap.put(GEOPOINT, mf.compile(cellTemplateRoot + "geolocationCell.ttl.mustache"));
    }

    @Override
    public void beforeProcessSubmissions(CallingContext cc) throws ODKDatastoreException {
        //Namespaces
        List<RdfNamespace> namespaces = new ArrayList<RdfNamespace>(){
            {
                add(new RdfNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
                add(new RdfNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#"));
                add(new RdfNamespace("owl", "http://www.w3.org/2002/07/owl#"));
                add(new RdfNamespace("oboe-core", "http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#"));
                add(new RdfNamespace("xsd", "http://www.w3.org/2001/XMLSchema#"));
                add(new RdfNamespace("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#"));
            }
        };
        NamespacesModel namespacesModel = new NamespacesModel("http://example.org", namespaces);
        Mustache namespacesMustache = mf.compile("mustache_templates/common/namespaces.ttl.mustache");
        namespacesMustache.execute(output, namespacesModel );

        //Toplevel
        toplevelModel = modelBuilder.buildTopLevelModel(this.form);
        toplevelMustache.execute(output, toplevelModel);

        //Columns
        //For each column create the ColumnModel and fill the template
        output.append("#Each column describes one observation\n");
        for(int col = 0; col < headerNames.size(); col++){
            ColumnModel columnModel = modelBuilder.buildColumnModel(toplevelModel, headerNames.get(col), headerTypes.get(col));
            columnModels.add(columnModel);
            columnMustache.execute(output, columnModel);
        }
    }

    @Override
    public void processSubmissions(List<Submission> submissions, CallingContext cc) throws ODKDatastoreException {
        //Function currently not in use, the functions are separately called
        beforeProcessSubmissions(cc);
        processSubmissionSegment(submissions, cc);
        afterProcessSubmissions(cc);
    }

    @Override
    public void processSubmissionSegment(List<Submission> submissions, CallingContext cc) throws ODKDatastoreException {
        //For each row create the ColumnModel and fill the template
        for (Submission sub : submissions) {
            //Rows
            Row row = sub.getFormattedValuesAsRow(namespaces, columnFormElementModels, elemFormatter, false, cc);
            List<String> formattedValues = row.getFormattedValues();

            if(formattedValues.size() != headerTypes.size() || formattedValues.size() != headerNames.size()){
                System.out.println("Houston, we have a problem!"); //TODO Remove
            }

            RowModel rowModel = modelBuilder.buildRowModel(toplevelModel, formattedValues, columnFormElementModels, requireRowGuid);
            rowMustache.execute(output, rowModel);

            //Cells
            int columnNumber = 0;
            for(String cellValue : formattedValues){
                FormElementModel.ElementType elementType = headerTypes.get(columnNumber);
                AbstractCellModel cellModel = modelBuilder.buildCellModel(toplevelModel, columnModels.get(columnNumber), rowModel, cellValue, elementType);
                //Grab the suitable cell-template, defaulting to the String-template
                Mustache cellMustache;
                if(elementTypeToCellMustacheMap.containsKey(elementType)){
                    cellMustache = elementTypeToCellMustacheMap.get(elementType);
                } else{
                    cellMustache = elementTypeToCellMustacheMap.get(STRING);
                }
                output.append("#Element type: " + elementType.name() + "\n");
                cellMustache.execute(output, cellModel);
                columnNumber++;
            }
        }
    }

    @Override
    public void afterProcessSubmissions(CallingContext cc) throws ODKDatastoreException {
    }
}
