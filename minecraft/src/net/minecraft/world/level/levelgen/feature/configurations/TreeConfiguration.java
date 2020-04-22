package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class TreeConfiguration implements FeatureConfiguration {
	public final BlockStateProvider trunkProvider;
	public final BlockStateProvider leavesProvider;
	public final List<TreeDecorator> decorators;
	public transient boolean fromSapling;
	public final FoliagePlacer foliagePlacer;
	public final TrunkPlacer trunkPlacer;
	public final FeatureSize minimumSize;
	public final int maxWaterDepth;
	public final boolean ignoreVines;
	public final Heightmap.Types heightmap;

	protected TreeConfiguration(
		BlockStateProvider blockStateProvider,
		BlockStateProvider blockStateProvider2,
		FoliagePlacer foliagePlacer,
		TrunkPlacer trunkPlacer,
		FeatureSize featureSize,
		List<TreeDecorator> list,
		int i,
		boolean bl,
		Heightmap.Types types
	) {
		this.trunkProvider = blockStateProvider;
		this.leavesProvider = blockStateProvider2;
		this.decorators = list;
		this.foliagePlacer = foliagePlacer;
		this.minimumSize = featureSize;
		this.trunkPlacer = trunkPlacer;
		this.maxWaterDepth = i;
		this.ignoreVines = bl;
		this.heightmap = types;
	}

	public void setFromSapling() {
		this.fromSapling = true;
	}

	public TreeConfiguration withDecorators(List<TreeDecorator> list) {
		return new TreeConfiguration(
			this.trunkProvider, this.leavesProvider, this.foliagePlacer, this.trunkPlacer, this.minimumSize, list, this.maxWaterDepth, this.ignoreVines, this.heightmap
		);
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("trunk_provider"), this.trunkProvider.serialize(dynamicOps))
			.put(dynamicOps.createString("leaves_provider"), this.leavesProvider.serialize(dynamicOps))
			.put(dynamicOps.createString("decorators"), dynamicOps.createList(this.decorators.stream().map(treeDecorator -> treeDecorator.serialize(dynamicOps))))
			.put(dynamicOps.createString("foliage_placer"), this.foliagePlacer.serialize(dynamicOps))
			.put(dynamicOps.createString("trunk_placer"), this.trunkPlacer.serialize(dynamicOps))
			.put(dynamicOps.createString("minimum_size"), this.minimumSize.serialize(dynamicOps))
			.put(dynamicOps.createString("max_water_depth"), dynamicOps.createInt(this.maxWaterDepth))
			.put(dynamicOps.createString("ignore_vines"), dynamicOps.createBoolean(this.ignoreVines))
			.put(dynamicOps.createString("heightmap"), dynamicOps.createString(this.heightmap.getSerializationKey()));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
	}

	public static <T> TreeConfiguration deserialize(Dynamic<T> dynamic) {
		BlockStateProviderType<?> blockStateProviderType = Registry.BLOCKSTATE_PROVIDER_TYPES
			.get(new ResourceLocation((String)dynamic.get("trunk_provider").get("type").asString().orElseThrow(RuntimeException::new)));
		BlockStateProviderType<?> blockStateProviderType2 = Registry.BLOCKSTATE_PROVIDER_TYPES
			.get(new ResourceLocation((String)dynamic.get("leaves_provider").get("type").asString().orElseThrow(RuntimeException::new)));
		FoliagePlacerType<?> foliagePlacerType = Registry.FOLIAGE_PLACER_TYPES
			.get(new ResourceLocation((String)dynamic.get("foliage_placer").get("type").asString().orElseThrow(RuntimeException::new)));
		TrunkPlacerType<?> trunkPlacerType = Registry.TRUNK_PLACER_TYPES
			.get(new ResourceLocation((String)dynamic.get("trunk_placer").get("type").asString().orElseThrow(RuntimeException::new)));
		FeatureSizeType<?> featureSizeType = Registry.FEATURE_SIZE_TYPES
			.get(new ResourceLocation((String)dynamic.get("minimum_size").get("type").asString().orElseThrow(RuntimeException::new)));
		return new TreeConfiguration(
			blockStateProviderType.deserialize(dynamic.get("trunk_provider").orElseEmptyMap()),
			blockStateProviderType2.deserialize(dynamic.get("leaves_provider").orElseEmptyMap()),
			foliagePlacerType.deserialize(dynamic.get("foliage_placer").orElseEmptyMap()),
			trunkPlacerType.deserialize(dynamic.get("trunk_placer").orElseEmptyMap()),
			featureSizeType.deserialize(dynamic.get("minimum_size").orElseEmptyMap()),
			dynamic.get("decorators")
				.asList(
					dynamicx -> Registry.TREE_DECORATOR_TYPES
							.get(new ResourceLocation((String)dynamicx.get("type").asString().orElseThrow(RuntimeException::new)))
							.deserialize(dynamicx)
				),
			dynamic.get("max_water_depth").asInt(0),
			dynamic.get("ignore_vines").asBoolean(false),
			Heightmap.Types.getFromKey(dynamic.get("heightmap").asString(""))
		);
	}

	public static class TreeConfigurationBuilder {
		public final BlockStateProvider trunkProvider;
		public final BlockStateProvider leavesProvider;
		private final FoliagePlacer foliagePlacer;
		private final TrunkPlacer trunkPlacer;
		private final FeatureSize minimumSize;
		private List<TreeDecorator> decorators = ImmutableList.of();
		private int maxWaterDepth;
		private boolean ignoreVines;
		private Heightmap.Types heightmap = Heightmap.Types.OCEAN_FLOOR;

		public TreeConfigurationBuilder(
			BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, FoliagePlacer foliagePlacer, TrunkPlacer trunkPlacer, FeatureSize featureSize
		) {
			this.trunkProvider = blockStateProvider;
			this.leavesProvider = blockStateProvider2;
			this.foliagePlacer = foliagePlacer;
			this.trunkPlacer = trunkPlacer;
			this.minimumSize = featureSize;
		}

		public TreeConfiguration.TreeConfigurationBuilder decorators(List<TreeDecorator> list) {
			this.decorators = list;
			return this;
		}

		public TreeConfiguration.TreeConfigurationBuilder maxWaterDepth(int i) {
			this.maxWaterDepth = i;
			return this;
		}

		public TreeConfiguration.TreeConfigurationBuilder ignoreVines() {
			this.ignoreVines = true;
			return this;
		}

		public TreeConfiguration.TreeConfigurationBuilder heightmap(Heightmap.Types types) {
			this.heightmap = types;
			return this;
		}

		public TreeConfiguration build() {
			return new TreeConfiguration(
				this.trunkProvider,
				this.leavesProvider,
				this.foliagePlacer,
				this.trunkPlacer,
				this.minimumSize,
				this.decorators,
				this.maxWaterDepth,
				this.ignoreVines,
				this.heightmap
			);
		}
	}
}
