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
package org.opendatakit.aggregate.task.tomcat;

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.TemplateExportGenerator;
import org.opendatakit.aggregate.task.TemplateExportWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

import java.util.Map;

/**
 * This is a singleton bean.  It cannot have any per-request state.
 * It uses a static inner class to encapsulate the per-request state
 * of a running background task.
 */
public class TemplateExportGeneratorImpl implements TemplateExportGenerator {

    static class TemplateRunner implements Runnable {
        final TemplateExportWorkerImpl impl;

        public TemplateRunner(IForm form, SubmissionKey persistentResultsKey, long attemptCount,
                              String baseURI, Boolean requireRowUUIDs, String templateGroup, CallingContext cc) {
            impl = new TemplateExportWorkerImpl(form, persistentResultsKey, attemptCount, baseURI, requireRowUUIDs, templateGroup, cc );
        }

        @Override
        public void run() {
            impl.generateRdf();
        }
    }

    @Override
    public void createRdfTask(IForm form, PersistentResults persistentResults,
                              long attemptCount, CallingContext cc)
            throws ODKDatastoreException {
        //Grab parameters
        Map<String, String> params = persistentResults.getRequestParameters();
        String baseURI = params.get(TemplateExportGenerator.RDF_BASEURI_KEY);
        Boolean requireUUIDs = Boolean.parseBoolean(params.get(TemplateExportGenerator.RDF_REQUIREUUIDS_KEY));
        String templateGroup = params.get(TemplateExportGenerator.RDF_TEMPLATE_KEY);

        WatchdogImpl wd = (WatchdogImpl) cc.getBean(BeanDefs.WATCHDOG);
        // use watchdog's calling context in runner...
        TemplateRunner runner = new TemplateRunner(form, persistentResults.getSubmissionKey(), attemptCount,
                baseURI, requireUUIDs, templateGroup, wd.getCallingContext() );
        AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
        exec.execute(runner);
    }
}
