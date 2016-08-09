void createFeatureBundles(RepoEnv env, String featureBranch) {
    println env.basedir
    println featureBranch
    def targetDir = new File(env.getBuildTarget(), featureBranch)
    targetDir.mkdirs()
    Git.createFeatureBundles(env, featureBranch, targetDir )
}

def env = new RepoEnv(project)
def featureBranch = project.properties["featureBranch"]

createFeatureBundles(env, featureBranch)
