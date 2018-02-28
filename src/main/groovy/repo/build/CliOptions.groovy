package repo.build

import com.google.common.base.Splitter
import groovy.transform.CompileStatic

class CliOptions {

    final OptionAccessor options

    CliOptions(OptionAccessor options) {
        this.options = options
    }

    File getRepoBasedir() {
        return options.r ? new File(options.r) : new File(".").getAbsoluteFile()
    }

    String getFeatureBranch() {
        return getRequired(options.f, "Feature branch required.\nUse: 'repo-execute -f <featureBranch> ...'")
    }

    String getTaskBranch() {
        if (options.I) {
            return options.I
        } else {
            return null
        }
    }

    Boolean showAllStatus() {
        return options.all
    }

    String getRequiredTaskBranch() {
        return getRequired(getTaskBranch(), "Task branch required.\nUse: 'repo-execute -I <taskBranch> ...'")
    }

    String getParent() {
        if (options.P) {
            return options.P
        } else {
            return 'parent'
        }
    }

    String getContinueFromComponent() {
        options.C ? options.C : null
    }

    String getIncludes() {
        getRequired(options.i, "Includes required.\nUse: 'repo-execute -i groupId:* ...'")
    }

    @CompileStatic
    static def getRequired(value, String msg) {
        if (value) {
            return value
        } else {
            throw new RepoBuildException(msg)
        }
    }

    File getPomFile() {
        return options.p ?
                new File(options.p) :
                new File(getRepoBasedir(), 'pom.xml')
    }

    File getTargetExportDir() {
        return options.t ?
                new File(options.t)
                : new File(getRepoBasedir(), 'bundles')
    }

    String getExpression() {
        return getRequired(options.e, "Expression required.\nUse: 'repo-execute -e <expr> grep'")
    }

    String getManifestBranch() {
        return getRequired(options.b, "Use: repo-execute -b <manifestBranch> ...")
    }

    String getManifestUrl() {
        return getRequired(options.M, "Use: repo-execute -M <manifestUrl> ...")
    }

    Boolean getMergeAbortFlag() {
        return options.a
    }

    Boolean hasFeatureBransh() {
        return options.f
    }

    List<String> getArguments() {
        return options.arguments()
    }

    Boolean isDebugMode() {
        return options.X
    }

    Boolean isWaitBeforeExit() {
        return options.W
    }

    String getTag() {
        return getRequired(options.T, "Tag required.\nUse: 'repo-execute -T tag ...'")
    }

    int getParallel(RepoEnv env) {
        if (options.j) {
            return Integer.parseInt(options.j)
        } else {
            def defaultParallel = env.getDefaultParallel()
            return defaultParallel ? defaultParallel : 1
        }
    }

    Map<String, String> getSystemProperties() {
        def systemProperties = new HashMap<String, String>()
        def p = options.getInner().getOptionProperties('D')
        for (def name : p.propertyNames()) {
            systemProperties.put(name, p.getProperty(name))
        }
        return systemProperties
    }

    boolean hasVersion() {
        return options.version ? true : false
    }

    boolean hasMe() {
        return options.me
    }

    boolean hasMfae() {
        return options.mfae
    }

    File getMgs() {
        return options.mgs ? new File(options.mgs) : null
    }

    boolean hasMo() {
        return options.mo
    }

    List<String> getMP() {
        return options.mP ? Splitter.on(',').trimResults().splitToList(options.mP) : null
    }

    File getMs() {
        return options.ms ? new File(options.ms) : null
    }

    String getMT() {
        return options.mT ?: null
    }

    boolean hasMU() {
        return options.mU
    }

    File getMlr() {
        return options.mlr ? new File(options.mlr) : null
    }

    boolean hasFae() {
        return options.fae
    }

    String getSourceReleaseManifestBranch() {
        return getRequired(options.rs, "Use: release-merge-release -rs release-source -rd release-destination")
    }

    String getDestinationReleaseManifestBranch() {
        getRequired(options.rd,"Use: release-merge-release -rs release-source -rd release-destination")
    }

}
