package repo.build
class RepoEnv {

    final File basedir;
    final Node manifest;
    
    RepoEnv(File basedir) {
        this.basedir = basedir
        
        def manifestFile = new File(basedir, 'manifest/default.xml')
        manifest = new XmlParser().parse(manifestFile)
    }
    
}
