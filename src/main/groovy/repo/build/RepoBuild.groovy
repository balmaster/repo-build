package repo.build

import groovy.util.CliBuilder
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager
import groovy.transform.CompileStatic

class RepoBuild {

    static Logger logger = LogManager.getLogger(RepoBuild.class)

    private static final String ORIGIN = "origin"

    static final String BUNDLES = "bundles"

    static final String MANIFEST = "manifest"

    static final String POM_XML = "pom.xml"
    final CliBuilder cli
    final String[] args
    OptionAccessor options
    RepoEnv env

    RepoBuild(String [] args) {
        this.cli = CliBuilderFactory.build()
        this.args = args
    }

    @CompileStatic
    static void main(String [] args) {
        def repoBuild = new RepoBuild(args)
        try {
            repoBuild.execute()
        }
        catch(Exception e) {
            logger.error(e.message)
        }
    }
    
    void execute() {
        options = cli.parse(args)
        env = new RepoEnv(getRepoBasedir())
        println getRepoBasedir()

        def commands = options.arguments()
        commands.each {
            switch(it) {
                case "build-pom":
                    doBuildPom()
                    break
                case "switch":
                    doSwitch()
                    break
                case "prepare-merge":
                    doPrepareMerge()
                    break
                case "import-bundles":
                    doImportBundles()
                    break
                case "export-bundles":
                    doExportBundles()
                    break
                case "init":
                    doInit()
                    break
                case "sync":
                    doSync()
                    break
                default:
                    println cli.usage()
            }
        }
    }

    File getRepoBasedir() {
        return options.r ? new File(options.r) : new File(".").getAbsoluteFile()
    }

    void doBuildPom() {
        def buildPomFile = options.p ?
                new File(options.p) :
                new File(getRepoBasedir(),POM_XML)
        def featureBranch = options.f ?
                options.f :
                Git.getBranch(new File(getRepoBasedir(), MANIFEST))
        Pom.generateXml(env, featureBranch, buildPomFile)
    }

    void doSwitch() {
        if(options.m) {
            //RepoManifest.
        } else {
            def featureBranch = options.f
            if( featureBranch ) {
                RepoManifest.switchToBranch(env, featureBranch)
            } else {
                throw new RepoBuildException("Use: 'repo-build -f <featureBranch> switch'")
            }
        }
    }

    void doPrepareMerge() {
        def featureBranch = getRequired(options.f,"featureBranch")
        if( featureBranch) {
            RepoManifest.mergeFeatureBranch(env, featureBranch )
        } else {
            throw new RepoBuildException("Use: 'repo-build -f <featureBranch> prepare-merge'")
        }
    }

    void doImportBundles() {
    }

    void doExportBundles() {
        if(options.f) {
            def featureBranch = getRequired(options.f,"featureBranch")
            def targetExportDir = options.t ?
                    new File(options.t)
                    : new File(getRepoBasedir(), BUNDLES)

            targetExportDir.mkdirs()
            RepoManifest.createFeatureBundles(env, targetExportDir, featureBranch )
        } else if(options.m){
            def targetExportDir = options.t ?
                    new File(options.t)
                    : new File(getRepoBasedir(), BUNDLES)

            targetExportDir.mkdirs()
            RepoManifest.createManifestBundles(env, targetExportDir )
        } else {
            throw new RepoBuildException("Use: 'repo-build -m export-bundles' or 'repo-build -f <featureBranch> export-bundles'")
        }
    }

    void doInit() {
        if(!env.manifest) {
            cloneManifest()
        } else {
            def manifestBranch = getRequired(options.b, "Use: 'repo-build -b <manifestBranch>'")
            checkoutUpdateManifest(manifestBranch)
        }
    }

    @CompileStatic
    void doSync() {
        if(getManifestDir().exists()) {
            def manifestBranch = Git.getBranch(getManifestDir())
            if("HEAD".equals(manifestBranch)) {
                throw new RepoBuildException("manifest branch must be local, use repo-build -b <manifestBranch> init" )
            }
            checkoutUpdateManifest(manifestBranch)
            RepoManifest.fetchUpdate(env)
        } else {
            throw new RepoBuildException("manifest dir not found")
        }
    }

    @CompileStatic
    File getManifestDir() {
        return new File(getRepoBasedir(), MANIFEST)
    }

    void cloneManifest() {
        def manifestUrl = options.M
        def manifestBranch = options.b
        def manifestDir = getManifestDir()
        if(manifestUrl && manifestBranch) {
            manifestDir.mkdirs()
            Git.clone(env, manifestUrl, ORIGIN, manifestDir )
        } else {
            throw new RepoBuildException("Use: 'repo-build -M <manifestUrl> -b <manifestBranch>'")
        }
        Git.checkoutUpdate(env, manifestBranch, "origin/$manifestBranch", manifestDir)
        env.openManifest()
    }

    void checkoutUpdateManifest(String manifestBranch) {
        def manifestDir = getManifestDir()
        Git.fetch(env, ORIGIN,  manifestDir)
        Git.checkoutUpdate(env, manifestBranch, "origin/$manifestBranch", manifestDir)
        env.openManifest()
    }

    @CompileStatic
    String getRequired(value, String msg) {
        if(value) {
            return value
        } else {
            throw new RepoBuildException(msg)
        }
    }
}
