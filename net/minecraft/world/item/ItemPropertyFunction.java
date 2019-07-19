/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface ItemPropertyFunction {
    @Environment(value=EnvType.CLIENT)
    public float call(ItemStack var1, @Nullable Level var2, @Nullable LivingEntity var3);
}

