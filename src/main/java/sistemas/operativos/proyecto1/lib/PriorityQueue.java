package sistemas.operativos.proyecto1.lib;

import java.util.NoSuchElementException;

/**
 *
 * @author Sebastián
 * @param <T>
 */
public class PriorityQueue<T extends Comparable<? super T>> {
    private final LinkedList<T> list = new LinkedList<>();
    
    public void add(T value) {
        if (value == null) throw new NullPointerException("Null elements not allowed");
        list.addLast(value);
    }

    public T peek() {
        if (list.isEmpty()) throw new NoSuchElementException("PriorityQueue is empty");
        T best = null;
        for (T v : list) {
            if (best == null || v.compareTo(best) < 0) best = v;
        }
        return best;
    }

    public T poll() {
        if (list.isEmpty()) throw new NoSuchElementException("PriorityQueue is empty");
        T best = null;
        for (T v : list) {
            if (best == null || v.compareTo(best) < 0) best = v;
        }
        // remove first occurrence of best and return it
        boolean removed = list.remove(best);
        if (!removed) throw new IllegalStateException("Element found but couldn't remove");
        return best;
    }

    public boolean isEmpty() { return list.isEmpty(); }
    public int size() { return list.size(); }

    @Override
    public String toString() { return "PriorityQueue" + list.toString(); }
}
