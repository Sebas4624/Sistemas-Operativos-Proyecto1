package sistemas.operativos.proyecto1.lib;

/**
 *
 * @author Sebastián
 * @param <T>
 */
public class Queue<T> implements Iterable<T> {
    private final LinkedList<T> list = new LinkedList<>();

    public Queue() { }

    public void enqueue(T value) { list.addLast(value); }

    public T dequeue() { return list.removeFirst(); }

    public T peek() { return list.size() == 0 ? null : list.get(0); }

    public int size() { return list.size(); }

    public boolean isEmpty() { return list.isEmpty(); }

    public java.util.Iterator<T> iterator() { return list.iterator(); }
    
    public LinkedList<T> toLinkedList() { return list; }
    
    @Override
    public String toString() { return list.toString(); }
}