package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidPiece extends ScatteredFeaturePiece {
	private final boolean[] hasPlacedChest = new boolean[4];

	public DesertPyramidPiece(Random random, int i, int j) {
		super(StructurePieceType.DESERT_PYRAMID_PIECE, i, 64, j, 21, 15, 21, getRandomHorizontalDirection(random));
	}

	public DesertPyramidPiece(ServerLevel serverLevel, CompoundTag compoundTag) {
		super(StructurePieceType.DESERT_PYRAMID_PIECE, compoundTag);
		this.hasPlacedChest[0] = compoundTag.getBoolean("hasPlacedChest0");
		this.hasPlacedChest[1] = compoundTag.getBoolean("hasPlacedChest1");
		this.hasPlacedChest[2] = compoundTag.getBoolean("hasPlacedChest2");
		this.hasPlacedChest[3] = compoundTag.getBoolean("hasPlacedChest3");
	}

	@Override
	protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
		super.addAdditionalSaveData(serverLevel, compoundTag);
		compoundTag.putBoolean("hasPlacedChest0", this.hasPlacedChest[0]);
		compoundTag.putBoolean("hasPlacedChest1", this.hasPlacedChest[1]);
		compoundTag.putBoolean("hasPlacedChest2", this.hasPlacedChest[2]);
		compoundTag.putBoolean("hasPlacedChest3", this.hasPlacedChest[3]);
	}

	@Override
	public boolean postProcess(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		BlockPos blockPos
	) {
		this.generateBox(
			worldGenLevel, boundingBox, 0, -4, 0, this.width - 1, 0, this.depth - 1, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
		);

		for (int i = 1; i <= 9; i++) {
			this.generateBox(
				worldGenLevel,
				boundingBox,
				i,
				i,
				i,
				this.width - 1 - i,
				i,
				this.depth - 1 - i,
				Blocks.SANDSTONE.defaultBlockState(),
				Blocks.SANDSTONE.defaultBlockState(),
				false
			);
			this.generateBox(
				worldGenLevel,
				boundingBox,
				i + 1,
				i,
				i + 1,
				this.width - 2 - i,
				i,
				this.depth - 2 - i,
				Blocks.AIR.defaultBlockState(),
				Blocks.AIR.defaultBlockState(),
				false
			);
		}

		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.depth; j++) {
				int k = -5;
				this.fillColumnDown(worldGenLevel, Blocks.SANDSTONE.defaultBlockState(), i, -5, j, boundingBox);
			}
		}

		BlockState blockState = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
		BlockState blockState2 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
		BlockState blockState3 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
		BlockState blockState4 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
		this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 1, 10, 1, 3, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
		this.placeBlock(worldGenLevel, blockState, 2, 10, 0, boundingBox);
		this.placeBlock(worldGenLevel, blockState2, 2, 10, 4, boundingBox);
		this.placeBlock(worldGenLevel, blockState3, 0, 10, 2, boundingBox);
		this.placeBlock(worldGenLevel, blockState4, 4, 10, 2, boundingBox);
		this.generateBox(
			worldGenLevel, boundingBox, this.width - 5, 0, 0, this.width - 1, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false
		);
		this.generateBox(
			worldGenLevel, boundingBox, this.width - 4, 10, 1, this.width - 2, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
		);
		this.placeBlock(worldGenLevel, blockState, this.width - 3, 10, 0, boundingBox);
		this.placeBlock(worldGenLevel, blockState2, this.width - 3, 10, 4, boundingBox);
		this.placeBlock(worldGenLevel, blockState3, this.width - 5, 10, 2, boundingBox);
		this.placeBlock(worldGenLevel, blockState4, this.width - 1, 10, 2, boundingBox);
		this.generateBox(worldGenLevel, boundingBox, 8, 0, 0, 12, 4, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 9, 1, 0, 11, 3, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 1, 1, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 2, 1, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 3, 1, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, 3, 1, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 3, 1, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 2, 1, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 1, 1, boundingBox);
		this.generateBox(worldGenLevel, boundingBox, 4, 1, 1, 8, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 4, 1, 2, 8, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 12, 1, 1, 16, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 12, 1, 2, 16, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.generateBox(
			worldGenLevel, boundingBox, 5, 4, 5, this.width - 6, 4, this.depth - 6, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
		);
		this.generateBox(worldGenLevel, boundingBox, 9, 4, 9, 11, 4, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 8, 1, 8, 8, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 12, 1, 8, 12, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 8, 1, 12, 8, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 12, 1, 12, 12, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 1, 1, 5, 4, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
		this.generateBox(
			worldGenLevel, boundingBox, this.width - 5, 1, 5, this.width - 2, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
		);
		this.generateBox(worldGenLevel, boundingBox, 6, 7, 9, 6, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
		this.generateBox(
			worldGenLevel, boundingBox, this.width - 7, 7, 9, this.width - 7, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
		);
		this.generateBox(worldGenLevel, boundingBox, 5, 5, 9, 5, 7, 11, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
		this.generateBox(
			worldGenLevel,
			boundingBox,
			this.width - 6,
			5,
			9,
			this.width - 6,
			7,
			11,
			Blocks.CUT_SANDSTONE.defaultBlockState(),
			Blocks.CUT_SANDSTONE.defaultBlockState(),
			false
		);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 5, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 6, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 6, 6, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), this.width - 6, 5, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), this.width - 6, 6, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), this.width - 7, 6, 10, boundingBox);
		this.generateBox(worldGenLevel, boundingBox, 2, 4, 4, 2, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.generateBox(
			worldGenLevel, boundingBox, this.width - 3, 4, 4, this.width - 3, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false
		);
		this.placeBlock(worldGenLevel, blockState, 2, 4, 5, boundingBox);
		this.placeBlock(worldGenLevel, blockState, 2, 3, 4, boundingBox);
		this.placeBlock(worldGenLevel, blockState, this.width - 3, 4, 5, boundingBox);
		this.placeBlock(worldGenLevel, blockState, this.width - 3, 3, 4, boundingBox);
		this.generateBox(worldGenLevel, boundingBox, 1, 1, 3, 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
		this.generateBox(
			worldGenLevel, boundingBox, this.width - 3, 1, 3, this.width - 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
		);
		this.placeBlock(worldGenLevel, Blocks.SANDSTONE.defaultBlockState(), 1, 1, 2, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.SANDSTONE.defaultBlockState(), this.width - 2, 1, 2, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.SANDSTONE_SLAB.defaultBlockState(), 1, 2, 2, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.SANDSTONE_SLAB.defaultBlockState(), this.width - 2, 2, 2, boundingBox);
		this.placeBlock(worldGenLevel, blockState4, 2, 1, 2, boundingBox);
		this.placeBlock(worldGenLevel, blockState3, this.width - 3, 1, 2, boundingBox);
		this.generateBox(worldGenLevel, boundingBox, 4, 3, 5, 4, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
		this.generateBox(
			worldGenLevel, boundingBox, this.width - 5, 3, 5, this.width - 5, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
		);
		this.generateBox(worldGenLevel, boundingBox, 3, 1, 5, 4, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.generateBox(
			worldGenLevel, boundingBox, this.width - 6, 1, 5, this.width - 5, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false
		);

		for (int l = 5; l <= 17; l += 2) {
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 4, 1, l, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 4, 2, l, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), this.width - 5, 1, l, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), this.width - 5, 2, l, boundingBox);
		}

		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 7, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 8, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 9, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 9, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 8, 0, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 12, 0, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 7, 0, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 13, 0, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 11, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 11, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 12, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 13, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.BLUE_TERRACOTTA.defaultBlockState(), 10, 0, 10, boundingBox);

		for (int l = 0; l <= this.width - 1; l += this.width - 1) {
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 2, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 2, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 2, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 3, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 3, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 3, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 4, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), l, 4, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 4, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 5, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 5, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 5, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 6, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), l, 6, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 6, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 7, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 7, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 7, 3, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 8, 1, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 8, 2, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 8, 3, boundingBox);
		}

		for (int l = 2; l <= this.width - 3; l += this.width - 3 - 2) {
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l - 1, 2, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 2, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l + 1, 2, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l - 1, 3, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 3, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l + 1, 3, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l - 1, 4, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), l, 4, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l + 1, 4, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l - 1, 5, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 5, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l + 1, 5, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l - 1, 6, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), l, 6, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l + 1, 6, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l - 1, 7, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l, 7, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), l + 1, 7, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l - 1, 8, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l, 8, 0, boundingBox);
			this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), l + 1, 8, 0, boundingBox);
		}

		this.generateBox(worldGenLevel, boundingBox, 8, 4, 0, 12, 6, 0, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 8, 6, 0, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 12, 6, 0, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 5, 0, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, 5, 0, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 5, 0, boundingBox);
		this.generateBox(
			worldGenLevel, boundingBox, 8, -14, 8, 12, -11, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false
		);
		this.generateBox(
			worldGenLevel, boundingBox, 8, -10, 8, 12, -10, 12, Blocks.CHISELED_SANDSTONE.defaultBlockState(), Blocks.CHISELED_SANDSTONE.defaultBlockState(), false
		);
		this.generateBox(worldGenLevel, boundingBox, 8, -9, 8, 12, -9, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 8, -8, 8, 12, -1, 12, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
		this.generateBox(worldGenLevel, boundingBox, 9, -11, 9, 11, -1, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.placeBlock(worldGenLevel, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 10, -11, 10, boundingBox);
		this.generateBox(worldGenLevel, boundingBox, 9, -13, 9, 11, -13, 11, Blocks.TNT.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 8, -11, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 8, -10, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 7, -10, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 7, -11, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 12, -11, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 12, -10, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 13, -10, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 13, -11, 10, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -11, 8, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -10, 8, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 7, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 7, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -11, 12, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -10, 12, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 13, boundingBox);
		this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 13, boundingBox);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!this.hasPlacedChest[direction.get2DDataValue()]) {
				int m = direction.getStepX() * 2;
				int n = direction.getStepZ() * 2;
				this.hasPlacedChest[direction.get2DDataValue()] = this.createChest(
					worldGenLevel, boundingBox, random, 10 + m, -11, 10 + n, BuiltInLootTables.DESERT_PYRAMID
				);
			}
		}

		return true;
	}
}
