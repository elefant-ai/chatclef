package adris.altoclef.player2api.LLM.Event;

import java.util.Optional;

import adris.altoclef.player2api.LLM.State.CharacterState;

public class CharacterChangeEvent extends Event {
    public final CharacterState oldCharacter;
    public final CharacterState newCharacter;

    public CharacterChangeEvent(CharacterState oldCharacter, CharacterState newCharacter){
        super(true);
        this.newCharacter = newCharacter;
        this.oldCharacter = oldCharacter;
    }

    @Override
    public Optional<Event[]> handle(){
        return Optional.empty();
    }

}
