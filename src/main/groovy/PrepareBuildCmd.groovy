void prepareBuild(RepoEnv env, String featureBranch) {
    println env.basedir
    println featureBranch
    Git.mergeFeatureBranch(env, featureBranch )
    Pom.generateXml(env, featureBranch, new File(env.getBuildTarget(),"buildPom.xml"))
}

def env = new RepoEnv(project)
def featureBranch = project.properties["featureBranch"]

prepareBuild(env, featureBranch)
