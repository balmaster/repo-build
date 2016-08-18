package repo.build
import java.io.File;

class Git {

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

        RepoManifest.forEachWithFeatureBranch(env, 
            { Node project ->
                def dir = new File(env.basedir, project.@path)
                println "branch $remoteBranch found in ${project.@path}"
                def startCommit = project.@revision.replaceFirst("refs/heads", env.manifest.remote[0].@name)
                println ExecuteProcess.executeCmd0(dir,"git checkout -B $PREPARE_BUILD $startCommit")
                println ExecuteProcess.executeCmd0(dir,"git merge $remoteBranch")
            }, branch)
    }

    static void createFeatureBundles( RepoEnv env, String branch, File targetDir ) {
        def remoteBranch = getRemoteBranch(env, branch)

        RepoManifest.forEachWithFeatureBranch(env, 
            { Node project ->
                println "branch $remoteBranch found in ${project.@path}"
                def gitName = new File(project.@name).getName().split("\\.").first()
                println gitName
                def bundleFile = new File(targetDir,"${gitName}.bundle")
                println ExecuteProcess.executeCmd0(new File(env.basedir, project.@path),
                    "git bundle create $bundleFile $remoteBranch")
            }, branch)
    }
    
    
    static void switchToBranch( RepoEnv env, String branch ) {
        def remoteBranch = getRemoteBranch(env, branch)

        RepoManifest.forEachWithFeatureBranch(env,
            { Node project ->
                println "branch $remoteBranch found in ${project.@path}"
                println ExecuteProcess.executeCmd0(new File(env.basedir, project.@path),
                    "git checkout $branch")
            }, branch)
    }

}
