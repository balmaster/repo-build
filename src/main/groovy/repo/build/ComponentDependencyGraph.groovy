package repo.build

import org.jgrapht.DirectedGraph
import org.jgrapht.alg.CycleDetector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.TopologicalOrderIterator
import repo.build.maven.MavenComponent
import repo.build.maven.MavenComponentRef

/**
 */
class ComponentDependencyGraph {
    private final Map<MavenComponentRef, MavenComponent> componentsMap;
    private final DirectedGraph<MavenComponent, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class)
    private final Map<MavenComponent, Set<MavenComponent>> cycleRefs = new HashMap<>()

    ComponentDependencyGraph(Map<MavenComponentRef, MavenComponent> componentsMap) {
        this.componentsMap = componentsMap
    }

    public static ComponentDependencyGraph build(Collection<MavenComponent> components,
                                                 Closure dependencyFilter) {
        ComponentDependencyGraph result = new ComponentDependencyGraph()
        for (MavenComponent c : components) {
            result.add(c);
        }
        return result;
    }

    private void add(MavenComponent component) {
        if (graph.addVertex(component)) {
            // рекурсивно добавляем
            addAllDependencies(component);
        }
    }

    private void addAllDependencies(MavenComponent component) {
        for (def ref : component.getDependencies()) {
            MavenComponent refComponent = componentsMap.get(ref)
            if (refComponent == null) {
                // it is thirdparty component ref
                continue
            }
            add(refComponent)
            DefaultEdge e = graph.addEdge(component, refComponent)
            if (hasCycles()) {
                graph.removeEdge(e);
                if (!cycleRefs.containsKey(component)) {
                    cycleRefs.put(component, new HashSet<String>())
                }
                cycleRefs.get(component).add(refComponent)
            }
        }
    }

    public boolean hasCycles() {
        CycleDetector<MavenComponent, DefaultEdge> cycleDetector = new CycleDetector<>(graph)
        return cycleDetector.detectCycles();
    }

    public List<MavenComponent> sort() {
        TopologicalOrderIterator<MavenComponent, DefaultEdge> i = new TopologicalOrderIterator<>(graph);
        List<MavenComponent> items = new ArrayList<>();
        while (i.hasNext()) {
            items.add(i.next());
        }
        Collections.reverse(items);
        return items;
    }

    public Map<MavenComponent, Set<String>> getCycleRefs() {
        return cycleRefs;
    }

}
