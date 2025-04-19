package adris.altoclef;

import adris.altoclef.commands.AttackPlayerOrMobCommand;
import adris.altoclef.commands.DepositCommand;
import adris.altoclef.commands.EquipCommand;
import adris.altoclef.commands.FollowCommand;
import adris.altoclef.commands.FoodCommand;
import adris.altoclef.commands.GamerCommand;
import adris.altoclef.commands.GetCommand;
import adris.altoclef.commands.GiveCommand;
import adris.altoclef.commands.GotoCommand;
import adris.altoclef.commands.HeroCommand;
import adris.altoclef.commands.IdleCommand;
import adris.altoclef.commands.LocateStructureCommand;
import adris.altoclef.commands.MeatCommand;
import adris.altoclef.commands.PauseCommand;
import adris.altoclef.commands.ReloadSettingsCommand;
import adris.altoclef.commands.SetAIBridgeEnabledCommand;
import adris.altoclef.commands.SetGammaCommand;
import adris.altoclef.commands.StashCommand;
import adris.altoclef.commands.StopCommand;
import adris.altoclef.commands.UnPauseCommand;
import adris.altoclef.commands.random.ScanCommand;
import adris.altoclef.commandsystem.CommandException;

/**
 * Initializes altoclef's built in commands.
 */
public class AltoClefCommands {

    public static void init() throws CommandException {
        // List commands here
        AltoClef.getCommandExecutor().registerNewCommand(
                new GetCommand(),
                new EquipCommand(),
                new DepositCommand(),
                new StashCommand(),
                new GotoCommand(),
                new IdleCommand(),
                new HeroCommand(),
                new LocateStructureCommand(),
                new StopCommand(),
                new PauseCommand(),
                new UnPauseCommand(),
                new SetGammaCommand(),
                new FoodCommand(),
                new MeatCommand(),
                new ReloadSettingsCommand(),
                new GamerCommand(),
                new FollowCommand(),
                new GiveCommand(),
                new ScanCommand(),
                new AttackPlayerOrMobCommand(),
                new SetAIBridgeEnabledCommand()
        );
    }
}
