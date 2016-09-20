package repo.build

import groovy.transform.CompileStatic;

@CompileStatic
class RepoEnv {

    final File basedir;
    Node manifest;

    RepoEnv(File basedir) {
        this.basedir = basedir

        openManifest()
    }
    
    void openManifest() {
        def manifestFile = new File(basedir, 'manifest/default.xml')
        if(manifestFile.exists()) {
            manifest = new XmlParser().parse(manifestFile)
        }
    }
}
