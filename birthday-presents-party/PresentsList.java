import java.util.concurrent.locks.ReentrantLock;


// Implements Lazy linked list.
// This is adopted code from the book "The Art of Multiprocessor Programming"
// by Maurice Herlihy & Nir Shavit, from p. 208.
public class PresentsList {
    public Node head;

    public PresentsList() {
        this.head = new Node(Integer.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE);
    }

    public boolean add(int target) {
        while (true) {
            Node pred = head;
            Node curr = pred.next;

            while (curr.itemNum < target) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock.lock();

            try {
                curr.lock.lock();
        
                try {
                    if (validate(pred, curr)) {
                        if (curr.itemNum == target) {
                            return false;
                        } else {
                            Node node = new Node(target);
                            node.next = curr;
                            pred.next = node;
                            return true;
                        }
                    }
                } finally {
                    curr.lock.unlock();
                }
            } finally {
                pred.lock.unlock();
            }
        }
    } 

    public boolean remove(int target) {
        
        while (true) {
            Node pred = head;
            Node curr = pred.next;

            while (curr.itemNum < target) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock.lock(); 
            
            try {
                curr.lock.lock();

                try {
                    if (validate(pred, curr)) {
                        if (curr.itemNum != target) {
                            return false;
                        } else {
                            curr.marked = true;
                            pred.next = curr.next;
                            return true;
                        }
                    }
                } finally {
                    curr.lock.unlock();
                }
            } finally {
                pred.lock.unlock();
            }
        }
    }
    
    

    public boolean contains(int target) {
        Node curr = head;

        while (curr.itemNum < target) 
            curr = curr.next;      
        
        return curr.itemNum == target && !curr.marked;
    }

    private boolean validate(Node pred, Node curr) {
        return !pred.marked && !curr.marked && pred.next == curr;
    }
}


// Single element in linked list.
class Node {
    public int itemNum;
    public Node next;
    public boolean marked;
    ReentrantLock lock;

    Node(int itemNum) {
        this.itemNum = itemNum;
        lock = new ReentrantLock();
        marked = false;
    }
}

