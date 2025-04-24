package adris.altoclef.player2api.actions;

import adris.altoclef.player2api.AICommandBridge;
import adris.altoclef.player2api.LLM.LLMState;

public class SendUserMessage implements GameAction {
    String message;

    public SendUserMessage(String msg) {
        this.message = msg;
    }

    public void handle(AICommandBridge bridge, LLMState state) {
        bridge.sendUserMessage(message);
    }

}
