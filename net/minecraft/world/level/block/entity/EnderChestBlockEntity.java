/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;

@EnvironmentInterfaces(value={@EnvironmentInterface(value=EnvType.CLIENT, itf=LidBlockEntity.class)})
public class EnderChestBlockEntity
extends BlockEntity
implements LidBlockEntity,
TickableBlockEntity {
    public float openness;
    public float oOpenness;
    public int openCount;
    private int tickInterval;

    public EnderChestBlockEntity() {
        super(BlockEntityType.ENDER_CHEST);
    }

    @Override
    public void tick() {
        double e;
        if (++this.tickInterval % 20 * 4 == 0) {
            this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
        }
        this.oOpenness = this.openness;
        int i = this.worldPosition.getX();
        int j = this.worldPosition.getY();
        int k = this.worldPosition.getZ();
        float f = 0.1f;
        if (this.openCount > 0 && this.openness == 0.0f) {
            double d = (double)i + 0.5;
            e = (double)k + 0.5;
            this.level.playSound(null, d, (double)j + 0.5, e, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5f, this.level.random.nextFloat() * 0.1f + 0.9f);
        }
        if (this.openCount == 0 && this.openness > 0.0f || this.openCount > 0 && this.openness < 1.0f) {
            float g = this.openness;
            this.openness = this.openCount > 0 ? (this.openness += 0.1f) : (this.openness -= 0.1f);
            if (this.openness > 1.0f) {
                this.openness = 1.0f;
            }
            float h = 0.5f;
            if (this.openness < 0.5f && g >= 0.5f) {
                e = (double)i + 0.5;
                double l = (double)k + 0.5;
                this.level.playSound(null, e, (double)j + 0.5, l, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, this.level.random.nextFloat() * 0.1f + 0.9f);
            }
            if (this.openness < 0.0f) {
                this.openness = 0.0f;
            }
        }
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            this.openCount = j;
            return true;
        }
        return super.triggerEvent(i, j);
    }

    @Override
    public void setRemoved() {
        this.clearCache();
        super.setRemoved();
    }

    public void startOpen() {
        ++this.openCount;
        this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
    }

    public void stopOpen() {
        --this.openCount;
        this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
    }

    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) > 64.0);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public float getOpenNess(float f) {
        return Mth.lerp(f, this.oOpenness, this.openness);
    }
}

