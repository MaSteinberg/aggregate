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
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.structure.rdf.models.*;
import org.opendatakit.aggregate.server.GenerateHeaderInfo;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RdfFormatterWithFilters implements SubmissionFormatter {
    private final Logger logger = LoggerFactory.getLogger(RdfFormatterWithFilters.class);

    private ElementFormatter elemFormatter;
    private List<FormElementModel> propertyNames;
    private List<String> headers;
    private final IForm form;
    private final PrintWriter output;
    private List<FormElementNamespace> namespaces;

    private boolean requireRowGuid = false;

    private MustacheFactory mf;
    private Mustache toplevelMustache;
    private Mustache columnMustache;
    private Mustache rowMustache;
    private Mustache cellMustache;
    private Mustache toplevelMetadataMustache;
    private Mustache columnMetadataMustache;
    private Mustache rowMetadataMustache;
    private Mustache cellMetadataMustache;

    private ModelBuilder modelBuilder = new ModelBuilder();
    private TopLevelModel toplevelModel;
    private List<ColumnModel> columnModels = new ArrayList<>();

    public RdfFormatterWithFilters(IForm xform, String webServerUrl, PrintWriter printWriter,
                                   FilterGroup filterGroup) {
        form = xform;
        output = printWriter;

        headers = new ArrayList<String>();
        SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());

        GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
        headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
        propertyNames = headerGenerator.getIncludedElements();
        namespaces = headerGenerator.includedFormElementNamespaces();
        elemFormatter = new BasicElementFormatter(false, true, true, false);

        //Initialize Mustache & compile the templates
        mf = new DefaultMustacheFactory();
        this.toplevelMustache = mf.compile("mustache_templates/oboe/toplevel.ttl.mustache");
        this.columnMustache = mf.compile("mustache_templates/oboe/column.ttl.mustache");
        this.rowMustache = mf.compile("mustache_templates/oboe/row.ttl.mustache");
        this.cellMustache = mf.compile("mustache_templates/oboe/cell.ttl.mustache");
        this.toplevelMetadataMustache = mf.compile("mustache_templates/oboe/metadata/toplevelMetadata.ttl.mustache");
        this.columnMetadataMustache = mf.compile("mustache_templates/oboe/metadata/columnMetadata.ttl.mustache");
        this.rowMetadataMustache = mf.compile("mustache_templates/oboe/metadata/rowMetadata.ttl.mustache");
        this.cellMetadataMustache = mf.compile("mustache_templates/oboe/metadata/cellMetadata.ttl.mustache");
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
                add(new RdfNamespace("dcterms", "http://purl.org/dc/terms/"));
            }
        };
        NamespacesModel namespacesModel = new NamespacesModel("http://example.org", namespaces);
        Mustache namespacesMustache = mf.compile("mustache_templates/common/namespaces.ttl.mustache");
        namespacesMustache.execute(output, namespacesModel );

        //Toplevel
        toplevelModel = modelBuilder.buildTopLevelModel(this.form);
        toplevelMustache.execute(output, toplevelModel);
        toplevelMetadataMustache.execute(output, toplevelModel);

        //For each column create the ColumnModel and fill the template
        output.append("#Each column describes one observation\n");
        for(FormElementModel col : propertyNames){
            ColumnModel columnModel = modelBuilder.buildColumnModel(toplevelModel, col.getElementName());
            columnModels.add(columnModel);
            columnMustache.execute(output, columnModel);
            columnMetadataMustache.execute(output, columnModel);
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
            Row row = sub.getFormattedValuesAsRow(namespaces, propertyNames, elemFormatter, false, cc);
            List<String> formattedValues = row.getFormattedValues();

            RowModel rowModel = modelBuilder.buildRowModel(toplevelModel, formattedValues, propertyNames, requireRowGuid);
            rowMustache.execute(output, rowModel);
            rowMetadataMustache.execute(output, rowModel);

            output.append("#Each cell describes one measurement\n");
            int columnNumber = 0;
            for(String cellValue : formattedValues){
                CellModel cellModel = modelBuilder.buildCellModel(toplevelModel, columnModels.get(columnNumber), rowModel, cellValue);
                cellMustache.execute(output, cellModel);
                cellMetadataMustache.execute(output, cellModel);
                columnNumber++;
            }
        }
    }

    @Override
    public void afterProcessSubmissions(CallingContext cc) throws ODKDatastoreException {
    }
}
