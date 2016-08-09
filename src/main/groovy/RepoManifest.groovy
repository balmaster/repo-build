class RepoManifest {
    static void forEach(RepoEnv env, Closure filter, Closure action) {
        env.manifest.project
        .findAll { 
            filter(it)
        }
        .each {
            action(it)
        }

    }
}
