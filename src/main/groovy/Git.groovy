import java.io.File;

class Git {

    static final String BUILD = "build"
    static final String PREPARE_BUILD = "prepareBuild"
            
    static boolean branchPresent( File dir, String branch ) {
        return ! ExecuteProcess.executeCmd0(dir, "git ls-remote . $branch").empty;
    }

    static String getRemoteBranch(RepoEnv env, String branch) {
        def remoteName = env.manifest.remote[0].@name
        return "$remoteName/$branch"
    }

    static void deleteBranch(File dir, String branch) {
        ExecuteProcess.executeCmd0(dir,"git branch -d $branch")
    }
    
    static void mergeFeatureBranch( RepoEnv env, String branch ) {
        def remoteBranch = getRemoteBranch(env, branch)

        RepoManifest.forEach(env, 
            { Node project ->
                branchPresent(new File(env.basedir,project.@path), remoteBranch) && !BUILD.equals(project.@path)
            }, 
            { Node project ->
                def dir = new File(env.basedir, project.@path)
                println "branch $remoteBranch found in ${project.@path}"
                if(branchPresent(dir, PREPARE_BUILD)){
                    deleteBranch(dir, PREPARE_BUILD)
                }
                println ExecuteProcess.executeCmd0(dir,"git checkout -b $PREPARE_BUILD")
                println ExecuteProcess.executeCmd0(dir,"git merge $remoteBranch")
            })
    }

    static void createFeatureBundles( RepoEnv env, String branch ) {
        def remoteBranch = getRemoteBranch(env, branch)

        RepoManifest.forEach(env, 
            { Node project ->
                branchPresent(new File(env.basedir,project.@path), remoteBranch) && !BUILD.equals(project.@path)
            }, 
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
