package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.time.TimerGame;

public class BodyLanguageTask extends Task{
    public int phase = 0;
    private final TimerGame timerGame = new TimerGame(1);
    private String type;
    private int count;
    private boolean isDone = false;

    public BodyLanguageTask(String type, int count){
        this.type = type;
        this.count = count;
    }


    @Override
    public boolean isFinished() {
        return isDone;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BodyLanguageTask && ((BodyLanguageTask)other).phase == phase;
    }

    @Override
    protected void onStart() {
        AltoClefController mod = this.controller;
        mod.getEntity().setShiftKeyDown(true);
    }

    @Override
    protected void onStop(Task var1) {
    }

    private Task onTickGreeting(AltoClefController mod){
        if(phase > 2){
            return null; 
        }
        if(timerGame.elapsed()){
            phase += 1;
            timerGame.reset();
            mod.getEntity().setShiftKeyDown(!mod.getEntity().isShiftKeyDown());
        }
        isDone = true;
        return null;
    }

    private Task onTickNodHead(AltoClefController mod){
        
    }
    private Task onTickShakeHead(AltoClefController mod){

    }
    private Task onTickVictoryDance(AltoClefController mod){

    }

    @Override
    protected Task onTick() {
        AltoClefController mod = this.controller;
        switch(type){
            case "greeting":
                return onTickGreeting(mod);
            case "nodHead":
                return onTickNodHead(mod);
            case "shakeHead":
                return onTickShakeHead(mod);
            case "victoryDance":
                return onTickVictoryDance(mod);
        }
    }
    @Override
    protected String toDebugString() {
        return "GreetingCrouch";
    }
}