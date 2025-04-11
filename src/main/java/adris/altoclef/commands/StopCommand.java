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
        mod.getUserTaskChain().cancel(mod);
        // also disable idle, but we can re-enable it as soon as any task runs
        mod.getTaskRunner().disable();
        finish();
    }
}
