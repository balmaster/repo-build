package repo.build.command

import repo.build.ActionContext
import repo.build.CliOptions
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.RepoEnv

class CheckoutTagCommand extends AbstractCommand {
    CheckoutTagCommand() {
        super('checkout-tag', 'Checkout to tag')
    }

    public static final String ACTION_EXECUTE = 'checkoutTagCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        def tag = options.getTag()
        context.withCloseable {
            GitFeature.checkoutTag(context, tag)
        }
    }
}
