package repo.build

import org.apache.log4j.Logger

class RepoEnv {

    static Logger logger = Logger.getLogger(RepoEnv.class)

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
        if (manifestFile.exists()) {
            manifest = new XmlParser().parse(manifestFile)
        }
    }

    void loadProperties() {
        props = new Properties()
        File f = new File(basedir, "repo-build.properties")
        try {
            props.load(f.newDataInputStream())
        } catch (IOException ignored) {
            logger.warn("Could not load properties")
        }
    }

    Integer getDefaultParallel() {
        def value = manifest?.'default'.first()?.'sync-j'?.value
        return value ? Integer.parseInt(value) : null
    }
}
