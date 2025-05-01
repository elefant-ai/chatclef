package adris.altoclef.commands.random;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.commands.BlockScanner;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.util.helpers.FuzzySearchHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class ScanCommand extends Command {

    public ScanCommand() throws CommandException {
        super("scan", "Locates nearest block", new Arg<>(String.class, "block", "DIRT", 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String blockStr = parser.get(String.class);

        Field[] declaredFields = Blocks.class.getDeclaredFields();
        Block block = null;

        List<String> allBlockNames = new ArrayList<>();

        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                String fieldName = field.getName();
                allBlockNames.add(fieldName.toLowerCase());
                if (fieldName.equalsIgnoreCase(blockStr)) {
                    block = (Block) field.get(Blocks.class);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            field.setAccessible(false);
        }

        if (block == null) {
            String closest = FuzzySearchHelper.getClosestMatchMinecraftItems(blockStr, allBlockNames);
            mod.log("Block named: \"" + blockStr + "\" not a valid block. Perhaps the user meant \"" + closest + "\"?");
            finish();
            return;
        }

        BlockScanner blockScanner = mod.getBlockScanner();
        Optional<BlockPos> p = blockScanner.getNearestBlock(block,mod.getPlayer().getPos());
        if (p.isPresent()) {
            mod.log("Closest " + blockStr + ": " + p.get().toString());
        } else {
            mod.log("No blocks of type " + blockStr + " found nearby.");
        }
        finish();
    }

}
