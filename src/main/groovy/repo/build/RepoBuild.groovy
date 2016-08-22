package repo.build

import groovy.util.CliBuilder;

class RepoBuild {

    private static final String ORIGIN = "origin"
    final CliBuilder cli
    final String[] args
    OptionAccessor options
    RepoEnv env

    RepoBuild(args) {
        this.cli = CliBuilderFactory.build()
        this.args = args
    }

    static main(args) {
        def repoBuild = new RepoBuild(args)
        try {
            repoBuild.execute()
        }
        catch(Exception e) {
            e.printStackTrace()
            System.out.println(e.message)
        }
    }



    void execute() {
        options = cli.parse(args)
        env = new RepoEnv(getRepoBasedir())
        println getRepoBasedir()

        def commands = options.arguments();
        commands.each {
            switch(it) {
                case "build-pom":
                    doBuildPom()
                    break;
                case "switch":
                    doSwitch()
                    break;
                case "prepare-merge":
                    doPrepareMerge()
                    break;
                case "import-bundles":
                    doImportBundles()
                    break;
                case "export-bundles":
                    doExportBundles()
                    break;
                case "init":
                    doInit()
                    break;
                case "sync":
                    doSync()
                    break;
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
                new File(getRepoBasedir(),"pom.xml")
        def featureBranch = options.f ?
                options.f :
                RepoManifest.getBranch(env, "manifest")
        Pom.generateXml(env, featureBranch, buildPomFile)
    }

    void doSwitch() {
        def featureBranch = options.f
        if( featureBranch ) {
            RepoManifest.switchToBranch(env, featureBranch)
        } else {
            throw new RepoBuildException("featureBranch required")
        }
    }

    void doPrepareMerge() {
        def featureBranch = getRequired(options.f,"featureBranch")
        if( featureBranch) {
            RepoManifest.mergeFeatureBranch(env, featureBranch )
        } else {
            throw new RepoBuildException("featureBranch required")
        }
    }

    void doImportBundles() {
        // чтобы импортировать бандлы нам нужен файл преобразования веток
        // если записи в файле нет то такая ветка в бандле пропускается
        // repository должно кодироваться в имени бандла
        // формаат файла преобразования
        // repository;branchSource;branchTarget
    }

    void doExportBundles() {
        def featureBranch = getRequired(options.f,"featureBranch")
        def targetExportDir = options.t ?
                new File(options.t)
                : new File(getRepoBasedir(), featureBranch)

        targetExportDir.mkdirs()
        RepoManifest.createFeatureBundles(env, targetExportDir, featureBranch )
    }

    void doInit() {
        def manifestUrl = options.M
        def manifestBranch = getRequired(options.b, "manifestBranch")
        if(!env.manifest) {
            cloneManifest(manifestUrl, manifestBranch)
        } else {
            checkoutUpdateManifest(manifestBranch)
        }
    }

    void doSync(options) {
        if(getManifestDir().exists()) {
            def manifestBranch = Git.getBranch(getManifestDir())
            if("HEAD".equals(manifestBranch)) {
                throw new RepoBuildException("manifest branch must be local")
            }
            checkoutUpdateManifest(manifestBranch)
            RepoManifest.fetchUpdate(env)
        } else {
            throw new RepoBuildException("manifest dir not found")
        }
    }

    File getManifestDir() {
        return new File(getRepoBasedir(), "manifest")
    }

    void cloneManifest(String manifestUrl, String manifestBranch) {
        def manifestDir = getManifestDir()
        if(manifestUrl) {
            manifestDir.mkdirs()
            Git.clone(env, manifestUrl, ORIGIN, manifestDir )
        } else {
            throw new RepoBuildException("manifestUrl required")
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

    String getRequired(value, name) {
        if(value) {
            return value;
        } else {
            throw new RepoBuildException("$name required")
        }
    }
}
