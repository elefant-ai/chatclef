package adris.altoclef.player2api.events;
public class TaskResultEvent extends Event {
    public enum ResultType {
        SUCCEED,
        FAILED,
        CANCELLED
    }

    private final ResultType result;
    private final String currentCommand;
    private final CommandQueue commandQueue;

    public TaskResultEvent(String currentCommand, ResultType result, CommandQueue commandQueue) {
        super("task_result");
        this.result = result;
        this.currentCommand = currentCommand;
        this.commandQueue = commandQueue;
    }

    @Override
    public void combineWith(Event other) {
        // TODO: Do nothing?
    }

    @Override
    public void handle(boolean isFinalCall) {
        switch (result) {
            case SUCCEED:
                System.out.println("Task succeeded: " + currentCommand);
                // TODO: start executing next action if you want or wait for user
                commandQueue.setProcessing(false);
                break;

            case FAILED:
                System.err.println("Task failed: " + currentCommand);
                commandQueue.clearCommandQueue();
                break;

            case CANCELLED:
                System.out.println("Task cancelled: " + currentCommand);
                commandQueue.setProcessing(false);
                commandQueue.emit("executionCancelled");
                break;
        }
    }
}
