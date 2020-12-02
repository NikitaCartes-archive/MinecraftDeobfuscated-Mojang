package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LevelHeightAccessor {
	int getHeight();

	int getMinBuildHeight();

	default int getMaxBuildHeight() {
		return this.getMinBuildHeight() + this.getHeight();
	}

	default int getSectionsCount() {
		return this.getMaxSection() - this.getMinSection();
	}

	default int getMinSection() {
		return SectionPos.blockToSectionCoord(this.getMinBuildHeight());
	}

	default int getMaxSection() {
		return SectionPos.blockToSectionCoord(this.getMaxBuildHeight() - 1) + 1;
	}

	default boolean isOutsideBuildHeight(BlockPos blockPos) {
		return this.isOutsideBuildHeight(blockPos.getY());
	}

	default boolean isOutsideBuildHeight(int i) {
		return i < this.getMinBuildHeight() || i >= this.getMaxBuildHeight();
	}

	default int getSectionIndex(int i) {
		return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(i));
	}

	default int getSectionIndexFromSectionY(int i) {
		return i - this.getMinSection();
	}

	default int getSectionYFromSectionIndex(int i) {
		return i + this.getMinSection();
	}
}
