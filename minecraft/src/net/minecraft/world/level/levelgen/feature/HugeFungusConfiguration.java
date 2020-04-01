package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class HugeFungusConfiguration implements FeatureConfiguration {
	public static final HugeFungusConfiguration HUGE_CRIMSON_FUNGI_PLANTED_CONFIG = new HugeFungusConfiguration(
		Blocks.CRIMSON_NYLIUM.defaultBlockState(),
		Blocks.CRIMSON_STEM.defaultBlockState(),
		Blocks.NETHER_WART_BLOCK.defaultBlockState(),
		Blocks.SHROOMLIGHT.defaultBlockState(),
		true
	);
	public static final HugeFungusConfiguration HUGE_CRIMSON_FUNGI_NOT_PLANTED_CONFIG;
	public static final HugeFungusConfiguration HUGE_WARPED_FUNGI_PLANTED_CONFIG = new HugeFungusConfiguration(
		Blocks.WARPED_NYLIUM.defaultBlockState(),
		Blocks.WARPED_STEM.defaultBlockState(),
		Blocks.WARPED_WART_BLOCK.defaultBlockState(),
		Blocks.SHROOMLIGHT.defaultBlockState(),
		true
	);
	public static final HugeFungusConfiguration HUGE_WARPED_FUNGI_NOT_PLANTED_CONFIG;
	public final BlockState validBaseState;
	public final BlockState stemState;
	public final BlockState hatState;
	public final BlockState decorState;
	public final boolean planted;

	public HugeFungusConfiguration(BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4, boolean bl) {
		this.validBaseState = blockState;
		this.stemState = blockState2;
		this.hatState = blockState3;
		this.decorState = blockState4;
		this.planted = bl;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("valid_base_block"),
					BlockState.serialize(dynamicOps, this.validBaseState).getValue(),
					dynamicOps.createString("stem_state"),
					BlockState.serialize(dynamicOps, this.stemState).getValue(),
					dynamicOps.createString("hat_state"),
					BlockState.serialize(dynamicOps, this.hatState).getValue(),
					dynamicOps.createString("decor_state"),
					BlockState.serialize(dynamicOps, this.decorState).getValue(),
					dynamicOps.createString("planted"),
					dynamicOps.createBoolean(this.planted)
				)
			)
		);
	}

	public static <T> HugeFungusConfiguration deserialize(Dynamic<T> dynamic) {
		BlockState blockState = (BlockState)dynamic.get("valid_base_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState2 = (BlockState)dynamic.get("stem_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState3 = (BlockState)dynamic.get("hat_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState4 = (BlockState)dynamic.get("decor_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		boolean bl = dynamic.get("planted").asBoolean(false);
		return new HugeFungusConfiguration(blockState, blockState2, blockState3, blockState4, bl);
	}

	public static <T> HugeFungusConfiguration random(Random random) {
		return new HugeFungusConfiguration(
			Registry.BLOCK.getRandom(random).defaultBlockState(),
			Registry.BLOCK.getRandom(random).defaultBlockState(),
			Registry.BLOCK.getRandom(random).defaultBlockState(),
			Registry.BLOCK.getRandom(random).defaultBlockState(),
			false
		);
	}

	static {
		HUGE_CRIMSON_FUNGI_NOT_PLANTED_CONFIG = new HugeFungusConfiguration(
			HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.validBaseState,
			HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.stemState,
			HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.hatState,
			HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.decorState,
			false
		);
		HUGE_WARPED_FUNGI_NOT_PLANTED_CONFIG = new HugeFungusConfiguration(
			HUGE_WARPED_FUNGI_PLANTED_CONFIG.validBaseState,
			HUGE_WARPED_FUNGI_PLANTED_CONFIG.stemState,
			HUGE_WARPED_FUNGI_PLANTED_CONFIG.hatState,
			HUGE_WARPED_FUNGI_PLANTED_CONFIG.decorState,
			false
		);
	}
}
