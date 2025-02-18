package xyz.wagyourtail.jsmacros.client.tick;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.client.api.event.impl.*;
import xyz.wagyourtail.jsmacros.client.api.library.impl.FClient;

public class TickBasedEvents {
    private static boolean initialized = false;
    private static ItemStack mainHand = ItemStack.EMPTY;
    private static ItemStack offHand = ItemStack.EMPTY;

    private static ItemStack footArmor = ItemStack.EMPTY;
    private static ItemStack legArmor = ItemStack.EMPTY;
    private static ItemStack chestArmor = ItemStack.EMPTY;
    private static ItemStack headArmor = ItemStack.EMPTY;
    private static boolean previousFallFlyState = false;



    public static final MultiplayerServerListPinger serverListPinger = new MultiplayerServerListPinger();


    public static boolean areNotEqual(ItemStack a, ItemStack b) {
        return (!a.isEmpty() || !b.isEmpty()) && (a.isEmpty() || b.isEmpty() || !a.isItemEqualIgnoreDamage(b) || a.getCount() != b.getCount() || !ItemStack.areNbtEqual(a, b) || a.getDamage() != b.getDamage());
    }
    
    public static boolean areTagsEqualIgnoreDamage(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        } else if (!a.isEmpty() && !b.isEmpty()) {
            if (a.getNbt() == null && b.getNbt() == null) {
                return true;
            } else {
                NbtCompound at;
                NbtCompound bt;
                if (a.getNbt() != null) at = a.getNbt().copy();
                else at = new NbtCompound();
                if (b.getNbt() != null) bt = b.getNbt().copy();
                else bt = new NbtCompound();
                at.remove("Damage");
                bt.remove("Damage");
                return at.equals(bt);
            }
            
        } else {
            return false;
        }
    }
    
    public static boolean areEqualIgnoreDamage(ItemStack a, ItemStack b) {
        return (a.isEmpty() && b.isEmpty()) || (!a.isEmpty() && !b.isEmpty() && a.isItemEqualIgnoreDamage(b) && a.getCount() == b.getCount() && areTagsEqualIgnoreDamage(a, b));    
    }
    
    public static void onTick(MinecraftClient mc) {
        if (JsMacros.keyBinding.wasPressed() && mc.currentScreen == null) {
            mc.setScreen(JsMacros.prevScreen);
        }

        FClient.tickSynchronizer.tick();
        serverListPinger.tick();

        new EventTick();
        new EventJoinedTick();

        if (mc.player != null) {
            boolean state = mc.player.isFallFlying();
            if (previousFallFlyState ^ state) {
                new EventFallFlying(state);
                previousFallFlyState = state;
            }
        }

        if (mc.player != null && mc.player.getInventory() != null) {
            PlayerInventory inv = mc.player.getInventory();

            ItemStack newMainHand = inv.getMainHandStack();
            if (areNotEqual(newMainHand, mainHand)) {
                if (areEqualIgnoreDamage(newMainHand, mainHand)) {
                    new EventItemDamage(newMainHand, newMainHand.getDamage());
                }
                new EventHeldItemChange(newMainHand, mainHand, false);
                mainHand = newMainHand.copy();
            }

            ItemStack newOffHand = inv.offHand.get(0);
            if (areNotEqual(newOffHand, offHand)) {
                if (areEqualIgnoreDamage(newOffHand, offHand)) {
                    new EventItemDamage(newOffHand, newOffHand.getDamage());
                }
                new EventHeldItemChange(newOffHand, offHand, true);
                offHand = newOffHand.copy();
            }

            ItemStack newHeadArmor = inv.getArmorStack(3);
            if (areNotEqual(newHeadArmor, headArmor)) {
                if (areEqualIgnoreDamage(newHeadArmor, headArmor)) {
                    new EventItemDamage(newHeadArmor, newHeadArmor.getDamage());
                }
                new EventArmorChange("HEAD", newHeadArmor, headArmor);
                headArmor = newHeadArmor.copy();
            }

            ItemStack newChestArmor = inv.getArmorStack(2);
            if (areNotEqual(newChestArmor, chestArmor)) {
                if (areEqualIgnoreDamage(newChestArmor, chestArmor)) {
                    new EventItemDamage(newChestArmor, newChestArmor.getDamage());
                }
                new EventArmorChange("CHEST", newChestArmor, chestArmor);
                chestArmor = newChestArmor.copy();

            }

            ItemStack newLegArmor = inv.getArmorStack(1);
            if (areNotEqual(newLegArmor, legArmor)) {
                if (areEqualIgnoreDamage(newLegArmor, legArmor)) {
                    new EventItemDamage(newLegArmor, newLegArmor.getDamage());
                }
                new EventArmorChange("LEGS", newLegArmor, legArmor);
                legArmor = newLegArmor.copy();
            }

            ItemStack newFootArmor = inv.getArmorStack(0);
            if (areNotEqual(newFootArmor, footArmor)) {
                if (areEqualIgnoreDamage(newFootArmor, footArmor)) {
                    new EventItemDamage(newFootArmor, newFootArmor.getDamage());
                }
                new EventArmorChange("FEET", newFootArmor, footArmor);
                footArmor = newFootArmor.copy();
            }
        }
    }
}
