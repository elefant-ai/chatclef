package adris.altoclef.player2api.eventqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import adris.altoclef.AltoClef;
import adris.altoclef.player2api.AICommandBridge;
import adris.altoclef.player2api.LLM.LLMService;
import adris.altoclef.player2api.LLM.LLMState;
import adris.altoclef.player2api.actions.GameAction;
import adris.altoclef.player2api.status.AgentStatus;
import adris.altoclef.player2api.status.WorldStatus;

public class QueueProcessor {
    private static final ExecutorService QUEUE_PROCESSOR = Executors.newSingleThreadExecutor();

    static boolean isCallingLLM = false;

    static LLMState state = new LLMState();
    static EventQueue eventQ = new EventQueue();

    private static final List<GameAction> pendingActions = new ArrayList<>();

    public static void addEvent(Event e){
        eventQ.addEvent(e);
    }

    public static void onTick(AltoClef mod, AICommandBridge bridge) {
        // handle any pending actions, wait if another thread is adding to it (should be
        // done quickly)
        synchronized (pendingActions) {
            if (!pendingActions.isEmpty()) {
                for (GameAction action : pendingActions) {
                    action.handle(bridge, state);
                }
                pendingActions.clear();
                return;
            }
        }

        if (isCallingLLM)
            return;

        Optional<Event> topEventOption = eventQ.poll();

        // if there is an event handle it (add to conversation history), otherwise get the LLM response and add the
        // actions.
        topEventOption.ifPresentOrElse(
                evt -> evt.handle(state),
                () -> {
                    if (state.isLastMessageFromAssistant()) {
                        // if last message was from assistant then dont do anything.
                        return;
                    }
                    isCallingLLM = true;

                    AgentStatus agentStatus = AgentStatus.fromMod(mod);
                    WorldStatus worldStatus = WorldStatus.fromMod(mod);

                    // WITHOUT BLOCKING, use the api to get the response
                    QUEUE_PROCESSOR.execute(() -> {
                        List<GameAction> actions = LLMService.GetResponse(state, agentStatus, worldStatus);

                        synchronized (pendingActions) {
                            pendingActions.addAll(actions);
                        }

                        isCallingLLM = false;
                    });
                });
    }
}
