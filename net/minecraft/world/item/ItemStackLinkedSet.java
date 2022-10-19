/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public final class ItemStackLinkedSet
extends ObjectLinkedOpenCustomHashSet<ItemStack> {
    private static final Hash.Strategy<? super ItemStack> STRATEGY = new Hash.Strategy<ItemStack>(){

        @Override
        public int hashCode(ItemStack itemStack) {
            if (itemStack != null) {
                return Objects.hash(itemStack.getItem(), itemStack.getTag());
            }
            return 0;
        }

        @Override
        public boolean equals(ItemStack itemStack, ItemStack itemStack2) {
            return itemStack == itemStack2 || itemStack != null && itemStack2 != null && ItemStack.matches(itemStack, itemStack2);
        }

        @Override
        public /* synthetic */ boolean equals(Object object, Object object2) {
            return this.equals((ItemStack)object, (ItemStack)object2);
        }

        @Override
        public /* synthetic */ int hashCode(Object object) {
            return this.hashCode((ItemStack)object);
        }
    };

    public ItemStackLinkedSet() {
        super(STRATEGY);
    }
}

