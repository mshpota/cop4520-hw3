import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;


public class TemperatureModule {
    private static final int TIME_IN_MINS = 60;
    private static final int NUM_OF_SENSORS = 8;
    private static final int SLEEP_TIME_IN_MS = 100;

    public static void main(String [] args) {

        DataBuffer buffer = new DataBuffer();
        SensorThread [] sensors = new SensorThread[NUM_OF_SENSORS];

        for (int i = 0; i < NUM_OF_SENSORS; i++) {
            sensors[i] = new SensorThread(buffer);
            new Thread(sensors[i]).start();
        }

        for (int i = 0; i < TIME_IN_MINS; i++) {
            try {
                System.out.print(buffer.dequeue() + " ");
            } catch (EmptyBufferException ex) {
                System.out.println("Oops, Empty.");
            }
            
            try {
                Thread.sleep(SLEEP_TIME_IN_MS);
            } catch (InterruptedException ex) {}
        }

        System.out.println();

        // Stopping threads.
        for (int i = 0; i < NUM_OF_SENSORS; i++) 
            sensors[i].stopWorking();

       

        printBuffer(buffer);
    }


    public static void printBuffer(DataBuffer buffer) {
        if (buffer == null) return;
        int count = -1;
        if (buffer.head.get().next == null)
            System.out.println("Buffer empty.");

        Node curr = buffer.head.get();

        System.out.print("\nItems in the buffer:\n");

        while (curr != null) {
            System.out.print(curr.temperature + " ");
            curr = curr.next.get();
            count++;
        }
        System.out.println();
        System.out.println("\nNumber of elements in the buffer = " + count);
    }
}


// Implements temperature sensor that does
class SensorThread implements Runnable {
    private static final int SLEEP_TIME_IN_MS = 100;
    private volatile boolean stop = false;
    DataBuffer buffer;

    public SensorThread(DataBuffer buffer) {
        this.buffer = buffer;
    }

    public void run() {

        while (!stop) {  
            int reading = ThreadLocalRandom.current().nextInt(-100, 71); 
            buffer.enqueue(reading);

            try {
                Thread.sleep(SLEEP_TIME_IN_MS);
            } catch (InterruptedException ex) {}
            
        }
    }

    public void stopWorking() {
        stop = true;
    }
}