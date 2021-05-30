package adris.altoclef.tasksystem.chains;


import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.misc.IdleTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.InputUtil;
import adris.altoclef.util.csharpisbetter.Action;
import adris.altoclef.util.csharpisbetter.Stopwatch;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;


// A task chain that runs a user defined task at the same priority.
// This basically replaces our old Task Runner.
@SuppressWarnings("ALL")
public class UserTaskChain extends SingleTaskChain {
    public final Action<String> onTaskFinish = new Action<>();
    private final Stopwatch taskStopwatch = new Stopwatch();
    private Consumer currentOnFinish = null;

    public UserTaskChain(TaskRunner runner) {
        super(runner);
    }

    private static String prettyPrintTimeDuration(double seconds) {
        int minutes = (int) (seconds / 60);
        int hours = minutes / 60;
        int days = hours / 24;

        String result = "";
        if (days != 0) {
            result += days + " days ";
        }
        if (hours != 0) {
            result += (hours % 24) + " hours ";
        }
        if (minutes != 0) {
            result += (minutes % 60) + " minutes ";
        }
        if (!result.equals("")) {
            result += "and ";
        }
        result += String.format("%.2f", (seconds % 60));
        return result;
    }

    @Override
    protected void onTick(AltoClef mod) {

        // Pause if we're not loaded into a world.
        if (!mod.inGame()) return;

        super.onTick(mod);
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        boolean shouldIdle = mod.getModSettings().shouldIdleWhenNotActive();
        if (!shouldIdle) {
            // Stop.
            mod.getTaskRunner().disable();
            // Extra reset. Sometimes baritone is laggy and doesn't properly reset our press
            mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }
        double seconds = taskStopwatch.time();
        Debug.logMessage("User task FINISHED. Took %s seconds.", prettyPrintTimeDuration(seconds));
        if (currentOnFinish != null) {
            //noinspection unchecked
            currentOnFinish.accept(null);
        }
        currentOnFinish = null;
        onTaskFinish.invoke(String.format("Took %.2f seconds", taskStopwatch.time()));
        mainTask = null;
        if (shouldIdle) {
            mod.runUserTask(new IdleTask());
        }
    }

    public void cancel(AltoClef mod) {
        if (mainTask != null && mainTask.isActive()) {
            stop(mod);
            onTaskFinish(mod);
        }
    }

    @Override
    public float getPriority(AltoClef mod) {
        // Stop shortcut
        if (mainTask != null && mainTask.isActive() && InputUtil.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && InputUtil.isKeyPressed(
                GLFW.GLFW_KEY_K)) {
            // Ignore if we're idling as a background task.
            if (mainTask instanceof IdleTask && mod.getModSettings().shouldIdleWhenNotActive()) {
                return 50;
            }
            Debug.logMessage("(stop shortcut sent)");
            cancel(mod);
        }
        return 50;
    }

    @Override
    public String getName() {
        return "User Tasks";
    }

    public void runTask(AltoClef mod, Task task, Consumer onFinish) {
        currentOnFinish = onFinish;
        Debug.logMessage("User Task Set: " + task.toString());
        mod.getTaskRunner().enable();
        taskStopwatch.begin();
        setTask(task);
    }
}
