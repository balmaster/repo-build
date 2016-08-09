import java.io.File;

class Git {
    static boolean branchPresent( File dir, String branch ) {
        return ! ExecuteProcess.executeCmd0(dir, "git ls-remote . $branch").empty;
    }

    static String getRemoteBranch(RepoEnv env, String branch) {
        def remoteName = env.manifest.remote[0].@name
        return "$remoteName/$branch"
    } 
    
    static void mergeFeatureBranch( RepoEnv env, String branch ) {
        def remoteBranch = getRemoteBranch(env, branch)

        RepoManifest.forEach(env,
                { Node project -> branchPresent(new File(env.basedir,project.@path), remoteBranch)  && !"build".equals(project.@path) }, 
                { Node project ->
                    println "branch $remoteBranch found in ${project.@path}"
                    println ExecuteProcess.executeCmd0(new File(env.basedir, project.@path),"git merge $remoteBranch")
                })
    }

    static void createFeatureBundles( RepoEnv env, String branch ) {
        def remoteBranch = getRemoteBranch(env, branch)

        RepoManifest.forEach(env,
                { Node project -> branchPresent(new File(env.basedir,project.@path), remoteBranch)  && !"build".equals(project.@path)}, 
                { Node project ->
                    println "branch $remoteBranch found in ${project.@path}"
                    def gitName = new File(project.@name).getName().split("\\.").first()
                    println gitName
                    def bundleFile = new File(env.getBuildTarget(),"${gitName}.bundle")
                    println ExecuteProcess.executeCmd0(new File(env.basedir, project.@path),
                        "git bundle create $bundleFile $remoteBranch")
                })
    }
}
