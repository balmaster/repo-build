package repo.build

import groovy.transform.CompileStatic
import repo.build.command.AbstractCommand

@CompileStatic
class CommandRegistry {
    Map<String, AbstractCommand> commandMap = new TreeMap<>()

    void registerCommand(AbstractCommand command) {
        if (commandMap.containsKey(command.name)) {
            throw new RepoBuildException("Command with name ${command.name} already registered")
        }
        commandMap.put(command.name, command)
    }

    AbstractCommand getCommand(String name) {
        if (!commandMap.containsKey(name)) {
            throw new RepoBuildException("Command with name ${name} not registered")
        }
        return commandMap.get(name)
    }

    Collection<AbstractCommand> getCommands() {
        return commandMap.values()
    }
}
