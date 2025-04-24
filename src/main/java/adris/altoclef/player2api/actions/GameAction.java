package adris.altoclef.player2api.actions;

import adris.altoclef.player2api.AICommandBridge;
import adris.altoclef.player2api.LLM.LLMState;

public interface GameAction extends Action {
    public void handle(AICommandBridge bridge, LLMState state);
}
