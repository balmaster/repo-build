import groovy.xml.MarkupBuilder

String executeCmd0(File dir, String cmd) {
    println "execute command '$cmd' in '$dir'"
    def proc = cmd.execute(null, dir)
    def exitCode = proc.waitFor()
    if( exitCode != 0 ) {
        println "${proc.getErrorStream()}"
        println proc.text
        throw new RuntimeException("command '$cmd' has exit code $exitCode");
    }
    println "${proc.getErrorStream()}"
    return proc.text;
}

boolean gitBranchPresent( File dir, String branch ) {
    return ! executeCmd0(dir, "git ls-remote . $branch").empty;
}

void mergeFeatureBranch( File basedir, manifest, String branch ) {
    def remoteName = manifest.remote[0].@name
    def remoteBranch = "$remoteName/$branch"
    manifest.project
            .findAll {
                gitBranchPresent(new File(basedir,it.@path), remoteBranch)
            }
            .each {
                println "branch $remoteBranch found in ${it.@path}"
                println executeCmd0(new File(basedir, it.@path),"git merge $remoteBranch")
            }
}

void generatePomXml(File basedir, manifest, String featureBranch, File targetPom) {
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
                    manifest.project
                            .findAll {
                                new File(new File(basedir,it.@path),"pom.xml").exists() && !"build".equals(it.@path)
                            }
                            .each { "module"("../../${it.@path}") }
                }
            }
}

void prepareBuild(File basedir, manifest, featureBranch ) {
    mergeFeatureBranch(basedir, manifest, featureBranch )
    generatePomXml(basedir, manifest, featureBranch,new File("target","buildPom.xml"))
}

def basedir = new File('..')
def manifestFile = new File(basedir, 'manifest/default.xml')
def manifest = new XmlParser().parse(manifestFile)
def featureBranch = project.properties["featureBranch"]

prepareBuild(basedir, manifest, featureBranch)