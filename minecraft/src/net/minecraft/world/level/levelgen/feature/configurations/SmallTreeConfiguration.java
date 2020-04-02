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
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class SmallTreeConfiguration extends TreeConfiguration {
	public final FoliagePlacer foliagePlacer;
	public final TrunkPlacer trunkPlacer;
	public final int maxWaterDepth;
	public final boolean ignoreVines;

	protected SmallTreeConfiguration(
		BlockStateProvider blockStateProvider,
		BlockStateProvider blockStateProvider2,
		FoliagePlacer foliagePlacer,
		TrunkPlacer trunkPlacer,
		List<TreeDecorator> list,
		int i,
		boolean bl
	) {
		super(blockStateProvider, blockStateProvider2, list, trunkPlacer.getBaseHeight());
		this.foliagePlacer = foliagePlacer;
		this.trunkPlacer = trunkPlacer;
		this.maxWaterDepth = i;
		this.ignoreVines = bl;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("foliage_placer"), this.foliagePlacer.serialize(dynamicOps))
			.put(dynamicOps.createString("trunk_placer"), this.trunkPlacer.serialize(dynamicOps))
			.put(dynamicOps.createString("max_water_depth"), dynamicOps.createInt(this.maxWaterDepth))
			.put(dynamicOps.createString("ignore_vines"), dynamicOps.createBoolean(this.ignoreVines));
		Dynamic<T> dynamic = new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
		return dynamic.merge(super.serialize(dynamicOps));
	}

	public static <T> SmallTreeConfiguration deserialize(Dynamic<T> dynamic) {
		TreeConfiguration treeConfiguration = TreeConfiguration.deserialize(dynamic);
		FoliagePlacerType<?> foliagePlacerType = Registry.FOLIAGE_PLACER_TYPES
			.get(new ResourceLocation((String)dynamic.get("foliage_placer").get("type").asString().orElseThrow(RuntimeException::new)));
		TrunkPlacerType<?> trunkPlacerType = Registry.TRUNK_PLACER_TYPES
			.get(new ResourceLocation((String)dynamic.get("trunk_placer").get("type").asString().orElseThrow(RuntimeException::new)));
		return new SmallTreeConfiguration(
			treeConfiguration.trunkProvider,
			treeConfiguration.leavesProvider,
			foliagePlacerType.deserialize(dynamic.get("foliage_placer").orElseEmptyMap()),
			trunkPlacerType.deserialize(dynamic.get("trunk_placer").orElseEmptyMap()),
			treeConfiguration.decorators,
			dynamic.get("max_water_depth").asInt(0),
			dynamic.get("ignore_vines").asBoolean(false)
		);
	}

	public static class SmallTreeConfigurationBuilder extends TreeConfiguration.TreeConfigurationBuilder {
		private final FoliagePlacer foliagePlacer;
		private final TrunkPlacer trunkPlacer;
		private List<TreeDecorator> decorators = ImmutableList.of();
		private int maxWaterDepth;
		private boolean ignoreVines;

		public SmallTreeConfigurationBuilder(
			BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, FoliagePlacer foliagePlacer, TrunkPlacer trunkPlacer
		) {
			super(blockStateProvider, blockStateProvider2);
			this.foliagePlacer = foliagePlacer;
			this.trunkPlacer = trunkPlacer;
		}

		public SmallTreeConfiguration.SmallTreeConfigurationBuilder decorators(List<TreeDecorator> list) {
			this.decorators = list;
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
				this.trunkProvider, this.leavesProvider, this.foliagePlacer, this.trunkPlacer, this.decorators, this.maxWaterDepth, this.ignoreVines
			);
		}
	}
}
