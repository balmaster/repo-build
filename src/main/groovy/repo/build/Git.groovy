package repo.build

import groovy.transform.CompileStatic

@CompileStatic
class Git {

    static final String PREPARE_BUILD = "prepareBuild"

    static boolean branchPresent( File dir, String branch ) {
        return ! ExecuteProcess.executeCmd0(dir, "git ls-remote . $branch").empty
    }

    static String getBranch(File dir) {
        return ExecuteProcess.executeCmd0(dir, "git rev-parse --abbrev-ref HEAD").replace( "\n", "" )
    }

    static void deleteBranch(File dir, String branch) {
        ExecuteProcess.executeCmd0(dir, "git branch -d $branch")
    }

    static void mergeFeatureBranch( RepoEnv env, String branch, String remoteBranch, String startCommit, File dir ) {
        ExecuteProcess.executeCmd0(dir, "git checkout -B $PREPARE_BUILD $startCommit")
        ExecuteProcess.executeCmd0(dir, "git merge $remoteBranch")
    }

    static void mergeAbort( RepoEnv env, File dir ) {
        ExecuteProcess.executeCmd0(dir, "git merge --abort", false)
    }

    static void createFeatureBundle( RepoEnv env, String branch, File dir, File bundleFile ) {
        ExecuteProcess.executeCmd0(dir, "git bundle create $bundleFile $branch")
    }

    static void fetch( RepoEnv env, String remoteName, File dir ) {
        ExecuteProcess.executeCmd0(dir, "git fetch $remoteName")
    }

    static void user(File dir, String userName, String userEmail) {
        ExecuteProcess.executeCmd0(dir, 'git config --local --remove-section user',false)
        if (userName?.trim() && userEmail?.trim()) {
            String[] args = ["git", "config", "--local", "user.name", userName]
            ExecuteProcess.executeCmd0(dir, args, true)

            args = ["git", "config", "--local", "user.email", userEmail]
            ExecuteProcess.executeCmd0(dir, args, true)
        }
    }

    static void checkoutUpdate(RepoEnv env, String branch, String remoteBranch, File dir) {
        if(Git.branchPresent(dir, branch)) {
            ExecuteProcess.executeCmd0(dir, "git checkout $branch")
            if(branchPresent(dir, remoteBranch)) {
                ExecuteProcess.executeCmd0(dir, "git merge $remoteBranch")
            }
        } else if(branchPresent(dir, remoteBranch)) {
            ExecuteProcess.executeCmd0(dir, "git checkout $branch")
        } else {
            throw new RepoBuildException(" no branch $branch or remote branch $remoteBranch present")
        }
    }

    static void clone( RepoEnv env, String url, String remoteName, File dir) {
        dir.mkdirs()
        ExecuteProcess.executeCmd0(dir, "git clone -o $remoteName $url .")
    }

    static void status( RepoEnv env, File dir) {
        ExecuteProcess.executeCmd0(dir, "git status -s")
    }

    static void logUnpushed( RepoEnv env, File dir, String remoteBranch) {
        ExecuteProcess.executeCmd0(dir, "git log $remoteBranch..HEAD --not --remotes --oneline")
    }

    static void grep( RepoEnv env, File dir, String expr) {
        ExecuteProcess.executeCmd0(dir, "git grep $expr", false)
    }

    static void stash( RepoEnv env, File dir) {
        ExecuteProcess.executeCmd0(dir, "git stash", false)
    }

    static void stashPop( RepoEnv env, File dir) {
        ExecuteProcess.executeCmd0(dir, "git stash pop", false)
    }

}
