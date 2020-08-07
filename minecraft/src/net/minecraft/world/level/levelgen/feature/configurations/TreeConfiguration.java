package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TreeConfiguration implements FeatureConfiguration {
	public static final Codec<TreeConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(treeConfiguration -> treeConfiguration.trunkProvider),
					BlockStateProvider.CODEC.fieldOf("leaves_provider").forGetter(treeConfiguration -> treeConfiguration.leavesProvider),
					FoliagePlacer.CODEC.fieldOf("foliage_placer").forGetter(treeConfiguration -> treeConfiguration.foliagePlacer),
					TrunkPlacer.CODEC.fieldOf("trunk_placer").forGetter(treeConfiguration -> treeConfiguration.trunkPlacer),
					FeatureSize.CODEC.fieldOf("minimum_size").forGetter(treeConfiguration -> treeConfiguration.minimumSize),
					TreeDecorator.CODEC.listOf().fieldOf("decorators").forGetter(treeConfiguration -> treeConfiguration.decorators),
					Codec.INT.fieldOf("max_water_depth").orElse(0).forGetter(treeConfiguration -> treeConfiguration.maxWaterDepth),
					Codec.BOOL.fieldOf("ignore_vines").orElse(false).forGetter(treeConfiguration -> treeConfiguration.ignoreVines),
					Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(treeConfiguration -> treeConfiguration.heightmap)
				)
				.apply(instance, TreeConfiguration::new)
	);
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
