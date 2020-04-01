package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class RandomPatchConfiguration implements FeatureConfiguration {
	public final BlockStateProvider stateProvider;
	public final BlockPlacer blockPlacer;
	public final Set<Block> whitelist;
	public final Set<BlockState> blacklist;
	public final int tries;
	public final int xspread;
	public final int yspread;
	public final int zspread;
	public final boolean canReplace;
	public final boolean project;
	public final boolean needWater;

	private RandomPatchConfiguration(
		BlockStateProvider blockStateProvider,
		BlockPlacer blockPlacer,
		Set<Block> set,
		Set<BlockState> set2,
		int i,
		int j,
		int k,
		int l,
		boolean bl,
		boolean bl2,
		boolean bl3
	) {
		this.stateProvider = blockStateProvider;
		this.blockPlacer = blockPlacer;
		this.whitelist = set;
		this.blacklist = set2;
		this.tries = i;
		this.xspread = j;
		this.yspread = k;
		this.zspread = l;
		this.canReplace = bl;
		this.project = bl2;
		this.needWater = bl3;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("state_provider"), this.stateProvider.serialize(dynamicOps))
			.put(dynamicOps.createString("block_placer"), this.blockPlacer.serialize(dynamicOps))
			.put(
				dynamicOps.createString("whitelist"),
				dynamicOps.createList(this.whitelist.stream().map(block -> BlockState.serialize(dynamicOps, block.defaultBlockState()).getValue()))
			)
			.put(
				dynamicOps.createString("blacklist"),
				dynamicOps.createList(this.blacklist.stream().map(blockState -> BlockState.serialize(dynamicOps, blockState).getValue()))
			)
			.put(dynamicOps.createString("tries"), dynamicOps.createInt(this.tries))
			.put(dynamicOps.createString("xspread"), dynamicOps.createInt(this.xspread))
			.put(dynamicOps.createString("yspread"), dynamicOps.createInt(this.yspread))
			.put(dynamicOps.createString("zspread"), dynamicOps.createInt(this.zspread))
			.put(dynamicOps.createString("can_replace"), dynamicOps.createBoolean(this.canReplace))
			.put(dynamicOps.createString("project"), dynamicOps.createBoolean(this.project))
			.put(dynamicOps.createString("need_water"), dynamicOps.createBoolean(this.needWater));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
	}

	public static <T> RandomPatchConfiguration deserialize(Dynamic<T> dynamic) {
		BlockStateProviderType<?> blockStateProviderType = Registry.BLOCKSTATE_PROVIDER_TYPES
			.get(new ResourceLocation((String)dynamic.get("state_provider").get("type").asString().orElseThrow(RuntimeException::new)));
		BlockPlacerType<?> blockPlacerType = Registry.BLOCK_PLACER_TYPES
			.get(new ResourceLocation((String)dynamic.get("block_placer").get("type").asString().orElseThrow(RuntimeException::new)));
		return new RandomPatchConfiguration(
			blockStateProviderType.deserialize(dynamic.get("state_provider").orElseEmptyMap()),
			blockPlacerType.deserialize(dynamic.get("block_placer").orElseEmptyMap()),
			(Set<Block>)dynamic.get("whitelist").asList(BlockState::deserialize).stream().map(BlockBehaviour.BlockStateBase::getBlock).collect(Collectors.toSet()),
			Sets.<BlockState>newHashSet(dynamic.get("blacklist").asList(BlockState::deserialize)),
			dynamic.get("tries").asInt(128),
			dynamic.get("xspread").asInt(7),
			dynamic.get("yspread").asInt(3),
			dynamic.get("zspread").asInt(7),
			dynamic.get("can_replace").asBoolean(false),
			dynamic.get("project").asBoolean(true),
			dynamic.get("need_water").asBoolean(false)
		);
	}

	public static RandomPatchConfiguration random(Random random) {
		return new RandomPatchConfiguration(
			BlockStateProvider.random(random),
			BlockPlacer.random(random),
			ImmutableSet.of(),
			ImmutableSet.of(),
			random.nextInt(50),
			1 + random.nextInt(20),
			1 + random.nextInt(20),
			1 + random.nextInt(20),
			random.nextBoolean(),
			random.nextBoolean(),
			random.nextInt(7) == 0
		);
	}

	public static class GrassConfigurationBuilder {
		private final BlockStateProvider stateProvider;
		private final BlockPlacer blockPlacer;
		private Set<Block> whitelist = ImmutableSet.of();
		private Set<BlockState> blacklist = ImmutableSet.of();
		private int tries = 64;
		private int xspread = 7;
		private int yspread = 3;
		private int zspread = 7;
		private boolean canReplace;
		private boolean project = true;
		private boolean needWater = false;

		public GrassConfigurationBuilder(BlockStateProvider blockStateProvider, BlockPlacer blockPlacer) {
			this.stateProvider = blockStateProvider;
			this.blockPlacer = blockPlacer;
		}

		public RandomPatchConfiguration.GrassConfigurationBuilder whitelist(Set<Block> set) {
			this.whitelist = set;
			return this;
		}

		public RandomPatchConfiguration.GrassConfigurationBuilder blacklist(Set<BlockState> set) {
			this.blacklist = set;
			return this;
		}

		public RandomPatchConfiguration.GrassConfigurationBuilder tries(int i) {
			this.tries = i;
			return this;
		}

		public RandomPatchConfiguration.GrassConfigurationBuilder xspread(int i) {
			this.xspread = i;
			return this;
		}

		public RandomPatchConfiguration.GrassConfigurationBuilder yspread(int i) {
			this.yspread = i;
			return this;
		}

		public RandomPatchConfiguration.GrassConfigurationBuilder zspread(int i) {
			this.zspread = i;
			return this;
		}

		public RandomPatchConfiguration.GrassConfigurationBuilder canReplace() {
			this.canReplace = true;
			return this;
		}

		public RandomPatchConfiguration.GrassConfigurationBuilder noProjection() {
			this.project = false;
			return this;
		}

		public RandomPatchConfiguration.GrassConfigurationBuilder needWater() {
			this.needWater = true;
			return this;
		}

		public RandomPatchConfiguration build() {
			return new RandomPatchConfiguration(
				this.stateProvider,
				this.blockPlacer,
				this.whitelist,
				this.blacklist,
				this.tries,
				this.xspread,
				this.yspread,
				this.zspread,
				this.canReplace,
				this.project,
				this.needWater
			);
		}
	}
}
