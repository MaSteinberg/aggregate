package org.opendatakit.aggregate.client.popups;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.TemplateExportOptions;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.SubTabs;

import static org.opendatakit.aggregate.client.security.SecurityUtils.secureRequest;

/**
 * @author Markus Steinberg
 */
public class TemplateExportOptionsPopup extends AbstractPopupBase {
    private static final String EXPORT_ERROR_MSG = "One of the options was invalid. Did you forget to enter a base-URI?";

    private static final String CREATE_BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Export";
    private static final String CREATE_BUTTON_TOOLTIP = "Create File";
    private static final String CREATE_BUTTON_HELP_BALLOON = "This exports your data into a file with the following options.";

    private final FlexTable topBar;
    private final AggregateButton exportButton;
    private final String formId;
    private final FilterGroup selectedFilterGroup;

    private FlexTable layout;
    private TextBox baseUriInput;
    private CheckBox requireRowUUIDsInput;
    private ListBox templateGroupDropdown;


    public TemplateExportOptionsPopup(String formId, FilterGroup selectedFilterGroup){
        super();
        this.formId = formId;
        this.selectedFilterGroup = selectedFilterGroup;

        // Export button
        // Disable it until we receive the list of available templates from the server
        exportButton = new AggregateButton(CREATE_BUTTON_TXT, CREATE_BUTTON_TOOLTIP,
                CREATE_BUTTON_HELP_BALLOON);
        exportButton.addClickHandler(new CreateExportHandler());
        exportButton.setEnabled(false);

        // Header with Form-id and filter
        topBar = new FlexTable();
        topBar.addStyleName("stretch_header");
        topBar.setWidget(0, 0, new HTML("<h2> Form:</h2>"));
        topBar.setWidget(0, 1, new HTML(new SafeHtmlBuilder().appendEscaped(formId).toSafeHtml()));
        topBar.setWidget(0, 4, exportButton);
        topBar.setWidget(0, 5, new ClosePopupButton(this));
        topBar.setWidget(1, 0, new HTML("<h2>Filter:</h2>"));
        topBar.setWidget(1, 1, new HTML(new SafeHtmlBuilder().appendEscaped(selectedFilterGroup.getName()).toSafeHtml()));

        //Basic layout
        layout = new FlexTable();
        layout.setWidget(0, 0, topBar);
        setWidget(layout);

        //TextBox for base URI
        this.baseUriInput = new TextBox();
        this.baseUriInput.setValue("http://example.org");
        layout.setWidget(1, 0, new HTML("<h3>Base URI (empty prefix):</h3>"));
        layout.setWidget(1, 1, this.baseUriInput);

        //CheckBox for rowUUIDs
        this.requireRowUUIDsInput = new CheckBox();
        this.requireRowUUIDsInput.setValue(false);
        layout.setWidget(2, 0, new HTML("<h3>Require row UUIDs</h3>"));
        layout.setWidget(2,1, this.requireRowUUIDsInput);

        //Placeholders for template list
        layout.setWidget(3, 0, new HTML("<h3>Template</h3>"));
        layout.setWidget(3,1, new HTML("Loading..."));

        //Asynchronous call to get the list of available templates
        SecureGWT.getFormService().getFlexibleExportSettings(formId, selectedFilterGroup, new TemplateSettingsCallback());
    }

    /*
    * Handler for clicking the "Export" button
    * */
    private class CreateExportHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            //Start the export
            String baseUri = baseUriInput.getValue().trim();
            Boolean requireRowUUID = requireRowUUIDsInput.getValue();
            String templateGroup = templateGroupDropdown.getSelectedValue();

            //Don't accept an empty base URI
            if(baseUri.length() == 0)
                Window.alert(EXPORT_ERROR_MSG);

            //Trigger the actual template-based export, register the success- and failure-handler
            secureRequest(
                SecureGWT.getFormService(),
                (rpc, sc, cb) -> rpc.createFlexibleFileFromFilter(selectedFilterGroup, baseUri, requireRowUUID, templateGroup, cb),
                this::onSuccess,
                this::onFailure
            );
        }

        //Failure-Handler for template-based export
        private void onFailure(Throwable cause) {
            AggregateUI.getUI().reportError(cause);
        }

        //Success-Handler for template-based export
        private void onSuccess(Boolean result) {
            if (result) {
                AggregateUI.getUI().redirectToSubTab(SubTabs.EXPORT);
            } else {
                Window.alert(EXPORT_ERROR_MSG);
            }
            hide();
        }
    }

    //Callbacks for the TemplateExportOptions-request
    private class TemplateSettingsCallback implements AsyncCallback<TemplateExportOptions> {
        @Override
        public void onFailure(Throwable caught) {
            AggregateUI.getUI().reportError(caught);
        }

        @Override
        public void onSuccess(TemplateExportOptions result) {
            //Display the available templates in a dropdown-list
            templateGroupDropdown = new ListBox();
            if(result.getRegisteredTemplateIds().size() == 0){
                //No templates available (e.g. no semantic annotations for this dataset)
                layout.setWidget(3, 1, new HTML("No templates available for this dataset"));
            } else{
                //Fill & insert dropdown list
                for(String templateId : result.getRegisteredTemplateIds()){
                    templateGroupDropdown.addItem(result.getTemplateDisplayName(templateId), templateId);
                }
                templateGroupDropdown.setVisibleItemCount(1);
                layout.setWidget(3, 1, templateGroupDropdown);

                //Enable the export button now that the template-list is available
                exportButton.setEnabled(true);
            }
        }
    }
}
