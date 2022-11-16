/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemStackLinkedSet {
    private static final Hash.Strategy<? super ItemStack> TYPE_AND_TAG = new Hash.Strategy<ItemStack>(){

        @Override
        public int hashCode(@Nullable ItemStack itemStack) {
            return ItemStackLinkedSet.hashStackAndTag(itemStack);
        }

        @Override
        public boolean equals(@Nullable ItemStack itemStack, @Nullable ItemStack itemStack2) {
            return itemStack == itemStack2 || itemStack != null && itemStack2 != null && itemStack.isEmpty() == itemStack2.isEmpty() && ItemStack.isSameItemSameTags(itemStack, itemStack2);
        }

        @Override
        public /* synthetic */ boolean equals(@Nullable Object object, @Nullable Object object2) {
            return this.equals((ItemStack)object, (ItemStack)object2);
        }

        @Override
        public /* synthetic */ int hashCode(@Nullable Object object) {
            return this.hashCode((ItemStack)object);
        }
    };

    static int hashStackAndTag(@Nullable ItemStack itemStack) {
        if (itemStack != null) {
            CompoundTag compoundTag = itemStack.getTag();
            int i = 31 + itemStack.getItem().hashCode();
            return 31 * i + (compoundTag == null ? 0 : compoundTag.hashCode());
        }
        return 0;
    }

    public static Set<ItemStack> createTypeAndTagSet() {
        return new ObjectLinkedOpenCustomHashSet<ItemStack>(TYPE_AND_TAG);
    }
}

