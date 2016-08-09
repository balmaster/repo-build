void createFeatureBundles(RepoEnv env, String featureBranch) {
    println env.basedir
    println featureBranch
    env.getBuildTarget().mkdirs();
    Git.createFeatureBundles(env, featureBranch )
}

def env = new RepoEnv(project)
def featureBranch = project.properties["featureBranch"]

createFeatureBundles(env, featureBranch)
