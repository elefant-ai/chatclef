package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop", "Stop task runner (stops all automation), also stops the IDLE task until a new task is started");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.stop();
        finish();
    }
}
