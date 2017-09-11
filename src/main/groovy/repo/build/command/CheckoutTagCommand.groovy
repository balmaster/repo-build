package repo.build.command

import repo.build.*

class CheckoutTagCommand extends AbstractCommand {
    CheckoutTagCommand() {
        super('checkout-tag', 'Checkout to tag')
    }

    public static final String ACTION_EXECUTE = 'checkoutTagCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultParallelActionHandler())
        def tag = options.getTag()
        context.withCloseable {
            GitFeature.checkoutTag(context, tag)
        }
    }
}
