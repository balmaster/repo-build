package repo.build.command.combo

import repo.build.command.*

class FeatureSyncStashComboCommand extends ComboCommand {

    FeatureSyncStashComboCommand() {
        super('fss', 'stash sync switch stash-pop combo',
                new StashCommand(),
                new SyncCommand(),
                new SwitchCommand(),
                new StashPopCommand()
        )
    }
}
