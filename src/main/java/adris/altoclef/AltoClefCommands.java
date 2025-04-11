package adris.altoclef;

import adris.altoclef.commands.*;
import adris.altoclef.commands.random.ScanCommand;
import adris.altoclef.commands.random.DummyTaskCommand;
import adris.altoclef.commandsystem.CommandException;

/**
 * Initializes altoclef's built in commands.
 */
public class AltoClefCommands {

    public static void init() throws CommandException {
        // List commands here
        AltoClef.getCommandExecutor().registerNewCommand(
                new GetCommand(),
                new ListCommand(),
                new EquipCommand(),
                new DepositCommand(),
                new StashCommand(),
                new GotoCommand(),
                new IdleCommand(),
                new HeroCommand(),
                new StatusCommand(),
                new InventoryCommand(),
                new LocateStructureCommand(),
                new StopCommand(),
                new PauseCommand(),
                new UnPauseCommand(),
                new SetGammaCommand(),
                new FoodCommand(),
                new MeatCommand(),
                new ReloadSettingsCommand(),
                new GamerCommand(),
                new DummyTaskCommand(),
                new FollowCommand(),
                new GiveCommand(),
                new ScanCommand()
        );
    }
}
