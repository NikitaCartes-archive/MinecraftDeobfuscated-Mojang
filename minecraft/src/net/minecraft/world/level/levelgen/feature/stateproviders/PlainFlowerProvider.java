package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PlainFlowerProvider extends BlockStateProvider {
	private static final BlockState[] LOW_NOISE_FLOWERS = new BlockState[]{
		Blocks.ORANGE_TULIP.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.WHITE_TULIP.defaultBlockState()
	};
	private static final BlockState[] HIGH_NOISE_FLOWERS = new BlockState[]{
		Blocks.POPPY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState()
	};

	public PlainFlowerProvider() {
		super(BlockStateProviderType.PLAIN_FLOWER_PROVIDER);
	}

	public <T> PlainFlowerProvider(Dynamic<T> dynamic) {
		this();
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0, false);
		if (d < -0.8) {
			return Util.getRandom(LOW_NOISE_FLOWERS, random);
		} else {
			return random.nextInt(3) > 0 ? Util.getRandom(HIGH_NOISE_FLOWERS, random) : Blocks.DANDELION.defaultBlockState();
		}
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("type"), dynamicOps.createString(Registry.BLOCKSTATE_PROVIDER_TYPES.getKey(this.type).toString()));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build())).getValue();
	}
}
