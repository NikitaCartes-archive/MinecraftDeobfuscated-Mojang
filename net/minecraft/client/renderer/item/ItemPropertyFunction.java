/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Deprecated
@Environment(value=EnvType.CLIENT)
public interface ItemPropertyFunction {
    public float call(ItemStack var1, @Nullable ClientLevel var2, @Nullable LivingEntity var3, int var4);
}

