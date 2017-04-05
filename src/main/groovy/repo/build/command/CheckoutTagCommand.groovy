package repo.build.command

import repo.build.CliOptions
import repo.build.GitFeature
import repo.build.RepoEnv

class CheckoutTagCommand extends AbstractCommand {
    CheckoutTagCommand() {
        super('checkout-tag', 'Checkout to tag')
    }

    void execute(RepoEnv env, CliOptions options) {
        def tag = options.getTag()
        GitFeature.checkoutTag(env, options.getParallel(), tag)
    }
}
