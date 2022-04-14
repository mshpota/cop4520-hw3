import java.util.concurrent.atomic.AtomicReference;


// Implements an unbounded Lock-Free Queue.
// This is adopted code from the book "The Art of Multiprocessor Programming"
// by Maurice Herlihy & Nir Shavit, from p. 230.
public class DataBuffer {
    AtomicReference<Node> head, tail;

    public DataBuffer() {
        head = new AtomicReference<Node>(new Node(Integer.MIN_VALUE));
        tail = new AtomicReference<Node>(head.get());
    }

    public void enqueue(int temperature) {
        Node node = new Node(temperature);

        while (true) {
            Node last = tail.get();
            Node next = last.next.get();
            if (last == tail.get()) {
                if (next == null) {
                    if (last.next.compareAndSet(next, node)) {
                        tail.compareAndSet(last, node);
                        return;
                    }
                } else {
                    tail.compareAndSet(last, next);
                }

            }
        }
    }

    public int dequeue() throws EmptyBufferException {
        while (true) {
            Node first = head.get();
            Node last = tail.get();
            Node next = first.next.get();
            if (first == head.get()) {
                if (first == last) {
                    if (next == null) {
                        throw new EmptyBufferException("Buffer is empty.");
                    }
                    tail.compareAndSet(last, next);
                } else {
                    int value = next.temperature;
                    if (head.compareAndSet(first, next))
                        return value;
                }
            }
        }
    }
}


// Single element in the buffer list.
class Node {
    public int temperature;
    public AtomicReference<Node> next;

    public Node (int temperature) {
        this.temperature = temperature;
        next = new AtomicReference<Node>(null);
    }
}


class EmptyBufferException extends Exception {

    public EmptyBufferException() {}

    public EmptyBufferException(String message) {
        super(message);
    }
}