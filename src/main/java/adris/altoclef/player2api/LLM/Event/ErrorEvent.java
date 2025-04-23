package adris.altoclef.player2api.LLM.Event;
import java.util.List;
import java.util.Optional;

import adris.altoclef.player2api.LLM.State.LLMState;

public class ErrorEvent extends Event {
    private String errMessage;

    public ErrorEvent(String err) {
        super(true);
        this.errMessage = err;
    }

    @Override
    public List<Event> handle(LLMState state) {
        // TODO: Send error to LLM
        System.err.println("TODO: Send error back to llm"  + errMessage);
        return List.of();
    }
}
