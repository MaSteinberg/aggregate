package org.opendatakit.aggregate.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mysql.jdbc.StringUtils;
import org.opendatakit.aggregate.client.form.*;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Markus Steinberg
 * Class that manages access and parsing of the configuration files for the template-based export.
 */
public class ExportTemplateConfigManager {
    private static final String configRoot = "templateExport";
    private static final String templateDirectory = "mustache_templates";
    private static final String rootConfigFilename = "TemplateExportConfig.yml";
    private static final String templateConfigFilename = "config.yml";

    public static TemplateExportOptions getTemplateExportOptions(){
        TemplateToplevelConfig toplevelConfig = ExportTemplateConfigManager.getToplevelConfig();
        Map<String, ExportTemplateConfig> templateConfigs = new HashMap<>();
        for (String templateGroupName : toplevelConfig.getTemplates()) {
            ExportTemplateConfig templateConfig = ExportTemplateConfigManager.getExportTemplateConfig(templateGroupName);
            templateConfigs.put(templateGroupName, templateConfig);
        }
        return new TemplateExportOptions(toplevelConfig.getAvailableProperties(), templateConfigs);
    }

    public static TemplateToplevelConfig getToplevelConfig(){
        try{
            //https://www.baeldung.com/spring-classpath-file-access
            //Maven's src/main/resources are automatically added to the root of the classpath
            File file = new ClassPathResource(configRoot + "/" + rootConfigFilename).getFile();
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            return yamlMapper.readValue(file, TemplateToplevelConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    Parses the configuration of the templateGroup from it's configuration file.
    TemplateGroup has to match the directory name where the configuration file and the templates are placed.
     */
    public static ExportTemplateConfig getExportTemplateConfig(String templateGroup){
        try{
            //https://www.baeldung.com/spring-classpath-file-access
            //Maven's src/main/resources are automatically added to the root of the classpath
            File file = new ClassPathResource(configRoot + "/" + templateDirectory + "/" + templateGroup + "/" + templateConfigFilename).getFile();
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            ExportTemplateConfig conf = yamlMapper.readValue(file, ExportTemplateConfig.class);
            if(StringUtils.isEmptyOrWhitespaceOnly(conf.getDisplayName())){
                conf.setDisplayName(templateGroup);
            }
            return conf;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    Utility to get the configuration of a single property
     */
    public static SemanticPropertyConfiguration getPropertyConfig(String property){
        TemplateToplevelConfig config = getToplevelConfig();
        return config.getAvailableProperties().get(property);
    }
}
