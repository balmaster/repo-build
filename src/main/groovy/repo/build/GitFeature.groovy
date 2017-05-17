package repo.build

import java.util.concurrent.ConcurrentHashMap

class GitFeature {

    static File getManifestDir(ActionContext context) {
        return new File(context.env.basedir, 'manifest')
    }

    public static final String ACTION_CLONE_MANIFEST = 'gitFeatureCloneManifest'

    static void cloneManifest(ActionContext parentContext, String url, String branch) {
        def context = parentContext.newChild(ACTION_CLONE_MANIFEST)
        context.withCloseable {
            def dir = getManifestDir(context)
            dir.mkdirs()
            Git.clone(context, url, "origin", dir)
            Git.checkoutUpdate(context, branch, "origin/$branch", dir)
        }
    }

    public static final String ACTION_UPDATE_MANIFEST = 'gitFeatureUpdateManifest'

    static void updateManifest(ActionContext parentContext, String branch) {
        def context = parentContext.newChild(ACTION_UPDATE_MANIFEST)
        context.withCloseable {
            def dir = getManifestDir(context)
            Git.fetch(context, "origin", dir)
            Git.checkoutUpdate(context, branch, "origin/$branch", dir)
        }
    }

    public static final String ACTION_SYNC = 'gitFeatureSync'

    static void sync(ActionContext parentContext) {
        def context = parentContext.newChild(ACTION_SYNC)
        context.withCloseable {
            def manifestDir = getManifestDir(context)
            if (manifestDir.exists()) {
                def manifestBranch = Git.getBranch(context, manifestDir)
                if ("HEAD" == manifestBranch) {
                    throw new RepoBuildException("manifest branch must be local, use: repo-build -b <manifestBranch> init")
                }
                updateManifest(context, manifestBranch)
                context.env.openManifest()
                fetchUpdate(context)
            } else {
                throw new RepoBuildException("manifest dir $manifestDir not found")
            }
        }
    }

    public static final String ACTION_MERGE_RELEASE = 'gitFeatureMergeRelease'

    static void mergeRelease(ActionContext parentContext, String featureBranch) {
        def context = parentContext.newChild(ACTION_MERGE_RELEASE)
        context.withCloseable {
            RepoManifest.forEachWithFeatureBranch(context,
                    { ActionContext actionContext, project ->
                        def env = actionContext.env
                        def manifestBranch = RepoManifest.getBranch(actionContext, project.@path)
                        Git.merge(actionContext, manifestBranch, new File(env.basedir, project.@path))
                    }, featureBranch)
        }
    }


    public static final String ACTION_MERGE_FEATURE = 'gitFeatureMergeFeature'

    static void mergeFeature(ActionContext parentContext, String featureBranch) {
        def context = parentContext.newChild(ACTION_MERGE_FEATURE)
        context.withCloseable {
            RepoManifest.forEachWithFeatureBranch(context,
                    { ActionContext actionContext, project ->
                        // check current components branch
                        def dir = new File(actionContext.env.basedir, project.@path)
                        def manifestBranch = RepoManifest.getBranch(actionContext, project.@path)
                        if (Git.getBranch(actionContext, dir) != manifestBranch) {
                            throw new RepoBuildException("Component ${project.@path} must be set to branch $manifestBranch")
                        }
                        Git.merge(actionContext, featureBranch, dir)
                    }, featureBranch)
        }
    }

    public static final String ACTION_SWITCH = 'gitFeatureSwitch'

    static void 'switch'(ActionContext parentContext, String branch) {
        def context = parentContext.newChild(ACTION_SWITCH)
        context.withCloseable {
            def remoteBranch = RepoManifest.getRemoteBranch(context, branch)
            RepoManifest.forEach(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        // переключаемся только если есть локальная или удаленная фича ветка
                        if (Git.branchPresent(actionContext, dir, branch) || Git.branchPresent(actionContext, dir, remoteBranch)) {
                            Git.checkoutUpdate(actionContext, branch, remoteBranch, new File(actionContext.env.basedir, project.@path))
                        }
                    }
            )
        }
    }

    public static final String ACTION_FETCH_UPDATE = 'gitFeatureFetchUpdate'

    static void fetchUpdate(ActionContext parentContext) {
        def context = parentContext.newChild(ACTION_FETCH_UPDATE)
        context.withCloseable {
            def remoteName = RepoManifest.getRemoteName(context)
            def remoteBaseUrl = RepoManifest.getRemoteBaseUrl(context)
            RepoManifest.forEach(context,
                    { ActionContext actionContext, Node project ->
                        def branch = RepoManifest.getBranch(actionContext, project.@path)
                        def env = actionContext.env
                        def remoteBranch = RepoManifest.getRemoteBranch(actionContext, branch)
                        def dir = new File(env.basedir, project.@path)
                        def name = project.@name
                        if (new File(dir, ".git").exists()) {
                            Git.fetch(actionContext, remoteName, dir)
                        } else {
                            dir.mkdirs()
                            Git.clone(actionContext, "$remoteBaseUrl/$name", remoteName, dir)
                        }
                        Git.checkoutUpdate(actionContext, branch, remoteBranch, dir)
                        Git.user(actionContext, dir,
                                env.props.getProperty("git.user.name"),
                                env.props.getProperty("git.user.email"))
                    })
        }
    }

    static void mergeFeature(ActionContext parentContext, String branch, Boolean mergeAbort) {
        def context = parentContext.newChild(ACTION_MERGE_FEATURE)
        context.withCloseable {
            def remoteBranch = RepoManifest.getRemoteBranch(context, branch)
            RepoManifest.forEachWithFeatureBranch(context,
                    { ActionContext actionContext, Node project ->
                        def env = actionContext.env
                        def dir = new File(env.basedir, project.@path)
                        //println "branch $remoteBranch found in ${project.@path}"
                        def startCommit = project.@revision.replaceFirst("refs/heads", env.manifest.remote[0].@name)
                        if (mergeAbort) {
                            try {
                                Git.mergeAbort(context, dir)
                            } catch (Exception e) {
                                // skip
                            }
                        }
                        Git.mergeFeatureBranch(actionContext, branch, remoteBranch, startCommit, dir)
                    },
                    branch
            )
        }
    }

    public static final String ACTION_CREATE_FEATURE_BUNDLES = 'gitFeatureCreateFeatureBundles'

    static void createFeatureBundles(ActionContext parentContext, File targetDir, String branch) {
        def context = parentContext.newChild(ACTION_CREATE_FEATURE_BUNDLES)
        context.withCloseable {
            def remoteBranch = RepoManifest.getRemoteBranch(context, branch)

            RepoManifest.forEachWithFeatureBranch(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        def gitName = new File(project.@name).getName().split("\\.").first()
                        //println gitName
                        def bundleFile = new File(targetDir, "${gitName}.bundle")
                        Git.createFeatureBundle(actionContext, remoteBranch, dir, bundleFile)
                    },
                    branch
            )
        }
    }

    public static final String ACTION_CREATE_MANIFEST_BUNDLES = 'gitFeatureCreateManifestBundles'

    static void createManifestBundles(ActionContext parentContext, File targetDir) {
        def context = parentContext.newChild(ACTION_CREATE_MANIFEST_BUNDLES)
        context.withCloseable {
            RepoManifest.forEach(context,
                    { ActionContext actionContext, Node project ->
                        def remoteBranch = RepoManifest.getRemoteBranch(actionContext,
                                RepoManifest.getBranch(actionContext, project.@path))
                        def dir = new File(actionContext.env.basedir, project.@path)
                        def gitName = new File(project.@name).getName().split("\\.").first()
                        //println gitName
                        def bundleFile = new File(targetDir, "${gitName}.bundle")
                        Git.createFeatureBundle(actionContext, remoteBranch, dir, bundleFile)
                    }
            )
        }
    }

    static void forEachWithProjectDirExists(ActionContext context, Closure action) {
        RepoManifest.forEach(context,
                { ActionContext actionContext, Node project -> return RepoManifest.projectDirExists(actionContext, project) },
                action
        )
    }

    public static final String ACTION_STATUS = 'gitFeatureStatus'

    static Map<String, String> status(ActionContext parentContext) {
        def context = parentContext.newChild(ACTION_STATUS)
        Map<String, String> result = new ConcurrentHashMap<>()
        context.withCloseable {
            forEachWithProjectDirExists(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        def branch = Git.getBranch(actionContext, dir)
                        def remoteName = RepoManifest.getRemoteName(actionContext)
                        def remoteBranch = "$remoteName/$branch"
                        def status = Git.status(actionContext, dir)
                        def unpushed
                        if (Git.branchPresent(actionContext, dir, remoteBranch)) {
                            unpushed = Git.logUnpushed(actionContext, dir, remoteBranch)
                        } else {
                            Git.fetch(actionContext, remoteName, dir)
                            if (Git.branchPresent(actionContext, dir, remoteBranch)) {
                                unpushed = Git.logUnpushed(actionContext, dir, remoteBranch)
                            } else {
                                unpushed = "Branch not pushed"
                                actionContext.writeOut(unpushed + '\n')
                            }
                        }
                        result.put(project.@path, status + '\n' + unpushed)
                    }
            )
        }
        return result
    }

    public static final String ACTION_GREP = 'gitFeatureGrep'

    static Map<String, String> grep(ActionContext parentContext, String exp) {
        Map<String, String> result = new HashMap<>()
        def context = parentContext.newChild(ACTION_GREP)
        context.withCloseable {
            forEachWithProjectDirExists(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        def grepResult = Git.grep(actionContext, dir, exp)
                        synchronized (result) {
                            result.put(project.@path, grepResult)
                        }
                    }
            )
        }
        return result
    }

    public static final String ACTION_MERGE_ABORT = 'gitFeatureMergeAbort'

    static void mergeAbort(ActionContext parentContext) {
        def context = parentContext.newChild(ACTION_MERGE_ABORT)
        context.withCloseable {
            forEachWithProjectDirExists(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        Git.mergeAbort(actionContext, dir)
                    }
            )
        }
    }

    public static final String ACTION_STASH = 'gitFeatureStash'

    static void stash(ActionContext parentContext) {
        def context = parentContext.newChild(ACTION_STASH)
        context.withCloseable {
            forEachWithProjectDirExists(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        Git.stash(actionContext, dir)
                    }
            )
        }
    }

    public static final String ACTION_STASH_POP = 'gitFeatureStashPop'

    static void stashPop(ActionContext parentContext) {
        def context = parentContext.newChild(ACTION_STASH_POP)
        context.withCloseable {
            forEachWithProjectDirExists(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        Git.stashPop(actionContext, dir)
                    }
            )
        }
    }

    public static final String ACTION_PUSH_FEATURE_BRANCH = 'gitFeaturePushFeatureBranch'

    static void pushFeatureBranch(ActionContext parentContext, String featureBranch, boolean setUpstream) {
        def context = parentContext.newChild(ACTION_PUSH_FEATURE_BRANCH)
        context.withCloseable {
            RepoManifest.forEach(context,
                    // use only existing local componentn whith have featureBranch
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        return RepoManifest.projectDirExists(actionContext, project) && Git.branchPresent(actionContext, dir, featureBranch)
                    },
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        Git.pushBranch(actionContext, dir, project.@remote, featureBranch, setUpstream)
                    }
            )
        }
    }

    public static final String ACTION_PUSH_MANIFEST_BRANCH = 'gitFeaturePushManifestBranch'

    static void pushManifestBranch(ActionContext parentContext, boolean setUpstream) {
        def context = parentContext.newChild(ACTION_PUSH_MANIFEST_BRANCH)
        context.withCloseable {
            forEachWithProjectDirExists(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        def manifestBranch = RepoManifest.getBranch(actionContext, project.@path)
                        Git.pushBranch(actionContext, dir, project.@remote, manifestBranch, setUpstream)
                    }
            )
        }
    }

    public static final String ACTION_ADD_TAG_TO_CURRENT_HEADS = 'gitFeatureAddTagToCurrentHeads'

    static void addTagToCurrentHeads(ActionContext parentContext, String tag) {
        def context = parentContext.newChild(ACTION_ADD_TAG_TO_CURRENT_HEADS)
        context.withCloseable {
            forEachWithProjectDirExists(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        Git.addTagToCurrentHead(actionContext, dir, tag)
                    }
            )
        }
    }

    public static final String ACTION_PUSH_TAG = 'gitFeaturePushTag'

    static void pushTag(ActionContext parentContext, String tag) {
        def context = parentContext.newChild(ACTION_PUSH_TAG)
        context.withCloseable {
            forEachWithProjectDirExists(context,
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        Git.pushTag(actionContext, dir, project.@remote, tag)
                    }
            )
        }
    }

    public static final String ACTION_CHECKOUT_TAG = 'gitFeatureCheckoutTag'

    static void checkoutTag(ActionContext parentContext, String tag) {
        def context = parentContext.newChild(ACTION_CHECKOUT_TAG)
        context.withCloseable {
            RepoManifest.forEach(context,
                    // use only existing local componentn whith have tag
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        return RepoManifest.projectDirExists(actionContext, project) && Git.tagPresent(actionContext, dir, tag)
                    },
                    { ActionContext actionContext, Node project ->
                        def dir = new File(actionContext.env.basedir, project.@path)
                        Git.checkoutTag(actionContext, dir, tag)
                    }
            )
        }
    }

}
