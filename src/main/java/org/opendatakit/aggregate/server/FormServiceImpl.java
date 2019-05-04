/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.*;
import java.net.URL;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.*;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.common.ExportType;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.aggregate.constants.format.FormTableConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.form.PersistentResults.ResultFileInfo;
import org.opendatakit.aggregate.odktables.rdf.SemanticsTable;
import org.opendatakit.aggregate.task.CsvGenerator;
import org.opendatakit.aggregate.task.JsonFileGenerator;
import org.opendatakit.aggregate.task.RdfGenerator;
import org.opendatakit.aggregate.task.KmlGenerator;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

public class FormServiceImpl extends RemoteServiceServlet implements
    org.opendatakit.aggregate.client.form.FormService {
  private static final Logger logger = LoggerFactory.getLogger(FormServiceImpl.class);

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -193679930586769386L;
  private static final String LIMITATION_MSG = "Picture and Title must be in the submission (top-level) or must be in the same repeat group as the GeoPoint";

  @Override
  public ArrayList<FormSummary> getForms() throws RequestFailureException, DatastoreFailureException {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    ArrayList<FormSummary> formSummaries = new ArrayList<FormSummary>();

    try {
      // ensure that Form table exists...
      List<IForm> forms = FormFactory.getForms(false, cc);
      if (forms.size() == 0)
        return formSummaries;

      // get most recent form-deletion statuses
      Map<String, FormActionStatusTimestamp> formDeletionStatuses = MiscTasks
          .getFormDeletionStatusTimestampOfAllFormIds(cc);

      // get most recent purge-submissions statuses
      Map<String, FormActionStatusTimestamp> submissionPurgeStatuses = MiscTasks
          .getPurgeSubmissionsStatusTimestampOfAllFormIds(cc);

      for (IForm form : forms) {
        FormSummary summary = form.generateFormSummary(cc);
        Date formLoadDate = summary.getCreationDate();
        formSummaries.add(summary);

        // the form could have been deleted and reloaded...
        // make sure that the action is after the creation date for
        // this instance of this form id.
        FormActionStatusTimestamp t;
        t = formDeletionStatuses.get(form.getFormId());
        if (t != null && t.getTimestamp().after(formLoadDate)) {
          summary.setMostRecentDeletionRequestStatus(t);
        }
        t = submissionPurgeStatuses.get(form.getFormId());
        if (t != null && t.getTimestamp().after(formLoadDate)) {
          summary.setMostRecentPurgeSubmissionsRequestStatus(t);
        }
      }
      Collections.sort(formSummaries, new Comparator<FormSummary>() {

        @Override
        public int compare(FormSummary arg0, FormSummary arg1) {
          int cmp;
          cmp = arg0.getTitle().compareTo(arg1.getTitle());
          if (cmp != 0) return cmp;
          cmp = arg0.getId().compareTo(arg1.getId());
          return cmp;
        }
      });

      return formSummaries;

    } catch (ODKOverQuotaException e) {
      logger.error("Over quota failure", e);
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      logger.error("Datastore failure", e);
      throw new DatastoreFailureException();
    } catch (Throwable t) {
      logger.error("Possible corruption", t);
      throw new RequestFailureException(ErrorConsts.POSSIBLE_CORRUPTION);
    }
  }

  @Override
  public ArrayList<ExportSummary> getExports() throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    ArrayList<ExportSummary> exports = new ArrayList<ExportSummary>();
    try {
      List<PersistentResults> results = PersistentResults.getAvailablePersistentResults(cc);
      for (PersistentResults export : results) {
        ExportSummary summary = new ExportSummary();

        summary.setUri(export.getUri());
        summary.setFileType(export.getResultType());
        summary.setTimeRequested(export.getRequestDate());
        summary.setStatus(export.getStatus());
        summary.setTimeLastAction(export.getLastRetryDate());
        summary.setTimeCompleted(export.getCompletionDate());

        // get info about the downloadable file.
        // null if no file yet....
        ResultFileInfo info = export.getResultFileInfo(cc);
        if (info != null) {
          String linkText = info.unrootedFilename;
          if (linkText == null || linkText.length() == 0) {
            linkText = FormTableConsts.DOWNLOAD_LINK_TEXT;
          }
          String url = HtmlUtil.createHref(info.downloadUrl, linkText, false);
          summary.setResultFile(url);
        }
        exports.add(summary);
      }

      return exports;

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException();
    }
  }

  @Override
  public KmlOptionsSummary getPossibleKmlSettings(String formId) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID); // ill-formed definition
      }
      GenerateKmlOptions kmlOptions = new GenerateKmlOptions(form);
      return kmlOptions.generate();

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException();
    }
  }

  /**
   * Used by the frontend to grab the RDF Export configuration.
   * Only returns the templates for which all required annotations are available
   * @see org.opendatakit.aggregate.client.popups.RdfOptionsPopup#RdfOptionsPopup
   */
  @Override
  public RdfExportOptions getRdfExportSettings(String formId, FilterGroup filterGroup) throws AccessDeniedException, RequestFailureException, DatastoreFailureException {
    //Grab all form details & included columns to check for required annotations
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    IForm form;
    try {
      form = FormFactory.retrieveFormByFormId(formId, cc);
    } catch (ODKFormNotFoundException e) {
      throw new RequestFailureException(ErrorConsts.FORM_NOT_FOUND);
    } catch (ODKOverQuotaException e) {
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      throw new DatastoreFailureException(e.getMessage());
    }
    if (!form.hasValidFormDefinition()) {
      throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID); // ill-formed definition
    }
    SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());
    GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
    headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
    List<FormElementModel> columnFormElementModelsFiltered = headerGenerator.getIncludedElements();

    //Retrieve semantics of the form from the DB
    List<SemanticsTable> sem = SemanticsTable.findEntriesByFormId(formId, cc);
    Map<String, Map<String, String>> semantics = new HashMap<>(); //(fieldName -> (propertyName -> propertyValue))
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

    //Find all registered template groups
    RdfExportOptions options = RdfTemplateConfigManager.getRdfExportOptions();
    Map<String, RdfTemplateConfig> templates = options.getTemplates();
    //Iterate all registered template groups
    for(Map.Entry<String, RdfTemplateConfig> entry: templates.entrySet()){
      List<String> requiredProperties;
      if(entry.getValue().getTemplateProperties() != null){
        requiredProperties = entry.getValue().getTemplateProperties().getRequiredProperties();
        //Check if we have all required annotations
        if(requiredProperties != null) {
          for(String requiredProperty : requiredProperties) {
            for (FormElementModel col : columnFormElementModelsFiltered) {
              //"instanceID" is a special case - it's automatically generated and thus semantic information
              //can't be entered by the form author
              //Repeats also don't have semantic information attached so we can ignore these
              if(!col.getElementName().equals("instanceID") &&
                      !col.getElementType().equals(FormElementModel.ElementType.REPEAT)){
                Map<String, String> semanticsForColumn = semantics.get(col.getElementName());
                if (semanticsForColumn == null ||
                    !(semanticsForColumn.containsKey(requiredProperty)) ||
                    semanticsForColumn.get(requiredProperty) == null ||
                    semanticsForColumn.get(requiredProperty).trim().length() == 0){
                  //Missing required annotations so we don't return this template group as an option
                  templates.remove(entry.getKey());
                }
              }
            }
          }
        }
      }
    }
    return options;
  }

  @Override
  public GeopointElementList getGpsCoordnates(String formId) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID); // ill-formed definition
      }

      GenerateGeopointElementList geopointList = new GenerateGeopointElementList(form, true);
      return geopointList.generate();

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException();
    }
  }

  @Override
  public Boolean createCsvFromFilter(FilterGroup group) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(group.getFormId(), cc);
      // Form is being deleted. Disallow exports.
      if (deletionTimestamp != null) {
        throw new RequestFailureException("Form is marked for deletion - csv export request aborted.");
      }

      // clear uri so a copy can be saved
      group.resetUriToDefault();

      // save the filter group
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.setName("FilterForExport-" + filterGrp.getName());
      filterGrp.setIsPublic(false); // make the filter not visible in the UI since it's an internal filter for export
      filterGrp.persist(cc);

      // create csv job
      IForm form = FormFactory.retrieveFormByFormId(filterGrp.getFormId(), cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID); // ill-formed definition
      }
      PersistentResults r = new PersistentResults(ExportType.CSV, form, filterGrp, null, cc);
      r.persist(cc);

      // create csv task
      CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
      ccDaemon.setAsDaemon(true);
      CsvGenerator generator = (CsvGenerator) cc.getBean(BeanDefs.CSV_BEAN);
      generator.createCsvTask(form, r.getSubmissionKey(), 1L, ccDaemon);
      return true;

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException();
    }
  }

  @Override
  public Boolean createJsonFileFromFilter(FilterGroup group) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(group.getFormId(), cc);
      // Form is being deleted. Disallow exports.
      if (deletionTimestamp != null) {
        throw new RequestFailureException("Form is marked for deletion - JSON File export request aborted.");
      }

      // clear uri so a copy can be saved
      group.resetUriToDefault();

      // save the filter group
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.setName("FilterForExport");
      filterGrp.persist(cc);

      // create csv job
      IForm form = FormFactory.retrieveFormByFormId(filterGrp.getFormId(), cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID); // ill-formed definition
      }
      PersistentResults r = new PersistentResults(ExportType.JSONFILE, form, filterGrp, null, cc);
      r.persist(cc);

      // create csv task
      CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
      ccDaemon.setAsDaemon(true);
      JsonFileGenerator generator = (JsonFileGenerator) cc.getBean(BeanDefs.JSON_FILE_BEAN);
      generator.createJsonFileTask(form, r.getSubmissionKey(), 1L, ccDaemon);
      return true;

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException();
    }
  }

  @Override
  public Boolean createRdfFileFromFilter(FilterGroup group, String baseURI, Boolean requireRowUUIDs, String templateGroup) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
              .getFormDeletionStatusTimestampOfFormId(group.getFormId(), cc);
      // Form is being deleted. Disallow exports.
      if (deletionTimestamp != null) {
        throw new RequestFailureException("Form is marked for deletion - csv export request aborted.");
      }

      // clear uri so a copy can be saved
      group.resetUriToDefault();

      // save the filter group
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.setName("FilterForExport-" + filterGrp.getName());
      filterGrp.setIsPublic(false); // make the filter not visible in the UI since it's an internal filter for export
      filterGrp.persist(cc);

      // create rdf job
      IForm form = FormFactory.retrieveFormByFormId(filterGrp.getFormId(), cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID); // ill-formed definition
      }

      //Build parameter map
      Map<String, String> params = new HashMap<>();
      params.put(RdfGenerator.RDF_BASEURI_KEY, baseURI);
      params.put(RdfGenerator.RDF_REQUIREUUIDS_KEY, String.valueOf(requireRowUUIDs));
      params.put(RdfGenerator.RDF_TEMPLATE_KEY, templateGroup);
      PersistentResults r = new PersistentResults(ExportType.RDF, form, filterGrp, params, cc);
      r.persist(cc);

      // create rdf task
      CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
      ccDaemon.setAsDaemon(true);
      RdfGenerator generator = (RdfGenerator) cc.getBean(BeanDefs.RDF_BEAN);
      generator.createRdfTask(form, r, 1L, ccDaemon);
      return true;

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException();
    }
  }

  @Override
  public Boolean createKmlFromFilter(FilterGroup group, ArrayList<KmlSelection> kmlElementsToInclude) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    if (group == null || group.getFormId() == null || kmlElementsToInclude == null) {
      return false;
    }

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(group.getFormId(), cc);
      // Form is being deleted. Disallow exports.
      if (deletionTimestamp != null) {
        throw new RequestFailureException("Form is marked for deletion - kml export request aborted.");
      }

      // clear uri so a copy can be saved
      group.resetUriToDefault();

      // save the filter group
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.setName("FilterForExport");
      filterGrp.persist(cc);

      IForm form = FormFactory.retrieveFormByFormId(group.getFormId(), cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID); // ill-formed definition
      }

//      FormElementModel titleField = null;
//      if (titleKey != null) {
//        FormElementKey titleFEMKey = new FormElementKey(titleKey);
//        titleField = FormElementModel.retrieveFormElementModel(form, titleFEMKey);
//      }
//
//      FormElementModel geopointField = null;
//      if (geopointKey != null) {
//        FormElementKey geopointFEMKey = new FormElementKey(geopointKey);
//        geopointField = FormElementModel.retrieveFormElementModel(form, geopointFEMKey);
//      }
//
//      FormElementModel imageField = null;
//      if (binaryKey != null) {
//        FormElementKey imageFEMKey = new FormElementKey(binaryKey);
//        imageField = FormElementModel.retrieveFormElementModel(form, imageFEMKey);
//      }
//
//      // Apply rendering constraints
//      FormElementModel topElement = form.getTopLevelGroupElement();
//
//      FormElementModel titleParent = titleField.getParent();
//      // ignore semantically meaningless nesting groups
//      while ( titleParent.getParent() != null && titleParent.getElementType().equals(ElementType.GROUP) ) {
//        titleParent = titleParent.getParent();
//      }
//      FormElementModel gpsParent = geopointField.getParent();
//      // ignore semantically meaningless nesting groups
//      while ( gpsParent.getParent() != null && gpsParent.getElementType().equals(ElementType.GROUP) ) {
//        gpsParent = gpsParent.getParent();
//      }
//
//      if (!titleParent.equals(topElement) && !titleParent.equals(gpsParent)) {
//        throw new RequestFailureException(LIMITATION_MSG);
//      }
//      if (imageField == null) {
//      } else {
//        FormElementModel imgParent = imageField.getParent();
//        // ignore semantically meaningless nesting groups
//        while ( imgParent.getParent() != null && imgParent.getElementType().equals(ElementType.GROUP) ) {
//          imgParent = imgParent.getParent();
//        }
//        if (!imgParent.equals(topElement) && !imgParent.equals(gpsParent)) {
//          throw new RequestFailureException(LIMITATION_MSG);
//        }
//      }


      // encode all the settings form the selections
      StringBuilder encodedKmlSettings = new StringBuilder();
      boolean firstItem = true;
      for (KmlSelection kmlSetting : kmlElementsToInclude) {
        if (firstItem) {
          firstItem = false;
        } else {
          encodedKmlSettings.append(KmlGenerator.KML_SELECTIONS_DELIMITER);
        }
        // TODO: think about a bad setting (checking comment out above will prevent this);
        String tmpString = kmlSetting.generateEncodedString();
        encodedKmlSettings.append(tmpString);
      }

      Map<String, String> params = new HashMap<String, String>();
      params.put(KmlGenerator.KML_SELECTIONS_KEY, encodedKmlSettings.toString());

      PersistentResults r = new PersistentResults(ExportType.KML, form, filterGrp, params, cc);
      r.persist(cc);

      KmlGenerator generator = (KmlGenerator) cc.getBean(BeanDefs.KML_BEAN);
      CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
      ccDaemon.setAsDaemon(true);
      generator.createKmlTask(form, r, 1L, ccDaemon);
      return true;
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException();
    }
  }

  @Override
  public void deleteExport(String uri) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      PersistentResults r = PersistentResults.getPersistentResult(uri, cc);
      r.delete(cc);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.EXPORTED_FILE_PROBLEM);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException();
    }
  }


}
