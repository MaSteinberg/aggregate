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
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.structure.rdf.models.ColumnModel;
import org.opendatakit.aggregate.format.structure.rdf.models.NamespacesModel;
import org.opendatakit.aggregate.format.structure.rdf.models.RdfNamespace;
import org.opendatakit.aggregate.format.structure.rdf.models.TopLevelModel;
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
    private MustacheFactory mf;

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

        //Initialize Mustache
        mf = new DefaultMustacheFactory();
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
            }
        };
        NamespacesModel namespacesModel = new NamespacesModel("http://example.org", namespaces);
        Mustache namespacesMustache = mf.compile("mustache_templates/common/namespaces.ttl.mustache");
        namespacesMustache.execute(output, namespacesModel );

        //Toplevel
        Mustache toplevelMustache = mf.compile("mustache_templates/oboe/toplevel.ttl.mustache");
        TopLevelModel toplevelModel = ModelBuilder.buildTopLevelModel(this.form);
        toplevelMustache.execute(output, toplevelModel);

        //For each column create the ColumnModel and fill the template
        output.append("#Each column describes one observation\n");
        Mustache columnsMustache = mf.compile("mustache_templates/oboe/column.ttl.mustache");
        for(FormElementModel col : propertyNames){
            ColumnModel columnModel = ModelBuilder.buildColumnModel(toplevelModel, col.getElementName());
            columnsMustache.execute(output, columnModel);
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

    }

    @Override
    public void afterProcessSubmissions(CallingContext cc) throws ODKDatastoreException {
    }
}
