import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import com.mot.rfid.api3.*;

public class App {

    String hostName = null;
    Integer port = null;

    BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    MEMORY_BANK[] memoryBanksToRead = {
        MEMORY_BANK.MEMORY_BANK_EPC,
        MEMORY_BANK.MEMORY_BANK_TID,
        MEMORY_BANK.MEMORY_BANK_USER
    };

    EventsHandler myEventHandler = new EventsHandler(memoryBanksToRead);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Ready");
        System.setProperty("java.library.path", ":/platform/lib:/apps:/usr/java/packages/lib/arm:/lib:/usr/lib");
        System.out.println(System.getProperty("java.library.path"));
        System.loadLibrary( "rfidapi32jni" );
        App rfidBase;
        rfidBase = new App();
    }

    public App() {

        System.out.println("Connecting...");
        connectToReader("127.0.0.1", 5084);

        Boolean keepWorking = true;

        while (keepWorking) {
            try {
                try {
                    StartReading();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException ex) {
                    System.out.println(ex.getResults());
                }

                inputReader.readLine();
                System.out.println("Stopping...");
                StopReading();
                System.out.println("Evaluating tags...");
                myEventHandler.EvaluateMovingTags();

                ReaderSingleton.getInstance().disconnect();
                keepWorking = false;
            }catch (Exception ex) {
                System.out.println(ex.getMessage());
            } finally {
                System.exit(0);
            }
        }

    }
    
    private void StartReading() throws InterruptedException, InvalidUsageException, OperationFailureException {


        // Add tag sequence to read specific memory banks
        for (MEMORY_BANK bank : memoryBanksToRead)
        {
            TagAccess ta = new TagAccess();
            TagAccess.Sequence opSequence = ta.new Sequence(ta);
            TagAccess.Sequence.Operation op = opSequence.new Operation();
            op.setAccessOperationCode(ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ);
            op.WriteAccessParams.setMemoryBank(bank);
            ReaderSingleton.getInstance().Actions.TagAccess.OperationSequence.add(op);
        }

        // Add RSSI filter to read just the tags within specific range
    /*    AccessFilter accessFilter = new AccessFilter();
        accessFilter.setRSSIRangeFilter( true);
        accessFilter.RssiRangeFilter.setMatchRange(MATCH_RANGE.WITHIN_RANGE);
        accessFilter.RssiRangeFilter.setPeakRSSILowerLimit((short)-40);
        accessFilter.RssiRangeFilter.setPeakRSSIUpperLimit((short)-10);
        
        ReaderSingleton.getInstance().Actions.TagAccess.OperationSequence.performSequence(accessFilter, null, null);
     */
        ReaderSingleton.getInstance().Actions.TagAccess.OperationSequence.performSequence(null, null, null);

        System.out.println("Press Enter to stop reading tags");
    }

    private void StopReading() {
        try {
            ReaderSingleton.getInstance().Actions.TagAccess.OperationSequence.stopSequence();;
        } catch (Exception ioex) {
            System.out.println("IO Exception.Stopping the sequence");
        }
    }

    public boolean connectToReader(String readerHostName, int readerPort) {

        boolean isConnected = false;
        hostName = readerHostName;
        port = readerPort;
        ReaderSingleton.getInstance().setHostName(hostName);
        ReaderSingleton.getInstance().setPort(port);

        System.out.println("Connecting to: " + ReaderSingleton.getInstance().getHostName());
        try {
            ReaderSingleton.getInstance().connect();

            System.out.println("OK..." + ReaderSingleton.getInstance().getHostName());
            // Register events
            ReaderSingleton.getInstance().Events.setAccessStartEvent(true);
            ReaderSingleton.getInstance().Events.setAccessStopEvent(true);
            ReaderSingleton.getInstance().Events.setAntennaEvent(true);
            ReaderSingleton.getInstance().Events.setGPIEvent(true);
            ReaderSingleton.getInstance().Events.setBufferFullEvent(true);
            ReaderSingleton.getInstance().Events.setBufferFullWarningEvent(true);
            ReaderSingleton.getInstance().Events.setReaderDisconnectEvent(true);
            ReaderSingleton.getInstance().Events.setReaderExceptionEvent(true);
            ReaderSingleton.getInstance().Events.setTagReadEvent(true);
            // Group read events (better performance!)
            ReaderSingleton.getInstance().Events.setAttachTagDataWithReadEvent(false); 

            // Event handler registration
            ReaderSingleton.getInstance().Events.addEventsListener(myEventHandler);

            isConnected = true;
            System.out.println("Connected to " + hostName);

        } catch (InvalidUsageException ex) {
            System.out.println("Unable to connect: " + ex.getVendorMessage());
            ex.printStackTrace();
        } catch (OperationFailureException ex) {
            System.out.println(ex.getStatusDescription() + ex.getVendorMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        return isConnected;

    }
}