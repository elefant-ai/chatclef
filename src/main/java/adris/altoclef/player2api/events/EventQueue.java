package adris.altoclef.player2api.events;
import java.util.*;

public class EventQueue {
    private final Map<String, Event> eventMap = new HashMap<>();
    private boolean polling = false;
    private long lastLLMCallTime = 0;
    private final long minDelay = 5000;
    private final long maxDelay = 30000;
    private long delay = 5000;

    public void addEvent(Event newEvent) {
        String type = newEvent.getType();
        if (eventMap.containsKey(type)) {
            eventMap.get(type).combineWith(newEvent);
            System.out.println("Combined event of type: " + type);
        } else {
            eventMap.put(type, newEvent);
            System.out.println("Added new event of type: " + type);
        }
    }

    public void poll() {
        if (polling) return;
        polling = true;

        if (!eventMap.isEmpty()) {
            Iterator<Map.Entry<String, Event>> iterator = eventMap.entrySet().iterator();

            if (eventMap.size() > 1 && iterator.hasNext()) {
                Event event = iterator.next().getValue();
                iterator.remove();
                event.handle(false);
            } else if (iterator.hasNext()) {
                Event event = iterator.next().getValue();
                long now = System.currentTimeMillis();
                long timeSinceLast = now - lastLLMCallTime;

                if (timeSinceLast >= delay) {
                    lastLLMCallTime = now;
                    iterator.remove();
                    event.handle(true);
                } else {
                    if (delay < maxDelay) delay += 20;
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
