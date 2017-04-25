package repo.build

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
interface ActionHandler {
    void beginAction(ActionContext context)
    void endAction(ActionContext context)
}