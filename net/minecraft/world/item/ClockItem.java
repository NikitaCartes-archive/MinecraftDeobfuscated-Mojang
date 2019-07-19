/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemPropertyFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ClockItem
extends Item {
    public ClockItem(Item.Properties properties) {
        super(properties);
        this.addProperty(new ResourceLocation("time"), new ItemPropertyFunction(){
            @Environment(value=EnvType.CLIENT)
            private double rotation;
            @Environment(value=EnvType.CLIENT)
            private double rota;
            @Environment(value=EnvType.CLIENT)
            private long lastUpdateTick;

            @Override
            @Environment(value=EnvType.CLIENT)
            public float call(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
                Entity entity;
                boolean bl = livingEntity != null;
                Entity entity2 = entity = bl ? livingEntity : itemStack.getFrame();
                if (level == null && entity != null) {
                    level = entity.level;
                }
                if (level == null) {
                    return 0.0f;
                }
                double d = level.dimension.isNaturalDimension() ? (double)level.getTimeOfDay(1.0f) : Math.random();
                d = this.wobble(level, d);
                return (float)d;
            }

            @Environment(value=EnvType.CLIENT)
            private double wobble(Level level, double d) {
                if (level.getGameTime() != this.lastUpdateTick) {
                    this.lastUpdateTick = level.getGameTime();
                    double e = d - this.rotation;
                    e = Mth.positiveModulo(e + 0.5, 1.0) - 0.5;
                    this.rota += e * 0.1;
                    this.rota *= 0.9;
                    this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0);
                }
                return this.rotation;
            }
        });
    }
}

