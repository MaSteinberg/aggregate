package org.opendatakit.aggregate.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mysql.jdbc.StringUtils;
import org.opendatakit.aggregate.client.form.RdfExportOptions;
import org.opendatakit.aggregate.client.form.RdfTemplateConfig;
import org.opendatakit.aggregate.client.form.RdfToplevelConfig;
import org.opendatakit.aggregate.client.form.TemplateProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RdfTemplateConfigManager {
    private static final String configRoot = "rdfExport";
    private static final String templateDirectory = "mustache_templates";
    private static final String rootConfigFilename = "rdfExportTemplateConfig.yml";
    private static final String templateConfigFilename = "config.yml";

    public static RdfExportOptions getRdfExportOptions(){
        RdfToplevelConfig toplevelConfig = RdfTemplateConfigManager.getToplevelRdfConfig();
        Map<String, RdfTemplateConfig> templateConfigs = new HashMap<>();
        for (String templateGroupName : toplevelConfig.getTemplates()) {
            RdfTemplateConfig templateConfig = RdfTemplateConfigManager.getRdfTemplateConfig(templateGroupName);
            templateConfigs.put(templateGroupName, templateConfig);
        }
        return new RdfExportOptions(toplevelConfig.getAvailableProperties(), templateConfigs);
    }

    public static RdfToplevelConfig getToplevelRdfConfig(){
        try{
            //https://www.baeldung.com/spring-classpath-file-access
            //Maven's src/main/resources are automatically added to the root of the classpath
            File file = new ClassPathResource(configRoot + "/" + rootConfigFilename).getFile();
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            return yamlMapper.readValue(file, RdfToplevelConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    Parses the configuration of the templateGroup from it's configuration file.
    TemplateGroup has to match the directory name where the configuration file and the templates are placed.
     */
    public static RdfTemplateConfig getRdfTemplateConfig(String templateGroup){
        try{
            //https://www.baeldung.com/spring-classpath-file-access
            //Maven's src/main/resources are automatically added to the root of the classpath
            File file = new ClassPathResource(configRoot + "/" + templateDirectory + "/" + templateGroup + "/" + templateConfigFilename).getFile();
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            RdfTemplateConfig conf = yamlMapper.readValue(file, RdfTemplateConfig.class);
            if(StringUtils.isEmptyOrWhitespaceOnly(conf.getDisplayName())){
                conf.setDisplayName(templateGroup);
            }
            return conf;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
