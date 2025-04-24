package adris.altoclef.player2api.actions;

import adris.altoclef.player2api.AICommandBridge;
import adris.altoclef.player2api.Character;
import adris.altoclef.player2api.LLM.LLMState;

public class SendAssistantMessage implements GameAction {
    String message;
    Character character;

    public SendAssistantMessage(String msg, Character character) {
        this.message = msg;
        this.character = character;
    }

    public void handle(AICommandBridge bridge, LLMState state) {
        state.addAssistantMessage(message);
        bridge.sendAssistantMessage(message, character);
    }

}
