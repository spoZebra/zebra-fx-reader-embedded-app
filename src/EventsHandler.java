import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.mot.rfid.api3.*;

public class EventsHandler implements RfidEventsListener {

    MEMORY_BANK[] memoryBanksToRead;
    Short threshold = 1;

    // Need custom comparator as HashMap key is a custom object
    Comparator<TagData> tagDataComparator = new Comparator<TagData>() {
        @Override public int compare(TagData a, TagData b) {
            return a.getTagID().compareTo(b.getTagID()); // Compare by tag id
        }           
    };
    // HashMap to store RSSI changes within a tag
    ConcurrentHashMap<TagData, List<Short>> tagHashMap = new ConcurrentHashMap<>();

    public EventsHandler(MEMORY_BANK[] memoryBanksToRead) {
        this.memoryBanksToRead = memoryBanksToRead;
    }

    Integer count = 0;
    @Override
    public void eventReadNotify(RfidReadEvents e){
        try{
            count++;
            System.out.println(count.toString());

            TagData tag = e.getReadEventData().tagData;
            
            List<Short> rssiLevels = tagHashMap.putIfAbsent(tag, new ArrayList<>());
            Short lastRSSI = 0;

            if(rssiLevels != null && rssiLevels.size() > 0)
                lastRSSI = rssiLevels.get(rssiLevels.size() -1);

            Short newRSSI = tag.getPeakRSSI();

            // If RSSI is different, add it to the list (tag is moving!)
            if(newRSSI <= lastRSSI - threshold || newRSSI >= lastRSSI + threshold){
                tagHashMap.get(tag).add(tag.getPeakRSSI());
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void EvaluateMovingTags() {
        // Remove tags that has just one RSSI
        tagHashMap.entrySet().removeIf(x -> (x.getValue().size() == 1));

        // Print moving tags and its values
        for(Entry<TagData, List<Short>> entry : tagHashMap.entrySet()){

            TagData tag = entry.getKey();
            System.out.println("Tag ID " + tag.getTagID());
            System.out.println("\t RSSI: " + String.join(",", entry.getValue().stream().map(Object::toString).collect(Collectors.toList())));

            if (tag.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ && tag.getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)
            { 
                    // Add tag sequence to read specific memory banks
                for (MEMORY_BANK bank : memoryBanksToRead)
                {
                    String memoryBankData = tag.getMemoryBankData();
                        
                    if (memoryBankData.length() > 0)
                        System.out.println(" Mem Bank Data " + bank + " = "+ memoryBankData);
                }
            }
        }

        System.out.println("---> TOTAL Moving Tags: " + tagHashMap.size());
    }
    // Decomment to read just tag data
/*
    @Override
    public void eventReadNotify(RfidReadEvents e){

        TagData tag = e.getReadEventData().tagData;
        
        System.out.println("Tag ID " + tag.getTagID());

        if (tag.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ && tag.getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)
        { 
                // Add tag sequence to read specific memory banks
            for (MEMORY_BANK bank : memoryBanksToRead)
            {
                String memoryBankData = tag.getMemoryBankData();
                    
                if (memoryBankData.length() > 0)
                    System.out.println(" Mem Bank Data " + bank + " = "+ memoryBankData);
            }
        }
        System.out.println("---");
    }
    */
    @Override
    public void eventStatusNotify(RfidStatusEvents e) {
        System.out.println("Status Notification: " + e.StatusEventData.getStatusEventType());
    }

}
