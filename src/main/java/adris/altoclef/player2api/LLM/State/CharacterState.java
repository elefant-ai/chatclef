package adris.altoclef.player2api.LLM.State;

import java.util.Arrays;
import java.util.Optional;

import adris.altoclef.player2api.LLM.Event.Event;

public class CharacterState implements AtomicState{
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
                "Character{name='%s', greeting='%s', voiceIds=%s}",
                name,
                greetingInfo,
                Arrays.toString(voiceIds));
    } 

    @Override
    public Optional<Event> onChange(AtomicState oldState, AtomicState newState) {
        return Optional.empty();
    }
    
}
