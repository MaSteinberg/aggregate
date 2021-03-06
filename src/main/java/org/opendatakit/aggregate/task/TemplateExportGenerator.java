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
package org.opendatakit.aggregate.task;

import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * @author Markus Steinberg
 * API for creating and restarting template export.
 */
public interface TemplateExportGenerator {
    public static final String TEMPLATE_EXPORT_BASEURI_KEY = "TEMPLATE_BASEURI";
    public static final String TEMPLATE_EXPORT_REQUIRE_UUIDS_KEY = "TEMPLATE_UUIDS";
    public static final String TEMPLATE_EXPORT_TEMPLATE_KEY = "TEMPLATEGROUP";
    public void createTemplateExportTask(IForm form, PersistentResults persistentResults,
                                         long attemptCount, CallingContext cc) throws ODKDatastoreException;
}
