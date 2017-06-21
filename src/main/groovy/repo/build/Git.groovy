package repo.build

import groovy.transform.CompileStatic

@CompileStatic
class Git {
    static final String PREPARE_BUILD = "prepareBuild"

    public static final String ACTION_BRANCH_PRESENT = 'gitBranchPresent'

    static boolean branchPresent(ActionContext parentContext, File dir, String branch) {
        def context = parentContext.newChild(ACTION_BRANCH_PRESENT)
        context.withCloseable {
            return !ExecuteProcess.executeCmd0(context, dir, "git ls-remote . $branch", true).empty
        }
    }

    public static final String ACTION_GET_BRANCH = 'gitGetBranch'

    static String getBranch(ActionContext parentContext, File dir) {
        def context = parentContext.newChild(ACTION_GET_BRANCH)
        context.withCloseable {
            return ExecuteProcess.executeCmd0(context, dir, "git rev-parse --abbrev-ref HEAD", true).replace("\n", "")
        }
    }

    public static final String ACTION_CREATE_BRANCH = 'gitCreateBranch'

    static void createBranch(ActionContext parentContext, File dir, String branch) {
        def context = parentContext.newChild(ACTION_CREATE_BRANCH)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git branch $branch", true)
        }
    }

    public static final String ACTION_CHECKOUT = 'gitCheckout'

    static void checkout(ActionContext parentContext, File dir, String branch) {
        def context = parentContext.newChild(ACTION_CHECKOUT)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git checkout $branch", true)
        }
    }

    public static final String ACTION_DELETE_BRANCH = 'gitDeleteBranch'

    static void deleteBranch(ActionContext parentContext, File dir, String branch) {
        def context = parentContext.newChild(ACTION_DELETE_BRANCH)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git branch -d $branch", true)
        }
    }

    public static final String ACTION_MERGE_FEATURE_BRANCH = 'gitMergeFeatureBranch'

    static void mergeFeatureBranch(ActionContext parentContext, String branch, String remoteBranch,
                                   String startCommit, File dir) {
        def context = parentContext.newChild(ACTION_MERGE_FEATURE_BRANCH)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git checkout -B $PREPARE_BUILD $startCommit", true)
            merge(context, remoteBranch, dir)
        }
    }

    public static final String ACTION_MERGE = 'gitMerge'

    static void merge(ActionContext parentContext, String branch, File dir) {
        def context = parentContext.newChild(ACTION_MERGE)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git merge $branch", true)
        }
    }

    public static final String ACTION_MERGE_ABORT = 'gitMergeAbort'

    static void mergeAbort(ActionContext parentContext, File dir) {
        def context = parentContext.newChild(ACTION_MERGE_ABORT)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git merge --abort", false)
        }
    }

    public static final String ACTION_CREATE_FEATURE_BUNDLE = 'gitCreateFeatureBundle'

    static void createFeatureBundle(ActionContext parentContext, String branch, File dir, File bundleFile) {
        def context = parentContext.newChild(ACTION_CREATE_FEATURE_BUNDLE)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git bundle create $bundleFile $branch", true)
        }
    }

    public static final String ACTION_FETCH = 'gitFetch'

    static void fetch(ActionContext parentContext, String remoteName, File dir) {
        def context = parentContext.newChild(ACTION_FETCH)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git fetch $remoteName", true)
        }
    }

    public static final String ACTION_USER = 'gitUser'

    static void user(ActionContext parentContext, File dir, String userName, String userEmail) {
        def context = parentContext.newChild(ACTION_USER)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, 'git config --local --remove-section user', false)
            if (userName?.trim() && userEmail?.trim()) {
                ExecuteProcess.executeCmd0(context, dir, ['git', 'config', '--local', 'user.name', userName] as String[], true);
                ExecuteProcess.executeCmd0(context, dir, ['git', 'config', '--local', 'user.email', userEmail] as String[], true);
            }
        }
    }

    public static final String ACTION_CHECKOUT_UPDATE = 'gitCheckoutUpdate'

    static void checkoutUpdate(ActionContext parentContext, String branch, String remoteBranch, File dir) {
        def context = parentContext.newChild(ACTION_CHECKOUT_UPDATE)
        context.withCloseable {
            if (Git.branchPresent(context, dir, branch)) {
                ExecuteProcess.executeCmd0(context, dir, "git checkout $branch", true)
                if (branchPresent(context, dir, remoteBranch)) {
                    ExecuteProcess.executeCmd0(context, dir, "git merge $remoteBranch", true)
                }
            } else if (branchPresent(context, dir, remoteBranch)) {
                ExecuteProcess.executeCmd0(context, dir, "git checkout $branch", true)
            } else {
                throw new RepoBuildException(" no branch $branch or remote branch $remoteBranch present")
            }
        }
    }

    public static final String ACTION_CLONE = 'gitClone'

    static void clone(ActionContext parentContext, String url, String remoteName, File dir) {
        def context = parentContext.newChild(ACTION_CLONE)
        context.withCloseable {
            dir.mkdirs()
            ExecuteProcess.executeCmd0(context, dir, "git clone -o $remoteName $url .", true)
        }
    }

    public static final String ACTION_STATUS = 'gitStatus'

    static String status(ActionContext parentContext, File dir) {
        def context = parentContext.newChild(ACTION_STATUS)
        context.withCloseable {
            return ExecuteProcess.executeCmd0(context, dir, "git status -s", true)
        }
    }

    public static final String ACTION_LOG_UNPUSHED = 'gitLogUnpushed'

    static String logUnpushed(ActionContext parentContext, File dir, String remoteBranch) {
        def context = parentContext.newChild(ACTION_LOG_UNPUSHED)
        context.withCloseable {
            return ExecuteProcess.executeCmd0(context, dir, "git log $remoteBranch..HEAD --not --oneline", true)
        }
    }

    public static final String ACTION_GREP = 'gitGrep'

    static String grep(ActionContext parentContext, File dir, String expr) {
        def context = parentContext.newChild(ACTION_GREP)
        context.withCloseable {
            return ExecuteProcess.executeCmd0(context, dir, "git grep $expr", false)
        }
    }

    public static final String ACTION_STASH = 'gitStash'

    static void stash(ActionContext parentContext, File dir) {
        def context = parentContext.newChild(ACTION_STASH)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git stash", false)
        }
    }

    public static final String ACTION_STASH_POP = 'gitStashPop'

    static void stashPop(ActionContext parentContext, File dir) {
        def context = parentContext.newChild(ACTION_STASH_POP)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git stash pop", false)
        }
    }

    public static final String ACTION_GET_FILE_STATUS = 'gitGetFileStatus'

    static String getFileStatus(ActionContext parentContext, File dir, String fileName) {
        def context = parentContext.newChild(ACTION_GET_FILE_STATUS)
        context.withCloseable {
            return ExecuteProcess.executeCmd0(context, dir, "git status $fileName -s", true)
        }
    }

    static boolean isFileModified(ActionContext parentContext, File dir, String fileName) {
        def status = getFileStatus(parentContext, dir, fileName)
        return status.startsWith(" M ")
    }

    public static final String ACTION_ADD = 'gitAdd'

    static void add(ActionContext parentContext, File dir, String fileName) {
        def context = parentContext.newChild(ACTION_ADD)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git add $fileName", true)
        }
    }

    public static final String ACTION_ADD_UPDATED = 'gitAddUpdated'

    static void addUpdated(ActionContext parentContext, File dir) {
        def context = parentContext.newChild(ACTION_ADD_UPDATED)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git add -u", true)
        }
    }

    public static final String ACTION_COMMIT = 'gitCommit'

    static void commit(ActionContext parentContext, File dir, String message) {
        def context = parentContext.newChild(ACTION_COMMIT)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git commit -m \"$message\"", true)
        }
    }

    public static final String ACTION_INIT = 'gitInit'

    static void init(ActionContext parentContext, File dir) {
        def context = parentContext.newChild(ACTION_INIT)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git init", true)
        }
    }

    public static final String ACTION_PUSH_BRANCH = 'gitPushBranch'

    static void pushBranch(ActionContext parentContext, File dir, String remote, String branch, boolean setUpstream) {
        def context = parentContext.newChild(ACTION_PUSH_BRANCH)
        context.withCloseable {
            if (setUpstream) {
                ExecuteProcess.executeCmd0(context, dir, "git push -u $remote $branch", true)
            } else {
                ExecuteProcess.executeCmd0(context, dir, "git push $remote $branch", true)
            }
        }
    }

    public static final String ACTION_ADD_TAG_TO_CURRENT_HEAD = 'gitAddTagToCurrentHead'

    static void addTagToCurrentHead(ActionContext parentContext, File dir, String tag) {
        def context = parentContext.newChild(ACTION_ADD_TAG_TO_CURRENT_HEAD)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git tag $tag", true)
        }
    }

    public static final String ACTION_PUSH_TAG = 'gitPushTag'

    static void pushTag(ActionContext parentContext, File dir, String remote, String tag) {
        def context = parentContext.newChild(ACTION_PUSH_TAG)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git push $remote tag $tag", true)
        }
    }

    public static final String ACTION_CHECKOUT_TAG = 'gitCheckoutTag'

    static void checkoutTag(ActionContext parentContext, File dir, String tag) {
        def context = parentContext.newChild(ACTION_CHECKOUT_TAG)
        context.withCloseable {
            ExecuteProcess.executeCmd0(context, dir, "git checkout tags/$tag", true)
        }
    }

    public static final String ACTION_TAG_PRESENT = 'gitTagPresent'

    static boolean tagPresent(ActionContext parentContext, File dir, String tag) {
        def context = parentContext.newChild(ACTION_TAG_PRESENT)
        context.withCloseable {
            return !ExecuteProcess.executeCmd0(context, dir, "git tag -l $tag", true).empty
        }
    }

}
