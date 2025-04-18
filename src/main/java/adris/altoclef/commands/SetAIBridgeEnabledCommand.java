package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;

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
                Debug.logMessage(
                        "Enabling the AI Bridge! You can now hear the player again and will intercept their messages, give them a quick welcome back message.");
                mod.setChatClefEnabled(true);
                break;
            case OFF:
                Debug.logMessage(
                        "AI Bridge disabled! Say goodbye to the player as you won't hear or intercept any of their messages until they turn you back on.");
                mod.setChatClefEnabled(false);
                break;
        }
        finish();
    }

    public enum ToggleState {
        ON,
        OFF
    }
}
