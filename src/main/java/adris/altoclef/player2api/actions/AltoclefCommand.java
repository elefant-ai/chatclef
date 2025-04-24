package adris.altoclef.player2api.actions;

import adris.altoclef.player2api.AICommandBridge;
import adris.altoclef.player2api.LLM.LLMState;

public class AltoclefCommand implements GameAction {
    String command;

    public AltoclefCommand(String cmd) {
        this.command = cmd;
    }

    @Override
    public void handle(AICommandBridge bridge, LLMState state) {
        bridge.executeAltoclefCommand(command);
    }

}
