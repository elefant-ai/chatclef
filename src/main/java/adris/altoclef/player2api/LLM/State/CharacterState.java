package adris.altoclef.player2api.LLM.State;

import java.util.Arrays;
import java.util.List;

import adris.altoclef.player2api.LLM.Event.CharacterChangeEvent;
import adris.altoclef.player2api.LLM.Event.Event;

public class CharacterState extends AtomicState<CharacterState>{
    public final String name;
    public final String greetingInfo;
    public final String description;
    public final String[] voiceIds;

    public CharacterState(String characterName, String greetingInfo, String description, String[] voiceIds) {
        this.name = characterName;
        this.greetingInfo = greetingInfo;
        this.voiceIds = voiceIds;
        this.description = description;
    }


    @Override
    public String getSummary() {
        return String.format(
                "{name='%s',\ngreeting='%s',\nvoiceIds=%s}",
                name,
                greetingInfo,
                Arrays.toString(voiceIds));
    } 

    @Override
    public List<Event> onChange(CharacterState oldState, CharacterState newState) {
        CharacterChangeEvent a = new CharacterChangeEvent(oldState, newState);
        return (List.of(a));
    }
    
}
