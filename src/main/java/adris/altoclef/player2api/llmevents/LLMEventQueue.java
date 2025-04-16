package adris.altoclef.player2api.llmevents;

import java.util.ArrayList;

public class LLMEventQueue {
    ArrayList<LLMEvent> eventList;
    int maxSize;
    public LLMEventQueue(int maxSize){
        eventList = new ArrayList<LLMEvent>();
        this.maxSize = maxSize;
    }

    public void add(LLMEvent event){
        eventList.add(event);
        if(eventList.size() > maxSize){
            eventList.removeFirst();
        }


    }

}
