/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface SlotAccess {
    public static final SlotAccess NULL = new SlotAccess(){

        @Override
        public ItemStack get() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean set(ItemStack itemStack) {
            return false;
        }
    };

    public static SlotAccess forContainer(final Container container, final int i, final Predicate<ItemStack> predicate) {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return container.getItem(i);
            }

            @Override
            public boolean set(ItemStack itemStack) {
                if (!predicate.test(itemStack)) {
                    return false;
                }
                container.setItem(i, itemStack);
                return true;
            }
        };
    }

    public static SlotAccess forContainer(Container container, int i) {
        return SlotAccess.forContainer(container, i, itemStack -> true);
    }

    public static SlotAccess forEquipmentSlot(final LivingEntity livingEntity, final EquipmentSlot equipmentSlot, final Predicate<ItemStack> predicate) {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return livingEntity.getItemBySlot(equipmentSlot);
            }

            @Override
            public boolean set(ItemStack itemStack) {
                if (!predicate.test(itemStack)) {
                    return false;
                }
                livingEntity.setItemSlot(equipmentSlot, itemStack);
                return true;
            }
        };
    }

    public static SlotAccess forEquipmentSlot(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        return SlotAccess.forEquipmentSlot(livingEntity, equipmentSlot, itemStack -> true);
    }

    public ItemStack get();

    public boolean set(ItemStack var1);
}

