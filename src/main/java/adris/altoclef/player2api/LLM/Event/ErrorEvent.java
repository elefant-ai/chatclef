package adris.altoclef.player2api.LLM.Event;
import java.util.Optional;

public class ErrorEvent extends Event {
    private String errMessage;

    public ErrorEvent(String err) {
        super(true);
        this.errMessage = err;
    }

    @Override
    public Optional<Event[]> handle() {
        // TODO: Send error to LLM
        System.err.println("TODO: Send error back to llm"  + errMessage);
        return Optional.empty();
    }
}
