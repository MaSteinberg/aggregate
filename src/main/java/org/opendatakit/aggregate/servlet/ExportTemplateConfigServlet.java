package org.opendatakit.aggregate.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatakit.aggregate.client.form.TemplateExportOptions;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.server.ExportTemplateConfigManager;
import org.opendatakit.common.web.constants.HtmlConsts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Markus Steinberg
 * REST-Servlet to make the template-based export configuration available to ODK Build
 */
public class ExportTemplateConfigServlet extends ServletUtilBase {
    /**
     * Serial number for serialization
     */
    private static final long serialVersionUID = -3784463015082808112L;

    /**
     * URI from base
     */
    public static final String ADDR = UIConsts.TEMPLATE_CONFIG_SERVLET_ADDR;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            PrintWriter out = resp.getWriter();
            resp.setContentType(HtmlConsts.RESP_TYPE_JSON);
            resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
            resp.addHeader("Access-Control-Allow-Origin", "*");
            TemplateExportOptions options = ExportTemplateConfigManager.getTemplateExportOptions();
            ObjectMapper jsonMapper = new ObjectMapper();
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(out, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
