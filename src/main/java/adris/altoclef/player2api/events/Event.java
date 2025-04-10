package adris.altoclef.player2api.events;

public abstract class Event {
    protected final String type;

    public Event(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public abstract void combineWith(Event other);

    public abstract void handle(boolean isFinalCall);
}

