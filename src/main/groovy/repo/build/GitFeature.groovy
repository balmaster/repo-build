package repo.build

/**
 */
class GitFeature {
    static void mergeRelease(RepoEnv env, String featureBranch) {
        // для всех компонентов в кторых ест фича бранч
        RepoManifest.forEachWithFeatureBranch(env, { project ->
            def manifestBranch = RepoManifest.getBranch(env, project.@path)
            Git.merge(env, manifestBranch, new File(env.basedir, project.@path))
        }, featureBranch)
    }
}
