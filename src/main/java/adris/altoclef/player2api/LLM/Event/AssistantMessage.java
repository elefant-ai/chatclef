package adris.altoclef.player2api.LLM.Event;

import java.util.Optional;

public class AssistantMessage extends Event {
    

    private String msg;
    private String[] commands;

    public AssistantMessage(String msg, String[] commands) {
        super(true);
        this.msg = msg;
        this.commands = commands;
    }

    @Override
    public Optional<Event[]> handle() {
        // TODO: 
        return Optional.empty();
    }
}
