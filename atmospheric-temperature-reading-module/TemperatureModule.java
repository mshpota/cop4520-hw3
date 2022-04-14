import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;


// Implements temperature module central control unit.
public class TemperatureModule {
    private static final int TIME_IN_MINS = 60;
    private static final int NUM_OF_SENSORS = 8;
    private static final int SLEEP_TIME_IN_MS = 17;

    public static void main(String [] args) {

        DataBuffer buffer = new DataBuffer();
        SensorThread [] sensors = new SensorThread[NUM_OF_SENSORS];
        AnalyzerThread analyzer = new AnalyzerThread(buffer);
        Thread analyzerTh = new Thread(analyzer);
        
        // Start sensors.
        for (int i = 0; i < NUM_OF_SENSORS; i++) {
            sensors[i] = new SensorThread(buffer);
            new Thread(sensors[i]).start();
        }
        // Start analyzer.
        analyzerTh.start();

        // Minute timer.
        for (int i = 1; i < TIME_IN_MINS+1; i++) {    
            // One hour passed.
            if (i % 60 == 0) { 
                // Signal the analyzer to start processing data.
                analyzer.processData();
            }

            try {
                Thread.sleep(SLEEP_TIME_IN_MS);
            } catch (InterruptedException ex) {}   
        }

        // Stopping threads.
        for (int i = 0; i < NUM_OF_SENSORS; i++) 
            sensors[i].stopWorking();
        
        try {
            analyzer.stopWorking();
            analyzerTh.join();
        } catch (InterruptedException ex) {}
    }

    // For debugging.
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


// Implements temperature sensor that takes temperature 
// readings every minute and push result into the buffer.
class SensorThread implements Runnable {
    private static final int SLEEP_TIME_IN_MS = 17;
    private volatile boolean stop = false;
    private DataBuffer buffer;

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


// Implements temperature analyzing unit that does all the data processing.
class AnalyzerThread implements Runnable {
    private volatile boolean stop = false;
    private volatile boolean process = false;
    private DataBuffer buffer;
    private int [] moduleMemory;

    public AnalyzerThread(DataBuffer buffer) {
        this.buffer = buffer;
        moduleMemory = new int[480];
    }

    public void run() {

        while (!stop) {
            // Spin and wait for a signal from main module.  
            while (!process && !stop) {}

            if (stop) break;

            analyzeTemperatures(buffer, moduleMemory);
            process = false;
        }
    }

    // Function that takes its input from buffer and copies it into 
    // the local memory. It then processes the data and outputs the 
    // results according to assignment instructions.
    public void analyzeTemperatures(DataBuffer buffer, int[] mem) {
        // Flush memory.
        for (int i = 0; i < 480; i++) 
            mem[i] = Integer.MIN_VALUE;

        // Copy data into the local memory.
        for (int i = 0; i < 480; i++) {
            try {
                mem[i] = buffer.dequeue();
            } catch (EmptyBufferException ex) {}
        }

        // Find largest difference on 10 min interval.
        int largestDiff = -1;
        int idx = -1;

        for (int i = 0; i < 480; i += 80) {
            Arrays.sort(mem, i, i + 80);

            int diff = Math.abs(mem[i+79] - mem[i]);
            if (diff > largestDiff) {
                largestDiff = diff;
                idx = i;
            }
        }

        // For debugging.
        // System.out.println("\nModule memory:");
        // for (int i = 0; i < 480; i++) 
        //     System.out.print(moduleMemory[i] + " ");
        // System.out.println();

        System.out.print("Largest temperature difference = " + largestDiff);
        System.out.println(", on interval from " + (idx / 80 * 10) + 
                           " to " + (idx / 80 * 10 + 10) + " mins");

        Arrays.sort(mem, 0, 480);
        System.out.print("Five highest temperatures: ");
        for (int i = 479; i > 473; i--)
            System.out.print(mem[i] + " ");
        System.out.println();

        System.out.print("Five lowest temperatures: ");
        for (int i = 0; i < 5; i++)
            System.out.print(mem[i] + " ");
        System.out.println();
    }

    public void stopWorking() {
        stop = true;
    }

    public void processData() {
        process = true;
    }
}