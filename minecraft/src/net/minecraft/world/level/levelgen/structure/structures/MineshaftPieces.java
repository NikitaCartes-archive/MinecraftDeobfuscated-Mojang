package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.slf4j.Logger;

public class MineshaftPieces {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final int DEFAULT_SHAFT_WIDTH = 3;
	private static final int DEFAULT_SHAFT_HEIGHT = 3;
	private static final int DEFAULT_SHAFT_LENGTH = 5;
	private static final int MAX_PILLAR_HEIGHT = 20;
	private static final int MAX_CHAIN_HEIGHT = 50;
	private static final int MAX_DEPTH = 8;
	public static final int MAGIC_START_Y = 50;

	private static MineshaftPieces.MineShaftPiece createRandomShaftPiece(
		StructurePieceAccessor structurePieceAccessor,
		RandomSource randomSource,
		int i,
		int j,
		int k,
		@Nullable Direction direction,
		int l,
		MineshaftStructure.Type type
	) {
		int m = randomSource.nextInt(100);
		if (m >= 80) {
			BoundingBox boundingBox = MineshaftPieces.MineShaftCrossing.findCrossing(structurePieceAccessor, randomSource, i, j, k, direction);
			if (boundingBox != null) {
				return new MineshaftPieces.MineShaftCrossing(l, boundingBox, direction, type);
			}
		} else if (m >= 70) {
			BoundingBox boundingBox = MineshaftPieces.MineShaftStairs.findStairs(structurePieceAccessor, randomSource, i, j, k, direction);
			if (boundingBox != null) {
				return new MineshaftPieces.MineShaftStairs(l, boundingBox, direction, type);
			}
		} else {
			BoundingBox boundingBox = MineshaftPieces.MineShaftCorridor.findCorridorSize(structurePieceAccessor, randomSource, i, j, k, direction);
			if (boundingBox != null) {
				return new MineshaftPieces.MineShaftCorridor(l, randomSource, boundingBox, direction, type);
			}
		}

		return null;
	}

	static MineshaftPieces.MineShaftPiece generateAndAddPiece(
		StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int i, int j, int k, Direction direction, int l
	) {
		if (l > 8) {
			return null;
		} else if (Math.abs(i - structurePiece.getBoundingBox().minX()) <= 80 && Math.abs(k - structurePiece.getBoundingBox().minZ()) <= 80) {
			MineshaftStructure.Type type = ((MineshaftPieces.MineShaftPiece)structurePiece).type;
			MineshaftPieces.MineShaftPiece mineShaftPiece = createRandomShaftPiece(structurePieceAccessor, randomSource, i, j, k, direction, l + 1, type);
			if (mineShaftPiece != null) {
				structurePieceAccessor.addPiece(mineShaftPiece);
				mineShaftPiece.addChildren(structurePiece, structurePieceAccessor, randomSource);
			}

			return mineShaftPiece;
		} else {
			return null;
		}
	}

	public static class MineShaftCorridor extends MineshaftPieces.MineShaftPiece {
		private final boolean hasRails;
		private final boolean spiderCorridor;
		private boolean hasPlacedSpider;
		private final int numSections;

		public MineShaftCorridor(CompoundTag compoundTag) {
			super(StructurePieceType.MINE_SHAFT_CORRIDOR, compoundTag);
			this.hasRails = compoundTag.getBoolean("hr");
			this.spiderCorridor = compoundTag.getBoolean("sc");
			this.hasPlacedSpider = compoundTag.getBoolean("hps");
			this.numSections = compoundTag.getInt("Num");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putBoolean("hr", this.hasRails);
			compoundTag.putBoolean("sc", this.spiderCorridor);
			compoundTag.putBoolean("hps", this.hasPlacedSpider);
			compoundTag.putInt("Num", this.numSections);
		}

		public MineShaftCorridor(int i, RandomSource randomSource, BoundingBox boundingBox, Direction direction, MineshaftStructure.Type type) {
			super(StructurePieceType.MINE_SHAFT_CORRIDOR, i, type, boundingBox);
			this.setOrientation(direction);
			this.hasRails = randomSource.nextInt(3) == 0;
			this.spiderCorridor = !this.hasRails && randomSource.nextInt(23) == 0;
			if (this.getOrientation().getAxis() == Direction.Axis.Z) {
				this.numSections = boundingBox.getZSpan() / 5;
			} else {
				this.numSections = boundingBox.getXSpan() / 5;
			}
		}

		@Nullable
		public static BoundingBox findCorridorSize(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int i, int j, int k, Direction direction) {
			for (int l = randomSource.nextInt(3) + 2; l > 0; l--) {
				int m = l * 5;

				BoundingBox boundingBox = switch (direction) {
					default -> new BoundingBox(0, 0, -(m - 1), 2, 2, 0);
					case SOUTH -> new BoundingBox(0, 0, 0, 2, 2, m - 1);
					case WEST -> new BoundingBox(-(m - 1), 0, 0, 0, 2, 2);
					case EAST -> new BoundingBox(0, 0, 0, m - 1, 2, 2);
				};
				boundingBox.move(i, j, k);
				if (structurePieceAccessor.findCollisionPiece(boundingBox) == null) {
					return boundingBox;
				}
			}

			return null;
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
			int i = this.getGenDepth();
			int j = randomSource.nextInt(4);
			Direction direction = this.getOrientation();
			if (direction != null) {
				switch (direction) {
					case NORTH:
					default:
						if (j <= 1) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.minX(),
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.minZ() - 1,
								direction,
								i
							);
						} else if (j == 2) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.minX() - 1,
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.minZ(),
								Direction.WEST,
								i
							);
						} else {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.maxX() + 1,
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.minZ(),
								Direction.EAST,
								i
							);
						}
						break;
					case SOUTH:
						if (j <= 1) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.minX(),
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.maxZ() + 1,
								direction,
								i
							);
						} else if (j == 2) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.minX() - 1,
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.maxZ() - 3,
								Direction.WEST,
								i
							);
						} else {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.maxX() + 1,
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.maxZ() - 3,
								Direction.EAST,
								i
							);
						}
						break;
					case WEST:
						if (j <= 1) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.minX() - 1,
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.minZ(),
								direction,
								i
							);
						} else if (j == 2) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.minX(),
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.minZ() - 1,
								Direction.NORTH,
								i
							);
						} else {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.minX(),
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.maxZ() + 1,
								Direction.SOUTH,
								i
							);
						}
						break;
					case EAST:
						if (j <= 1) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.maxX() + 1,
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.minZ(),
								direction,
								i
							);
						} else if (j == 2) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.maxX() - 3,
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.minZ() - 1,
								Direction.NORTH,
								i
							);
						} else {
							MineshaftPieces.generateAndAddPiece(
								structurePiece,
								structurePieceAccessor,
								randomSource,
								this.boundingBox.maxX() - 3,
								this.boundingBox.minY() - 1 + randomSource.nextInt(3),
								this.boundingBox.maxZ() + 1,
								Direction.SOUTH,
								i
							);
						}
				}
			}

			if (i < 8) {
				if (direction != Direction.NORTH && direction != Direction.SOUTH) {
					for (int k = this.boundingBox.minX() + 3; k + 3 <= this.boundingBox.maxX(); k += 5) {
						int l = randomSource.nextInt(5);
						if (l == 0) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece, structurePieceAccessor, randomSource, k, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i + 1
							);
						} else if (l == 1) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece, structurePieceAccessor, randomSource, k, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i + 1
							);
						}
					}
				} else {
					for (int kx = this.boundingBox.minZ() + 3; kx + 3 <= this.boundingBox.maxZ(); kx += 5) {
						int l = randomSource.nextInt(5);
						if (l == 0) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY(), kx, Direction.WEST, i + 1
							);
						} else if (l == 1) {
							MineshaftPieces.generateAndAddPiece(
								structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), kx, Direction.EAST, i + 1
							);
						}
					}
				}
			}
		}

		@Override
		protected boolean createChest(
			WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, int i, int j, int k, ResourceLocation resourceLocation
		) {
			BlockPos blockPos = this.getWorldPos(i, j, k);
			if (boundingBox.isInside(blockPos) && worldGenLevel.getBlockState(blockPos).isAir() && !worldGenLevel.getBlockState(blockPos.below()).isAir()) {
				BlockState blockState = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, randomSource.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
				this.placeBlock(worldGenLevel, blockState, i, j, k, boundingBox);
				MinecartChest minecartChest = new MinecartChest(
					worldGenLevel.getLevel(), (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5
				);
				minecartChest.setLootTable(resourceLocation, randomSource.nextLong());
				worldGenLevel.addFreshEntity(minecartChest);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureManager structureManager,
			ChunkGenerator chunkGenerator,
			RandomSource randomSource,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			if (!this.isInInvalidLocation(worldGenLevel, boundingBox)) {
				int i = 0;
				int j = 2;
				int k = 0;
				int l = 2;
				int m = this.numSections * 5 - 1;
				BlockState blockState = this.type.getPlanksState();
				this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 2, 1, m, CAVE_AIR, CAVE_AIR, false);
				this.generateMaybeBox(worldGenLevel, boundingBox, randomSource, 0.8F, 0, 2, 0, 2, 2, m, CAVE_AIR, CAVE_AIR, false, false);
				if (this.spiderCorridor) {
					this.generateMaybeBox(worldGenLevel, boundingBox, randomSource, 0.6F, 0, 0, 0, 2, 1, m, Blocks.COBWEB.defaultBlockState(), CAVE_AIR, false, true);
				}

				for (int n = 0; n < this.numSections; n++) {
					int o = 2 + n * 5;
					this.placeSupport(worldGenLevel, boundingBox, 0, 0, o, 2, 2, randomSource);
					this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.1F, 0, 2, o - 1);
					this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.1F, 2, 2, o - 1);
					this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.1F, 0, 2, o + 1);
					this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.1F, 2, 2, o + 1);
					this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.05F, 0, 2, o - 2);
					this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.05F, 2, 2, o - 2);
					this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.05F, 0, 2, o + 2);
					this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.05F, 2, 2, o + 2);
					if (randomSource.nextInt(100) == 0) {
						this.createChest(worldGenLevel, boundingBox, randomSource, 2, 0, o - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
					}

					if (randomSource.nextInt(100) == 0) {
						this.createChest(worldGenLevel, boundingBox, randomSource, 0, 0, o + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
					}

					if (this.spiderCorridor && !this.hasPlacedSpider) {
						int p = 1;
						int q = o - 1 + randomSource.nextInt(3);
						BlockPos blockPos2 = this.getWorldPos(1, 0, q);
						if (boundingBox.isInside(blockPos2) && this.isInterior(worldGenLevel, 1, 0, q, boundingBox)) {
							this.hasPlacedSpider = true;
							worldGenLevel.setBlock(blockPos2, Blocks.SPAWNER.defaultBlockState(), 2);
							BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos2);
							if (blockEntity instanceof SpawnerBlockEntity) {
								((SpawnerBlockEntity)blockEntity).getSpawner().setEntityId(EntityType.CAVE_SPIDER);
							}
						}
					}
				}

				for (int n = 0; n <= 2; n++) {
					for (int ox = 0; ox <= m; ox++) {
						this.setPlanksBlock(worldGenLevel, boundingBox, blockState, n, -1, ox);
					}
				}

				int n = 2;
				this.placeDoubleLowerOrUpperSupport(worldGenLevel, boundingBox, 0, -1, 2);
				if (this.numSections > 1) {
					int ox = m - 2;
					this.placeDoubleLowerOrUpperSupport(worldGenLevel, boundingBox, 0, -1, ox);
				}

				if (this.hasRails) {
					BlockState blockState2 = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);

					for (int p = 0; p <= m; p++) {
						BlockState blockState3 = this.getBlock(worldGenLevel, 1, -1, p, boundingBox);
						if (!blockState3.isAir() && blockState3.isSolidRender(worldGenLevel, this.getWorldPos(1, -1, p))) {
							float f = this.isInterior(worldGenLevel, 1, 0, p, boundingBox) ? 0.7F : 0.9F;
							this.maybeGenerateBlock(worldGenLevel, boundingBox, randomSource, f, 1, 0, p, blockState2);
						}
					}
				}
			}
		}

		private void placeDoubleLowerOrUpperSupport(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k) {
			BlockState blockState = this.type.getWoodState();
			BlockState blockState2 = this.type.getPlanksState();
			if (this.getBlock(worldGenLevel, i, j, k, boundingBox).is(blockState2.getBlock())) {
				this.fillPillarDownOrChainUp(worldGenLevel, blockState, i, j, k, boundingBox);
			}

			if (this.getBlock(worldGenLevel, i + 2, j, k, boundingBox).is(blockState2.getBlock())) {
				this.fillPillarDownOrChainUp(worldGenLevel, blockState, i + 2, j, k, boundingBox);
			}
		}

		@Override
		protected void fillColumnDown(WorldGenLevel worldGenLevel, BlockState blockState, int i, int j, int k, BoundingBox boundingBox) {
			BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(i, j, k);
			if (boundingBox.isInside(mutableBlockPos)) {
				int l = mutableBlockPos.getY();

				while (this.isReplaceableByStructures(worldGenLevel.getBlockState(mutableBlockPos)) && mutableBlockPos.getY() > worldGenLevel.getMinBuildHeight() + 1) {
					mutableBlockPos.move(Direction.DOWN);
				}

				if (this.canPlaceColumnOnTopOf(worldGenLevel, mutableBlockPos, worldGenLevel.getBlockState(mutableBlockPos))) {
					while (mutableBlockPos.getY() < l) {
						mutableBlockPos.move(Direction.UP);
						worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
					}
				}
			}
		}

		protected void fillPillarDownOrChainUp(WorldGenLevel worldGenLevel, BlockState blockState, int i, int j, int k, BoundingBox boundingBox) {
			BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(i, j, k);
			if (boundingBox.isInside(mutableBlockPos)) {
				int l = mutableBlockPos.getY();
				int m = 1;
				boolean bl = true;

				for (boolean bl2 = true; bl || bl2; m++) {
					if (bl) {
						mutableBlockPos.setY(l - m);
						BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos);
						boolean bl3 = this.isReplaceableByStructures(blockState2) && !blockState2.is(Blocks.LAVA);
						if (!bl3 && this.canPlaceColumnOnTopOf(worldGenLevel, mutableBlockPos, blockState2)) {
							fillColumnBetween(worldGenLevel, blockState, mutableBlockPos, l - m + 1, l);
							return;
						}

						bl = m <= 20 && bl3 && mutableBlockPos.getY() > worldGenLevel.getMinBuildHeight() + 1;
					}

					if (bl2) {
						mutableBlockPos.setY(l + m);
						BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos);
						boolean bl3 = this.isReplaceableByStructures(blockState2);
						if (!bl3 && this.canHangChainBelow(worldGenLevel, mutableBlockPos, blockState2)) {
							worldGenLevel.setBlock(mutableBlockPos.setY(l + 1), this.type.getFenceState(), 2);
							fillColumnBetween(worldGenLevel, Blocks.CHAIN.defaultBlockState(), mutableBlockPos, l + 2, l + m);
							return;
						}

						bl2 = m <= 50 && bl3 && mutableBlockPos.getY() < worldGenLevel.getMaxBuildHeight() - 1;
					}
				}
			}
		}

		private static void fillColumnBetween(WorldGenLevel worldGenLevel, BlockState blockState, BlockPos.MutableBlockPos mutableBlockPos, int i, int j) {
			for (int k = i; k < j; k++) {
				worldGenLevel.setBlock(mutableBlockPos.setY(k), blockState, 2);
			}
		}

		private boolean canPlaceColumnOnTopOf(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
			return blockState.isFaceSturdy(levelReader, blockPos, Direction.UP);
		}

		private boolean canHangChainBelow(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
			return Block.canSupportCenter(levelReader, blockPos, Direction.DOWN) && !(blockState.getBlock() instanceof FallingBlock);
		}

		private void placeSupport(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k, int l, int m, RandomSource randomSource) {
			if (this.isSupportingBox(worldGenLevel, boundingBox, i, m, l, k)) {
				BlockState blockState = this.type.getPlanksState();
				BlockState blockState2 = this.type.getFenceState();
				this.generateBox(worldGenLevel, boundingBox, i, j, k, i, l - 1, k, blockState2.setValue(FenceBlock.WEST, Boolean.valueOf(true)), CAVE_AIR, false);
				this.generateBox(worldGenLevel, boundingBox, m, j, k, m, l - 1, k, blockState2.setValue(FenceBlock.EAST, Boolean.valueOf(true)), CAVE_AIR, false);
				if (randomSource.nextInt(4) == 0) {
					this.generateBox(worldGenLevel, boundingBox, i, l, k, i, l, k, blockState, CAVE_AIR, false);
					this.generateBox(worldGenLevel, boundingBox, m, l, k, m, l, k, blockState, CAVE_AIR, false);
				} else {
					this.generateBox(worldGenLevel, boundingBox, i, l, k, m, l, k, blockState, CAVE_AIR, false);
					this.maybeGenerateBlock(
						worldGenLevel, boundingBox, randomSource, 0.05F, i + 1, l, k - 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH)
					);
					this.maybeGenerateBlock(
						worldGenLevel, boundingBox, randomSource, 0.05F, i + 1, l, k + 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH)
					);
				}
			}
		}

		private void maybePlaceCobWeb(WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, float f, int i, int j, int k) {
			if (this.isInterior(worldGenLevel, i, j, k, boundingBox) && randomSource.nextFloat() < f && this.hasSturdyNeighbours(worldGenLevel, boundingBox, i, j, k, 2)
				)
			 {
				this.placeBlock(worldGenLevel, Blocks.COBWEB.defaultBlockState(), i, j, k, boundingBox);
			}
		}

		private boolean hasSturdyNeighbours(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k, int l) {
			BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(i, j, k);
			int m = 0;

			for (Direction direction : Direction.values()) {
				mutableBlockPos.move(direction);
				if (boundingBox.isInside(mutableBlockPos)
					&& worldGenLevel.getBlockState(mutableBlockPos).isFaceSturdy(worldGenLevel, mutableBlockPos, direction.getOpposite())) {
					if (++m >= l) {
						return true;
					}
				}

				mutableBlockPos.move(direction.getOpposite());
			}

			return false;
		}
	}

	public static class MineShaftCrossing extends MineshaftPieces.MineShaftPiece {
		private final Direction direction;
		private final boolean isTwoFloored;

		public MineShaftCrossing(CompoundTag compoundTag) {
			super(StructurePieceType.MINE_SHAFT_CROSSING, compoundTag);
			this.isTwoFloored = compoundTag.getBoolean("tf");
			this.direction = Direction.from2DDataValue(compoundTag.getInt("D"));
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putBoolean("tf", this.isTwoFloored);
			compoundTag.putInt("D", this.direction.get2DDataValue());
		}

		public MineShaftCrossing(int i, BoundingBox boundingBox, @Nullable Direction direction, MineshaftStructure.Type type) {
			super(StructurePieceType.MINE_SHAFT_CROSSING, i, type, boundingBox);
			this.direction = direction;
			this.isTwoFloored = boundingBox.getYSpan() > 3;
		}

		@Nullable
		public static BoundingBox findCrossing(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int i, int j, int k, Direction direction) {
			int l;
			if (randomSource.nextInt(4) == 0) {
				l = 6;
			} else {
				l = 2;
			}
			BoundingBox boundingBox = switch (direction) {
				default -> new BoundingBox(-1, 0, -4, 3, l, 0);
				case SOUTH -> new BoundingBox(-1, 0, 0, 3, l, 4);
				case WEST -> new BoundingBox(-4, 0, -1, 0, l, 3);
				case EAST -> new BoundingBox(0, 0, -1, 4, l, 3);
			};
			boundingBox.move(i, j, k);
			return structurePieceAccessor.findCollisionPiece(boundingBox) != null ? null : boundingBox;
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
			int i = this.getGenDepth();
			switch (this.direction) {
				case NORTH:
				default:
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ() - 1,
						Direction.NORTH,
						i
					);
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() - 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ() + 1,
						Direction.WEST,
						i
					);
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.maxX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ() + 1,
						Direction.EAST,
						i
					);
					break;
				case SOUTH:
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.maxZ() + 1,
						Direction.SOUTH,
						i
					);
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() - 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ() + 1,
						Direction.WEST,
						i
					);
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.maxX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ() + 1,
						Direction.EAST,
						i
					);
					break;
				case WEST:
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ() - 1,
						Direction.NORTH,
						i
					);
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.maxZ() + 1,
						Direction.SOUTH,
						i
					);
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() - 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ() + 1,
						Direction.WEST,
						i
					);
					break;
				case EAST:
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ() - 1,
						Direction.NORTH,
						i
					);
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.maxZ() + 1,
						Direction.SOUTH,
						i
					);
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.maxX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ() + 1,
						Direction.EAST,
						i
					);
			}

			if (this.isTwoFloored) {
				if (randomSource.nextBoolean()) {
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY() + 3 + 1,
						this.boundingBox.minZ() - 1,
						Direction.NORTH,
						i
					);
				}

				if (randomSource.nextBoolean()) {
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() - 1,
						this.boundingBox.minY() + 3 + 1,
						this.boundingBox.minZ() + 1,
						Direction.WEST,
						i
					);
				}

				if (randomSource.nextBoolean()) {
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.maxX() + 1,
						this.boundingBox.minY() + 3 + 1,
						this.boundingBox.minZ() + 1,
						Direction.EAST,
						i
					);
				}

				if (randomSource.nextBoolean()) {
					MineshaftPieces.generateAndAddPiece(
						structurePiece,
						structurePieceAccessor,
						randomSource,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY() + 3 + 1,
						this.boundingBox.maxZ() + 1,
						Direction.SOUTH,
						i
					);
				}
			}
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureManager structureManager,
			ChunkGenerator chunkGenerator,
			RandomSource randomSource,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			if (!this.isInInvalidLocation(worldGenLevel, boundingBox)) {
				BlockState blockState = this.type.getPlanksState();
				if (this.isTwoFloored) {
					this.generateBox(
						worldGenLevel,
						boundingBox,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ(),
						this.boundingBox.maxX() - 1,
						this.boundingBox.minY() + 3 - 1,
						this.boundingBox.maxZ(),
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						worldGenLevel,
						boundingBox,
						this.boundingBox.minX(),
						this.boundingBox.minY(),
						this.boundingBox.minZ() + 1,
						this.boundingBox.maxX(),
						this.boundingBox.minY() + 3 - 1,
						this.boundingBox.maxZ() - 1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						worldGenLevel,
						boundingBox,
						this.boundingBox.minX() + 1,
						this.boundingBox.maxY() - 2,
						this.boundingBox.minZ(),
						this.boundingBox.maxX() - 1,
						this.boundingBox.maxY(),
						this.boundingBox.maxZ(),
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						worldGenLevel,
						boundingBox,
						this.boundingBox.minX(),
						this.boundingBox.maxY() - 2,
						this.boundingBox.minZ() + 1,
						this.boundingBox.maxX(),
						this.boundingBox.maxY(),
						this.boundingBox.maxZ() - 1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						worldGenLevel,
						boundingBox,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY() + 3,
						this.boundingBox.minZ() + 1,
						this.boundingBox.maxX() - 1,
						this.boundingBox.minY() + 3,
						this.boundingBox.maxZ() - 1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
				} else {
					this.generateBox(
						worldGenLevel,
						boundingBox,
						this.boundingBox.minX() + 1,
						this.boundingBox.minY(),
						this.boundingBox.minZ(),
						this.boundingBox.maxX() - 1,
						this.boundingBox.maxY(),
						this.boundingBox.maxZ(),
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						worldGenLevel,
						boundingBox,
						this.boundingBox.minX(),
						this.boundingBox.minY(),
						this.boundingBox.minZ() + 1,
						this.boundingBox.maxX(),
						this.boundingBox.maxY(),
						this.boundingBox.maxZ() - 1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
				}

				this.placeSupportPillar(
					worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY()
				);
				this.placeSupportPillar(
					worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY()
				);
				this.placeSupportPillar(
					worldGenLevel, boundingBox, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY()
				);
				this.placeSupportPillar(
					worldGenLevel, boundingBox, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY()
				);
				int i = this.boundingBox.minY() - 1;

				for (int j = this.boundingBox.minX(); j <= this.boundingBox.maxX(); j++) {
					for (int k = this.boundingBox.minZ(); k <= this.boundingBox.maxZ(); k++) {
						this.setPlanksBlock(worldGenLevel, boundingBox, blockState, j, i, k);
					}
				}
			}
		}

		private void placeSupportPillar(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k, int l) {
			if (!this.getBlock(worldGenLevel, i, l + 1, k, boundingBox).isAir()) {
				this.generateBox(worldGenLevel, boundingBox, i, j, k, i, l, k, this.type.getPlanksState(), CAVE_AIR, false);
			}
		}
	}

	abstract static class MineShaftPiece extends StructurePiece {
		protected MineshaftStructure.Type type;

		public MineShaftPiece(StructurePieceType structurePieceType, int i, MineshaftStructure.Type type, BoundingBox boundingBox) {
			super(structurePieceType, i, boundingBox);
			this.type = type;
		}

		public MineShaftPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
			super(structurePieceType, compoundTag);
			this.type = MineshaftStructure.Type.byId(compoundTag.getInt("MST"));
		}

		@Override
		protected boolean canBeReplaced(LevelReader levelReader, int i, int j, int k, BoundingBox boundingBox) {
			BlockState blockState = this.getBlock(levelReader, i, j, k, boundingBox);
			return !blockState.is(this.type.getPlanksState().getBlock())
				&& !blockState.is(this.type.getWoodState().getBlock())
				&& !blockState.is(this.type.getFenceState().getBlock())
				&& !blockState.is(Blocks.CHAIN);
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			compoundTag.putInt("MST", this.type.ordinal());
		}

		protected boolean isSupportingBox(BlockGetter blockGetter, BoundingBox boundingBox, int i, int j, int k, int l) {
			for (int m = i; m <= j; m++) {
				if (this.getBlock(blockGetter, m, k + 1, l, boundingBox).isAir()) {
					return false;
				}
			}

			return true;
		}

		protected boolean isInInvalidLocation(LevelAccessor levelAccessor, BoundingBox boundingBox) {
			int i = Math.max(this.boundingBox.minX() - 1, boundingBox.minX());
			int j = Math.max(this.boundingBox.minY() - 1, boundingBox.minY());
			int k = Math.max(this.boundingBox.minZ() - 1, boundingBox.minZ());
			int l = Math.min(this.boundingBox.maxX() + 1, boundingBox.maxX());
			int m = Math.min(this.boundingBox.maxY() + 1, boundingBox.maxY());
			int n = Math.min(this.boundingBox.maxZ() + 1, boundingBox.maxZ());
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos((i + l) / 2, (j + m) / 2, (k + n) / 2);
			if (levelAccessor.getBiome(mutableBlockPos).is(BiomeTags.MINESHAFT_BLOCKING)) {
				return true;
			} else {
				for (int o = i; o <= l; o++) {
					for (int p = k; p <= n; p++) {
						if (levelAccessor.getBlockState(mutableBlockPos.set(o, j, p)).getMaterial().isLiquid()) {
							return true;
						}

						if (levelAccessor.getBlockState(mutableBlockPos.set(o, m, p)).getMaterial().isLiquid()) {
							return true;
						}
					}
				}

				for (int o = i; o <= l; o++) {
					for (int p = j; p <= m; p++) {
						if (levelAccessor.getBlockState(mutableBlockPos.set(o, p, k)).getMaterial().isLiquid()) {
							return true;
						}

						if (levelAccessor.getBlockState(mutableBlockPos.set(o, p, n)).getMaterial().isLiquid()) {
							return true;
						}
					}
				}

				for (int o = k; o <= n; o++) {
					for (int p = j; p <= m; p++) {
						if (levelAccessor.getBlockState(mutableBlockPos.set(i, p, o)).getMaterial().isLiquid()) {
							return true;
						}

						if (levelAccessor.getBlockState(mutableBlockPos.set(l, p, o)).getMaterial().isLiquid()) {
							return true;
						}
					}
				}

				return false;
			}
		}

		protected void setPlanksBlock(WorldGenLevel worldGenLevel, BoundingBox boundingBox, BlockState blockState, int i, int j, int k) {
			if (this.isInterior(worldGenLevel, i, j, k, boundingBox)) {
				BlockPos blockPos = this.getWorldPos(i, j, k);
				BlockState blockState2 = worldGenLevel.getBlockState(blockPos);
				if (!blockState2.isFaceSturdy(worldGenLevel, blockPos, Direction.UP)) {
					worldGenLevel.setBlock(blockPos, blockState, 2);
				}
			}
		}
	}

	public static class MineShaftRoom extends MineshaftPieces.MineShaftPiece {
		private final List<BoundingBox> childEntranceBoxes = Lists.<BoundingBox>newLinkedList();

		public MineShaftRoom(int i, RandomSource randomSource, int j, int k, MineshaftStructure.Type type) {
			super(
				StructurePieceType.MINE_SHAFT_ROOM,
				i,
				type,
				new BoundingBox(j, 50, k, j + 7 + randomSource.nextInt(6), 54 + randomSource.nextInt(6), k + 7 + randomSource.nextInt(6))
			);
			this.type = type;
		}

		public MineShaftRoom(CompoundTag compoundTag) {
			super(StructurePieceType.MINE_SHAFT_ROOM, compoundTag);
			BoundingBox.CODEC
				.listOf()
				.parse(NbtOps.INSTANCE, compoundTag.getList("Entrances", 11))
				.resultOrPartial(MineshaftPieces.LOGGER::error)
				.ifPresent(this.childEntranceBoxes::addAll);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
			int i = this.getGenDepth();
			int j = this.boundingBox.getYSpan() - 3 - 1;
			if (j <= 0) {
				j = 1;
			}

			int k = 0;

			while (k < this.boundingBox.getXSpan()) {
				k += randomSource.nextInt(this.boundingBox.getXSpan());
				if (k + 3 > this.boundingBox.getXSpan()) {
					break;
				}

				MineshaftPieces.MineShaftPiece mineShaftPiece = MineshaftPieces.generateAndAddPiece(
					structurePiece,
					structurePieceAccessor,
					randomSource,
					this.boundingBox.minX() + k,
					this.boundingBox.minY() + randomSource.nextInt(j) + 1,
					this.boundingBox.minZ() - 1,
					Direction.NORTH,
					i
				);
				if (mineShaftPiece != null) {
					BoundingBox boundingBox = mineShaftPiece.getBoundingBox();
					this.childEntranceBoxes
						.add(
							new BoundingBox(boundingBox.minX(), boundingBox.minY(), this.boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), this.boundingBox.minZ() + 1)
						);
				}

				k += 4;
			}

			k = 0;

			while (k < this.boundingBox.getXSpan()) {
				k += randomSource.nextInt(this.boundingBox.getXSpan());
				if (k + 3 > this.boundingBox.getXSpan()) {
					break;
				}

				MineshaftPieces.MineShaftPiece mineShaftPiece = MineshaftPieces.generateAndAddPiece(
					structurePiece,
					structurePieceAccessor,
					randomSource,
					this.boundingBox.minX() + k,
					this.boundingBox.minY() + randomSource.nextInt(j) + 1,
					this.boundingBox.maxZ() + 1,
					Direction.SOUTH,
					i
				);
				if (mineShaftPiece != null) {
					BoundingBox boundingBox = mineShaftPiece.getBoundingBox();
					this.childEntranceBoxes
						.add(
							new BoundingBox(boundingBox.minX(), boundingBox.minY(), this.boundingBox.maxZ() - 1, boundingBox.maxX(), boundingBox.maxY(), this.boundingBox.maxZ())
						);
				}

				k += 4;
			}

			k = 0;

			while (k < this.boundingBox.getZSpan()) {
				k += randomSource.nextInt(this.boundingBox.getZSpan());
				if (k + 3 > this.boundingBox.getZSpan()) {
					break;
				}

				MineshaftPieces.MineShaftPiece mineShaftPiece = MineshaftPieces.generateAndAddPiece(
					structurePiece,
					structurePieceAccessor,
					randomSource,
					this.boundingBox.minX() - 1,
					this.boundingBox.minY() + randomSource.nextInt(j) + 1,
					this.boundingBox.minZ() + k,
					Direction.WEST,
					i
				);
				if (mineShaftPiece != null) {
					BoundingBox boundingBox = mineShaftPiece.getBoundingBox();
					this.childEntranceBoxes
						.add(
							new BoundingBox(this.boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), this.boundingBox.minX() + 1, boundingBox.maxY(), boundingBox.maxZ())
						);
				}

				k += 4;
			}

			k = 0;

			while (k < this.boundingBox.getZSpan()) {
				k += randomSource.nextInt(this.boundingBox.getZSpan());
				if (k + 3 > this.boundingBox.getZSpan()) {
					break;
				}

				StructurePiece structurePiece2 = MineshaftPieces.generateAndAddPiece(
					structurePiece,
					structurePieceAccessor,
					randomSource,
					this.boundingBox.maxX() + 1,
					this.boundingBox.minY() + randomSource.nextInt(j) + 1,
					this.boundingBox.minZ() + k,
					Direction.EAST,
					i
				);
				if (structurePiece2 != null) {
					BoundingBox boundingBox = structurePiece2.getBoundingBox();
					this.childEntranceBoxes
						.add(
							new BoundingBox(this.boundingBox.maxX() - 1, boundingBox.minY(), boundingBox.minZ(), this.boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ())
						);
				}

				k += 4;
			}
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureManager structureManager,
			ChunkGenerator chunkGenerator,
			RandomSource randomSource,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			if (!this.isInInvalidLocation(worldGenLevel, boundingBox)) {
				this.generateBox(
					worldGenLevel,
					boundingBox,
					this.boundingBox.minX(),
					this.boundingBox.minY() + 1,
					this.boundingBox.minZ(),
					this.boundingBox.maxX(),
					Math.min(this.boundingBox.minY() + 3, this.boundingBox.maxY()),
					this.boundingBox.maxZ(),
					CAVE_AIR,
					CAVE_AIR,
					false
				);

				for (BoundingBox boundingBox2 : this.childEntranceBoxes) {
					this.generateBox(
						worldGenLevel,
						boundingBox,
						boundingBox2.minX(),
						boundingBox2.maxY() - 2,
						boundingBox2.minZ(),
						boundingBox2.maxX(),
						boundingBox2.maxY(),
						boundingBox2.maxZ(),
						CAVE_AIR,
						CAVE_AIR,
						false
					);
				}

				this.generateUpperHalfSphere(
					worldGenLevel,
					boundingBox,
					this.boundingBox.minX(),
					this.boundingBox.minY() + 4,
					this.boundingBox.minZ(),
					this.boundingBox.maxX(),
					this.boundingBox.maxY(),
					this.boundingBox.maxZ(),
					CAVE_AIR,
					false
				);
			}
		}

		@Override
		public void move(int i, int j, int k) {
			super.move(i, j, k);

			for (BoundingBox boundingBox : this.childEntranceBoxes) {
				boundingBox.move(i, j, k);
			}
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			BoundingBox.CODEC
				.listOf()
				.encodeStart(NbtOps.INSTANCE, this.childEntranceBoxes)
				.resultOrPartial(MineshaftPieces.LOGGER::error)
				.ifPresent(tag -> compoundTag.put("Entrances", tag));
		}
	}

	public static class MineShaftStairs extends MineshaftPieces.MineShaftPiece {
		public MineShaftStairs(int i, BoundingBox boundingBox, Direction direction, MineshaftStructure.Type type) {
			super(StructurePieceType.MINE_SHAFT_STAIRS, i, type, boundingBox);
			this.setOrientation(direction);
		}

		public MineShaftStairs(CompoundTag compoundTag) {
			super(StructurePieceType.MINE_SHAFT_STAIRS, compoundTag);
		}

		@Nullable
		public static BoundingBox findStairs(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int i, int j, int k, Direction direction) {
			BoundingBox boundingBox = switch (direction) {
				default -> new BoundingBox(0, -5, -8, 2, 2, 0);
				case SOUTH -> new BoundingBox(0, -5, 0, 2, 2, 8);
				case WEST -> new BoundingBox(-8, -5, 0, 0, 2, 2);
				case EAST -> new BoundingBox(0, -5, 0, 8, 2, 2);
			};
			boundingBox.move(i, j, k);
			return structurePieceAccessor.findCollisionPiece(boundingBox) != null ? null : boundingBox;
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
			int i = this.getGenDepth();
			Direction direction = this.getOrientation();
			if (direction != null) {
				switch (direction) {
					case NORTH:
					default:
						MineshaftPieces.generateAndAddPiece(
							structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i
						);
						break;
					case SOUTH:
						MineshaftPieces.generateAndAddPiece(
							structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i
						);
						break;
					case WEST:
						MineshaftPieces.generateAndAddPiece(
							structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.WEST, i
						);
						break;
					case EAST:
						MineshaftPieces.generateAndAddPiece(
							structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.EAST, i
						);
				}
			}
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureManager structureManager,
			ChunkGenerator chunkGenerator,
			RandomSource randomSource,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			if (!this.isInInvalidLocation(worldGenLevel, boundingBox)) {
				this.generateBox(worldGenLevel, boundingBox, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);

				for (int i = 0; i < 5; i++) {
					this.generateBox(worldGenLevel, boundingBox, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, CAVE_AIR, CAVE_AIR, false);
				}
			}
		}
	}
}
