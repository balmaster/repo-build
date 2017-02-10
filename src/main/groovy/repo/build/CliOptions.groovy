package repo.build

import groovy.transform.CompileStatic

class CliOptions {

    final OptionAccessor options;

    CliOptions(OptionAccessor options) {
        this.options = options
    }

    public File getRepoBasedir() {
        return options.r ? new File(options.r) : new File(".").getAbsoluteFile()
    }

    public String getFeatureBranch() {
        return getRequired(options.f, "Feature branch required.\nUse: 'repo-build -f feature ...'")
    }

    public String getParent() {
        getRequired(options.P, "Parent component required.\nUse: 'repo-build -P parent ...'")
    }

    public String getContinueFromComponent() {
        options.C ? options.C : null
    }

    public String getIncludes() {
        getRequired(options.i, "Includes required.\nUse: 'repo-build -i groupId:* ...'")
    }

    @CompileStatic
    def getRequired(value, String msg) {
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
        return getRequired(options.e, "Expression required.\nUse: 'repo-build -e <expr> grep'")
    }

    String getManifestBranch() {
        return getRequired(options.b, "Use: repo-build -b <manifestBranch> ...")
    }

    String getManifestUrl() {
        return getRequired(options.M, "Use: repo-build -M <manifestUrl> ...")
    }

    Boolean getAllFlag() {
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
}
