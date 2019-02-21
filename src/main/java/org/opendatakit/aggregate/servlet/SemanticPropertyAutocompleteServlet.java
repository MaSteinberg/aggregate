package org.opendatakit.aggregate.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.opendatakit.aggregate.client.form.SemanticAutocompleteElement;
import org.opendatakit.aggregate.client.form.SemanticPropertyConfiguration;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.server.RdfTemplateConfigManager;
import org.opendatakit.common.utils.SparqlQueryManager;
import org.opendatakit.common.web.constants.HtmlConsts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SemanticPropertyAutocompleteServlet extends ServletUtilBase {
    /**
     * Serial number for serialization
     */
    private static final long serialVersionUID = -3787446330180348112L;

    /**
     * URI from base
     */
    public static final String ADDR = UIConsts.SEMANTIC_AUTOCOMPLETE_SERVLET_ADDR;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get parameter
        String prop = getParameter(req, ServletConsts.SEMANTIC_PROPERTY);
        if (prop == null) {
            errorMissingKeyParam(resp);
            return;
        }

        //Get configuration of requested property
        SemanticPropertyConfiguration config = RdfTemplateConfigManager.getPropertyConfig(prop);

        List<SemanticAutocompleteElement> results;
        if(config == null || (StringUtils.isBlank(config.getEndpoint()) ^ StringUtils.isBlank(config.getQuery()))){
            //If we have no configuration for the requested property or
            //if either the endpoint or the query is missing, return null so the caller knows the configuration
            //is incorrect/incomplete
            results = null;
        } else if(StringUtils.isBlank(config.getEndpoint()) && StringUtils.isBlank(config.getQuery())){
            //Empty Endpoint and empty Query => Intentionally no autocompletion
            results = new ArrayList<>();
        } else{
            //Issue SPARQL query
            results = SparqlQueryManager.issueAutocompletionSparqlQuery(config.getEndpoint(),
                    config.getQuery());
        }

        try {
            PrintWriter out = resp.getWriter();
            resp.setContentType(HtmlConsts.RESP_TYPE_JSON);
            resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
            resp.addHeader("Access-Control-Allow-Origin", "*");
            ObjectMapper jsonMapper = new ObjectMapper();
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(out, results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
