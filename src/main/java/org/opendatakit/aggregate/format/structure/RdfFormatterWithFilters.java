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
package org.opendatakit.aggregate.format.structure;

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
import org.opendatakit.aggregate.server.GenerateHeaderInfo;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class RdfFormatterWithFilters implements SubmissionFormatter {
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
        List<Namespace> namespaces = new ArrayList<Namespace>(){
            {
                add(new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
                add(new Namespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#"));
                add(new Namespace("owl", "http://www.w3.org/2002/07/owl#"));
                add(new Namespace("oboe-core", "http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#"));
            }
        };
        NamespacesModel model = new NamespacesModel("http://example.org", namespaces);
        Mustache namespacesMustache = mf.compile("mustache_templates/common/namespaces.ttl.mustache");
        namespacesMustache.execute(output, model);

        //For each column...
        output.append("#Each column describes one observation\n");
        Mustache columnsMustache = mf.compile("mustache_templates/oboe/column.ttl.mustache");
        for(FormElementModel col : propertyNames){
            /*Check if the column is a metadata column
            Neither FormElementModel.isMetadata() nor
            FormElementModel.getElementType() offer the expected results
            so I have to check if the parent element is called "meta"*/
            if(col.getParent().getElementName().equals("meta")){
                //Special treatment for metadata
            } else{
                columnsMustache.execute(output, col.getElementName());
            }
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

    private class NamespacesModel{
        public String base;
        public List<Namespace> namespaces;

        public NamespacesModel(String base){
            this.base = base;
        }

        public NamespacesModel(String base, List<Namespace> namespaces){
            this.base = base;
            this.namespaces = namespaces;
        }
    }

    private class Namespace{
        public String prefix;
        public String uri;
        public Namespace(String prefix, String uri){
            this.prefix = prefix;
            this.uri = uri;
        }
    }
}
