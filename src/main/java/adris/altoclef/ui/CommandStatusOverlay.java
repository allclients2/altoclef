package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class CommandStatusOverlay {

    //For the ingame timer
    private long timeRunning;
    private long lastTime = 0;
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.from(ZoneOffset.of("+00:00"))); // The date formatter

    public void render(AltoClef mod, MatrixStack matrixstack) {
        List<Task> tasks = Collections.emptyList();
        if (mod.getTaskRunner().getCurrentTaskChain() != null) {
            tasks = mod.getTaskRunner().getCurrentTaskChain().getTasks();
        }

        matrixstack.push();

        drawTaskChain(MinecraftClient.getInstance().textRenderer, 10, 6,
                matrixstack.peek().getPositionMatrix(),
                MinecraftClient.getInstance().getBufferBuilders().getOutlineVertexConsumers(),
                TextRenderer.TextLayerType.SEE_THROUGH, 6, tasks, mod);

        matrixstack.pop();
    }

    private void drawTaskChain(TextRenderer renderer, float x, float y, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int maxLines, List<Task> tasks, AltoClef mod) {
        int whiteColor = 0xFFFFFFFF;

        matrix.scale(0.86F, 0.86F, 0.86F);

        float fontHeight = renderer.fontHeight;
        float addX = 4;
        float addY = fontHeight + 2;

        String headerInfo = mod.getTaskRunner().statusReport;
        String realTime = DATE_TIME_FORMATTER.format(Instant.ofEpochMilli((long) (mod.getUserTaskChain().taskStopwatch.time())));

        renderer.draw(headerInfo + ((mod.getModSettings().shouldShowTimer() && mod.getUserTaskChain().isActive()) ? (", timer: " + realTime) : ""), x, y, Color.LIGHT_GRAY.getRGB(), true, matrix, vertexConsumers, layerType, 0, 255);
        y += addY;

        if (tasks.isEmpty()) {
            if (mod.getTaskRunner().isActive()) {
                renderer.draw(" (no task running) ", x, y, whiteColor, true, matrix, vertexConsumers, layerType, 0, 255);
            }
            if (lastTime + 10000 < Instant.now().toEpochMilli() && mod.getModSettings().shouldShowTimer()) {//if it doesn't run any task in 10 secs
                timeRunning = Instant.now().toEpochMilli();//reset the timer
            }
            return;
        }


        if (tasks.size() <= maxLines) {
            for (Task task : tasks) {
                renderTask(task, renderer, x, y, matrix, vertexConsumers, layerType);

                x += addX;
                y += addY;
            }
            return;
        }

        // FIXME: Don't think the number of displayed "Other tasks" is accurate...
        for (int i = 0; i < tasks.size(); ++i) {
            if (i == 2) { // So we can see the second top task..
                x += addX * 2;
                renderer.draw("... " + (tasks.size() - maxLines) + " other task(s) ...", x, y, whiteColor, true, matrix, vertexConsumers, layerType, 0, 255);
            } else if (i <= 1 || i > (tasks.size() - maxLines + 1)) {
                renderTask(tasks.get(i), renderer, x, y, matrix, vertexConsumers, layerType);
            } else {
                continue;
            }

            x += addX;
            y += addY;
        }


    }


    private void renderTask(Task task, TextRenderer renderer, float x, float y, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType) {
        String taskName = task.getClass().getSimpleName() + " ";
        renderer.draw(taskName, x, y, new Color(128, 128, 128).getRGB(), true, matrix, vertexConsumers, layerType, 0, 255);

        renderer.draw(task.toString(), x + renderer.getWidth(taskName), y, new Color(255, 255, 255).getRGB(), true, matrix, vertexConsumers, layerType, 0, 255);

    }

}
