package adris.altoclef.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import adris.altoclef.multiversion.DrawContextWrapper;
import adris.altoclef.player2api.ChatclefConfigPersistantState;

public class STTfeedback {
    public enum Phase {
        Idle, Listening, Hint
    }

    public static Phase phase = ChatclefConfigPersistantState.isSttHintEnabled() ? Phase.Hint : Phase.Idle;

    public static void setIdle() {

        phase = Phase.Idle;
    }

    public static void setListening() {
        if (phase == Phase.Hint) {
            System.out.println("Running updateSttHint");
            ChatclefConfigPersistantState.updateSttHint(false);
        }
        phase = Phase.Listening;
    }

    public static void render(DrawContextWrapper ctx, MatrixStack matrices, KeyBinding sttKeybind) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        String keyName = sttKeybind.getBoundKeyLocalizedText().getString();
        String label;
        switch (phase) {
            case Hint:
                label = String.format("Press %s to use STT", keyName);
                break;
            case Idle:
                return;
            case Listening:
                label = String.format("STT: Listening");
                break;
            default:
                return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        // int screenHeight = client.getWindow().getScaledHeight();

        int textWidth = textRenderer.getWidth(label);
        int textX = (screenWidth - textWidth) / 2;
        int textY = 15;

        int pad = 4;
        ctx.fill(textX - pad, textY - pad, textX + textWidth + pad, textY + textRenderer.fontHeight + pad, 0xAA000000);

        ctx.drawText(textRenderer, label, textX, textY, 0xFFFFFF, true);
    }
}
