import java.io.File;
import groovy.xml.MarkupBuilder;

class Pom {
    static void generateXml(RepoEnv env, String featureBranch, File targetPom) {
        println "Generate pom $targetPom"
    
        def xmlWriter = new FileWriter(targetPom)
        def xmlMarkup = new MarkupBuilder(xmlWriter)
    
        xmlMarkup
                ."project"("xmlns":"http://maven.apache.org/POM/4.0.0","xmlns:xsi":"http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation":"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd") {
                    "modelVersion"("4.0.0")
                    "groupId"("ru.sberbank.sirius")
                    "artifactId"("sirius-build-$featureBranch")
                    "version"("7.0.0-SNAPSHOT")
                    "packaging"("pom")
                    "modules" {
                        env.manifest.project
                                .findAll {
                                    new File(new File(env.basedir,it.@path),"pom.xml").exists() && !"build".equals(it.@path)
                                }
                                .each { "module"("../../${it.@path}") }
                    }
                }
    }
    
    
}
