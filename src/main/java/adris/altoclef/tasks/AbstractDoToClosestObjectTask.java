package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.misc.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Optional;

/**
 * https://www.notion.so/Closest-threshold-ing-system-utility-c3816b880402494ba9209c9f9b62b8bf
 *
 * Use this whenever you want to travel to a target position that may change.
 */
public abstract class AbstractDoToClosestObjectTask<T> extends Task {

    private T _currentlyPursuing = null;

    private final HashMap<T, CachedHeuristic> _heuristicMap = new HashMap<>();

    protected abstract Vec3d getPos(AltoClef mod, T obj);
    protected abstract T getClosestTo(AltoClef mod, Vec3d pos);
    protected abstract Vec3d getOriginPos(AltoClef mod);
    protected abstract Task getGoalTask(T obj);
    protected abstract boolean isValid(AltoClef mod, T obj);

    private boolean _wasWandering;

    // Virtual
    protected Task getWanderTask(AltoClef mod) {
        return new TimeoutWanderTask(true);
    }

    private Task _goalTask = null;

    public void resetSearch() {
        _currentlyPursuing = null;
        _heuristicMap.clear();
        _goalTask = null;
    }
    public boolean wasWandering() {return _wasWandering;}

    private double getCurrentCalculatedHeuristic(AltoClef mod) {
        Optional<Double> ticksRemainingOp = mod.getClientBaritone().getPathingBehavior().ticksRemainingInSegment();
        return ticksRemainingOp.orElse(Double.POSITIVE_INFINITY);
    }

    private boolean isMovingToClosestPos(AltoClef mod) {
        return _goalTask != null;// && _goalTask.isActive() && !_goalTask.isFinished(mod);
    }

    @Override
    protected Task onTick(AltoClef mod) {

        _wasWandering = false;

        // Reset our pursuit if our pursuing object no longer is pursuable.
        if (_currentlyPursuing != null && !isValid(mod, _currentlyPursuing)) {
            // This is probably a good idea, no?
            _heuristicMap.remove(_currentlyPursuing);
            _currentlyPursuing = null;
        }

        // Get closest object
        T newClosest = getClosestTo(mod, getOriginPos(mod));

        // Receive closest object and position
        if (newClosest != null && !newClosest.equals(_currentlyPursuing)) {
            // Different closest object
            if (_currentlyPursuing == null) {
                // We don't have a closest object
                _currentlyPursuing = newClosest;
            } else {
                if (isMovingToClosestPos(mod)) {
                    setDebugState("Moving towards closest...");
                    double currentHeuristic = getCurrentCalculatedHeuristic(mod);
                    double closestDistanceSqr = getPos(mod, _currentlyPursuing).squaredDistanceTo(mod.getPlayer().getPos());
                    int lastTick = AltoClef.getTicks();

                    if (!_heuristicMap.containsKey(_currentlyPursuing)) {
                        _heuristicMap.put(_currentlyPursuing, new CachedHeuristic());
                    }
                    CachedHeuristic h = _heuristicMap.get(_currentlyPursuing);
                    h.updateHeuristic(currentHeuristic);
                    h.updateDistance(closestDistanceSqr);
                    h.setTickAttempted(lastTick);
                    if (_heuristicMap.containsKey(newClosest)) {
                        // Our new object has a past potential heuristic calculated, if it's better try it out.
                        CachedHeuristic maybeReAttempt = _heuristicMap.get(newClosest);
                        double maybeClosestDistance = getPos(mod, newClosest).squaredDistanceTo(mod.getPlayer().getPos());
                        // Get considerably closer (divide distance by 2)
                        if (maybeReAttempt.getHeuristicValue() < h.getHeuristicValue() || maybeClosestDistance < maybeReAttempt.getClosestDistanceSqr() / 4) {
                            setDebugState("Retrying old heuristic!");
                            // The currently closest previously calculated heuristic is better, move towards it!
                            _currentlyPursuing = newClosest;
                            // In theory, this next line shouldn't need to be run,
                            // but it's CRITICAL to making this work for some reason
                            maybeReAttempt.updateDistance(maybeClosestDistance);
                        }
                    } else {
                        setDebugState("Trying out NEW pursuit");
                        // Our new object does not have a heuristic, TRY IT OUT!
                        _currentlyPursuing = newClosest;
                    }
                } else {
                    setDebugState("Waiting for move task to kick in...");
                    // We should keep moving towards our object until we get some new info.
                }
            }
        }

        if (_currentlyPursuing != null) {
            _goalTask = getGoalTask(_currentlyPursuing);
            return _goalTask;
        } else {
            _goalTask = null;
        }

        //noinspection ConstantConditions
        if (newClosest == null && _currentlyPursuing == null) {
            setDebugState("Waiting for calculations I think (wandering)");
            _wasWandering = true;
            return getWanderTask(mod);
        }

        setDebugState("Waiting for calculations I think (NOT wandering)");
        return null;
    }

    private static class CachedHeuristic {

        private double _closestDistanceSqr;
        private int _tickAttempted;
        private double _heuristicValue;

        public CachedHeuristic() {
            _closestDistanceSqr = Double.POSITIVE_INFINITY;
            _heuristicValue = Double.POSITIVE_INFINITY;
        }
        public CachedHeuristic(double closestDistanceSqr, int tickAttempted, double heuristicValue) {
            _closestDistanceSqr = closestDistanceSqr;
            _tickAttempted = tickAttempted;
            _heuristicValue = heuristicValue;
        }

        public double getHeuristicValue() {
            return _heuristicValue;
        }

        public void updateHeuristic(double heuristicValue) {
            _heuristicValue = Math.min(_heuristicValue, heuristicValue);
        }

        public double getClosestDistanceSqr() {
            return _closestDistanceSqr;
        }

        public void updateDistance(double closestDistanceSqr) {
            _closestDistanceSqr = Math.min(_closestDistanceSqr, closestDistanceSqr);
        }

        public int getTickAttempted() {
            return _tickAttempted;
        }

        public void setTickAttempted(int tickAttempted) {
            _tickAttempted = tickAttempted;
        }
    }

    // Interface DRAFT:
    /*
     * MAKE THIS AN ABSTRACT TASK
     *
     * T can be
     *      - BlockPos
     *      - Entity
     *      - Any object we might want to travel to
     *
     * Abstract functions
     *  - get position(T) -> position
     *  - get closest(T) -> (position, T)
     *
     *
     * Private fields
     * - best heurisitc
     * - best object
     * - (best position)?
     *
     * Methods
     * - Reset Search
     * - Get closest object
     *      - Runs closest position function
     *      - If different object:
     *          If previous object was null, accept and get to.
     *          If we're not running our GOTO task/process, ignore and keep going to previous object.
     *          If we ARE running our GOTO task/process, GET CURRENT CALCULATED HEURISTIC and MAP.
     *          If the NEW object has a MAPPING to a calculated heuristic:
     *              If PREVIOUS object has BETTER heuristic, accept previous object.
     *          If the NEW object does NOT have a mapping to a calculated heuristic, ACCEPT IT!
     *      - If same object, keep running the GOTO Task (if no goto task, create one)
     * - Create GOTO task/process given T
     */
}
