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
        
        // Start sensors.
        for (int i = 0; i < NUM_OF_SENSORS; i++) {
            sensors[i] = new SensorThread(buffer);
            new Thread(sensors[i]).start();
        }
        // Start analyzer.
        new Thread(analyzer).start();

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
        analyzer.stopWorking();

        // printBuffer(buffer);
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


// Implements temperature analyzing unit that does all the data processing.
class AnalyzerThread implements Runnable {
    private volatile boolean stop = false;
    private volatile boolean process = false;
    private int [][] moduleMemory;
    DataBuffer buffer;

    public AnalyzerThread(DataBuffer buffer) {
        this.buffer = buffer;
        moduleMemory = new int[6][80];
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
    public static void analyzeTemperatures(DataBuffer buffer, int[][] moduleMemory) {
        // Copy data into the local memory.
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 80; j++) {
                try {
                    moduleMemory[i][j] = buffer.dequeue();
                } catch (EmptyBufferException ex) {}
            }
        }
        
        // For debugging.
        // System.out.println("\nModule memory:");
        // for (int i = 0; i < 6; i++) {
        //     for (int k = 0; k < 80; k++) {
        //         System.out.print(moduleMemory[i][k] + " ");
        //     }  
        // }
        // System.out.println();
    }

    public void stopWorking() {
        stop = true;
    }

    public void processData() {
        process = true;
    }
}