package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.CharConfiguration;

public class CharFeature extends Feature<CharConfiguration> {
	public CharFeature(Function<Dynamic<?>, ? extends CharConfiguration> function, Function<Random, ? extends CharConfiguration> function2) {
		super(function, function2);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		CharConfiguration charConfiguration
	) {
		return place(blockPos, charConfiguration, blockPosx -> this.setBlock(levelAccessor, blockPosx, charConfiguration.material.getState(random, blockPosx)));
	}

	private static void addPixel(Consumer<BlockPos> consumer, BlockPos.MutableBlockPos mutableBlockPos, Direction direction, int i, byte b) {
		if ((i & b) != 0) {
			consumer.accept(mutableBlockPos);
		}

		mutableBlockPos.move(direction);
	}

	public static boolean place(BlockPos blockPos, CharConfiguration charConfiguration, Consumer<BlockPos> consumer) {
		Direction direction = charConfiguration.orientation.rotate(Direction.EAST);
		Direction direction2 = charConfiguration.orientation.rotate(Direction.DOWN);
		byte[] bs = charConfiguration.getBytes();
		if (bs == null) {
			return false;
		} else {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; i < 8; i++) {
				mutableBlockPos.set(blockPos).move(direction2, i);
				byte b = bs[i];
				addPixel(consumer, mutableBlockPos, direction, 128, b);
				addPixel(consumer, mutableBlockPos, direction, 64, b);
				addPixel(consumer, mutableBlockPos, direction, 32, b);
				addPixel(consumer, mutableBlockPos, direction, 16, b);
				addPixel(consumer, mutableBlockPos, direction, 8, b);
				addPixel(consumer, mutableBlockPos, direction, 4, b);
				addPixel(consumer, mutableBlockPos, direction, 2, b);
				addPixel(consumer, mutableBlockPos, direction, 1, b);
			}

			return true;
		}
	}
}
