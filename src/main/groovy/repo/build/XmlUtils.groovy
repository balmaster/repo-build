package repo.build

import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 */
class XmlUtils {

    static Node parse(File xmlFile) {
        return new XmlParser().parse(xmlFile)
    }

    static String modifyWithPreserveFormatting(String xml, Closure action) {
        def document = groovy.xml.DOMBuilder.parse(new StringReader(xml))
        def root = document.documentElement
        use(groovy.xml.dom.DOMCategory) {
            action(root)
        }

        StringWriter sw = new StringWriter()
        def source = new DOMSource(root)
        def target = new StreamResult(sw)

        TransformerFactory factory = TransformerFactory.newInstance()
        groovy.xml.XmlUtil.setIndent(factory, 2)
        try {
            Transformer transformer = factory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(OutputKeys.METHOD, "xml")
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            transformer.transform(source, target)
        }
        catch (TransformerException e) {
            throw new GroovyRuntimeException(e.getMessage())
        }

        return sw.toString();
    }
}
