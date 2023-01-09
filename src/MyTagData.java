import java.util.ArrayList;
import java.util.List;

public class MyTagData {
    public String epc;
    public String tid;
    public String user;
    List<Short> rssiLevels = new ArrayList<>();

    public MyTagData() {
    }

    public MyTagData(String epc, String tid, String user) {
        this.epc = epc;
        this.tid = tid;
        this.user = user;
    }

}
