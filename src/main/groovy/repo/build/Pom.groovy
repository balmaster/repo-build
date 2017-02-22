package repo.build

import groovy.xml.MarkupBuilder;

class Pom {
    static void generateXml(RepoEnv env, String artifactName, File targetPom) {
        println "Generate pom $targetPom"

        new FileWriter(targetPom).withCloseable { xmlWriter ->
            def xmlMarkup = new MarkupBuilder(xmlWriter)
            def artifact = artifactName.replace('/', '-')
            xmlMarkup
                    ."project"("xmlns": "http://maven.apache.org/POM/4.0.0", "xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:schemaLocation": "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd") {
                "modelVersion"("4.0.0")
                "groupId"("repo.build")
                "artifactId"(artifact)
                "version"("1.0.0-SNAPSHOT")
                "packaging"("pom")
                "modules" {
                    env.manifest.project
                            .findAll {
                        new File(new File(env.basedir, it.@path), "pom.xml").exists()
                    }
                    .each { "module"(it.@path) }
                }
            }
        }
    }


    static List<String> getModules(File pomFile) {
        def xml = XmlUtils.parse(pomFile)
        return xml.modules.module.inject([], { result, module ->
            result.add(module.text())
            result
        })
    }

}
