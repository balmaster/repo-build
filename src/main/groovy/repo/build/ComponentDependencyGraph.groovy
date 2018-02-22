package repo.build

import groovy.transform.CompileStatic
import org.jgrapht.DirectedGraph
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.alg.CycleDetector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.TopologicalOrderIterator
import repo.build.maven.MavenArtifact
import repo.build.maven.MavenArtifactRef
import repo.build.maven.MavenComponent

/**
 */
@CompileStatic
class ComponentDependencyGraph {
    private final Map<MavenArtifactRef, MavenComponent> componentsMap
    private final DirectedGraph<MavenComponent, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class)
    private final CycleDetector<MavenComponent, DefaultEdge> cycleDetector = new CycleDetector<>(graph)

    ComponentDependencyGraph(Map<MavenArtifactRef, MavenComponent> componentsMap) {
        this.componentsMap = componentsMap
    }

    static ComponentDependencyGraph build(Collection<MavenComponent> components) {
        def componentsMap = getModuleToComponentMap(components)
        ComponentDependencyGraph result = new ComponentDependencyGraph(componentsMap)
        for (MavenComponent c : componentsMap.values()) {
            result.add(c)
        }
        return result
    }

    private void add(MavenComponent component) {
        if (graph.addVertex(component)) {
            // рекурсивно добавляем
            addAllDependencies(component)
        }
    }

    private void addAllDependencies(MavenComponent component) {
        if (component.parent) {
            // add parent ref
            addComponentRef(component, component.parent)
        }
        // for all modules
        for (def m : component.getModules()) {
            // add dependencies refs
            for (def ref : m.getDependencies()) {
                addComponentRef(component, ref)
            }
        }
    }

    private void addComponentRef(MavenComponent component, MavenArtifactRef ref) {
        MavenComponent refComponent = componentsMap.get(ref)
        if (refComponent && !component.equals(refComponent)) {
            add(refComponent)
            graph.addEdge(component, refComponent)
        }
    }

    boolean hasCycles() {
        return cycleDetector.detectCycles()
    }

    Set<MavenComponent> findCycles() {
        return cycleDetector.findCycles()
    }

    Set<MavenComponent> findCycles(MavenComponent v) {
        return cycleDetector.findCyclesContainingVertex(v)
    }

    List<MavenComponent> sort() {
        TopologicalOrderIterator<MavenComponent, DefaultEdge> i = new TopologicalOrderIterator<>(graph)
        List<MavenComponent> items = new ArrayList<>()
        while (i.hasNext()) {
            items.add(i.next())
        }
        Collections.reverse(items)
        return items
    }

    List<MavenComponent> getIncoming(MavenComponent component) {
        return graph.incomingEdgesOf(component)
                .collect { graph.getEdgeSource(it) }
    }

    List<MavenComponent> getOutgoing(MavenComponent component) {
        return graph.outgoingEdgesOf(component)
                .collect { graph.getEdgeSource(it) }
    }

    @CompileStatic
    static Map<MavenArtifactRef, MavenComponent> getModuleToComponentMap(Collection<MavenComponent> components) {
        Map<MavenArtifactRef, MavenComponent> result = new HashMap<>()
        for (MavenComponent c : components) {
            for (MavenArtifact m : c.getModules()) {
                // map all component modules into host component
                result.put(new MavenArtifactRef(m.getGroupId(), m.getArtifactId()), c)
            }
        }
        return result
    }
}
