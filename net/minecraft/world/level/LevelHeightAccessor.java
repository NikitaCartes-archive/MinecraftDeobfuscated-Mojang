/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LevelHeightAccessor {
    public int getHeight();

    public int getMinBuildHeight();

    default public int getMaxBuildHeight() {
        return this.getMinBuildHeight() + this.getHeight();
    }

    default public int getSectionsCount() {
        return this.getMaxSection() - this.getMinSection();
    }

    default public int getMinSection() {
        return SectionPos.blockToSectionCoord(this.getMinBuildHeight());
    }

    default public int getMaxSection() {
        return SectionPos.blockToSectionCoord(this.getMaxBuildHeight() - 1) + 1;
    }

    default public boolean isOutsideBuildHeight(BlockPos blockPos) {
        return this.isOutsideBuildHeight(blockPos.getY());
    }

    default public boolean isOutsideBuildHeight(int i) {
        return i < this.getMinBuildHeight() || i >= this.getMaxBuildHeight();
    }

    default public int getSectionIndex(int i) {
        return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(i));
    }

    default public int getSectionIndexFromSectionY(int i) {
        return i - this.getMinSection();
    }

    default public int getSectionYFromSectionIndex(int i) {
        return i + this.getMinSection();
    }

    public static LevelHeightAccessor create(final int i, final int j) {
        return new LevelHeightAccessor(){

            @Override
            public int getHeight() {
                return j;
            }

            @Override
            public int getMinBuildHeight() {
                return i;
            }
        };
    }
}

