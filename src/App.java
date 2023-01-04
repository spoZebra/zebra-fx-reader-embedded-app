import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.mot.rfid.api3.*;

public class App {

    RFIDReader myReader = null;

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

        App rfidBase;
        rfidBase = new App();
    }

    public App() {

        myReader = new RFIDReader();

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
                StopReading();
                myEventHandler.EvaluateMovingTags();

                myReader.disconnect();
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
            myReader.Actions.TagAccess.OperationSequence.add(op);
        }

        // Add RSSI filter to read just the tags within specific range
    /*    AccessFilter accessFilter = new AccessFilter();
        accessFilter.setRSSIRangeFilter( true);
        accessFilter.RssiRangeFilter.setMatchRange(MATCH_RANGE.WITHIN_RANGE);
        accessFilter.RssiRangeFilter.setPeakRSSILowerLimit((short)-40);
        accessFilter.RssiRangeFilter.setPeakRSSIUpperLimit((short)-10);
        
        myReader.Actions.TagAccess.OperationSequence.performSequence(accessFilter, null, null);
     */
        myReader.Actions.TagAccess.OperationSequence.performSequence(null, null, null);

        System.out.println("Press Enter to stop inventory");
    }

    private void StopReading() {
        try {
            myReader.Actions.TagAccess.OperationSequence.stopSequence();;
        } catch (Exception ioex) {
            System.out.println("IO Exception.Stopping inventory");
        }
    }

    public boolean connectToReader(String readerHostName, int readerPort) {

        boolean isConnected = false;
        hostName = readerHostName;
        port = readerPort;
        myReader.setHostName(hostName);
        myReader.setPort(port);

        System.out.println("Connecting to: " + myReader.getHostName());
        try {
            myReader.connect();

            System.out.println("OK..." + myReader.getHostName());
            // Register events
            myReader.Events.setInventoryStartEvent(true);
            myReader.Events.setInventoryStopEvent(true);
            myReader.Events.setAccessStartEvent(true);
            myReader.Events.setAccessStopEvent(true);
            myReader.Events.setAntennaEvent(true);
            myReader.Events.setGPIEvent(true);
            myReader.Events.setBufferFullEvent(true);
            myReader.Events.setBufferFullWarningEvent(true);
            myReader.Events.setReaderDisconnectEvent(true);
            myReader.Events.setReaderExceptionEvent(true);
            myReader.Events.setTagReadEvent(true);
            // This will trigger the read event per each tag
            myReader.Events.setAttachTagDataWithReadEvent(true); 
            
            TagStorageSettings tagStorageSettings = myReader.Config.getTagStorageSettings();
            TAG_FIELD[] tagField = new TAG_FIELD[3];
            tagField[0] = TAG_FIELD.PC;
            tagField[1] = TAG_FIELD.PEAK_RSSI;
            tagField[2] = TAG_FIELD.ANTENNA_ID;
            tagStorageSettings.setTagFields(tagField);
            myReader.Config.setTagStorageSettings(tagStorageSettings);

            // Event handler registration
            myReader.Events.addEventsListener(myEventHandler);

            isConnected = true;
            System.out.println("Connected to " + hostName);
            myReader.Config.setTraceLevel(TRACE_LEVEL.TRACE_LEVEL_ALL);

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