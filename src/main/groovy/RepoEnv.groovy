class RepoEnv {

    File basedir;
    Node manifest;
    
    RepoEnv(buildProject) {
        basedir = new File(buildProject.basedir,'..')
        
        def manifestFile = new File(basedir, 'manifest/default.xml')
        manifest = new XmlParser().parse(manifestFile)
    }
    
    File getBuildTarget() {
        return new File(basedir,"build/target")
    }
}
