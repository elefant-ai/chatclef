package adris.altoclef.player2api;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.player2api.eventqueue.Greeting;
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
    private boolean inGame;

    public AICommandBridge(CommandExecutor cmdExecutor, AltoClef mod) {
        this.mod = mod;
        this.cmdExecutor = cmdExecutor;
        this.inGame = false;
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
        // make sure we only run the queueProcessor if we are in the game (mod and
        // player exist)
        if (mod != null && mod.getPlayer() != null && inGame) {
            QueueProcessor.onTick(mod, this);
        }
    }

    public void sendHeartbeat() {
        executorService.submit(() -> {
            Player2APIService.sendHeartbeat();
        });
    }

    public void sendAssistantMessage(String message, Character character) {
        mod.logCharacterMessage(message, character);
    }

    public void sendUserMessage(String message) {
        mod.log(message);
    }

    public void onLogin() {
        if (!inGame) {
            inGame = true;

            // put onLogin stuff in here:
            QueueProcessor.addEvent(new Greeting());
        }
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