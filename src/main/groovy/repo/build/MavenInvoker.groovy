package repo.build

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.InvocationResult
import org.apache.maven.shared.invoker.Invoker
import repo.build.maven.MavenArtifact
import repo.build.maven.MavenComponent


/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
class MavenInvoker {
    static List<MavenComponent> getComponents(File basedir) {
        InvocationRequest request = new DefaultInvocationRequest()
        File pomFile = new File(basedir, "pom.xml")
        request.setPomFile(pomFile)
        request.setGoals(Collections.singletonList("dependency:list"))
        request.setInteractive(false)
        Properties properties = new Properties();
        properties.put("outputFile", '${project.build.directory}/dependencies')
        // TODO:remove me
        System.properties.put("maven.home", "/usr/share/maven/")
        request.setProperties(properties)

        Invoker invoker = new DefaultInvoker()
        try {
            InvocationResult result = invoker.execute(request)
            if (result.exitCode != 0) {
                throw new RepoBuildException("exitCode: " + result.exitCode)
            }
        }
        catch (Exception e) {
            throw new RepoBuildException("getDependencyTree: " + e.getMessage(), e)
        }

        List<MavenComponent> result = new ArrayList<>()
        // собираем компоненты
        Pom.getModules(pomFile).each {
            File componentBasedir = new File(basedir, it)
            MavenComponent component = new MavenComponent()
            component.setBasedir(componentBasedir)
            component.setDependencies(getComponentDependencies(componentBasedir))
            result.add(component)
        }
        return result
    }

    static Set<MavenArtifact> getComponentDependencies(File basedir) {
        return getModuleDependencies(basedir)
    }

    static Set<MavenArtifact> getModuleDependencies(File basedir) {
        Set<MavenArtifact> result = new HashSet<>()
        result.addAll(readDependencies(basedir))
        Pom.getModules(new File(basedir, "pom.xml")).each {
            File moduleBasedir = new File(basedir, it)
            result.addAll(getModuleDependencies(moduleBasedir))
        }
        return result
    }

    static List<MavenArtifact> readDependencies(File basedir) {
        //TODO: fix target
        def lines = new File(basedir, "target/dependencies").readLines()
        List<MavenArtifact> result = new ArrayList<>()
        lines.each {
            def tokens = it.split(":")
            if (tokens.length > 3) {
                result.add(new MavenArtifact(tokens[0].trim(), tokens[1].trim()))
            }
        }
        return result
    }
}


