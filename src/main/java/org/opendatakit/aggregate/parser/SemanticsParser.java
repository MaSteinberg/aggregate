package org.opendatakit.aggregate.parser;

import org.apache.commons.io.IOUtils;
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
    private List<String> availableTerms;
    private Map<String, Map<String, String>> termValueMaps;

    public void parseSemantics(String xmlDocument) throws SAXException, ParserConfigurationException, IOException {
        availableTerms = new ArrayList<>();
        termValueMaps = new HashMap<>();
        //TODO This information should be stored in a config file
        availableTerms.add("Creator");
        availableTerms.add("Device");
        availableTerms.add("Unit");

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new SemanticsEventHandler());
        xmlReader.parse(new InputSource(IOUtils.toInputStream(xmlDocument, "UTF-8")));
    }

    private class SemanticsEventHandler extends DefaultHandler{
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(qName.equals("sem:node")){
                String controlName = attributes.getValue("fieldName");
                Map<String, String> termValueMap = new HashMap<>();
                for (String term: availableTerms) {
                    String val = attributes.getValue(term);
                    if(val != null && val.trim().length() > 0){
                        termValueMap.put(term, val);
                    }
                }
                termValueMaps.put(controlName, termValueMap);
            }
        }
    }
}
