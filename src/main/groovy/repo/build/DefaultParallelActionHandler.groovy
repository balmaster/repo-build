package repo.build

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
class DefaultParallelActionHandler implements ActionHandler{
    void beginAction(ActionContext context) {

    }

    void endAction(ActionContext context) {
        context.processOutList.forEach({ stream ->
            System.out.write(stream.toByteArray())
        })
    }
}
