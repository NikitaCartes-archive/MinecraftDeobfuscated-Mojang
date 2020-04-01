package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class EndIslandFeature extends Feature<BlockStateConfiguration> {
	public EndIslandFeature(Function<Dynamic<?>, ? extends BlockStateConfiguration> function, Function<Random, ? extends BlockStateConfiguration> function2) {
		super(function, function2);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		BlockStateConfiguration blockStateConfiguration
	) {
		float f = (float)(random.nextInt(3) + 4);

		for (int i = 0; f > 0.5F; i--) {
			for (int j = Mth.floor(-f); j <= Mth.ceil(f); j++) {
				for (int k = Mth.floor(-f); k <= Mth.ceil(f); k++) {
					if ((float)(j * j + k * k) <= (f + 1.0F) * (f + 1.0F)) {
						this.setBlock(levelAccessor, blockPos.offset(j, i, k), blockStateConfiguration.state);
					}
				}
			}

			f = (float)((double)f - ((double)random.nextInt(2) + 0.5));
		}

		return true;
	}
}
