package adris.altoclef.player2api.LLM.State;

import adris.altoclef.player2api.LLM.Event.*;

import java.util.*;
import java.util.concurrent.*;

public class LLMState {
    private static final ExecutorService IO_POOL = Executors.newSingleThreadExecutor();
    private static final ExecutorService IMMEDIATE_POOL = Executors.newCachedThreadPool();

    private final Deque<Event> eventQueue = new ArrayDeque<>();
    private final Object queueLock = new Object();

    private Optional<CharacterState> curCharState = Optional.empty();
    private Optional<AgentState> curAgentState = Optional.empty();
    private Optional<WorldState> curWorldState = Optional.empty();

    private volatile boolean running = false;

    public CharacterState getCurCharState(){
        return curCharState.orElseGet( () -> new CharacterState("AI", "Greet the user", "You are a friendly AI agent", new String[0]));
    }

    // public AgentState getAgentState(){
    //     // return curWorldState.orElseGet(() -> new AgentState());
    // }

    public LLMState() {
        startProcessingLoop();
    }

    public void addEvent(Event event) {
        if (event instanceof InstantEvent) {
            // Does NOT block the main queue thread
            IMMEDIATE_POOL.execute(() -> {
                System.err.println("[LLMState] Running immediate event");
                try {
                    List<Event> followUpEvents = event.handle(this);
                    if (followUpEvents != null && !followUpEvents.isEmpty()) {
                        synchronized (queueLock) {
                            eventQueue.addAll(followUpEvents);
                            queueLock.notify();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("[LLMState] Error during immediate event execution.");
                }
            });
            return;
        }

        synchronized (queueLock) {
            if (event instanceof CancelEvent) {
                eventQueue.clear();
                System.out.println("[LLMState] CancelEvent received. Queue cleared.");
            }
            eventQueue.addLast(event);
            queueLock.notify();
        }
    }

    public void stop() {
        running = false;
        synchronized (queueLock) {
            queueLock.notify();
        }
    }

    private void startProcessingLoop() {
        running = true;
        IO_POOL.execute(() -> {
            while (running) {
                Event event;
                synchronized (queueLock) {
                    while (eventQueue.isEmpty()) {
                        try {
                            queueLock.wait();
                            if (!running)
                                return;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    event = eventQueue.pollFirst();
                }

                if (event != null) {
                    List<Event> events = event.handle(this);
                    synchronized (queueLock) {
                        eventQueue.addAll(events);
                        queueLock.notify();
                    }
                }
            }
        });
    }

}
