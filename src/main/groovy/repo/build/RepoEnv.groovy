package repo.build

import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@CompileStatic
class RepoEnv {

    static Logger logger = LogManager.getLogger(RepoEnv.class)

    final File basedir
    Node manifest
    Properties props

    RepoEnv(File basedir) {
        this.basedir = basedir
        openManifest()
        loadProperties()
    }
    
    void openManifest() {
        def manifestFile = new File(basedir, 'manifest/default.xml')
        if(manifestFile.exists()) {
            manifest = new XmlParser().parse(manifestFile)
        }
    }

    void loadProperties() {
        props = new Properties()
        File f = new File(basedir, "repo-build.properties")
        try {
            props.load(f.newDataInputStream())
        } catch (IOException e) {
            logger.warn("Could not load properties", e)
        }
    }
}
