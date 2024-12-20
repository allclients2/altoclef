package adris.altoclef.tasksystem;

import adris.altoclef.AltoClef;

import java.util.ArrayList;
import java.util.List;

public abstract class TaskChain {

    private static final short maxTaskCountSize = 1024; // For safety
    private final List<Task> cachedTaskChain = new ArrayList<>();

    public TaskChain(TaskRunner runner) {
        runner.addTaskChain(this);
    }

    public void tick(AltoClef mod) {
        cachedTaskChain.clear();
        onTick(mod);
    }

    public void stop(AltoClef mod) {
        cachedTaskChain.clear();
        onStop(mod);
    }

    protected abstract void onStop(AltoClef mod);

    public abstract void onInterrupt(AltoClef mod, TaskChain other);

    protected abstract void onTick(AltoClef mod);

    public abstract float getPriority(AltoClef mod);

    public abstract boolean isActive();

    public abstract String getName();

    public List<Task> getTasks() {
        return cachedTaskChain;
    }


    void addTaskToChain(Task task) {
        if (cachedTaskChain.size() >= maxTaskCountSize) {
            throw new RuntimeException("Task overflow! (Attempt add task when task count is " + maxTaskCountSize + " or more)");
        }
        cachedTaskChain.add(task);
    }

    public String toString() {
        return getName();
    }

}
