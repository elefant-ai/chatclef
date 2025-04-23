package adris.altoclef.player2api.LLM.Event;

import java.util.List;
import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.player2api.LLM.State.LLMState;
import net.minecraft.server.MinecraftServer;

public class AssistantMessage extends Event {
    private String msg;
    private String[] commands;

    public AssistantMessage(String msg, String[] commands) {
        super(true);
        this.msg = msg;
        this.commands = commands;
    }

    @Override
    public List<Event> handle(LLMState state) {
        // TODO: 
        MinecraftServer server = AltoClef.server;
        server.execute(() -> {
           AltoClef.getInstance().logCharacterMessage(msg, state.); 
        });

        return List.of(); 
    }
}
