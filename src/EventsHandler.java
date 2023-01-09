import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mot.rfid.api3.*;

public class EventsHandler implements RfidEventsListener {

    MEMORY_BANK[] memoryBanksToRead;
    Short threshold = 2; // RSSI Delta

    // HashMap to store RSSI changes within a tag
    Map<String, MyTagData> tagHashMap = new TreeMap<>();

    public EventsHandler(MEMORY_BANK[] memoryBanksToRead) {
        this.memoryBanksToRead = memoryBanksToRead;
    }

    Integer count = 0;
    @Override
    public void eventReadNotify(RfidReadEvents e){
        TagDataArray tagArray = ReaderSingleton.getInstance().Actions.getReadTagsEx(100);

        if(tagArray != null)
        {
            for(TagData tag : tagArray.getTags())
                ParseTag(tag);
        }
    }
    
    private void ParseTag(TagData tag){
        try {
            String tagId = tag.getTagID();

            if(tagId == null)
                return;

            MyTagData myTagData = tagHashMap.putIfAbsent(tagId, new MyTagData());
                
            if(myTagData == null)
                myTagData = new MyTagData();
            
                
            if (tag.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ && tag.getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)
            {
                String memoryBankData = tag.getMemoryBankData();
                MEMORY_BANK readMemoryBank = tag.getMemoryBank();
                
                if(memoryBankData.length() > 0){ 
                    if(readMemoryBank.ordinal == MEMORY_BANK.MEMORY_BANK_EPC.ordinal)
                        myTagData.epc = memoryBankData;
                    else if(readMemoryBank.ordinal == MEMORY_BANK.MEMORY_BANK_USER.ordinal)
                        myTagData.user = memoryBankData;
                }
                
                // TID
                myTagData.tid = tagId;
            }

            Short lastRSSI = 0;

            if(myTagData.rssiLevels.size() > 0)
                lastRSSI = myTagData.rssiLevels.get(myTagData.rssiLevels.size() -1);

            Short newRSSI = tag.getPeakRSSI();

            // If RSSI is different, add it to the list (tag is moving!)
            if(newRSSI < lastRSSI - threshold || newRSSI > lastRSSI + threshold){
                tagHashMap.get(tag.getTagID()).rssiLevels.add(newRSSI);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();;
        }  
    }

    public void EvaluateMovingTags() {
        // Remove tags that has just one RSSI
        tagHashMap.entrySet().removeIf(x -> (x.getValue().rssiLevels.size() == 1));

        // Print moving tags and its values
        for(Entry<String, MyTagData> entry : tagHashMap.entrySet()){

            System.out.println("EPC: " + entry.getValue().epc);
            System.out.println("TID: " + entry.getValue().tid);
            System.out.println("User: " + entry.getValue().user);
            System.out.println("\t RSSI: " + String.join(",", entry.getValue().rssiLevels.stream().map(String::valueOf).collect(Collectors.toList())));
            
            System.out.println("---------------");
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
