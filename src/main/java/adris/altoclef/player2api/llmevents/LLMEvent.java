package adris.altoclef.player2api.llmevents;

public sealed interface LLMEvent permits UserMessageEvent, PlayerDeathEvent, AltoclefLogEvent {

    public static void handleEvent(LLMEvent event) {
        switch (event) {
            case UserMessageEvent msgEvent -> {
                System.out.println("User Message: " + msgEvent.message());
            }
            case PlayerDeathEvent deathEvent -> {
                System.out.println("Player died.");
            }
            case AltoclefLogEvent altoLogEvent -> {
                System.out.println("Altoclef log message: " + altoLogEvent.message());
            }
        }
    }
}
