package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class SmallTreeConfiguration extends TreeConfiguration {
	public final FoliagePlacer foliagePlacer;
	public final int heightRandA;
	public final int heightRandB;
	public final int trunkHeight;
	public final int trunkHeightRandom;
	public final int trunkTopOffset;
	public final int trunkTopOffsetRandom;
	public final int foliageHeight;
	public final int foliageHeightRandom;
	public final int maxWaterDepth;
	public final boolean ignoreVines;

	protected SmallTreeConfiguration(
		BlockStateProvider blockStateProvider,
		BlockStateProvider blockStateProvider2,
		FoliagePlacer foliagePlacer,
		List<TreeDecorator> list,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		int o,
		int p,
		int q,
		int r,
		boolean bl
	) {
		super(blockStateProvider, blockStateProvider2, list, i);
		this.foliagePlacer = foliagePlacer;
		this.heightRandA = j;
		this.heightRandB = k;
		this.trunkHeight = l;
		this.trunkHeightRandom = m;
		this.trunkTopOffset = n;
		this.trunkTopOffsetRandom = o;
		this.foliageHeight = p;
		this.foliageHeightRandom = q;
		this.maxWaterDepth = r;
		this.ignoreVines = bl;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("foliage_placer"), this.foliagePlacer.serialize(dynamicOps))
			.put(dynamicOps.createString("height_rand_a"), dynamicOps.createInt(this.heightRandA))
			.put(dynamicOps.createString("height_rand_b"), dynamicOps.createInt(this.heightRandB))
			.put(dynamicOps.createString("trunk_height"), dynamicOps.createInt(this.trunkHeight))
			.put(dynamicOps.createString("trunk_height_random"), dynamicOps.createInt(this.trunkHeightRandom))
			.put(dynamicOps.createString("trunk_top_offset"), dynamicOps.createInt(this.trunkTopOffset))
			.put(dynamicOps.createString("trunk_top_offset_random"), dynamicOps.createInt(this.trunkTopOffsetRandom))
			.put(dynamicOps.createString("foliage_height"), dynamicOps.createInt(this.foliageHeight))
			.put(dynamicOps.createString("foliage_height_random"), dynamicOps.createInt(this.foliageHeightRandom))
			.put(dynamicOps.createString("max_water_depth"), dynamicOps.createInt(this.maxWaterDepth))
			.put(dynamicOps.createString("ignore_vines"), dynamicOps.createBoolean(this.ignoreVines));
		Dynamic<T> dynamic = new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
		return dynamic.merge(super.serialize(dynamicOps));
	}

	public static <T> SmallTreeConfiguration deserialize(Dynamic<T> dynamic) {
		TreeConfiguration treeConfiguration = TreeConfiguration.deserialize(dynamic);
		FoliagePlacerType<?> foliagePlacerType = Registry.FOLIAGE_PLACER_TYPES
			.get(new ResourceLocation((String)dynamic.get("foliage_placer").get("type").asString().orElseThrow(RuntimeException::new)));
		return new SmallTreeConfiguration(
			treeConfiguration.trunkProvider,
			treeConfiguration.leavesProvider,
			foliagePlacerType.deserialize(dynamic.get("foliage_placer").orElseEmptyMap()),
			treeConfiguration.decorators,
			treeConfiguration.baseHeight,
			dynamic.get("height_rand_a").asInt(0),
			dynamic.get("height_rand_b").asInt(0),
			dynamic.get("trunk_height").asInt(-1),
			dynamic.get("trunk_height_random").asInt(0),
			dynamic.get("trunk_top_offset").asInt(0),
			dynamic.get("trunk_top_offset_random").asInt(0),
			dynamic.get("foliage_height").asInt(-1),
			dynamic.get("foliage_height_random").asInt(0),
			dynamic.get("max_water_depth").asInt(0),
			dynamic.get("ignore_vines").asBoolean(false)
		);
	}

	public static class SmallTreeConfigurationBuilder extends TreeConfiguration.TreeConfigurationBuilder {
		private final FoliagePlacer foliagePlacer;
		private List<TreeDecorator> decorators = ImmutableList.of();
		private int baseHeight;
		private int heightRandA;
		private int heightRandB;
		private int trunkHeight = -1;
		private int trunkHeightRandom;
		private int trunkTopOffset;
		private int trunkTopOffsetRandom;
		private int foliageHeight = -1;
		private int foliageHeightRandom;
		private int maxWaterDepth;
		private boolean ignoreVines;

		public SmallTreeConfigurationBuilder(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, FoliagePlacer foliagePlacer) {
			super(blockStateProvider, blockStateProvider2);
			this.foliagePlacer = foliagePlacer;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder decorators(List<TreeDecorator> list) {
			this.decorators = list;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder baseHeight(int i) {
			this.baseHeight = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder heightRandA(int i) {
			this.heightRandA = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder heightRandB(int i) {
			this.heightRandB = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder trunkHeight(int i) {
			this.trunkHeight = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder trunkHeightRandom(int i) {
			this.trunkHeightRandom = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder trunkTopOffset(int i) {
			this.trunkTopOffset = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder trunkTopOffsetRandom(int i) {
			this.trunkTopOffsetRandom = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder foliageHeight(int i) {
			this.foliageHeight = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder foliageHeightRandom(int i) {
			this.foliageHeightRandom = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder maxWaterDepth(int i) {
			this.maxWaterDepth = i;
			return this;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder ignoreVines() {
			this.ignoreVines = true;
			return this;
		}

		public SmallTreeConfiguration build() {
			return new SmallTreeConfiguration(
				this.trunkProvider,
				this.leavesProvider,
				this.foliagePlacer,
				this.decorators,
				this.baseHeight,
				this.heightRandA,
				this.heightRandB,
				this.trunkHeight,
				this.trunkHeightRandom,
				this.trunkTopOffset,
				this.trunkTopOffsetRandom,
				this.foliageHeight,
				this.foliageHeightRandom,
				this.maxWaterDepth,
				this.ignoreVines
			);
		}
	}
}
