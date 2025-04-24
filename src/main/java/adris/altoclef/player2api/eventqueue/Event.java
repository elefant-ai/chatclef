package adris.altoclef.player2api.eventqueue;

import adris.altoclef.player2api.LLM.LLMState;

public abstract class Event {
    public abstract LLMState handle(LLMState pvs);
}
