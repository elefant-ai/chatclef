package adris.altoclef.player2api;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.player2api.eventqueue.QueueProcessor;
import adris.altoclef.player2api.eventqueue.UserMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



// Interface between Altoclef and Player2
public class AICommandBridge {

    private CommandExecutor cmdExecutor = null;
    private AltoClef mod = null;

    private boolean _enabled = true;

    private MessageBuffer altoClefMsgBuffer = new MessageBuffer(10);

    public static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private long lastHeartbeatTime = System.nanoTime();

    public AICommandBridge(CommandExecutor cmdExecutor, AltoClef mod) {
        this.mod = mod;
        this.cmdExecutor = cmdExecutor;
    }

    public void executeAltoclefCommand(String cmd) {
        if (!cmdExecutor.isClientCommand(cmd)) {
            cmdExecutor.execute(cmdExecutor.getCommandPrefix() + cmd);
        } else {
            cmdExecutor.execute(cmd);
        }
    }

    public void addAltoclefLogMessage(String message) {
        System.out.printf("ADDING Altoclef System Message: %s", message);
        altoClefMsgBuffer.addMsg(message);
    }

    public void onTick() {
        long now = System.nanoTime();
        if (now - lastHeartbeatTime > 60_000_000_000L) {
            sendHeartbeat();
            lastHeartbeatTime = now;
        }
        QueueProcessor.onTick(mod, this);
    }

    public void sendHeartbeat() {
        executorService.submit(() -> {
            Player2APIService.sendHeartbeat();
        });
    }

    public void sendAssistantMessage(String message, Character character) {
        mod.logCharacterMessage(message, character);
    }
    public void sendUserMessage(String message){
        mod.log(message);
    }

    public void onLogin() {
        // sendGreeting();
    }

    public void onUserMessage(String Message) {
        QueueProcessor.addEvent(new UserMessage(Message));
    }

    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    public boolean getEnabled() {
        return _enabled;
    }

}