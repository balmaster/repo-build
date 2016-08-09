def env = new RepoEnv(project)
def featureBranch = project.properties["featureBranch"]

println env.basedir
println featureBranch

Git.switchToBranch(env, featureBranch)
