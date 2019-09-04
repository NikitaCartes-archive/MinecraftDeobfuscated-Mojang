package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ForestFlowerFeature extends FlowerFeature {
	private static final Block[] flowers = new Block[]{
		Blocks.DANDELION,
		Blocks.POPPY,
		Blocks.BLUE_ORCHID,
		Blocks.ALLIUM,
		Blocks.AZURE_BLUET,
		Blocks.RED_TULIP,
		Blocks.ORANGE_TULIP,
		Blocks.WHITE_TULIP,
		Blocks.PINK_TULIP,
		Blocks.OXEYE_DAISY,
		Blocks.CORNFLOWER,
		Blocks.LILY_OF_THE_VALLEY
	};

	public ForestFlowerFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	public BlockState getRandomFlower(Random random, BlockPos blockPos) {
		double d = Mth.clamp((1.0 + Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 48.0, (double)blockPos.getZ() / 48.0, false)) / 2.0, 0.0, 0.9999);
		Block block = flowers[(int)(d * (double)flowers.length)];
		return block == Blocks.BLUE_ORCHID ? Blocks.POPPY.defaultBlockState() : block.defaultBlockState();
	}
}
