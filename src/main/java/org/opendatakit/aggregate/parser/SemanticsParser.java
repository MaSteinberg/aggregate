package org.opendatakit.aggregate.parser;

import org.apache.commons.io.IOUtils;
import org.opendatakit.aggregate.odktables.flexibleExport.SemanticsTable;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Markus Steinberg
 *
 */
public class SemanticsParser {
    private static final String SEM_NAMESPACE = "http://annotation";
    private static final String SEM_NODE_TAG_NAME = "node";
    private static final String FIELD_IDENTIFIER_NAME = "fieldName";
    private Logger logger = LoggerFactory.getLogger(SemanticsParser.class);

    public void parseSemantics(String xmlDocument, String formId, CallingContext cc) throws SAXException, ParserConfigurationException, IOException {
        //Create a SAX-parser that parses the <sem:node> XML-Elements and persists them in the DB
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

        SemanticsEventHandler(String formId, CallingContext cc) {
            this.formId = formId;
            this.cc = cc;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            //A <sem:node></sem:node> describes the semantics of a single data-field in a survey
            if(uri.equals(SEM_NAMESPACE) && localName.equals(SEM_NODE_TAG_NAME)){
                String fieldName = "";
                Map<String, String> termValueMap = new HashMap<>();
                //Loop through all attributes of the current XML-element
                for(int i = 0; i < attributes.getLength(); i++){
                    String val = attributes.getValue(i);
                    String attributeName = attributes.getQName(i);
                    if(val != null && val.trim().length() > 0) {
                        //Store the value either as the fieldName or in a Map<AttributeName, Value>
                        if (attributeName.equals(FIELD_IDENTIFIER_NAME)) {
                            fieldName = val;
                        } else {
                            //Decode the URI
                            termValueMap.put(attributeName, val);
                        }
                    }
                }
                //We can't assert the DB-entities in the loop above as we need the fieldName but we can't guarantee
                //the order of the XML-attributes
                for(Map.Entry<String, String> entry : termValueMap.entrySet()){
                    try {
                        //Persist the semantics in the database
                        SemanticsTable.assertSemantics(formId, fieldName, entry.getKey(), entry.getValue(), cc);
                    } catch (ODKDatastoreException e) {
                        logger.error("Failed to create database entity");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
