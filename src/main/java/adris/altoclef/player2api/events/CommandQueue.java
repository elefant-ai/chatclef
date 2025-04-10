package adris.altoclef.player2api.events;

public class CommandQueue {
    private final EventQueue eventQueue = new EventQueue();
    private boolean polling = false;
    private boolean isProcessing = false;
    private boolean idleGoalOn = false;

    public int getCommandQueueLength() {
        return 0;
    }

    public boolean isPolling() {
        return polling;
    }

    public boolean isIdleGoalOn() {
        return idleGoalOn;
    }

    public EventQueue getEventQueue() {
        return eventQueue;
    }

    public void clearCommandQueue() {
        System.out.println("Clearing command queue...");
    }

    public void setProcessing(boolean val) {
        isProcessing = val;
    }
    public void emit(String msg){
       System.out.printf("Emmitted %s", msg);
    }

    public void process(){
        if(this.isProcessing) return; // Prevent concurrent processing
    }
}
