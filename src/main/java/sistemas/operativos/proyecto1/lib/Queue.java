package sistemas.operativos.proyecto1.lib;

import sistemas.operativos.proyecto1.process.Process;

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
    
    // Priority

    public Process peekPriority() {
        if (list.size() == 0) return null;
        
        Process best = null;
        
        for(T item : list) {
            if (item instanceof Process p) {
                if (best == null || p.priority() > best.priority()) {
                    best = p;
                }
            }
        }
        return best;
    }
    
    public Process pollPriority() {
        if (list.size() == 0) return null;
        
        Process best = null;
        T removal = null;
        
        for(T item : list) {
            if (item instanceof Process p) {
                if (best == null || p.priority() > best.priority()) {
                    best = p;
                    removal = item;
                }
            }
        }
        list.remove(removal);
        return best;
    }
    
    // Instructions
    
    public Process peekShortest() {
        if (list.size() == 0) return null;
        
        Process best = null;
        
        for(T item : list) {
            if (item instanceof Process p) {
                if (best == null || p.instructions() < best.instructions()) {
                    best = p;
                }
            }
        }
        return best;
    }
    
    public Process pollShortest() {
        if (list.size() == 0) return null;
        
        Process best = null;
        T removal = null;
        
        for(T item : list) {
            if (item instanceof Process p) {
                if (best == null || p.instructions() < best.instructions()) {
                    best = p;
                    removal = item;
                }
            }
        }
        list.remove(removal);
        return best;
    }

    @Override
    public java.util.Iterator<T> iterator() { return list.iterator(); }
    
    public LinkedList<T> toLinkedList() { return list; }
    
    @Override
    public String toString() { return list.toString(); }
}