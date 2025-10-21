package sistemas.operativos.proyecto1.lib;

/**
 *
 * @author Sebastián
 * @param <T>
 */
public class Stack<T> {
    private final LinkedList<T> list = new LinkedList<>();

    public Stack() { }

    public void push(T value) { list.addFirst(value); }

    public T pop() { return list.removeFirst(); }

    public T peek() { return list.size() == 0 ? null : list.get(0); }

    public int size() { return list.size(); }

    public boolean isEmpty() { return list.isEmpty(); }

    @Override
    public String toString() { return list.toString(); }
}
