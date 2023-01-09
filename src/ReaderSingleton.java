import com.mot.rfid.api3.RFIDReader;

public class ReaderSingleton {

    private static RFIDReader instance;

    public static synchronized RFIDReader getInstance() {
        if (instance == null) {
            instance = new RFIDReader();
        }
        return instance;
    }

}