package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LevelHeightAccessor {
	int getHeight();

	int getMinY();

	default int getMaxY() {
		return this.getMinY() + this.getHeight() - 1;
	}

	default int getSectionsCount() {
		return this.getMaxSectionY() - this.getMinSectionY() + 1;
	}

	default int getMinSectionY() {
		return SectionPos.blockToSectionCoord(this.getMinY());
	}

	default int getMaxSectionY() {
		return SectionPos.blockToSectionCoord(this.getMaxY());
	}

	default boolean isInsideBuildHeight(int i) {
		return i >= this.getMinY() && i <= this.getMaxY();
	}

	default boolean isOutsideBuildHeight(BlockPos blockPos) {
		return this.isOutsideBuildHeight(blockPos.getY());
	}

	default boolean isOutsideBuildHeight(int i) {
		return i < this.getMinY() || i > this.getMaxY();
	}

	default int getSectionIndex(int i) {
		return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(i));
	}

	default int getSectionIndexFromSectionY(int i) {
		return i - this.getMinSectionY();
	}

	default int getSectionYFromSectionIndex(int i) {
		return i + this.getMinSectionY();
	}

	static LevelHeightAccessor create(int i, int j) {
		return new LevelHeightAccessor() {
			@Override
			public int getHeight() {
				return j;
			}

			@Override
			public int getMinY() {
				return i;
			}
		};
	}
}
