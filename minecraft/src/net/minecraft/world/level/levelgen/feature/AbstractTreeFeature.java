package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public abstract class AbstractTreeFeature<T extends TreeConfiguration> extends Feature<T> {
	public AbstractTreeFeature(Function<Dynamic<?>, ? extends T> function, Function<Random, ? extends T> function2) {
		super(function, function2);
	}

	protected static boolean isFree(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(
			blockPos,
			blockState -> {
				Block block = blockState.getBlock();
				return blockState.isAir()
					|| blockState.is(BlockTags.LEAVES)
					|| isDirt(block)
					|| block.is(BlockTags.LOGS)
					|| block.is(BlockTags.SAPLINGS)
					|| block == Blocks.VINE;
			}
		);
	}

	public static boolean isAir(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, BlockBehaviour.BlockStateBase::isAir);
	}

	protected static boolean isDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
			Block block = blockState.getBlock();
			return isDirt(block) && block != Blocks.GRASS_BLOCK && block != Blocks.MYCELIUM;
		});
	}

	protected static boolean isVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.getBlock() == Blocks.VINE);
	}

	public static boolean isBlockWater(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.getBlock() == Blocks.WATER);
	}

	public static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
	}

	public static boolean isGrassOrDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> isDirt(blockState.getBlock()));
	}

	protected static boolean isGrassOrDirtOrFarmland(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
			Block block = blockState.getBlock();
			return isDirt(block) || block == Blocks.FARMLAND;
		});
	}

	public static boolean isReplaceablePlant(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
			Material material = blockState.getMaterial();
			return material == Material.REPLACEABLE_PLANT;
		});
	}

	protected void setDirtAt(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos) {
		if (!isDirt(levelSimulatedRW, blockPos)) {
			this.setBlock(levelSimulatedRW, blockPos, Blocks.DIRT.defaultBlockState());
		}
	}

	protected boolean placeLog(
		LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		if (!isAirOrLeaves(levelSimulatedRW, blockPos) && !isReplaceablePlant(levelSimulatedRW, blockPos) && !isBlockWater(levelSimulatedRW, blockPos)) {
			return false;
		} else {
			this.setBlock(levelSimulatedRW, blockPos, treeConfiguration.trunkProvider.getState(random, blockPos), boundingBox);
			set.add(blockPos.immutable());
			return true;
		}
	}

	protected boolean placeLeaf(
		LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		if (!isAirOrLeaves(levelSimulatedRW, blockPos) && !isReplaceablePlant(levelSimulatedRW, blockPos) && !isBlockWater(levelSimulatedRW, blockPos)) {
			return false;
		} else {
			this.setBlock(levelSimulatedRW, blockPos, treeConfiguration.leavesProvider.getState(random, blockPos), boundingBox);
			set.add(blockPos.immutable());
			return true;
		}
	}

	@Override
	protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		this.setBlockKnownShape(levelWriter, blockPos, blockState);
	}

	protected final void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState, BoundingBox boundingBox) {
		this.setBlockKnownShape(levelWriter, blockPos, blockState);
		boundingBox.expand(new BoundingBox(blockPos, blockPos));
	}

	private void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		levelWriter.setBlock(blockPos, blockState, 19);
	}

	public final boolean place(
		LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, T treeConfiguration
	) {
		Set<BlockPos> set = Sets.<BlockPos>newHashSet();
		Set<BlockPos> set2 = Sets.<BlockPos>newHashSet();
		Set<BlockPos> set3 = Sets.<BlockPos>newHashSet();
		BoundingBox boundingBox = BoundingBox.getUnknownBox();
		boolean bl = this.doPlace(levelAccessor, random, blockPos, set, set2, boundingBox, treeConfiguration);
		if (boundingBox.x0 <= boundingBox.x1 && bl && !set.isEmpty()) {
			if (!treeConfiguration.decorators.isEmpty()) {
				List<BlockPos> list = Lists.<BlockPos>newArrayList(set);
				List<BlockPos> list2 = Lists.<BlockPos>newArrayList(set2);
				list.sort(Comparator.comparingInt(Vec3i::getY));
				list2.sort(Comparator.comparingInt(Vec3i::getY));
				treeConfiguration.decorators.forEach(treeDecorator -> treeDecorator.place(levelAccessor, random, list, list2, set3, boundingBox));
			}

			DiscreteVoxelShape discreteVoxelShape = this.updateLeaves(levelAccessor, boundingBox, set, set3);
			StructureTemplate.updateShapeAtEdge(levelAccessor, 3, discreteVoxelShape, boundingBox.x0, boundingBox.y0, boundingBox.z0);
			return true;
		} else {
			return false;
		}
	}

	private DiscreteVoxelShape updateLeaves(LevelAccessor levelAccessor, BoundingBox boundingBox, Set<BlockPos> set, Set<BlockPos> set2) {
		List<Set<BlockPos>> list = Lists.<Set<BlockPos>>newArrayList();
		DiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
		int i = 6;

		for (int j = 0; j < 6; j++) {
			list.add(Sets.newHashSet());
		}

		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (BlockPos blockPos : Lists.newArrayList(set2)) {
			if (boundingBox.isInside(blockPos)) {
				discreteVoxelShape.setFull(blockPos.getX() - boundingBox.x0, blockPos.getY() - boundingBox.y0, blockPos.getZ() - boundingBox.z0, true, true);
			}
		}

		for (BlockPos blockPosx : Lists.newArrayList(set)) {
			if (boundingBox.isInside(blockPosx)) {
				discreteVoxelShape.setFull(blockPosx.getX() - boundingBox.x0, blockPosx.getY() - boundingBox.y0, blockPosx.getZ() - boundingBox.z0, true, true);
			}

			for (Direction direction : Direction.values()) {
				mutableBlockPos.setWithOffset(blockPosx, direction);
				if (!set.contains(mutableBlockPos)) {
					BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
					if (blockState.hasProperty(BlockStateProperties.DISTANCE)) {
						((Set)list.get(0)).add(mutableBlockPos.immutable());
						this.setBlockKnownShape(levelAccessor, mutableBlockPos, blockState.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
						if (boundingBox.isInside(mutableBlockPos)) {
							discreteVoxelShape.setFull(
								mutableBlockPos.getX() - boundingBox.x0, mutableBlockPos.getY() - boundingBox.y0, mutableBlockPos.getZ() - boundingBox.z0, true, true
							);
						}
					}
				}
			}
		}

		for (int k = 1; k < 6; k++) {
			Set<BlockPos> set3 = (Set<BlockPos>)list.get(k - 1);
			Set<BlockPos> set4 = (Set<BlockPos>)list.get(k);

			for (BlockPos blockPos2 : set3) {
				if (boundingBox.isInside(blockPos2)) {
					discreteVoxelShape.setFull(blockPos2.getX() - boundingBox.x0, blockPos2.getY() - boundingBox.y0, blockPos2.getZ() - boundingBox.z0, true, true);
				}

				for (Direction direction2 : Direction.values()) {
					mutableBlockPos.setWithOffset(blockPos2, direction2);
					if (!set3.contains(mutableBlockPos) && !set4.contains(mutableBlockPos)) {
						BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
						if (blockState2.hasProperty(BlockStateProperties.DISTANCE)) {
							int l = (Integer)blockState2.getValue(BlockStateProperties.DISTANCE);
							if (l > k + 1) {
								BlockState blockState3 = blockState2.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(k + 1));
								this.setBlockKnownShape(levelAccessor, mutableBlockPos, blockState3);
								if (boundingBox.isInside(mutableBlockPos)) {
									discreteVoxelShape.setFull(
										mutableBlockPos.getX() - boundingBox.x0, mutableBlockPos.getY() - boundingBox.y0, mutableBlockPos.getZ() - boundingBox.z0, true, true
									);
								}

								set4.add(mutableBlockPos.immutable());
							}
						}
					}
				}
			}
		}

		return discreteVoxelShape;
	}

	protected abstract boolean doPlace(
		LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, Set<BlockPos> set2, BoundingBox boundingBox, T treeConfiguration
	);
}
