package xmlprocess;

import org.codehaus.stax2.ri.evt.StartDocumentEventImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


public class StaXParser {
    static final String DATE = "date";
    static final String SBDH_HEADER = "StandardBusinessDocumentHeader";
    static final String SENDER = "Sender";
    static final String RECEIVER = "Receiver";
    static final String IDENTIFIER = "Identifier";
    static final String TYPE = "Type";
    static final String CREDIT_NOTE = "CreditNote";
    static final String DOCUMENT_IDENTIFICATION = "DocumentIdentification";
    static final String INSTANCE_IDENTIFIER = "InstanceIdentifier";
    static final String BUSINESS_SCOPE = "BusinessScope";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public List<Item> readConfig(InputStream in) {
        List<Item> items = new ArrayList<Item>();
        try {
            boolean xy = in.markSupported();
            in.mark(100);
            PushbackInputStream pIs = new PushbackInputStream(in, 1024);

            byte[] header = new byte[100];
            pIs.read(header);
            pIs.unread(header);

            // in.reset();
            // saveFile(pIs,"test_pushback2.xml");
            // in.reset();
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(pIs);
            //  XMLStreamReader s = inputFactory.createXMLStreamReader(in);
            //   s.getCharacterEncodingScheme();

            String encoding = getEncodingFromXml(eventReader);


                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();

                    // if (isAs2Document) {
                    if (event.isStartElement()) {
                        StartElement startElement = event.asStartElement();
                        // If we have an item element, we create a new item
                        if (startElement.getName().getLocalPart().equals(SBDH_HEADER)) {
                            while (!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(SBDH_HEADER))) {
                                event = eventReader.nextEvent();
                                if (event.isStartElement()) {
                                    if (event.asStartElement().getName().getLocalPart().equals(SENDER)) {
                                        getXmlValWithoutValueMatch(eventReader, SENDER, IDENTIFIER);
                                        continue;
                                    }
                                    if (event.asStartElement().getName().getLocalPart().equals(RECEIVER)) {
                                        getXmlValWithoutValueMatch(eventReader, RECEIVER, IDENTIFIER);
                                        continue;
                                    }
                                    if (event.asStartElement().getName().getLocalPart().equals(DOCUMENT_IDENTIFICATION)) {
                                        getXmlValWithoutValueMatch(eventReader, DOCUMENT_IDENTIFICATION, INSTANCE_IDENTIFIER);
                                        continue;
                                    }
                                    if (event.asStartElement().getName().getLocalPart().equals(BUSINESS_SCOPE)) {
                                        while (!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(BUSINESS_SCOPE))) {
                                            event = eventReader.nextEvent();
                                            if (event.isStartElement()) {
                                                String x = getXmlValWithoutValueMatch(eventReader, BUSINESS_SCOPE, TYPE);
                                                if (x.equals("DOCUMENTID")) {
                                                    getXmlValWithoutValueMatch(eventReader, TYPE, INSTANCE_IDENTIFIER);
                                                } else if (x.equals("PROCESSID")) {
                                                    getXmlValWithoutValueMatch(eventReader, TYPE, INSTANCE_IDENTIFIER);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // If we reach the //} end of an item element, we add it to the list
                    if (event.isEndElement()) {
                        EndElement endElement = event.asEndElement();
                        if (endElement.getName().getLocalPart().equals(SBDH_HEADER)) {
                            String xmlFragment = readUntilMatchElement(eventReader);
                            System.out.print(xmlFragment);
                        }
                    }
                }


            //    in.reset();
            // saveFile(in,"test2.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    private String getXmlValWithoutValueMatch(XMLEventReader eventReader, String h1, String h2) throws XMLStreamException {
        XMLEvent event;
        String ret = "";
        boolean foundEndElement = false;
        while (!foundEndElement) {
            do {
                event = eventReader.nextEvent(); // end element
            } while (!event.isStartElement());

            if (event.asStartElement().getName().getLocalPart().equals(h2)) {
                event = eventReader.nextEvent();
                if (event.isCharacters()) {
                    System.out.println(h1 + " " + h2 + " " + event.asCharacters().getData());
                    foundEndElement = true;
                    ret = event.asCharacters().getData();
                }
            }
        }
        return ret;
    }

    private String getXmlValWithValueMatch(XMLEventReader eventReader, String h1, String h2, String value) throws XMLStreamException {
        XMLEvent event;
        String ret = "";
        boolean foundEndElement = false;
        while (!foundEndElement) {
            do {
                event = eventReader.nextEvent(); // end element
            } while (!event.isStartElement());

            if (event.asStartElement().getName().getLocalPart().equals(h1)) {
                event = eventReader.nextEvent();
                if (event.isCharacters()) {
                    if (event.asCharacters().getData().equals(value)) {
                        // event = eventReader.nextEvent();
                        do {
                            event = eventReader.nextEvent(); // end element
                        } while (!event.isStartElement());

                        if (event.asStartElement().getName().getLocalPart().equals(h2)) {
                            event = eventReader.nextEvent();
                            if (event.isCharacters()) {
                                System.out.println("Document Identifier : " + event.asCharacters().getData());
                                foundEndElement = true;
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    private boolean checkForSbdhTag(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent event = eventReader.peek();
        return event.asStartElement().getName().getLocalPart().equals("StandardBusinessDocument");
    }

    private String getEncodingFromXml(XMLEventReader eventReader) throws XMLStreamException {
        String encoding = "";
        XMLEvent event = eventReader.nextEvent();

        if (event.isStartDocument()) {
            encoding = ((StartDocumentEventImpl) event).getCharacterEncodingScheme();
            System.out.println(encoding);
        }

        return encoding;
    }

    private void saveFile(InputStream in, String fileName) {
        try {
            OutputStream out = new FileOutputStream("/home/dileepa/peppol/" + fileName);
            byte[] buf = new byte[5000];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String readUntilMatchElement(XMLEventReader eventReader)
            throws XMLStreamException, UnsupportedEncodingException {

        StringWriter buf = new StringWriter();
        XMLEvent xmlEvent;

        while (true) {
            xmlEvent = eventReader.peek(); // end element
            if (xmlEvent.isStartElement()) {
                break;
            } else {
                eventReader.nextEvent();
            }
        }

        while (eventReader.hasNext()) {
            // peek event
            xmlEvent = eventReader.peek();

            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("StandardBusinessDocument")) {
                    break;
                } else {
                    xmlEvent = eventReader.nextEvent();
                }
            } else {
                xmlEvent = eventReader.nextEvent();
            }

            xmlEvent.writeAsEncodedUnicode(buf);
        }
        buf.getBuffer().toString().getBytes("UTF-8");
        return buf.getBuffer().toString();
    }

}