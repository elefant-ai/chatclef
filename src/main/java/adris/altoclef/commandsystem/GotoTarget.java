package adris.altoclef.commandsystem;

import adris.altoclef.util.Dimension;

import java.util.ArrayList;
import java.util.List;

public class GotoTarget {
    private final int x;
    private final int y;
    private final int z;
    private final Dimension dimension;
    private final GotoTargetCoordType type;

    public GotoTarget(int x, int y, int z, Dimension dimension, GotoTargetCoordType type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.type = type;
    }

    public static GotoTarget parseRemainder(String line) throws CommandException {
        line = line.trim();
        if (line.startsWith("(") && line.endsWith(")")) {
            line = line.substring(1, line.length() - 1);
        }
        String[] parts = line.split(" ");
        List<Integer> numbers = new ArrayList<>();
        Dimension dimension = null;
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                numbers.add(num);
            } catch (NumberFormatException e) {
                dimension = (Dimension) Arg.parseEnum(part, Dimension.class);
                break;
            }
        }
        int x = 0, y = 0, z = 0;
        GotoTarget.GotoTargetCoordType coordType;
        switch (numbers.size()) {
            case 0 -> coordType = GotoTargetCoordType.NONE;
            case 1 -> {
                y = numbers.get(0);
                coordType = GotoTargetCoordType.Y;
            }
            case 2 -> {
                x = numbers.get(0);
                z = numbers.get(1);
                coordType = GotoTargetCoordType.XZ;
            }
            case 3 -> {
                x = numbers.get(0);
                y = numbers.get(1);
                z = numbers.get(2);
                coordType = GotoTargetCoordType.XYZ;
            }
            default ->
                    throw new CommandException("Unexpected number of integers passed to coordinate: " + numbers.size());
        }
        return new GotoTarget(x, y, z, dimension, coordType);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public boolean hasDimension() {
        return dimension != null;
    }

    public GotoTargetCoordType getType() {
        return type;
    }

    // Combination of types we can have
    public enum GotoTargetCoordType {
        XYZ, // [x, y, z]
        XZ,  // [x, z]
        Y,   // [y]
        NONE // []
    }
}
