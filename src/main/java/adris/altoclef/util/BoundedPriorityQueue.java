package adris.altoclef.util;

import java.util.Comparator;
import java.util.PriorityQueue;

public class BoundedPriorityQueue<T> extends PriorityQueue<T> {
    private final int maxSize;

    public BoundedPriorityQueue(int maxSize, Comparator<T> comparator) {
        super(comparator);
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(T element) {
        if (size() < maxSize) {
            return super.add(element);
        } else if (comparator().compare(element, peek()) < 0) {
            poll();
            return super.add(element);
        }
        return false;
    }
}
