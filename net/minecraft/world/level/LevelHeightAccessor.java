/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

public interface LevelHeightAccessor {
    public int getSectionsCount();

    public int getMinSection();

    default public int getMaxSection() {
        return this.getMinSection() + this.getSectionsCount();
    }

    default public int getHeight() {
        return this.getSectionsCount() * 16;
    }

    default public int getMinBuildHeight() {
        return this.getMinSection() * 16;
    }

    default public int getMaxBuildHeight() {
        return this.getMinBuildHeight() + this.getHeight();
    }

    default public boolean isOutsideBuildHeight(BlockPos blockPos) {
        return this.isOutsideBuildHeight(blockPos.getY());
    }

    default public boolean isOutsideBuildHeight(int i) {
        return i < this.getMinBuildHeight() || i >= this.getMaxBuildHeight();
    }

    default public int getSectionIndex(int i) {
        return this.getSectionIndexFromSectionY(i >> 4);
    }

    default public int getSectionIndexFromSectionY(int i) {
        return i - this.getMinSection();
    }

    default public int getSectionYFromSectionIndex(int i) {
        return i + this.getMinSection();
    }
}

