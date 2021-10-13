package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface PositionalRandomFactory {
	default RandomSource at(BlockPos blockPos) {
		return this.at(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	default RandomSource fromHashOf(ResourceLocation resourceLocation) {
		return this.fromHashOf(resourceLocation.toString());
	}

	RandomSource fromHashOf(String string);

	RandomSource at(int i, int j, int k);

	@VisibleForTesting
	void parityConfigString(StringBuilder stringBuilder);
}
