package adris.altoclef.player2api.eventqueue;

import java.util.List;

import adris.altoclef.player2api.AICommandBridge;
import adris.altoclef.player2api.LLM.LLMState;
import adris.altoclef.player2api.actions.GameAction;

public abstract class Event {
    public abstract LLMState updateState(LLMState pvs);

    public abstract List<GameAction> immediateHandle();
}
