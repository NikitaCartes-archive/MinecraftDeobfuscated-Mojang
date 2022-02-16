package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class StrongholdPieces {
	private static final int SMALL_DOOR_WIDTH = 3;
	private static final int SMALL_DOOR_HEIGHT = 3;
	private static final int MAX_DEPTH = 50;
	private static final int LOWEST_Y_POSITION = 10;
	private static final boolean CHECK_AIR = true;
	public static final int MAGIC_START_Y = 64;
	private static final StrongholdPieces.PieceWeight[] STRONGHOLD_PIECE_WEIGHTS = new StrongholdPieces.PieceWeight[]{
		new StrongholdPieces.PieceWeight(StrongholdPieces.Straight.class, 40, 0),
		new StrongholdPieces.PieceWeight(StrongholdPieces.PrisonHall.class, 5, 5),
		new StrongholdPieces.PieceWeight(StrongholdPieces.LeftTurn.class, 20, 0),
		new StrongholdPieces.PieceWeight(StrongholdPieces.RightTurn.class, 20, 0),
		new StrongholdPieces.PieceWeight(StrongholdPieces.RoomCrossing.class, 10, 6),
		new StrongholdPieces.PieceWeight(StrongholdPieces.StraightStairsDown.class, 5, 5),
		new StrongholdPieces.PieceWeight(StrongholdPieces.StairsDown.class, 5, 5),
		new StrongholdPieces.PieceWeight(StrongholdPieces.FiveCrossing.class, 5, 4),
		new StrongholdPieces.PieceWeight(StrongholdPieces.ChestCorridor.class, 5, 4),
		new StrongholdPieces.PieceWeight(StrongholdPieces.Library.class, 10, 2) {
			@Override
			public boolean doPlace(int i) {
				return super.doPlace(i) && i > 4;
			}
		},
		new StrongholdPieces.PieceWeight(StrongholdPieces.PortalRoom.class, 20, 1) {
			@Override
			public boolean doPlace(int i) {
				return super.doPlace(i) && i > 5;
			}
		}
	};
	private static List<StrongholdPieces.PieceWeight> currentPieces;
	static Class<? extends StrongholdPieces.StrongholdPiece> imposedPiece;
	private static int totalWeight;
	static final StrongholdPieces.SmoothStoneSelector SMOOTH_STONE_SELECTOR = new StrongholdPieces.SmoothStoneSelector();

	public static void resetPieces() {
		currentPieces = Lists.<StrongholdPieces.PieceWeight>newArrayList();

		for (StrongholdPieces.PieceWeight pieceWeight : STRONGHOLD_PIECE_WEIGHTS) {
			pieceWeight.placeCount = 0;
			currentPieces.add(pieceWeight);
		}

		imposedPiece = null;
	}

	private static boolean updatePieceWeight() {
		boolean bl = false;
		totalWeight = 0;

		for (StrongholdPieces.PieceWeight pieceWeight : currentPieces) {
			if (pieceWeight.maxPlaceCount > 0 && pieceWeight.placeCount < pieceWeight.maxPlaceCount) {
				bl = true;
			}

			totalWeight = totalWeight + pieceWeight.weight;
		}

		return bl;
	}

	private static StrongholdPieces.StrongholdPiece findAndCreatePieceFactory(
		Class<? extends StrongholdPieces.StrongholdPiece> class_,
		StructurePieceAccessor structurePieceAccessor,
		Random random,
		int i,
		int j,
		int k,
		@Nullable Direction direction,
		int l
	) {
		StrongholdPieces.StrongholdPiece strongholdPiece = null;
		if (class_ == StrongholdPieces.Straight.class) {
			strongholdPiece = StrongholdPieces.Straight.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.PrisonHall.class) {
			strongholdPiece = StrongholdPieces.PrisonHall.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.LeftTurn.class) {
			strongholdPiece = StrongholdPieces.LeftTurn.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.RightTurn.class) {
			strongholdPiece = StrongholdPieces.RightTurn.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.RoomCrossing.class) {
			strongholdPiece = StrongholdPieces.RoomCrossing.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.StraightStairsDown.class) {
			strongholdPiece = StrongholdPieces.StraightStairsDown.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.StairsDown.class) {
			strongholdPiece = StrongholdPieces.StairsDown.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.FiveCrossing.class) {
			strongholdPiece = StrongholdPieces.FiveCrossing.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.ChestCorridor.class) {
			strongholdPiece = StrongholdPieces.ChestCorridor.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.Library.class) {
			strongholdPiece = StrongholdPieces.Library.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
		} else if (class_ == StrongholdPieces.PortalRoom.class) {
			strongholdPiece = StrongholdPieces.PortalRoom.createPiece(structurePieceAccessor, i, j, k, direction, l);
		}

		return strongholdPiece;
	}

	private static StrongholdPieces.StrongholdPiece generatePieceFromSmallDoor(
		StrongholdPieces.StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
	) {
		if (!updatePieceWeight()) {
			return null;
		} else {
			if (imposedPiece != null) {
				StrongholdPieces.StrongholdPiece strongholdPiece = findAndCreatePieceFactory(imposedPiece, structurePieceAccessor, random, i, j, k, direction, l);
				imposedPiece = null;
				if (strongholdPiece != null) {
					return strongholdPiece;
				}
			}

			int m = 0;

			while (m < 5) {
				m++;
				int n = random.nextInt(totalWeight);

				for (StrongholdPieces.PieceWeight pieceWeight : currentPieces) {
					n -= pieceWeight.weight;
					if (n < 0) {
						if (!pieceWeight.doPlace(l) || pieceWeight == startPiece.previousPiece) {
							break;
						}

						StrongholdPieces.StrongholdPiece strongholdPiece2 = findAndCreatePieceFactory(
							pieceWeight.pieceClass, structurePieceAccessor, random, i, j, k, direction, l
						);
						if (strongholdPiece2 != null) {
							pieceWeight.placeCount++;
							startPiece.previousPiece = pieceWeight;
							if (!pieceWeight.isValid()) {
								currentPieces.remove(pieceWeight);
							}

							return strongholdPiece2;
						}
					}
				}
			}

			BoundingBox boundingBox = StrongholdPieces.FillerCorridor.findPieceBox(structurePieceAccessor, random, i, j, k, direction);
			return boundingBox != null && boundingBox.minY() > 1 ? new StrongholdPieces.FillerCorridor(l, boundingBox, direction) : null;
		}
	}

	static StructurePiece generateAndAddPiece(
		StrongholdPieces.StartPiece startPiece,
		StructurePieceAccessor structurePieceAccessor,
		Random random,
		int i,
		int j,
		int k,
		@Nullable Direction direction,
		int l
	) {
		if (l > 50) {
			return null;
		} else if (Math.abs(i - startPiece.getBoundingBox().minX()) <= 112 && Math.abs(k - startPiece.getBoundingBox().minZ()) <= 112) {
			StructurePiece structurePiece = generatePieceFromSmallDoor(startPiece, structurePieceAccessor, random, i, j, k, direction, l + 1);
			if (structurePiece != null) {
				structurePieceAccessor.addPiece(structurePiece);
				startPiece.pendingChildren.add(structurePiece);
			}

			return structurePiece;
		} else {
			return null;
		}
	}

	public static class ChestCorridor extends StrongholdPieces.StrongholdPiece {
		private static final int WIDTH = 5;
		private static final int HEIGHT = 5;
		private static final int DEPTH = 7;
		private boolean hasPlacedChest;

		public ChestCorridor(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, i, boundingBox);
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
		}

		public ChestCorridor(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, compoundTag);
			this.hasPlacedChest = compoundTag.getBoolean("Chest");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putBoolean("Chest", this.hasPlacedChest);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 1);
		}

		public static StrongholdPieces.ChestCorridor createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 7, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.ChestCorridor(l, random, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 6, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
			this.generateBox(worldGenLevel, boundingBox, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(), false);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 5, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 4, boundingBox);

			for (int i = 2; i <= 4; i++) {
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2, 1, i, boundingBox);
			}

			if (!this.hasPlacedChest && boundingBox.isInside(this.getWorldPos(3, 2, 3))) {
				this.hasPlacedChest = true;
				this.createChest(worldGenLevel, boundingBox, random, 3, 2, 3, BuiltInLootTables.STRONGHOLD_CORRIDOR);
			}
		}
	}

	public static class FillerCorridor extends StrongholdPieces.StrongholdPiece {
		private final int steps;

		public FillerCorridor(int i, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, i, boundingBox);
			this.setOrientation(direction);
			this.steps = direction != Direction.NORTH && direction != Direction.SOUTH ? boundingBox.getXSpan() : boundingBox.getZSpan();
		}

		public FillerCorridor(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, compoundTag);
			this.steps = compoundTag.getInt("Steps");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putInt("Steps", this.steps);
		}

		public static BoundingBox findPieceBox(StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction) {
			int l = 3;
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 4, direction);
			StructurePiece structurePiece = structurePieceAccessor.findCollisionPiece(boundingBox);
			if (structurePiece == null) {
				return null;
			} else {
				if (structurePiece.getBoundingBox().minY() == boundingBox.minY()) {
					for (int m = 2; m >= 1; m--) {
						boundingBox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, m, direction);
						if (!structurePiece.getBoundingBox().intersects(boundingBox)) {
							return BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, m + 1, direction);
						}
					}
				}

				return null;
			}
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			for (int i = 0; i < this.steps; i++) {
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 0, i, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 0, i, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 0, i, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 0, i, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 0, i, boundingBox);

				for (int j = 1; j <= 3; j++) {
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, j, i, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), 1, j, i, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), 2, j, i, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), 3, j, i, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, j, i, boundingBox);
				}

				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 4, i, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, i, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, i, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 4, i, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 4, i, boundingBox);
			}
		}
	}

	public static class FiveCrossing extends StrongholdPieces.StrongholdPiece {
		protected static final int WIDTH = 10;
		protected static final int HEIGHT = 9;
		protected static final int DEPTH = 11;
		private final boolean leftLow;
		private final boolean leftHigh;
		private final boolean rightLow;
		private final boolean rightHigh;

		public FiveCrossing(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, i, boundingBox);
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
			this.leftLow = random.nextBoolean();
			this.leftHigh = random.nextBoolean();
			this.rightLow = random.nextBoolean();
			this.rightHigh = random.nextInt(3) > 0;
		}

		public FiveCrossing(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, compoundTag);
			this.leftLow = compoundTag.getBoolean("leftLow");
			this.leftHigh = compoundTag.getBoolean("leftHigh");
			this.rightLow = compoundTag.getBoolean("rightLow");
			this.rightHigh = compoundTag.getBoolean("rightHigh");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putBoolean("leftLow", this.leftLow);
			compoundTag.putBoolean("leftHigh", this.leftHigh);
			compoundTag.putBoolean("rightLow", this.rightLow);
			compoundTag.putBoolean("rightHigh", this.rightHigh);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			int i = 3;
			int j = 5;
			Direction direction = this.getOrientation();
			if (direction == Direction.WEST || direction == Direction.NORTH) {
				i = 8 - i;
				j = 8 - j;
			}

			this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 5, 1);
			if (this.leftLow) {
				this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, i, 1);
			}

			if (this.leftHigh) {
				this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, j, 7);
			}

			if (this.rightLow) {
				this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, i, 1);
			}

			if (this.rightHigh) {
				this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, j, 7);
			}
		}

		public static StrongholdPieces.FiveCrossing createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -4, -3, 0, 10, 9, 11, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.FiveCrossing(l, random, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 9, 8, 10, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 4, 3, 0);
			if (this.leftLow) {
				this.generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 5, 3, CAVE_AIR, CAVE_AIR, false);
			}

			if (this.rightLow) {
				this.generateBox(worldGenLevel, boundingBox, 9, 3, 1, 9, 5, 3, CAVE_AIR, CAVE_AIR, false);
			}

			if (this.leftHigh) {
				this.generateBox(worldGenLevel, boundingBox, 0, 5, 7, 0, 7, 9, CAVE_AIR, CAVE_AIR, false);
			}

			if (this.rightHigh) {
				this.generateBox(worldGenLevel, boundingBox, 9, 5, 7, 9, 7, 9, CAVE_AIR, CAVE_AIR, false);
			}

			this.generateBox(worldGenLevel, boundingBox, 5, 1, 10, 7, 3, 10, CAVE_AIR, CAVE_AIR, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 2, 1, 8, 2, 6, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 4, 1, 5, 4, 4, 9, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 8, 1, 5, 8, 4, 9, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 1, 4, 7, 3, 4, 9, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 1, 3, 5, 3, 3, 6, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(
				worldGenLevel, boundingBox, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false
			);
			this.generateBox(
				worldGenLevel, boundingBox, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false
			);
			this.generateBox(worldGenLevel, boundingBox, 5, 1, 7, 7, 1, 8, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(
				worldGenLevel, boundingBox, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false
			);
			this.generateBox(
				worldGenLevel, boundingBox, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false
			);
			this.generateBox(
				worldGenLevel, boundingBox, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false
			);
			this.generateBox(
				worldGenLevel, boundingBox, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false
			);
			this.generateBox(
				worldGenLevel,
				boundingBox,
				5,
				5,
				7,
				7,
				5,
				9,
				Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE),
				Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE),
				false
			);
			this.placeBlock(worldGenLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, boundingBox);
		}
	}

	public static class LeftTurn extends StrongholdPieces.Turn {
		public LeftTurn(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_LEFT_TURN, i, boundingBox);
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
		}

		public LeftTurn(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_LEFT_TURN, compoundTag);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			Direction direction = this.getOrientation();
			if (direction != Direction.NORTH && direction != Direction.EAST) {
				this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 1);
			} else {
				this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 1);
			}
		}

		public static StrongholdPieces.LeftTurn createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 5, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.LeftTurn(l, random, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 4, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
			Direction direction = this.getOrientation();
			if (direction != Direction.NORTH && direction != Direction.EAST) {
				this.generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
			} else {
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
			}
		}
	}

	public static class Library extends StrongholdPieces.StrongholdPiece {
		protected static final int WIDTH = 14;
		protected static final int HEIGHT = 6;
		protected static final int TALL_HEIGHT = 11;
		protected static final int DEPTH = 15;
		private final boolean isTall;

		public Library(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_LIBRARY, i, boundingBox);
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
			this.isTall = boundingBox.getYSpan() > 6;
		}

		public Library(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_LIBRARY, compoundTag);
			this.isTall = compoundTag.getBoolean("Tall");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putBoolean("Tall", this.isTall);
		}

		public static StrongholdPieces.Library createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -4, -1, 0, 14, 11, 15, direction);
			if (!isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
				boundingBox = BoundingBox.orientBox(i, j, k, -4, -1, 0, 14, 6, 15, direction);
				if (!isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
					return null;
				}
			}

			return new StrongholdPieces.Library(l, random, boundingBox, direction);
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			int i = 11;
			if (!this.isTall) {
				i = 6;
			}

			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 13, i - 1, 14, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 4, 1, 0);
			this.generateMaybeBox(
				worldGenLevel, boundingBox, random, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.defaultBlockState(), Blocks.COBWEB.defaultBlockState(), false, false
			);
			int j = 1;
			int k = 12;

			for (int l = 1; l <= 13; l++) {
				if ((l - 1) % 4 == 0) {
					this.generateBox(worldGenLevel, boundingBox, 1, 1, l, 1, 4, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
					this.generateBox(worldGenLevel, boundingBox, 12, 1, l, 12, 4, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
					this.placeBlock(worldGenLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 2, 3, l, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 11, 3, l, boundingBox);
					if (this.isTall) {
						this.generateBox(worldGenLevel, boundingBox, 1, 6, l, 1, 9, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
						this.generateBox(worldGenLevel, boundingBox, 12, 6, l, 12, 9, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
					}
				} else {
					this.generateBox(worldGenLevel, boundingBox, 1, 1, l, 1, 4, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
					this.generateBox(worldGenLevel, boundingBox, 12, 1, l, 12, 4, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
					if (this.isTall) {
						this.generateBox(worldGenLevel, boundingBox, 1, 6, l, 1, 9, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
						this.generateBox(worldGenLevel, boundingBox, 12, 6, l, 12, 9, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
					}
				}
			}

			for (int lx = 3; lx < 12; lx += 2) {
				this.generateBox(worldGenLevel, boundingBox, 3, 1, lx, 4, 3, lx, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
				this.generateBox(worldGenLevel, boundingBox, 6, 1, lx, 7, 3, lx, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
				this.generateBox(worldGenLevel, boundingBox, 9, 1, lx, 10, 3, lx, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
			}

			if (this.isTall) {
				this.generateBox(worldGenLevel, boundingBox, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
				this.generateBox(worldGenLevel, boundingBox, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
				this.generateBox(worldGenLevel, boundingBox, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
				this.generateBox(worldGenLevel, boundingBox, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
				this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 11, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 5, 11, boundingBox);
				this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 10, boundingBox);
				BlockState blockState = Blocks.OAK_FENCE
					.defaultBlockState()
					.setValue(FenceBlock.WEST, Boolean.valueOf(true))
					.setValue(FenceBlock.EAST, Boolean.valueOf(true));
				BlockState blockState2 = Blocks.OAK_FENCE
					.defaultBlockState()
					.setValue(FenceBlock.NORTH, Boolean.valueOf(true))
					.setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
				this.generateBox(worldGenLevel, boundingBox, 3, 6, 3, 3, 6, 11, blockState2, blockState2, false);
				this.generateBox(worldGenLevel, boundingBox, 10, 6, 3, 10, 6, 9, blockState2, blockState2, false);
				this.generateBox(worldGenLevel, boundingBox, 4, 6, 2, 9, 6, 2, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 4, 6, 12, 7, 6, 12, blockState, blockState, false);
				this.placeBlock(
					worldGenLevel,
					Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)),
					3,
					6,
					2,
					boundingBox
				);
				this.placeBlock(
					worldGenLevel,
					Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)),
					3,
					6,
					12,
					boundingBox
				);
				this.placeBlock(
					worldGenLevel,
					Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)),
					10,
					6,
					2,
					boundingBox
				);

				for (int m = 0; m <= 2; m++) {
					this.placeBlock(
						worldGenLevel,
						Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)),
						8 + m,
						6,
						12 - m,
						boundingBox
					);
					if (m != 2) {
						this.placeBlock(
							worldGenLevel,
							Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)),
							8 + m,
							6,
							11 - m,
							boundingBox
						);
					}
				}

				BlockState blockState3 = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
				this.placeBlock(worldGenLevel, blockState3, 10, 1, 13, boundingBox);
				this.placeBlock(worldGenLevel, blockState3, 10, 2, 13, boundingBox);
				this.placeBlock(worldGenLevel, blockState3, 10, 3, 13, boundingBox);
				this.placeBlock(worldGenLevel, blockState3, 10, 4, 13, boundingBox);
				this.placeBlock(worldGenLevel, blockState3, 10, 5, 13, boundingBox);
				this.placeBlock(worldGenLevel, blockState3, 10, 6, 13, boundingBox);
				this.placeBlock(worldGenLevel, blockState3, 10, 7, 13, boundingBox);
				int n = 7;
				int o = 7;
				BlockState blockState4 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true));
				this.placeBlock(worldGenLevel, blockState4, 6, 9, 7, boundingBox);
				BlockState blockState5 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true));
				this.placeBlock(worldGenLevel, blockState5, 7, 9, 7, boundingBox);
				this.placeBlock(worldGenLevel, blockState4, 6, 8, 7, boundingBox);
				this.placeBlock(worldGenLevel, blockState5, 7, 8, 7, boundingBox);
				BlockState blockState6 = blockState2.setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
				this.placeBlock(worldGenLevel, blockState6, 6, 7, 7, boundingBox);
				this.placeBlock(worldGenLevel, blockState6, 7, 7, 7, boundingBox);
				this.placeBlock(worldGenLevel, blockState4, 5, 7, 7, boundingBox);
				this.placeBlock(worldGenLevel, blockState5, 8, 7, 7, boundingBox);
				this.placeBlock(worldGenLevel, blockState4.setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 6, 7, 6, boundingBox);
				this.placeBlock(worldGenLevel, blockState4.setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 6, 7, 8, boundingBox);
				this.placeBlock(worldGenLevel, blockState5.setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 7, 7, 6, boundingBox);
				this.placeBlock(worldGenLevel, blockState5.setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 7, 7, 8, boundingBox);
				BlockState blockState7 = Blocks.TORCH.defaultBlockState();
				this.placeBlock(worldGenLevel, blockState7, 5, 8, 7, boundingBox);
				this.placeBlock(worldGenLevel, blockState7, 8, 8, 7, boundingBox);
				this.placeBlock(worldGenLevel, blockState7, 6, 8, 6, boundingBox);
				this.placeBlock(worldGenLevel, blockState7, 6, 8, 8, boundingBox);
				this.placeBlock(worldGenLevel, blockState7, 7, 8, 6, boundingBox);
				this.placeBlock(worldGenLevel, blockState7, 7, 8, 8, boundingBox);
			}

			this.createChest(worldGenLevel, boundingBox, random, 3, 3, 5, BuiltInLootTables.STRONGHOLD_LIBRARY);
			if (this.isTall) {
				this.placeBlock(worldGenLevel, CAVE_AIR, 12, 9, 1, boundingBox);
				this.createChest(worldGenLevel, boundingBox, random, 12, 8, 1, BuiltInLootTables.STRONGHOLD_LIBRARY);
			}
		}
	}

	static class PieceWeight {
		public final Class<? extends StrongholdPieces.StrongholdPiece> pieceClass;
		public final int weight;
		public int placeCount;
		public final int maxPlaceCount;

		public PieceWeight(Class<? extends StrongholdPieces.StrongholdPiece> class_, int i, int j) {
			this.pieceClass = class_;
			this.weight = i;
			this.maxPlaceCount = j;
		}

		public boolean doPlace(int i) {
			return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
		}

		public boolean isValid() {
			return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
		}
	}

	public static class PortalRoom extends StrongholdPieces.StrongholdPiece {
		protected static final int WIDTH = 11;
		protected static final int HEIGHT = 8;
		protected static final int DEPTH = 16;
		private boolean hasPlacedSpawner;

		public PortalRoom(int i, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, i, boundingBox);
			this.setOrientation(direction);
		}

		public PortalRoom(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, compoundTag);
			this.hasPlacedSpawner = compoundTag.getBoolean("Mob");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putBoolean("Mob", this.hasPlacedSpawner);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			if (structurePiece != null) {
				((StrongholdPieces.StartPiece)structurePiece).portalRoomPiece = this;
			}
		}

		public static StrongholdPieces.PortalRoom createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, Direction direction, int l) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -4, -1, 0, 11, 8, 16, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.PortalRoom(l, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 10, 7, 15, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPieces.StrongholdPiece.SmallDoorType.GRATES, 4, 1, 0);
			int i = 6;
			this.generateBox(worldGenLevel, boundingBox, 1, i, 1, 1, i, 14, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 9, i, 1, 9, i, 14, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 2, i, 1, 8, i, 2, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 2, i, 14, 8, i, 14, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 1, 2, 1, 4, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 8, 1, 1, 9, 1, 4, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 1, 1, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
			this.generateBox(worldGenLevel, boundingBox, 9, 1, 1, 9, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
			this.generateBox(worldGenLevel, boundingBox, 3, 1, 8, 7, 1, 12, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 4, 1, 9, 6, 1, 11, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
			BlockState blockState = Blocks.IRON_BARS
				.defaultBlockState()
				.setValue(IronBarsBlock.NORTH, Boolean.valueOf(true))
				.setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true));
			BlockState blockState2 = Blocks.IRON_BARS
				.defaultBlockState()
				.setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
				.setValue(IronBarsBlock.EAST, Boolean.valueOf(true));

			for (int j = 3; j < 14; j += 2) {
				this.generateBox(worldGenLevel, boundingBox, 0, 3, j, 0, 4, j, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 10, 3, j, 10, 4, j, blockState, blockState, false);
			}

			for (int j = 2; j < 9; j += 2) {
				this.generateBox(worldGenLevel, boundingBox, j, 3, 15, j, 4, 15, blockState2, blockState2, false);
			}

			BlockState blockState3 = Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
			this.generateBox(worldGenLevel, boundingBox, 4, 1, 5, 6, 1, 7, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 4, 2, 6, 6, 2, 7, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 4, 3, 7, 6, 3, 7, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);

			for (int k = 4; k <= 6; k++) {
				this.placeBlock(worldGenLevel, blockState3, k, 1, 4, boundingBox);
				this.placeBlock(worldGenLevel, blockState3, k, 2, 5, boundingBox);
				this.placeBlock(worldGenLevel, blockState3, k, 3, 6, boundingBox);
			}

			BlockState blockState4 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
			BlockState blockState5 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
			BlockState blockState6 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.EAST);
			BlockState blockState7 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.WEST);
			boolean bl = true;
			boolean[] bls = new boolean[12];

			for (int l = 0; l < bls.length; l++) {
				bls[l] = random.nextFloat() > 0.9F;
				bl &= bls[l];
			}

			this.placeBlock(worldGenLevel, blockState4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[0])), 4, 3, 8, boundingBox);
			this.placeBlock(worldGenLevel, blockState4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[1])), 5, 3, 8, boundingBox);
			this.placeBlock(worldGenLevel, blockState4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[2])), 6, 3, 8, boundingBox);
			this.placeBlock(worldGenLevel, blockState5.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[3])), 4, 3, 12, boundingBox);
			this.placeBlock(worldGenLevel, blockState5.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[4])), 5, 3, 12, boundingBox);
			this.placeBlock(worldGenLevel, blockState5.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[5])), 6, 3, 12, boundingBox);
			this.placeBlock(worldGenLevel, blockState6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[6])), 3, 3, 9, boundingBox);
			this.placeBlock(worldGenLevel, blockState6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[7])), 3, 3, 10, boundingBox);
			this.placeBlock(worldGenLevel, blockState6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[8])), 3, 3, 11, boundingBox);
			this.placeBlock(worldGenLevel, blockState7.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[9])), 7, 3, 9, boundingBox);
			this.placeBlock(worldGenLevel, blockState7.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[10])), 7, 3, 10, boundingBox);
			this.placeBlock(worldGenLevel, blockState7.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(bls[11])), 7, 3, 11, boundingBox);
			if (bl) {
				BlockState blockState8 = Blocks.END_PORTAL.defaultBlockState();
				this.placeBlock(worldGenLevel, blockState8, 4, 3, 9, boundingBox);
				this.placeBlock(worldGenLevel, blockState8, 5, 3, 9, boundingBox);
				this.placeBlock(worldGenLevel, blockState8, 6, 3, 9, boundingBox);
				this.placeBlock(worldGenLevel, blockState8, 4, 3, 10, boundingBox);
				this.placeBlock(worldGenLevel, blockState8, 5, 3, 10, boundingBox);
				this.placeBlock(worldGenLevel, blockState8, 6, 3, 10, boundingBox);
				this.placeBlock(worldGenLevel, blockState8, 4, 3, 11, boundingBox);
				this.placeBlock(worldGenLevel, blockState8, 5, 3, 11, boundingBox);
				this.placeBlock(worldGenLevel, blockState8, 6, 3, 11, boundingBox);
			}

			if (!this.hasPlacedSpawner) {
				BlockPos blockPos2 = this.getWorldPos(5, 3, 6);
				if (boundingBox.isInside(blockPos2)) {
					this.hasPlacedSpawner = true;
					worldGenLevel.setBlock(blockPos2, Blocks.SPAWNER.defaultBlockState(), 2);
					BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos2);
					if (blockEntity instanceof SpawnerBlockEntity) {
						((SpawnerBlockEntity)blockEntity).getSpawner().setEntityId(EntityType.SILVERFISH);
					}
				}
			}
		}
	}

	public static class PrisonHall extends StrongholdPieces.StrongholdPiece {
		protected static final int WIDTH = 9;
		protected static final int HEIGHT = 5;
		protected static final int DEPTH = 11;

		public PrisonHall(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_PRISON_HALL, i, boundingBox);
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
		}

		public PrisonHall(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_PRISON_HALL, compoundTag);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 1);
		}

		public static StrongholdPieces.PrisonHall createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 9, 5, 11, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.PrisonHall(l, random, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 8, 4, 10, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 10, 3, 3, 10, CAVE_AIR, CAVE_AIR, false);
			this.generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 3, 1, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 4, 1, 3, 4, 3, 3, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 4, 1, 7, 4, 3, 7, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateBox(worldGenLevel, boundingBox, 4, 1, 9, 4, 3, 9, false, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);

			for (int i = 1; i <= 3; i++) {
				this.placeBlock(
					worldGenLevel,
					Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true)),
					4,
					i,
					4,
					boundingBox
				);
				this.placeBlock(
					worldGenLevel,
					Blocks.IRON_BARS
						.defaultBlockState()
						.setValue(IronBarsBlock.NORTH, Boolean.valueOf(true))
						.setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
						.setValue(IronBarsBlock.EAST, Boolean.valueOf(true)),
					4,
					i,
					5,
					boundingBox
				);
				this.placeBlock(
					worldGenLevel,
					Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true)),
					4,
					i,
					6,
					boundingBox
				);
				this.placeBlock(
					worldGenLevel,
					Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)).setValue(IronBarsBlock.EAST, Boolean.valueOf(true)),
					5,
					i,
					5,
					boundingBox
				);
				this.placeBlock(
					worldGenLevel,
					Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)).setValue(IronBarsBlock.EAST, Boolean.valueOf(true)),
					6,
					i,
					5,
					boundingBox
				);
				this.placeBlock(
					worldGenLevel,
					Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)).setValue(IronBarsBlock.EAST, Boolean.valueOf(true)),
					7,
					i,
					5,
					boundingBox
				);
			}

			this.placeBlock(
				worldGenLevel,
				Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true)),
				4,
				3,
				2,
				boundingBox
			);
			this.placeBlock(
				worldGenLevel,
				Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true)),
				4,
				3,
				8,
				boundingBox
			);
			BlockState blockState = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
			BlockState blockState2 = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
			this.placeBlock(worldGenLevel, blockState, 4, 1, 2, boundingBox);
			this.placeBlock(worldGenLevel, blockState2, 4, 2, 2, boundingBox);
			this.placeBlock(worldGenLevel, blockState, 4, 1, 8, boundingBox);
			this.placeBlock(worldGenLevel, blockState2, 4, 2, 8, boundingBox);
		}
	}

	public static class RightTurn extends StrongholdPieces.Turn {
		public RightTurn(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_RIGHT_TURN, i, boundingBox);
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
		}

		public RightTurn(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_RIGHT_TURN, compoundTag);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			Direction direction = this.getOrientation();
			if (direction != Direction.NORTH && direction != Direction.EAST) {
				this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 1);
			} else {
				this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 1);
			}
		}

		public static StrongholdPieces.RightTurn createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 5, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.RightTurn(l, random, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 4, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
			Direction direction = this.getOrientation();
			if (direction != Direction.NORTH && direction != Direction.EAST) {
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
			} else {
				this.generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
			}
		}
	}

	public static class RoomCrossing extends StrongholdPieces.StrongholdPiece {
		protected static final int WIDTH = 11;
		protected static final int HEIGHT = 7;
		protected static final int DEPTH = 11;
		protected final int type;

		public RoomCrossing(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, i, boundingBox);
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
			this.type = random.nextInt(5);
		}

		public RoomCrossing(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, compoundTag);
			this.type = compoundTag.getInt("Type");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putInt("Type", this.type);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 4, 1);
			this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 4);
			this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 4);
		}

		public static StrongholdPieces.RoomCrossing createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -4, -1, 0, 11, 7, 11, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.RoomCrossing(l, random, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 10, 6, 10, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 4, 1, 0);
			this.generateBox(worldGenLevel, boundingBox, 4, 1, 10, 6, 3, 10, CAVE_AIR, CAVE_AIR, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 1, 4, 0, 3, 6, CAVE_AIR, CAVE_AIR, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 1, 4, 10, 3, 6, CAVE_AIR, CAVE_AIR, false);
			switch (this.type) {
				case 0:
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 4, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 6, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 4, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 6, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 4, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 6, boundingBox);
					break;
				case 1:
					for (int i = 0; i < 5; i++) {
						this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 1, 3 + i, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 7, 1, 3 + i, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i, 1, 3, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i, 1, 7, boundingBox);
					}

					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.WATER.defaultBlockState(), 5, 4, 5, boundingBox);
					break;
				case 2:
					for (int i = 1; i <= 9; i++) {
						this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 1, 3, i, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 9, 3, i, boundingBox);
					}

					for (int i = 1; i <= 9; i++) {
						this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), i, 3, 1, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), i, 3, 9, boundingBox);
					}

					this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 4, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 6, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 4, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 6, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 1, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 1, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 3, 5, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 3, 5, boundingBox);

					for (int i = 1; i <= 3; i++) {
						this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, i, 4, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, i, 4, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, i, 6, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, i, 6, boundingBox);
					}

					this.placeBlock(worldGenLevel, Blocks.TORCH.defaultBlockState(), 5, 3, 5, boundingBox);

					for (int i = 2; i <= 8; i++) {
						this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 2, 3, i, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 3, 3, i, boundingBox);
						if (i <= 3 || i >= 7) {
							this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 4, 3, i, boundingBox);
							this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 5, 3, i, boundingBox);
							this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 6, 3, i, boundingBox);
						}

						this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 7, 3, i, boundingBox);
						this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 3, i, boundingBox);
					}

					BlockState blockState = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);
					this.placeBlock(worldGenLevel, blockState, 9, 1, 3, boundingBox);
					this.placeBlock(worldGenLevel, blockState, 9, 2, 3, boundingBox);
					this.placeBlock(worldGenLevel, blockState, 9, 3, 3, boundingBox);
					this.createChest(worldGenLevel, boundingBox, random, 3, 4, 8, BuiltInLootTables.STRONGHOLD_CROSSING);
			}
		}
	}

	static class SmoothStoneSelector extends StructurePiece.BlockSelector {
		@Override
		public void next(Random random, int i, int j, int k, boolean bl) {
			if (bl) {
				float f = random.nextFloat();
				if (f < 0.2F) {
					this.next = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
				} else if (f < 0.5F) {
					this.next = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
				} else if (f < 0.55F) {
					this.next = Blocks.INFESTED_STONE_BRICKS.defaultBlockState();
				} else {
					this.next = Blocks.STONE_BRICKS.defaultBlockState();
				}
			} else {
				this.next = Blocks.CAVE_AIR.defaultBlockState();
			}
		}
	}

	public static class StairsDown extends StrongholdPieces.StrongholdPiece {
		private static final int WIDTH = 5;
		private static final int HEIGHT = 11;
		private static final int DEPTH = 5;
		private final boolean isSource;

		public StairsDown(StructurePieceType structurePieceType, int i, int j, int k, Direction direction) {
			super(structurePieceType, i, makeBoundingBox(j, 64, k, direction, 5, 11, 5));
			this.isSource = true;
			this.setOrientation(direction);
			this.entryDoor = StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING;
		}

		public StairsDown(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_STAIRS_DOWN, i, boundingBox);
			this.isSource = false;
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
		}

		public StairsDown(StructurePieceType structurePieceType, CompoundTag compoundTag) {
			super(structurePieceType, compoundTag);
			this.isSource = compoundTag.getBoolean("Source");
		}

		public StairsDown(CompoundTag compoundTag) {
			this(StructurePieceType.STRONGHOLD_STAIRS_DOWN, compoundTag);
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putBoolean("Source", this.isSource);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			if (this.isSource) {
				StrongholdPieces.imposedPiece = StrongholdPieces.FiveCrossing.class;
			}

			this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 1);
		}

		public static StrongholdPieces.StairsDown createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -7, 0, 5, 11, 5, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.StairsDown(l, random, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 10, 4, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 7, 0);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 4);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 6, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 6, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 5, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 4, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 2, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 3, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 2, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 2, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 1, 3, boundingBox);
		}
	}

	public static class StartPiece extends StrongholdPieces.StairsDown {
		public StrongholdPieces.PieceWeight previousPiece;
		@Nullable
		public StrongholdPieces.PortalRoom portalRoomPiece;
		public final List<StructurePiece> pendingChildren = Lists.<StructurePiece>newArrayList();

		public StartPiece(Random random, int i, int j) {
			super(StructurePieceType.STRONGHOLD_START, 0, i, j, getRandomHorizontalDirection(random));
		}

		public StartPiece(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_START, compoundTag);
		}

		@Override
		public BlockPos getLocatorPosition() {
			return this.portalRoomPiece != null ? this.portalRoomPiece.getLocatorPosition() : super.getLocatorPosition();
		}
	}

	public static class Straight extends StrongholdPieces.StrongholdPiece {
		private static final int WIDTH = 5;
		private static final int HEIGHT = 5;
		private static final int DEPTH = 7;
		private final boolean leftChild;
		private final boolean rightChild;

		public Straight(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_STRAIGHT, i, boundingBox);
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
			this.leftChild = random.nextInt(2) == 0;
			this.rightChild = random.nextInt(2) == 0;
		}

		public Straight(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_STRAIGHT, compoundTag);
			this.leftChild = compoundTag.getBoolean("Left");
			this.rightChild = compoundTag.getBoolean("Right");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putBoolean("Left", this.leftChild);
			compoundTag.putBoolean("Right", this.rightChild);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 1);
			if (this.leftChild) {
				this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 2);
			}

			if (this.rightChild) {
				this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 2);
			}
		}

		public static StrongholdPieces.Straight createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 7, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.Straight(l, random, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 6, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
			BlockState blockState = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST);
			BlockState blockState2 = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST);
			this.maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.1F, 1, 2, 1, blockState);
			this.maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.1F, 3, 2, 1, blockState2);
			this.maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.1F, 1, 2, 5, blockState);
			this.maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.1F, 3, 2, 5, blockState2);
			if (this.leftChild) {
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 2, 0, 3, 4, CAVE_AIR, CAVE_AIR, false);
			}

			if (this.rightChild) {
				this.generateBox(worldGenLevel, boundingBox, 4, 1, 2, 4, 3, 4, CAVE_AIR, CAVE_AIR, false);
			}
		}
	}

	public static class StraightStairsDown extends StrongholdPieces.StrongholdPiece {
		private static final int WIDTH = 5;
		private static final int HEIGHT = 11;
		private static final int DEPTH = 8;

		public StraightStairsDown(int i, Random random, BoundingBox boundingBox, Direction direction) {
			super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, i, boundingBox);
			this.setOrientation(direction);
			this.entryDoor = this.randomSmallDoor(random);
		}

		public StraightStairsDown(CompoundTag compoundTag) {
			super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, compoundTag);
		}

		@Override
		public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
			this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurePiece, structurePieceAccessor, random, 1, 1);
		}

		public static StrongholdPieces.StraightStairsDown createPiece(
			StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l
		) {
			BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -7, 0, 5, 11, 8, direction);
			return isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null
				? new StrongholdPieces.StraightStairsDown(l, random, boundingBox, direction)
				: null;
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 10, 7, true, random, StrongholdPieces.SMOOTH_STONE_SELECTOR);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 7, 0);
			this.generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 7);
			BlockState blockState = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);

			for (int i = 0; i < 6; i++) {
				this.placeBlock(worldGenLevel, blockState, 1, 6 - i, 1 + i, boundingBox);
				this.placeBlock(worldGenLevel, blockState, 2, 6 - i, 1 + i, boundingBox);
				this.placeBlock(worldGenLevel, blockState, 3, 6 - i, 1 + i, boundingBox);
				if (i < 5) {
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5 - i, 1 + i, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 5 - i, 1 + i, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 5 - i, 1 + i, boundingBox);
				}
			}
		}
	}

	abstract static class StrongholdPiece extends StructurePiece {
		protected StrongholdPieces.StrongholdPiece.SmallDoorType entryDoor = StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING;

		protected StrongholdPiece(StructurePieceType structurePieceType, int i, BoundingBox boundingBox) {
			super(structurePieceType, i, boundingBox);
		}

		public StrongholdPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
			super(structurePieceType, compoundTag);
			this.entryDoor = StrongholdPieces.StrongholdPiece.SmallDoorType.valueOf(compoundTag.getString("EntryDoor"));
		}

		@Override
		public NoiseEffect getNoiseEffect() {
			return NoiseEffect.BURY;
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			compoundTag.putString("EntryDoor", this.entryDoor.name());
		}

		protected void generateSmallDoor(
			WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox, StrongholdPieces.StrongholdPiece.SmallDoorType smallDoorType, int i, int j, int k
		) {
			switch (smallDoorType) {
				case OPENING:
					this.generateBox(worldGenLevel, boundingBox, i, j, k, i + 3 - 1, j + 3 - 1, k, CAVE_AIR, CAVE_AIR, false);
					break;
				case WOOD_DOOR:
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 1, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 2, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 1, j + 2, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 2, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 1, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.OAK_DOOR.defaultBlockState(), i + 1, j, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), i + 1, j + 1, k, boundingBox);
					break;
				case GRATES:
					this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), i + 1, j, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), i + 1, j + 1, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)), i, j, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)), i, j + 1, k, boundingBox);
					this.placeBlock(
						worldGenLevel,
						Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true)),
						i,
						j + 2,
						k,
						boundingBox
					);
					this.placeBlock(
						worldGenLevel,
						Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true)),
						i + 1,
						j + 2,
						k,
						boundingBox
					);
					this.placeBlock(
						worldGenLevel,
						Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true)),
						i + 2,
						j + 2,
						k,
						boundingBox
					);
					this.placeBlock(worldGenLevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)), i + 2, j + 1, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)), i + 2, j, k, boundingBox);
					break;
				case IRON_DOOR:
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 1, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 2, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 1, j + 2, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 2, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 1, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.IRON_DOOR.defaultBlockState(), i + 1, j, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), i + 1, j + 1, k, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.NORTH), i + 2, j + 1, k + 1, boundingBox);
					this.placeBlock(worldGenLevel, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.SOUTH), i + 2, j + 1, k - 1, boundingBox);
			}
		}

		protected StrongholdPieces.StrongholdPiece.SmallDoorType randomSmallDoor(Random random) {
			int i = random.nextInt(5);
			switch (i) {
				case 0:
				case 1:
				default:
					return StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING;
				case 2:
					return StrongholdPieces.StrongholdPiece.SmallDoorType.WOOD_DOOR;
				case 3:
					return StrongholdPieces.StrongholdPiece.SmallDoorType.GRATES;
				case 4:
					return StrongholdPieces.StrongholdPiece.SmallDoorType.IRON_DOOR;
			}
		}

		@Nullable
		protected StructurePiece generateSmallDoorChildForward(
			StrongholdPieces.StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j
		) {
			Direction direction = this.getOrientation();
			if (direction != null) {
				switch (direction) {
					case NORTH:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.minX() + i,
							this.boundingBox.minY() + j,
							this.boundingBox.minZ() - 1,
							direction,
							this.getGenDepth()
						);
					case SOUTH:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.minX() + i,
							this.boundingBox.minY() + j,
							this.boundingBox.maxZ() + 1,
							direction,
							this.getGenDepth()
						);
					case WEST:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.minX() - 1,
							this.boundingBox.minY() + j,
							this.boundingBox.minZ() + i,
							direction,
							this.getGenDepth()
						);
					case EAST:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.maxX() + 1,
							this.boundingBox.minY() + j,
							this.boundingBox.minZ() + i,
							direction,
							this.getGenDepth()
						);
				}
			}

			return null;
		}

		@Nullable
		protected StructurePiece generateSmallDoorChildLeft(
			StrongholdPieces.StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j
		) {
			Direction direction = this.getOrientation();
			if (direction != null) {
				switch (direction) {
					case NORTH:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.minX() - 1,
							this.boundingBox.minY() + i,
							this.boundingBox.minZ() + j,
							Direction.WEST,
							this.getGenDepth()
						);
					case SOUTH:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.minX() - 1,
							this.boundingBox.minY() + i,
							this.boundingBox.minZ() + j,
							Direction.WEST,
							this.getGenDepth()
						);
					case WEST:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.minX() + j,
							this.boundingBox.minY() + i,
							this.boundingBox.minZ() - 1,
							Direction.NORTH,
							this.getGenDepth()
						);
					case EAST:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.minX() + j,
							this.boundingBox.minY() + i,
							this.boundingBox.minZ() - 1,
							Direction.NORTH,
							this.getGenDepth()
						);
				}
			}

			return null;
		}

		@Nullable
		protected StructurePiece generateSmallDoorChildRight(
			StrongholdPieces.StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j
		) {
			Direction direction = this.getOrientation();
			if (direction != null) {
				switch (direction) {
					case NORTH:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.maxX() + 1,
							this.boundingBox.minY() + i,
							this.boundingBox.minZ() + j,
							Direction.EAST,
							this.getGenDepth()
						);
					case SOUTH:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.maxX() + 1,
							this.boundingBox.minY() + i,
							this.boundingBox.minZ() + j,
							Direction.EAST,
							this.getGenDepth()
						);
					case WEST:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.minX() + j,
							this.boundingBox.minY() + i,
							this.boundingBox.maxZ() + 1,
							Direction.SOUTH,
							this.getGenDepth()
						);
					case EAST:
						return StrongholdPieces.generateAndAddPiece(
							startPiece,
							structurePieceAccessor,
							random,
							this.boundingBox.minX() + j,
							this.boundingBox.minY() + i,
							this.boundingBox.maxZ() + 1,
							Direction.SOUTH,
							this.getGenDepth()
						);
				}
			}

			return null;
		}

		protected static boolean isOkBox(BoundingBox boundingBox) {
			return boundingBox != null && boundingBox.minY() > 10;
		}

		protected static enum SmallDoorType {
			OPENING,
			WOOD_DOOR,
			GRATES,
			IRON_DOOR;
		}
	}

	public abstract static class Turn extends StrongholdPieces.StrongholdPiece {
		protected static final int WIDTH = 5;
		protected static final int HEIGHT = 5;
		protected static final int DEPTH = 5;

		protected Turn(StructurePieceType structurePieceType, int i, BoundingBox boundingBox) {
			super(structurePieceType, i, boundingBox);
		}

		public Turn(StructurePieceType structurePieceType, CompoundTag compoundTag) {
			super(structurePieceType, compoundTag);
		}
	}
}
