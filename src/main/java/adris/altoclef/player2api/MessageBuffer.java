package adris.altoclef.player2api;
import java.util.ArrayList;

public class MessageBuffer {
    ArrayList<String> msgs;
    int maxSize;

    public MessageBuffer(int maxSize){
        msgs = new ArrayList<String>();
        this.maxSize = maxSize;
    }


    public void addMsg(String msg){
        msgs.add(msg);
        if(msgs.size() > maxSize){
            msgs.remove(0);
        }
    }
    private void dump(){
        this.msgs = new ArrayList<String>();
    }
    
    // dumps buffer and returns string 
    public String dumpAndGetString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(String msg : msgs){
            sb.append(String.format("\"%s\",", msg));
        }

        this.dump();
        return sb.append("]").toString();
    }
}
