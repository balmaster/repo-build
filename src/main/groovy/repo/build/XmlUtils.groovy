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

    static String transformManifestForBundle(File manifest, File sourceImportDir){

        modifyWithPreserveFormatting(manifest.text, { xml ->
            xml.remote[0].setAttribute('fetch', sourceImportDir.getAbsolutePath())
            //rewrite name for project element from <name>.git to <name>.bundle
            xml.'*'.findAll { node -> node.name() == 'project' }.each {
                project ->
                    def newName = project.getAttribute('name')
                    if (newName.endsWith('.git')){
                        newName = newName[0..-5] + '.bundle'
                    } else {
                        newName = newName + '.bundle'
                    }

                    project.setAttribute('name', newName)
            }
        })
    }

    static String changeManifestBaseRemoteUrl(File manifest, String newRemoteUrl){
        modifyWithPreserveFormatting(manifest.text, { xml ->
            xml.remote[0].setAttribute('fetch', newRemoteUrl)
        })
    }
}
