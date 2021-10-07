package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface PositionalRandomFactory {
	default RandomSource at(BlockPos blockPos) {
		return this.at(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	default RandomSource at(ResourceLocation resourceLocation) {
		return this.at(resourceLocation.toString());
	}

	RandomSource at(int i, int j, int k);

	RandomSource at(String string);
}
