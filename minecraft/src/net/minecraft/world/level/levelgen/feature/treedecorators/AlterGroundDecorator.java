package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class AlterGroundDecorator extends TreeDecorator {
	private final BlockStateProvider provider;

	public AlterGroundDecorator(BlockStateProvider blockStateProvider) {
		super(TreeDecoratorType.ALTER_GROUND);
		this.provider = blockStateProvider;
	}

	public <T> AlterGroundDecorator(Dynamic<T> dynamic) {
		this(
			Registry.BLOCKSTATE_PROVIDER_TYPES
				.get(new ResourceLocation((String)dynamic.get("provider").get("type").asString().orElseThrow(RuntimeException::new)))
				.deserialize(dynamic.get("provider").orElseEmptyMap())
		);
	}

	@Override
	public void place(LevelAccessor levelAccessor, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
		int i = ((BlockPos)list.get(0)).getY();
		list.stream().filter(blockPos -> blockPos.getY() == i).forEach(blockPos -> {
			this.placeCircle(levelAccessor, random, blockPos.west().north());
			this.placeCircle(levelAccessor, random, blockPos.east(2).north());
			this.placeCircle(levelAccessor, random, blockPos.west().south(2));
			this.placeCircle(levelAccessor, random, blockPos.east(2).south(2));

			for (int ix = 0; ix < 5; ix++) {
				int j = random.nextInt(64);
				int k = j % 8;
				int l = j / 8;
				if (k == 0 || k == 7 || l == 0 || l == 7) {
					this.placeCircle(levelAccessor, random, blockPos.offset(-3 + k, 0, -3 + l));
				}
			}
		});
	}

	private void placeCircle(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos) {
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if (Math.abs(i) != 2 || Math.abs(j) != 2) {
					this.placeBlockAt(levelSimulatedRW, random, blockPos.offset(i, 0, j));
				}
			}
		}
	}

	private void placeBlockAt(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos) {
		for (int i = 2; i >= -3; i--) {
			BlockPos blockPos2 = blockPos.above(i);
			if (AbstractTreeFeature.isGrassOrDirt(levelSimulatedRW, blockPos2)) {
				levelSimulatedRW.setBlock(blockPos2, this.provider.getState(random, blockPos), 19);
				break;
			}

			if (!AbstractTreeFeature.isAir(levelSimulatedRW, blockPos2) && i < 0) {
				break;
			}
		}
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
				dynamicOps,
				dynamicOps.createMap(
					ImmutableMap.of(
						dynamicOps.createString("type"),
						dynamicOps.createString(Registry.TREE_DECORATOR_TYPES.getKey(this.type).toString()),
						dynamicOps.createString("provider"),
						this.provider.serialize(dynamicOps)
					)
				)
			)
			.getValue();
	}

	public static AlterGroundDecorator random(Random random) {
		return new AlterGroundDecorator(BlockStateProvider.random(random));
	}
}
