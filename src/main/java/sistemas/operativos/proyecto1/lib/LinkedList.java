package sistemas.operativos.proyecto1.lib;

/**
 *
 * @author Sebastián
 * @param <T>
 */
public class LinkedList<T> implements Iterable<T> {
    private static class Node<T> {
        T value;
        Node<T> next;
        Node<T> prev;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public LinkedList() { }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    public void add(T value) { addLast(value); }

    public void addFirst(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
    }

    public void addLast(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
    }

    public T removeFirst() {
        if (head == null) return null;
        T val = head.value;
        head = head.next;
        if (head != null) head.prev = null;
        else tail = null;
        size--;
        return val;
    }

    public T removeLast() {
        if (tail == null) return null;
        T val = tail.value;
        tail = tail.prev;
        if (tail != null) tail.next = null;
        else head = null;
        size--;
        return val;
    }

    /**
     * Removes first occurrence of value. Returns true if removed.
     */
    public boolean remove(T value) {
        for (Node<T> cur = head; cur != null; cur = cur.next) {
            if (value == null ? cur.value == null : value.equals(cur.value)) {
                unlink(cur);
                return true;
            }
        }
        return false;
    }

    private void unlink(Node<T> node) {
        Node<T> p = node.prev;
        Node<T> n = node.next;
        if (p == null) head = n;
        else p.next = n;
        if (n == null) tail = p;
        else n.prev = p;
        node.next = node.prev = null;
        node.value = null;
        size--;
    }

    public T get(int index) {
        checkElementIndex(index);
        Node<T> x;
        if (index < (size >> 1)) {
            x = head;
            for (int i = 0; i < index; i++) x = x.next;
        } else {
            x = tail;
            for (int i = size - 1; i > index; i--) x = x.prev;
        }
        return x.value;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    public void clear() {
        this.head = null;
        this.tail = null;
        this.size = 0;    
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return new java.util.Iterator<T>() {
            private Node<T> cur = head;
            @Override public boolean hasNext() { return cur != null; }
            @Override public T next() {
                if (cur == null) throw new java.util.NoSuchElementException();
                T v = cur.value;
                cur = cur.next;
                return v;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append('[');
        Node<T> cur = head;
        while (cur != null) {
            sb.append(cur.value);
            cur = cur.next;
            if (cur != null) sb.append(", ");
        }
        sb.append(']');
        return sb.toString();
    }
}
