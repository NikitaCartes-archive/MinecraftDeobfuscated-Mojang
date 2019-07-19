/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ComparatorBlockEntity
extends BlockEntity {
    private int output;

    public ComparatorBlockEntity() {
        super(BlockEntityType.COMPARATOR);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.putInt("OutputSignal", this.output);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.output = compoundTag.getInt("OutputSignal");
    }

    public int getOutputSignal() {
        return this.output;
    }

    public void setOutputSignal(int i) {
        this.output = i;
    }
}

