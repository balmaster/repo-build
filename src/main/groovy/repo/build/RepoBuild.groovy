package repo.build

import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager
import groovy.transform.CompileStatic

class RepoBuild {

    static Logger logger = LogManager.getLogger(RepoBuild.class)

    static final String BUNDLES = "bundles"

    static final String POM_XML = "pom.xml"
    final CliBuilder cli
    final String[] args
    OptionAccessor options
    RepoEnv env

    RepoBuild(String[] args) {
        this.cli = CliBuilderFactory.build()
        this.args = args
    }

    static void main(String[] args) {
        def repoBuild = new RepoBuild(args)
        try {
            repoBuild.execute()
        }
        catch (Exception e) {
            if (repoBuild.options.X) {
                logger.error(e.message, e)
            } else {
                logger.error(e.message)
            }
            System.exit(1);
        }
    }

    void execute() {
        options = cli.parse(args)
        env = new RepoEnv(getRepoBasedir())
        println getRepoBasedir()

        def commands = options.arguments()
        if (commands.size() > 0) {
            commands.each {
                logger.info("----- do command: {} -----", it)
                switch (it) {
                    case CliBuilderFactory.CMD_BUILD_POM:
                        doBuildPom()
                        break
                    case CliBuilderFactory.CMD_SWITCH:
                        doSwitch()
                        break
                    case CliBuilderFactory.CMD_PREPARE_MERGE:
                        doPrepareMerge()
                        break
                    case CliBuilderFactory.CMD_EXPORT_BUNDLES:
                        doExportBundles()
                        break
                    case CliBuilderFactory.CMD_INIT:
                        doInit()
                        break
                    case CliBuilderFactory.CMD_SYNC:
                        doSync()
                        break
                    case CliBuilderFactory.CMD_STATUS:
                        doStatus()
                        break
                    case CliBuilderFactory.CMD_GREP:
                        doGrep()
                        break
                    case CliBuilderFactory.CMD_MERGE_ABORT:
                        doMergeAbort()
                        break
                    case CliBuilderFactory.CMD_STASH:
                        doStash()
                        break
                    case CliBuilderFactory.CMD_STASH_POP:
                        doStashPop()
                        break
                    case CliBuilderFactory.CMD_FEATURE_MERGE_RELEASE:
                        doFeatureMergeRelease()
                        break
                    case CliBuilderFactory.CMD_FEATURE_UPDATE_PARENT:
                        doFeatureUpdateParent()
                        break
                    case CliBuilderFactory.CMD_FEATURE_UPDATE_VERSIONS:
                        doFeatureUpdateVersions()
                        break
                    case CliBuilderFactory.CMD_RELEASE_MERGE_FEATURE:
                        doReleaseMergeFeature()
                        break
                    case CliBuilderFactory.CMD_RELEASE_UPDATE_PARENT:
                        doReleaseUpdateParent()
                        break
                    case CliBuilderFactory.CMD_RELEASE_UPDATE_VERSIONS:
                        doReleaseUpdateVersions()
                        break
                    case CliBuilderFactory.CMD_PUSH_FEATURE:
                        doPushFeature()
                        break
                    case CliBuilderFactory.CMD_PUSH_MANIFEST:
                        doPushManifest()
                        break
                    default:
                        throw new RepoBuildException("Invalid command: $it")
                }
            }
        } else {
            println cli.usage()
        }
    }

    @CompileStatic
    String getRequired(value, String msg) {
        if (value) {
            return value
        } else {
            throw new RepoBuildException(msg)
        }
    }

    File getRepoBasedir() {
        return options.r ? new File(options.r) : new File(".").getAbsoluteFile()
    }

    String getFeatureBranch() {
        return getRequired(options.f, "Feature branch required.\nUse: 'repo-build -f feature ...'")
    }

    private String getParent() {
        getRequired(options.P, "Parent component required.\nUse: 'repo-build -P parent ...'")
    }

    private getContinueFromComponent() {
        options.C ? options.C : null
    }

    void doBuildPom() {
        def buildPomFile = options.p ?
                new File(options.p) :
                new File(getRepoBasedir(), POM_XML)
        def featureBranch = options.f ?
                options.f :
                Git.getBranch(new File(getRepoBasedir(), "manifest"))
        Pom.generateXml(env, featureBranch, buildPomFile)
    }

    void doSwitch() {
        if (options.m) {
            //RepoManifest.
        } else {
            def featureBranch = options.f
            if (featureBranch) {
                GitFeature.switch(env, featureBranch)
            } else {
                throw new RepoBuildException("Use: 'repo-build -f <featureBranch> switch'")
            }
        }
    }

    void doPrepareMerge() {
        GitFeature.mergeFeature(env, getFeatureBranch(), options.a)
    }

    void doExportBundles() {
        if (options.f) {
            def targetExportDir = options.t ?
                    new File(options.t)
                    : new File(getRepoBasedir(), BUNDLES)

            targetExportDir.mkdirs()
            GitFeature.createFeatureBundles(env, targetExportDir, getFeatureBranch())
        } else if (options.m) {
            def targetExportDir = options.t ?
                    new File(options.t)
                    : new File(getRepoBasedir(), BUNDLES)

            targetExportDir.mkdirs()
            GitFeature.createManifestBundles(env, targetExportDir)
        } else {
            throw new RepoBuildException("Use: 'repo-build -m export-bundles' or 'repo-build -f <featureBranch> export-bundles'")
        }
    }

    void doInit() {
        def manifestBranch = getRequired(options.b, "Use: repo-build -b <manifestBranch> ...")
        if (!env.manifest) {
            def manifestUrl = getRequired(options.M, "Use: repo-build -M <manifestUrl> ...")
            if (!manifestUrl || !manifestBranch) {
                throw new RepoBuildException("Use: 'repo-build -M <manifestUrl> -b <manifestBranch>'")
            }
            GitFeature.cloneManifest(env, manifestUrl, manifestBranch)
        } else {
            GitFeature.updateManifest(env, manifestBranch)
        }
        env.openManifest()
    }

    @CompileStatic
    void doSync() {
        GitFeature.sync(env)
    }

    @CompileStatic
    void doStatus() {
        GitFeature.status(env)
    }

    void doGrep() {
        def expr = getRequired(options.e, "Expression required.\nUse: 'repo-build -e <expr> grep'")
        GitFeature.grep(env, expr)
    }

    void doMergeAbort() {
        GitFeature.mergeAbort(env)
    }

    void doStash() {
        GitFeature.stash(env)
    }

    void doStashPop() {
        GitFeature.stashPop(env)
    }

    void doFeatureMergeRelease() {
        GitFeature.mergeRelease(env, getFeatureBranch())
    }

    void doFeatureUpdateParent() {
        def parentComponent = getParent()
        MavenFeature.updateParent(env, getFeatureBranch(), parentComponent, false, true)
    }

    void doFeatureUpdateVersions() {
        def includes = getIncludes()
        def continueFromComponent = getContinueFromComponent()
        MavenFeature.updateVersions(env, getFeatureBranch(), includes, continueFromComponent, true)
    }

    void doReleaseMergeFeature() {
        GitFeature.mergeFeature(env, getFeatureBranch())
    }

    void doReleaseUpdateParent() {
        def parentComponent = getParent()
        MavenFeature.updateParent(env, getFeatureBranch(), parentComponent, false, true)
    }

    void doReleaseUpdateVersions() {
        def includes = getIncludes()
        def continueFromComponent = options.C ? options.C : null
        MavenFeature.updateVersions(env, getFeatureBranch(), includes, continueFromComponent, true)
    }

    private String getIncludes() {
        getRequired(options.i, "Includes required.\nUse: 'repo-build -i groupId:* ...'")
    }

    void doPushFeature() {
        GitFeature.pushFeatureBranch(env, getFeatureBranch(), true)
    }

    void doPushManifest() {
        GitFeature.pushManifestBranch(env, true)
    }

}
