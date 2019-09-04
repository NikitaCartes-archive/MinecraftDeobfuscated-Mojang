package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public abstract class AbstractTreeFeature<T extends FeatureConfiguration> extends Feature<T> {
	public AbstractTreeFeature(Function<Dynamic<?>, ? extends T> function, boolean bl) {
		super(function, bl);
	}

	protected static boolean isFree(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(
			blockPos,
			blockState -> {
				Block block = blockState.getBlock();
				return blockState.isAir()
					|| blockState.is(BlockTags.LEAVES)
					|| block == Blocks.GRASS_BLOCK
					|| Block.equalsDirt(block)
					|| block.is(BlockTags.LOGS)
					|| block.is(BlockTags.SAPLINGS)
					|| block == Blocks.VINE;
			}
		);
	}

	protected static boolean isAir(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, BlockState::isAir);
	}

	protected static boolean isDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> Block.equalsDirt(blockState.getBlock()));
	}

	protected static boolean isBlockWater(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.getBlock() == Blocks.WATER);
	}

	protected static boolean isLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(BlockTags.LEAVES));
	}

	protected static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
	}

	protected static boolean isGrassOrDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
			Block block = blockState.getBlock();
			return Block.equalsDirt(block) || block == Blocks.GRASS_BLOCK;
		});
	}

	protected static boolean isGrassOrDirtOrFarmland(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
			Block block = blockState.getBlock();
			return Block.equalsDirt(block) || block == Blocks.GRASS_BLOCK || block == Blocks.FARMLAND;
		});
	}

	protected static boolean isReplaceablePlant(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
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

	@Override
	protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		this.setBlockKnownShape(levelWriter, blockPos, blockState);
	}

	protected final void setBlock(Set<BlockPos> set, LevelWriter levelWriter, BlockPos blockPos, BlockState blockState, BoundingBox boundingBox) {
		this.setBlockKnownShape(levelWriter, blockPos, blockState);
		boundingBox.expand(new BoundingBox(blockPos, blockPos));
		if (BlockTags.LOGS.contains(blockState.getBlock())) {
			set.add(blockPos.immutable());
		}
	}

	private void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		if (this.doUpdate) {
			levelWriter.setBlock(blockPos, blockState, 19);
		} else {
			levelWriter.setBlock(blockPos, blockState, 18);
		}
	}

	public final boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		T featureConfiguration,
		boolean bl
	) {
		Set<BlockPos> set = Sets.<BlockPos>newHashSet();
		BoundingBox boundingBox = BoundingBox.getUnknownBox();
		boolean bl2 = this.doPlace(set, levelAccessor, random, blockPos, boundingBox);
		if (boundingBox.x0 > boundingBox.x1) {
			return false;
		} else {
			List<Set<BlockPos>> list = Lists.<Set<BlockPos>>newArrayList();
			int i = 6;

			for (int j = 0; j < 6; j++) {
				list.add(Sets.newHashSet());
			}

			DiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());

			try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
				if (bl2 && !set.isEmpty()) {
					for (BlockPos blockPos2 : Lists.newArrayList(set)) {
						if (boundingBox.isInside(blockPos2)) {
							discreteVoxelShape.setFull(blockPos2.getX() - boundingBox.x0, blockPos2.getY() - boundingBox.y0, blockPos2.getZ() - boundingBox.z0, true, true);
						}

						for (Direction direction : Direction.values()) {
							pooledMutableBlockPos.set(blockPos2).move(direction);
							if (!set.contains(pooledMutableBlockPos)) {
								BlockState blockState = levelAccessor.getBlockState(pooledMutableBlockPos);
								if (blockState.hasProperty(BlockStateProperties.DISTANCE)) {
									((Set)list.get(0)).add(pooledMutableBlockPos.immutable());
									this.setBlockKnownShape(levelAccessor, pooledMutableBlockPos, blockState.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
									if (boundingBox.isInside(pooledMutableBlockPos)) {
										discreteVoxelShape.setFull(
											pooledMutableBlockPos.getX() - boundingBox.x0,
											pooledMutableBlockPos.getY() - boundingBox.y0,
											pooledMutableBlockPos.getZ() - boundingBox.z0,
											true,
											true
										);
									}
								}
							}
						}
					}
				}

				for (int k = 1; k < 6; k++) {
					Set<BlockPos> set2 = (Set<BlockPos>)list.get(k - 1);
					Set<BlockPos> set3 = (Set<BlockPos>)list.get(k);

					for (BlockPos blockPos3 : set2) {
						if (boundingBox.isInside(blockPos3)) {
							discreteVoxelShape.setFull(blockPos3.getX() - boundingBox.x0, blockPos3.getY() - boundingBox.y0, blockPos3.getZ() - boundingBox.z0, true, true);
						}

						for (Direction direction2 : Direction.values()) {
							pooledMutableBlockPos.set(blockPos3).move(direction2);
							if (!set2.contains(pooledMutableBlockPos) && !set3.contains(pooledMutableBlockPos)) {
								BlockState blockState2 = levelAccessor.getBlockState(pooledMutableBlockPos);
								if (blockState2.hasProperty(BlockStateProperties.DISTANCE)) {
									int l = (Integer)blockState2.getValue(BlockStateProperties.DISTANCE);
									if (l > k + 1) {
										BlockState blockState3 = blockState2.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(k + 1));
										this.setBlockKnownShape(levelAccessor, pooledMutableBlockPos, blockState3);
										if (boundingBox.isInside(pooledMutableBlockPos)) {
											discreteVoxelShape.setFull(
												pooledMutableBlockPos.getX() - boundingBox.x0,
												pooledMutableBlockPos.getY() - boundingBox.y0,
												pooledMutableBlockPos.getZ() - boundingBox.z0,
												true,
												true
											);
										}

										set3.add(pooledMutableBlockPos.immutable());
									}
								}
							}
						}
					}
				}
			}

			if (bl) {
				Biome biome = levelAccessor.getBiome(blockPos);
				if (biome == Biomes.FLOWER_FOREST || biome == Biomes.SUNFLOWER_PLAINS || biome == Biomes.PLAINS) {
					this.spawnBeehive(levelAccessor, random, blockPos, boundingBox, list, biome);
				}
			}

			StructureTemplate.updateShapeAtEdge(levelAccessor, 3, discreteVoxelShape, boundingBox.x0, boundingBox.y0, boundingBox.z0);
			return bl2;
		}
	}

	@Override
	public final boolean place(
		LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, T featureConfiguration
	) {
		return this.place(levelAccessor, chunkGenerator, random, blockPos, featureConfiguration, true);
	}

	private void spawnBeehive(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BoundingBox boundingBox, List<Set<BlockPos>> list, Biome biome) {
		float f = biome == Biomes.FLOWER_FOREST ? 0.01F : 0.05F;
		if (random.nextFloat() < f) {
			Direction direction = BeehiveBlock.SPAWN_DIRECTIONS[random.nextInt(BeehiveBlock.SPAWN_DIRECTIONS.length)];
			int i = boundingBox.y1;
			if (!list.isEmpty()) {
				for (BlockPos blockPos2 : (Set)list.get(0)) {
					if (blockPos2.getY() < i) {
						i = blockPos2.getY();
					}
				}
			}

			BlockState blockState = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH);
			BlockPos blockPos2x = blockPos.offset(direction.getStepX(), i - 1 - blockPos.getY(), direction.getStepZ());
			if (levelAccessor.isEmptyBlock(blockPos2x) && levelAccessor.isEmptyBlock(blockPos2x.relative(Direction.SOUTH))) {
				this.setBlock(levelAccessor, blockPos2x, blockState);
				BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos2x);
				if (blockEntity instanceof BeehiveBlockEntity) {
					BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
					int j = 2 + random.nextInt(2);

					for (int k = 0; k < j; k++) {
						Bee bee = new Bee(EntityType.BEE, levelAccessor.getLevel());
						beehiveBlockEntity.addOccupantWithPresetTicks(bee, false, random.nextInt(599));
					}
				}
			}
		}
	}

	protected abstract boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox);
}
