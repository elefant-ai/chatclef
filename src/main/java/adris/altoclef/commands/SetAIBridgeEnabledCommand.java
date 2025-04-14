import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ItemList;
import adris.altoclef.tasks.movement.GoToStrongholdPortalTask;
import adris.altoclef.tasks.movement.LocateDesertTempleTask;

public class SetAIBridgeEnabledCommand extends Command {

    public SetAIBridgeEnabledCommand() throws CommandException {
        super("chatclef", "Turns chatclef on or off, can ONLY be run by the user (NOT the agent).",
                new Arg<>(ToggleState.class, "onOrOff"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        ToggleState toggle = parser.get(ToggleState.class);
        switch (toggle) {
            case ON:
                mod.getAiBridge().setEnabled(true);
                break;
            case OFF:
                mod.getAiBridge().setEnabled(false);
                break;
        }
    }

    public enum ToggleState {
        ON,
        OFF
    }
}
