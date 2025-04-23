package adris.altoclef.player2api.LLM.Event;

import java.util.List;
import java.util.Optional;

import adris.altoclef.player2api.LLM.State.CharacterState;
import adris.altoclef.player2api.LLM.State.LLMState;

public class CharacterChangeEvent extends Event {
    public final CharacterState oldCharacter;
    public final CharacterState newCharacter;

    public CharacterChangeEvent(CharacterState oldCharacter, CharacterState newCharacter){
        super(true);
        this.newCharacter = newCharacter;
        this.oldCharacter = oldCharacter;
    }

    @Override
    public List<Event> handle(LLMState state){
        return List.of(); 
    }

}
