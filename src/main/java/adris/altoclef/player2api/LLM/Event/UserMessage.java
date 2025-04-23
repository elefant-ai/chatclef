package adris.altoclef.player2api.LLM.Event;

import java.util.List;
import java.util.Optional;

public class UserMessage extends Event {
    

    private String msg;

    public UserMessage( String msg) {
        super(true);
        this.msg = msg;
    }

    @Override
    public List<Event> handle() {
        // TODO: use service to find and send assistant message
        return List.of();
    }
}
