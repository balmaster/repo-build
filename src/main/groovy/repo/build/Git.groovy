package repo.build

import groovy.transform.CompileStatic

@CompileStatic
class Git {

    static final String PREPARE_BUILD = "prepareBuild"

    static boolean branchPresent(File dir, String branch) {
        return !ExecuteProcess.executeCmd0(dir, "git ls-remote . $branch").empty
    }

    static String getBranch(File dir) {
        return ExecuteProcess.executeCmd0(dir, "git rev-parse --abbrev-ref HEAD").replace("\n", "")
    }

    static void createBranch(File dir, String branch) {
        ExecuteProcess.executeCmd0(dir, "git branch $branch")
    }

    static void checkout(File dir, String branch) {
        ExecuteProcess.executeCmd0(dir, "git checkout $branch")
    }

    static void deleteBranch(File dir, String branch) {
        ExecuteProcess.executeCmd0(dir, "git branch -d $branch")
    }

    static void mergeFeatureBranch(String branch, String remoteBranch, String startCommit, File dir) {
        ExecuteProcess.executeCmd0(dir, "git checkout -B $PREPARE_BUILD $startCommit")
        merge(remoteBranch, dir)
    }

    static void merge(String branch, File dir) {
        ExecuteProcess.executeCmd0(dir, "git merge $branch")
    }

    static void mergeAbort(File dir) {
        ExecuteProcess.executeCmd0(dir, "git merge --abort", false)
    }

    static void createFeatureBundle(String branch, File dir, File bundleFile) {
        ExecuteProcess.executeCmd0(dir, "git bundle create $bundleFile $branch")
    }

    static void fetch(String remoteName, File dir) {
        ExecuteProcess.executeCmd0(dir, "git fetch $remoteName")
    }

    static void user(File dir, String userName, String userEmail) {
        ExecuteProcess.executeCmd0(dir, 'git config --local --remove-section user', false)
        if (userName?.trim() && userEmail?.trim()) {
            String[] args = ["git", "config", "--local", "user.name", userName]
            ExecuteProcess.executeCmd0(dir, args, true)

            args = ["git", "config", "--local", "user.email", userEmail]
            ExecuteProcess.executeCmd0(dir, args, true)
        }
    }

    static void checkoutUpdate(String branch, String remoteBranch, File dir) {
        if (Git.branchPresent(dir, branch)) {
            ExecuteProcess.executeCmd0(dir, "git checkout $branch")
            if (branchPresent(dir, remoteBranch)) {
                ExecuteProcess.executeCmd0(dir, "git merge $remoteBranch")
            }
        } else if (branchPresent(dir, remoteBranch)) {
            ExecuteProcess.executeCmd0(dir, "git checkout $branch")
        } else {
            throw new RepoBuildException(" no branch $branch or remote branch $remoteBranch present")
        }
    }

    static void clone(String url, String remoteName, File dir) {
        dir.mkdirs()
        ExecuteProcess.executeCmd0(dir, "git clone -o $remoteName $url .")
    }

    static String status(File dir) {
        return ExecuteProcess.executeCmd0(dir, "git status -s")
    }

    static String logUnpushed(File dir, String remoteBranch) {
        return ExecuteProcess.executeCmd0(dir, "git log $remoteBranch..HEAD --not --oneline")
    }

    static String grep(File dir, String expr) {
        return ExecuteProcess.executeCmd0(dir, "git grep $expr", false)
    }

    static void stash(File dir) {
        ExecuteProcess.executeCmd0(dir, "git stash", false)
    }

    static void stashPop(File dir) {
        ExecuteProcess.executeCmd0(dir, "git stash pop", false)
    }

    static String getFileStatus(File dir, String fileName) {
        return ExecuteProcess.executeCmd0(dir, "git status $fileName -s")
    }

    static boolean isFileModified(File dir, String fileName) {
        def status = getFileStatus(dir, fileName)
        return status.startsWith(" M ")
    }

    static void add(File dir, String fileName) {
        ExecuteProcess.executeCmd0(dir, "git add $fileName")
    }

    static void addUpdated(File dir) {
        ExecuteProcess.executeCmd0(dir, "git add -u")
    }

    static void commit(File dir, String message) {
        ExecuteProcess.executeCmd0(dir, "git commit -m \"$message\"")
    }

    static void init(File dir) {
        ExecuteProcess.executeCmd0(dir, "git init")
    }

    static void pushBranch(File dir, String remote, String branch, boolean setUpstream) {
        if(setUpstream) {
            ExecuteProcess.executeCmd0(dir, "git push -u $remote $branch")
        } else {
            ExecuteProcess.executeCmd0(dir, "git push $remote $branch")
        }
    }
}
