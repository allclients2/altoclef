package adris.altoclef.util.math;

public class ArrayUtil {
    public static <T> boolean contains(T[] arr, T target) {
        for (T element : arr) {
            if (element == target) {
                return true;
            }
        }
        return false;
    }
}
