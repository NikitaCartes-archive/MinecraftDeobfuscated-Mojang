package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TreeConfiguration implements FeatureConfiguration {
	public static final Codec<TreeConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(treeConfiguration -> treeConfiguration.trunkProvider),
					TrunkPlacer.CODEC.fieldOf("trunk_placer").forGetter(treeConfiguration -> treeConfiguration.trunkPlacer),
					BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter(treeConfiguration -> treeConfiguration.foliageProvider),
					BlockStateProvider.CODEC.fieldOf("sapling_provider").forGetter(treeConfiguration -> treeConfiguration.saplingProvider),
					FoliagePlacer.CODEC.fieldOf("foliage_placer").forGetter(treeConfiguration -> treeConfiguration.foliagePlacer),
					BlockStateProvider.CODEC.fieldOf("dirt_provider").forGetter(treeConfiguration -> treeConfiguration.dirtProvider),
					FeatureSize.CODEC.fieldOf("minimum_size").forGetter(treeConfiguration -> treeConfiguration.minimumSize),
					TreeDecorator.CODEC.listOf().fieldOf("decorators").forGetter(treeConfiguration -> treeConfiguration.decorators),
					Codec.BOOL.fieldOf("ignore_vines").orElse(false).forGetter(treeConfiguration -> treeConfiguration.ignoreVines),
					Codec.BOOL.fieldOf("force_dirt").orElse(false).forGetter(treeConfiguration -> treeConfiguration.forceDirt)
				)
				.apply(instance, TreeConfiguration::new)
	);
	public final BlockStateProvider trunkProvider;
	public final BlockStateProvider dirtProvider;
	public final TrunkPlacer trunkPlacer;
	public final BlockStateProvider foliageProvider;
	public final BlockStateProvider saplingProvider;
	public final FoliagePlacer foliagePlacer;
	public final FeatureSize minimumSize;
	public final List<TreeDecorator> decorators;
	public final boolean ignoreVines;
	public final boolean forceDirt;

	protected TreeConfiguration(
		BlockStateProvider blockStateProvider,
		TrunkPlacer trunkPlacer,
		BlockStateProvider blockStateProvider2,
		BlockStateProvider blockStateProvider3,
		FoliagePlacer foliagePlacer,
		BlockStateProvider blockStateProvider4,
		FeatureSize featureSize,
		List<TreeDecorator> list,
		boolean bl,
		boolean bl2
	) {
		this.trunkProvider = blockStateProvider;
		this.trunkPlacer = trunkPlacer;
		this.foliageProvider = blockStateProvider2;
		this.foliagePlacer = foliagePlacer;
		this.dirtProvider = blockStateProvider4;
		this.saplingProvider = blockStateProvider3;
		this.minimumSize = featureSize;
		this.decorators = list;
		this.ignoreVines = bl;
		this.forceDirt = bl2;
	}

	public TreeConfiguration withDecorators(List<TreeDecorator> list) {
		return new TreeConfiguration(
			this.trunkProvider,
			this.trunkPlacer,
			this.foliageProvider,
			this.saplingProvider,
			this.foliagePlacer,
			this.dirtProvider,
			this.minimumSize,
			list,
			this.ignoreVines,
			this.forceDirt
		);
	}

	public static class TreeConfigurationBuilder {
		public final BlockStateProvider trunkProvider;
		private final TrunkPlacer trunkPlacer;
		public final BlockStateProvider foliageProvider;
		public final BlockStateProvider saplingProvider;
		private final FoliagePlacer foliagePlacer;
		private BlockStateProvider dirtProvider;
		private final FeatureSize minimumSize;
		private List<TreeDecorator> decorators = ImmutableList.of();
		private boolean ignoreVines;
		private boolean forceDirt;

		public TreeConfigurationBuilder(
			BlockStateProvider blockStateProvider,
			TrunkPlacer trunkPlacer,
			BlockStateProvider blockStateProvider2,
			BlockStateProvider blockStateProvider3,
			FoliagePlacer foliagePlacer,
			FeatureSize featureSize
		) {
			this.trunkProvider = blockStateProvider;
			this.trunkPlacer = trunkPlacer;
			this.foliageProvider = blockStateProvider2;
			this.saplingProvider = blockStateProvider3;
			this.dirtProvider = new SimpleStateProvider(Blocks.DIRT.defaultBlockState());
			this.foliagePlacer = foliagePlacer;
			this.minimumSize = featureSize;
		}

		public TreeConfiguration.TreeConfigurationBuilder dirt(BlockStateProvider blockStateProvider) {
			this.dirtProvider = blockStateProvider;
			return this;
		}

		public TreeConfiguration.TreeConfigurationBuilder decorators(List<TreeDecorator> list) {
			this.decorators = list;
			return this;
		}

		public TreeConfiguration.TreeConfigurationBuilder ignoreVines() {
			this.ignoreVines = true;
			return this;
		}

		public TreeConfiguration.TreeConfigurationBuilder forceDirt() {
			this.forceDirt = true;
			return this;
		}

		public TreeConfiguration build() {
			return new TreeConfiguration(
				this.trunkProvider,
				this.trunkPlacer,
				this.foliageProvider,
				this.saplingProvider,
				this.foliagePlacer,
				this.dirtProvider,
				this.minimumSize,
				this.decorators,
				this.ignoreVines,
				this.forceDirt
			);
		}
	}
}
