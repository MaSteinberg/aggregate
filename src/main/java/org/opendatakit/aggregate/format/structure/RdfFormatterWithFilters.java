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
    }

    @Override
    public void beforeProcessSubmissions(CallingContext cc) throws ODKDatastoreException {
        output.append("Hello world, this is before the RDF-export");
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
        output.append("Hello world, this is after the RDF-export");
    }
}
