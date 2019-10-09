/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemPropertyFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

public class CompassItem
extends Item {
    public CompassItem(Item.Properties properties) {
        super(properties);
        this.addProperty(new ResourceLocation("angle"), new ItemPropertyFunction(){
            @Environment(value=EnvType.CLIENT)
            private double rotation;
            @Environment(value=EnvType.CLIENT)
            private double rota;
            @Environment(value=EnvType.CLIENT)
            private long lastUpdateTick;

            @Override
            @Environment(value=EnvType.CLIENT)
            public float call(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
                double f;
                Entity entity;
                if (livingEntity == null && !itemStack.isFramed()) {
                    return 0.0f;
                }
                boolean bl = livingEntity != null;
                Entity entity2 = entity = bl ? livingEntity : itemStack.getFrame();
                if (level == null) {
                    level = entity.level;
                }
                if (level.dimension.isNaturalDimension()) {
                    double d = bl ? (double)entity.yRot : this.getFrameRotation((ItemFrame)entity);
                    d = Mth.positiveModulo(d / 360.0, 1.0);
                    double e = this.getSpawnToAngle(level, entity) / 6.2831854820251465;
                    f = 0.5 - (d - 0.25 - e);
                } else {
                    f = Math.random();
                }
                if (bl) {
                    f = this.wobble(level, f);
                }
                return Mth.positiveModulo((float)f, 1.0f);
            }

            @Environment(value=EnvType.CLIENT)
            private double wobble(Level level, double d) {
                if (level.getGameTime() != this.lastUpdateTick) {
                    this.lastUpdateTick = level.getGameTime();
                    double e = d - this.rotation;
                    e = Mth.positiveModulo(e + 0.5, 1.0) - 0.5;
                    this.rota += e * 0.1;
                    this.rota *= 0.8;
                    this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0);
                }
                return this.rotation;
            }

            @Environment(value=EnvType.CLIENT)
            private double getFrameRotation(ItemFrame itemFrame) {
                return Mth.wrapDegrees(180 + itemFrame.getDirection().get2DDataValue() * 90);
            }

            @Environment(value=EnvType.CLIENT)
            private double getSpawnToAngle(LevelAccessor levelAccessor, Entity entity) {
                BlockPos blockPos = levelAccessor.getSharedSpawnPos();
                return Math.atan2((double)blockPos.getZ() - entity.getZ(), (double)blockPos.getX() - entity.getX());
            }
        });
    }
}

