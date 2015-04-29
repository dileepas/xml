package xmlprocess;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;

import org.w3c.dom.NodeList;

/**
 * Created by dileepa on 4/27/15.
 */
public class DomParser {


    public static void main(String[] args) {

        try {
            File file = new File("/home/dileepa/myWork/backupIdeaProjects/test2/src/main/java/xmlprocess/as2.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            new DomParser().readUsingXpath(doc);
                    doc.getInputEncoding();
            String expression = "/Employees/Employee[@type='admin']/firstname";
            String expression2 = "/Employees/Employee[age>40]/firstname";
            System.out.println("done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void readByTagName(Document doc) {
        NodeList list = doc.getElementsByTagName("Scope");

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                System.out.println(element.getElementsByTagName("Type").item(0).getTextContent());
            }
        }
    }

    private void readUsingXpath(Document doc) throws Exception{
        XPath xPath = XPathFactory.newInstance().newXPath();

        String expression = "/StandardBusinessDocument/StandardBusinessDocumentHeader/HeaderVersion";
        String version = xPath.compile(expression).evaluate(doc);
        System.out.println("version " + version);
        String expression2 = "/StandardBusinessDocument/StandardBusinessDocumentHeader/BusinessScope/Scope";
        Node n = (Node) xPath.compile(expression2).evaluate(doc, XPathConstants.NODE);

        String expression3 = "/StandardBusinessDocument/StandardBusinessDocumentHeader/BusinessScope/Scope[Type=\"DOCUMENTID\"]/InstanceIdentifier";
        String val = xPath.compile(expression3).evaluate(doc);
        System.out.println(val);
    }

}
