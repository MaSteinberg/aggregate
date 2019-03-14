package org.opendatakit.aggregate.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.tools.ant.taskdefs.Javadoc;
import org.opendatakit.aggregate.client.form.RdfExportOptions;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.server.FormServiceImpl;
import org.opendatakit.aggregate.server.RdfTemplateConfigManager;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.springframework.util.ResourceUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class RdfTemplateConfigServlet extends ServletUtilBase {
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
            RdfExportOptions options = RdfTemplateConfigManager.getRdfExportOptions();
            ObjectMapper jsonMapper = new ObjectMapper();
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(out, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
