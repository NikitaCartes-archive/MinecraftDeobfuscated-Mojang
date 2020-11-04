package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

public interface LevelHeightAccessor {
	int getSectionsCount();

	int getMinSection();

	default int getMaxSection() {
		return this.getMinSection() + this.getSectionsCount();
	}

	default int getHeight() {
		return this.getSectionsCount() * 16;
	}

	default int getMinBuildHeight() {
		return this.getMinSection() * 16;
	}

	default int getMaxBuildHeight() {
		return this.getMinBuildHeight() + this.getHeight();
	}

	default boolean isOutsideBuildHeight(BlockPos blockPos) {
		return this.isOutsideBuildHeight(blockPos.getY());
	}

	default boolean isOutsideBuildHeight(int i) {
		return i < this.getMinBuildHeight() || i >= this.getMaxBuildHeight();
	}

	default int getSectionIndex(int i) {
		return this.getSectionIndexFromSectionY(i >> 4);
	}

	default int getSectionIndexFromSectionY(int i) {
		return i - this.getMinSection();
	}

	default int getSectionYFromSectionIndex(int i) {
		return i + this.getMinSection();
	}
}
