package org.opendatakit.aggregate.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opendatakit.aggregate.client.form.RdfExportOptions;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

public class RdfTemplateConfigManager {
    public static RdfExportOptions getRdfExportOptions(){
        try {
            File file = ResourceUtils.getFile("classpath:rdfExport/rdfExportTemplateConfig.yml");
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            return yamlMapper.readValue(file, RdfExportOptions.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
