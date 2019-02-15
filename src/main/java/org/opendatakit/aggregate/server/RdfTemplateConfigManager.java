package org.opendatakit.aggregate.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opendatakit.aggregate.client.form.RdfExportOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

public class RdfTemplateConfigManager {
    public static RdfExportOptions getRdfExportOptions(){
        try {
            //https://www.baeldung.com/spring-classpath-file-access
            //Maven's src/main/resources are automatically added to the root of the classpath
            File file = new ClassPathResource("rdfExport/rdfExportTemplateConfig.yml").getFile();
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            return yamlMapper.readValue(file, RdfExportOptions.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
