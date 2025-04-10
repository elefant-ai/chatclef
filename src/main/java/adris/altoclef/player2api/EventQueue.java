package adris.altoclef.player2api;

import java.util.*;
import java.util.function.BiFunction;

public class EventQueue {

    public interface EventHandler {
        void handle(Object data, boolean isFinalCall);
    }

    public record Event(
            String type,
            EventHandler handler,
            Object data,
            BiFunction<Object, Object, Object> combiner
    ) {
        public Event combineWith(Object newData) {
            return new Event(type, handler, combiner.apply(data, newData), combiner);
        }
    }

    private final Map<String, Event> eventMap = new HashMap<>();
    private boolean polling = false;
    private long lastLLMCallTime = 0;
    private long minDelay = 5000;
    private long maxDelay = 30000;
    private long delay = 5000;

    public void addEvent(String type, EventHandler handler, Object data, BiFunction<Object, Object, Object> combiner) {
        eventMap.compute(type, (k, existing) ->
                existing == null
                        ? new Event(type, handler, data, combiner)
                        : existing.combineWith(data)
        );
    }

    public void poll() {
        if (polling) return;
        polling = true;

        if (!eventMap.isEmpty()) {
            var entries = new ArrayList<>(eventMap.entrySet());
            var first = entries.get(0);
            String key = first.getKey();
            Event event = first.getValue();

            if (eventMap.size() > 1) {
                eventMap.remove(key);
                event.handler.handle(event.data, false);
            } else {
                long now = System.currentTimeMillis();
                long sinceLastLLM = now - lastLLMCallTime;

                if (sinceLastLLM >= delay) {
                    lastLLMCallTime = now;
                    eventMap.remove(key);
                    event.handler.handle(event.data, true);
                } else {
                    delay = Math.min(maxDelay, delay + 20);
                }
            }
        } else {
            delay = Math.max(minDelay, delay - 20);
        }

        polling = false;
    }

    public void clear() {
        eventMap.clear();
        polling = false;
    }
}
