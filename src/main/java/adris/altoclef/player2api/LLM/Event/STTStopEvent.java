package adris.altoclef.player2api.LLM.Event;

import java.util.List;

import adris.altoclef.player2api.Player2APIService;
import adris.altoclef.player2api.LLM.State.LLMState;

public class STTStopEvent extends Event implements InstantEvent {
    public STTStopEvent() {
        super(false);
    }

    @Override
    public List<Event> handle(LLMState state) {
        String msg = Player2APIService.stopSTT();
        System.out.printf("[STT] Stopping, got msg: '%s' ", msg);
        return List.of(new UserMessage(msg));
    }
}