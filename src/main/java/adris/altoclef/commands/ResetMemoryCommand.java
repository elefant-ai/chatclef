package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;

public class ResetMemoryCommand extends Command {

    public ResetMemoryCommand() {
        super("resetmemory", "Reset the memory, does not stop the agent, can ONLY be run by the user (NOT the agent).");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.getAiBridge().conversationHistory().clear();
        finish();
    }
}
