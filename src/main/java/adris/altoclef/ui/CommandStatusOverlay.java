package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.DrawContextWrapper;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;


import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandStatusOverlay {

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.from(ZoneOffset.of("+00:00"))); // The date formatter
    //For the ingame timer
    private long timeRunning;
    private long lastTime = 0;

    public void render(AltoClef mod, DrawContextWrapper context) {
        List<Task> tasks = Collections.emptyList();
        if (mod.getTaskRunner().getCurrentTaskChain() != null) {
            tasks = new ArrayList<>(mod.getTaskRunner().getCurrentTaskChain().getTasks());
        }

        MatrixStack matrixStack = context.getMatrices();

        matrixStack.push();

        drawTaskChain(context,MinecraftClient.getInstance().textRenderer, 10, 10,
                matrixStack, 10, tasks, mod);

        matrixStack.pop();
    }

    private void drawTaskChain(DrawContextWrapper context, TextRenderer renderer, int x, int y, MatrixStack matrices, int maxLines, List<Task> tasks, AltoClef mod) {
        int whiteColor = 0xFFFFFFFF;

        matrices.scale(0.5f,0.5f,0.5f);

        int fontHeight = renderer.fontHeight;
        int addX = 4;
        int addY = fontHeight + 2;

        context.drawText(renderer,mod.getTaskRunner().statusReport, x, y, Color.LIGHT_GRAY.getRGB(), true);
        y += addY;

        if (tasks.isEmpty()) {
            if (mod.getTaskRunner().isActive()) {
                context.drawText(renderer," (no task running) ", x, y, whiteColor, true);
            }
            if (lastTime + 10000 < Instant.now().toEpochMilli() && mod.getModSettings().shouldShowTimer()) {//if it doesn't run any task in 10 secs
                timeRunning = Instant.now().toEpochMilli();//reset the timer
            }
            return;
        }

        if (mod.getModSettings().shouldShowTimer()) {
            lastTime = Instant.now().toEpochMilli();

            String realTime = DATE_TIME_FORMATTER.format(Instant.now().minusMillis(timeRunning));
            context.drawText(renderer, "<" + realTime + ">", x, y, whiteColor, true);
            x += addX;
            y += addY;
        }

        if (tasks.size() <= maxLines) {
            for (Task task : tasks) {
                renderTask(task, context, renderer, x, y);

                x += addX;
                y += addY;
            }
            return;
        }

        for (int i = 0; i < tasks.size(); ++i) {
            if (i == 1) {
                x += addX * 2;
                context.drawText(renderer, "...", x, y, whiteColor, true);

            } else if (i == 0 || i > tasks.size() - maxLines) {
                renderTask(tasks.get(i),context ,renderer, x, y);
            } else {
                continue;
            }

            x += addX;
            y += addY;
        }


    }


    private void renderTask(Task task, DrawContextWrapper context, TextRenderer renderer, int x, int y) {
        String taskName = task.getClass().getSimpleName() + " ";
        context.drawText(renderer, taskName, x, y, new Color(128, 128, 128).getRGB(), true);

        context.drawText(renderer, task.toString(), x + renderer.getWidth(taskName), y, new Color(255, 255, 255).getRGB(), true);

    }

}
