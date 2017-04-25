package repo.build

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
class ActionContext implements Closeable {
    final ActionContext parent
    final RepoEnv env
    final String id
    final int parallel
    final List<ByteArrayOutputStream> processOutList = new ArrayList<>()
    final List<ActionContext> childList = new ArrayList<>()
    final ActionHandler actionHandler

    ActionContext(RepoEnv env, String id, int parallel, ActionHandler actionHandler1) {
        this.env = env
        this.id = id
        this.parallel = parallel
        this.actionHandler = actionHandler1
    }

    private ActionContext(ActionContext parent, String id) {
        this(parent.env, id, parent.parallel, parent.actionHandler)
        this.parent = parent
    }

    Closure newWriteOutHandler() {
        def out = new ByteArrayOutputStream();
        synchronized (processOutList) {
            processOutList.add(out)
        }
        return { int b ->
            out.write(b)
        }
    }

    ActionContext newChild(String id) {
        def child = new ActionContext(this, id)
        synchronized (childList) {
            childList.add(child)
        }
        actionHandler.beginAction(child)
        return child
    }

    void close() throws IOException {
        actionHandler.endAction(this)
    }
}
