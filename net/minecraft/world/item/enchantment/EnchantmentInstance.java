/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentInstance
extends WeightedEntry.IntrusiveBase {
    public final Enchantment enchantment;
    public final int level;

    public EnchantmentInstance(Enchantment enchantment, int i) {
        super(enchantment.getRarity().getWeight());
        this.enchantment = enchantment;
        this.level = i;
    }
}

