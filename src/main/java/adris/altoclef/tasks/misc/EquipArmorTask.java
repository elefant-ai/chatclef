package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.slot.MoveItemToSlotFromInventoryTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class EquipArmorTask extends Task {

    private final ItemTarget[] toEquip;

    public EquipArmorTask(ItemTarget... toEquip) {
        this.toEquip = toEquip;
    }

    public EquipArmorTask(Item... toEquip) {
        this(Arrays.stream(toEquip).map(ItemTarget::new).toArray(ItemTarget[]::new));
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected Task onTick() {
        ItemTarget[] armorsNotEquipped = Arrays.stream(toEquip).filter(target -> !StorageHelper.itemTargetsMetInventory(target) && !StorageHelper.isArmorEquipped(target.getMatches())).toArray(ItemTarget[]::new);
        boolean armorMet = armorsNotEquipped.length == 0;
        if (!armorMet) {
            setDebugState("Obtaining armor");
            return new CataloguedResourceTask(armorsNotEquipped);
        }

        setDebugState("Equipping armor");
        AltoClef mod = AltoClef.getInstance();

        // Now equip
        for (ItemTarget targetArmor : toEquip) {
            Item[] targetArmorMatches = targetArmor.getMatches();
            if (Arrays.stream(targetArmorMatches).toList().contains(Items.SHIELD)) {
                ShieldItem shield = (ShieldItem) Objects.requireNonNull(targetArmor.getMatches())[0];
                if (shield == null) {
                    Debug.logWarning("Item " + targetArmor + " is not armor! Will not equip.");
                } else {
                    if (!StorageHelper.isArmorEquipped(shield)) {
                        if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
                            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
                            if (!cursorStack.isEmpty()) {
                                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                                if (moveTo.isPresent()) {
                                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                                // Try throwing away cursor slot if it's garbage
                                if (garbage.isPresent()) {
                                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                            } else {
                                StorageHelper.closeScreen();
                            }
                        }
                        Slot toMove = PlayerSlot.getEquipSlot(EquipmentSlot.OFFHAND);
                        if (toMove == null) {
                            Debug.logWarning("Invalid armor equip slot for item " + shield.getTranslationKey());
                        }
                        return new MoveItemToSlotFromInventoryTask(targetArmor, toMove);
                    }
                }
            } else {
                ArmorItem item = (ArmorItem) Objects.requireNonNull(targetArmor.getMatches())[0];
                if (item == null) {
                    Debug.logWarning("Item " + targetArmor + " is not armor! Will not equip.");
                } else {
                    if (!StorageHelper.isArmorEquipped(item)) {
                        if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
                            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
                            if (!cursorStack.isEmpty()) {
                                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                                if (moveTo.isPresent()) {
                                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                                // Try throwing away cursor slot if it's garbage
                                if (garbage.isPresent()) {
                                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                            } else {
                                StorageHelper.closeScreen();
                            }
                        }
                        Slot toMove = PlayerSlot.getEquipSlot(item.getSlotType());
                        if (toMove == null) {
                            Debug.logWarning("Invalid armor equip slot for item " + item.getTranslationKey() + ": " + item.getSlotType());
                        }
                        return new MoveItemToSlotFromInventoryTask(targetArmor, toMove);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean isFinished() {
        return armorEquipped();
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof EquipArmorTask task) {
            return Arrays.equals(task.toEquip, toEquip);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Equipping armor " + ArrayUtils.toString(toEquip);
    }

    private boolean armorTestAll(Predicate<Item> armorSatisfies) {
        // If ALL item target has any match that is equipped...
        return Arrays.stream(toEquip).allMatch(
                target -> Arrays.stream(target.getMatches()).anyMatch(armorSatisfies)
        );
    }

    public boolean armorEquipped() {
        return armorTestAll(item -> StorageHelper.isArmorEquipped(item));
    }

}
