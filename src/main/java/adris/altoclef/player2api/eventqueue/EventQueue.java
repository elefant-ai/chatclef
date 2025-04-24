package adris.altoclef.player2api.eventqueue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class EventQueue {
    Deque<Event> queue;

    public EventQueue() {
        queue = new ArrayDeque<>();
    }

    public void addEvent(Event toAdd) {
        queue.add(toAdd);
    }

    public Optional<Event> poll() {
        Event b = queue.poll();
        return Optional.ofNullable(b);
    }
}
