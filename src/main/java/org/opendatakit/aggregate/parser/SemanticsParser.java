package org.opendatakit.aggregate.parser;

import org.apache.commons.io.IOUtils;
import org.opendatakit.aggregate.odktables.rdf.SemanticsTable;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticsParser {
    private Map<String, Map<String, String>> termValueMaps;
    private Logger logger = LoggerFactory.getLogger(SemanticsParser.class);

    public void parseSemantics(String xmlDocument, String formId, CallingContext cc) throws SAXException, ParserConfigurationException, IOException {
        termValueMaps = new HashMap<>();

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new SemanticsEventHandler(formId, cc));
        xmlReader.parse(new InputSource(IOUtils.toInputStream(xmlDocument, "UTF-8")));
    }

    private class SemanticsEventHandler extends DefaultHandler{
        private String formId;
        private CallingContext cc;

        public SemanticsEventHandler(String formId, CallingContext cc) {
            this.formId = formId;
            this.cc = cc;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(qName.equals("sem:node")){
                String controlName = "";
                Map<String, String> termValueMap = new HashMap<>();
                for(int i = 0; i < attributes.getLength(); i++){
                    String val = attributes.getValue(i);
                    String attributeName = attributes.getQName(i);
                    if(val != null && val.trim().length() > 0) {
                        if (attributeName.equals("fieldName")) {
                            controlName = val;
                        } else {
                            termValueMap.put(attributeName, val);
                        }
                    }
                }
                //We can't assert the DB-entities in the loop above as we need the fieldName but we can't guarantee
                // the order of the XML-attributes
                for(Map.Entry<String, String> entry : termValueMap.entrySet()){
                    try {
                        SemanticsTable.assertSemantics(formId, controlName, entry.getKey(), entry.getValue(), cc);
                    } catch (ODKDatastoreException e) {
                        logger.error("Failed to create database entity");
                        e.printStackTrace();
                    }
                }
                termValueMaps.put(controlName, termValueMap);
            }
        }
    }
}
