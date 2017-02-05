package repo.build

import groovy.xml.MarkupBuilder

/**
 */
class Sandbox {
    File basedir;
    Map<String, File> components;

    Sandbox(File basedir) {
        this.components = new HashMap<>()
        this.basedir = basedir

    }

    Sandbox newGitComponent(String component, Closure action) {
        def dir = new File(basedir, component)
        dir.mkdirs()
        Git.init(dir)
        Git.user(dir, "you@example.com", "Your Name")
        action(this, dir)
        components.put(component, dir)
        return this
    }

    Sandbox component(String component, Closure action) {
        action(this, components.get(component))
        return this
    }

    Sandbox newGitComponent(String component) {
        return newGitComponent(component,
                {
                    Sandbox sandbox, File dir ->
                        sandbox.gitInitialCommit(dir)
                })
    }

    Sandbox gitInitialCommit(File dir) {
        def readme = new File(dir, "README.md")
        readme.createNewFile()
        Git.add(dir, readme.canonicalPath)
        Git.commit(dir, "init")
        return this
    }

    Sandbox buildManifest(File dir) {
        new FileWriter(new File(dir, 'default.xml')).withCloseable { xmlWriter ->
            def xmlMarkup = new MarkupBuilder(xmlWriter)

            xmlMarkup.'manifest'() {
                'remote'('name': 'origin', 'fetch': dir.getParentFile().getAbsolutePath())
                'default'('revision': 'refs/heads/develop', 'remote': 'origin', 'sync': '1')
                components.each {
                    'project'('name': it.key, 'remote': 'origin', 'path': it.key, 'revision': 'refs/heads/master')
                }
            }
        }
        return this
    }

}
