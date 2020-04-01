package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class MegaTreeConfiguration extends TreeConfiguration {
	public final int heightInterval;
	public final int crownHeight;

	protected MegaTreeConfiguration(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, List<TreeDecorator> list, int i, int j, int k) {
		super(blockStateProvider, blockStateProvider2, list, i);
		this.heightInterval = j;
		this.crownHeight = k;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		Dynamic<T> dynamic = new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("height_interval"),
					dynamicOps.createInt(this.heightInterval),
					dynamicOps.createString("crown_height"),
					dynamicOps.createInt(this.crownHeight)
				)
			)
		);
		return dynamic.merge(super.serialize(dynamicOps));
	}

	public static MegaTreeConfiguration random(Random random) {
		TreeConfiguration treeConfiguration = TreeConfiguration.random(random);
		return new MegaTreeConfiguration(
			treeConfiguration.trunkProvider,
			treeConfiguration.leavesProvider,
			treeConfiguration.decorators,
			treeConfiguration.baseHeight,
			random.nextInt(50),
			random.nextInt(20)
		);
	}

	public static <T> MegaTreeConfiguration deserialize(Dynamic<T> dynamic) {
		TreeConfiguration treeConfiguration = TreeConfiguration.deserialize(dynamic);
		return new MegaTreeConfiguration(
			treeConfiguration.trunkProvider,
			treeConfiguration.leavesProvider,
			treeConfiguration.decorators,
			treeConfiguration.baseHeight,
			dynamic.get("height_interval").asInt(0),
			dynamic.get("crown_height").asInt(0)
		);
	}

	public static class MegaTreeConfigurationBuilder extends TreeConfiguration.TreeConfigurationBuilder {
		private List<TreeDecorator> decorators = ImmutableList.of();
		private int baseHeight;
		private int heightInterval;
		private int crownHeight;

		public MegaTreeConfigurationBuilder(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2) {
			super(blockStateProvider, blockStateProvider2);
		}

		public MegaTreeConfiguration.MegaTreeConfigurationBuilder decorators(List<TreeDecorator> list) {
			this.decorators = list;
			return this;
		}

		public MegaTreeConfiguration.MegaTreeConfigurationBuilder baseHeight(int i) {
			this.baseHeight = i;
			return this;
		}

		public MegaTreeConfiguration.MegaTreeConfigurationBuilder heightInterval(int i) {
			this.heightInterval = i;
			return this;
		}

		public MegaTreeConfiguration.MegaTreeConfigurationBuilder crownHeight(int i) {
			this.crownHeight = i;
			return this;
		}

		public MegaTreeConfiguration build() {
			return new MegaTreeConfiguration(this.trunkProvider, this.leavesProvider, this.decorators, this.baseHeight, this.heightInterval, this.crownHeight);
		}
	}
}
