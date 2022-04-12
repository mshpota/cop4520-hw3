import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;


public class BirthdayPresentsParty {
    private static final int PRESENTS_NUM = 500000;

    public static void main(String [] args) {
        // Aux list to get elements shuffled.
        ArrayList<Integer> auxList = new ArrayList<Integer>(1000);

        for (int i = 0; i < PRESENTS_NUM; i++) {
            auxList.add(i);
        }

        Collections.shuffle(auxList);
        // This is the bag with shuffled presents that need to be ordered.
        ConcurrentLinkedDeque<Integer> bag = new ConcurrentLinkedDeque<>();
        // This is to keep record of presents' numbers that have been pulled out of bag.
        // It will be used for removal from the chainOfPresents list.
        ConcurrentLinkedDeque<Integer> notes = new ConcurrentLinkedDeque<>();
        
        // Fill up bag with random presents.
        for (Integer i : auxList) {
            bag.addLast(i);
        }
        // Don't need it anymore.
        auxList = null;

        // Ordered list of presents.
        PresentsList chainOfPresents = new PresentsList();    
        // Counter for "Thank you" notes.
        AtomicInteger cardsCount = new AtomicInteger();
        // To implement Minotaur's inquiry about a certain present.
        AtomicInteger request = new AtomicInteger();
        ServantThread [] servants = new ServantThread[4];

        for (int i = 0; i < 4; i++) {
            servants[i] = new ServantThread(bag, notes, chainOfPresents, cardsCount, request);
            new Thread(servants[i]).start();
        }
        
        while (true) {
            if (cardsCount.get() > PRESENTS_NUM - 1)
                break;

            boolean choice = ThreadLocalRandom.current().nextBoolean();

            // Request for specific value.
            if (choice)
                request.set(ThreadLocalRandom.current().nextInt(PRESENTS_NUM + 1));      
        }

        // Stopping threads.
        for (int i = 0; i < 4; i++) 
            servants[i].stopWorking();

        // Check if ordered list is empty.
        if (checkTheOrderedList(chainOfPresents, false) == 0)
            System.out.println("\nAll done! List is empty and all "
                               + cardsCount.get() + " cards have been sent.\n");
    }

    // Output contents of the chained list. Boolean variable verbose controls 
    // printing output to console. Returns number of elements in the list, not 
    // counting sentinel nodes.
    private static int checkTheOrderedList(PresentsList list, boolean verbose) {

        if (verbose)
            System.out.println("\nItems in the ordered list:\n");
        Node curr = list.head;
        int nodeCount = 0;

        while (true) {
            if (curr.itemNum == Integer.MAX_VALUE) {
                if (verbose) {
                    System.out.print(curr.itemNum + "-> ");
                    if (curr.next == null)
                        System.out.println("null");
                    else
                        System.out.println(curr.next.itemNum);
                } 
                break;
            }     
            if (verbose)
                System.out.print(curr.itemNum + "-> ");
            nodeCount++;
            curr = curr.next;
        }
        if (verbose)
            System.out.println("\nNum elements in the list = " + (nodeCount - 1));      
        return nodeCount - 1;
    }
}


// Implements servant scenario.
class ServantThread implements Runnable {
    private volatile boolean stop = false;
    ConcurrentLinkedDeque<Integer> bag, notes;
    PresentsList chainOfPresents;
    AtomicInteger cardsCount, request;
    
    ServantThread(ConcurrentLinkedDeque<Integer> bag,
                  ConcurrentLinkedDeque<Integer> notes, 
                  PresentsList chainOfPresents,
                  AtomicInteger cardsCount,
                  AtomicInteger request) {            
        this.bag = bag;
        this.notes = notes;
        this.chainOfPresents = chainOfPresents;
        this.cardsCount = cardsCount; 
        this.request = request;
    }

    public void run() {
        
        while (!stop) {     
            int reqNum = request.getAndSet(0);

            // Check for requested number.
            if (reqNum != 0) {
                chainOfPresents.contains(reqNum);
                continue;
            }  
            boolean choice = ThreadLocalRandom.current().nextBoolean();  
            Integer present;

            // Take present from the bag and add it to the list of presents.
            if (choice) {
                present = bag.pollFirst();
            
                if (present != null) {
                    chainOfPresents.add(present);
                    // Save its number in the notes to be able to get it later.
                    notes.addLast(present);
                }

            // Write "Thank you" note and removes first element from the list.
            } else {
                present = notes.pollFirst();

                if (present != null && chainOfPresents.remove(present)) {
                    cardsCount.addAndGet(1);
                }
            }
        }
    }

    public void stopWorking() {
        stop = true;
    }
}