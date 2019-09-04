package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class MineShaftPieces {
	private static MineShaftPieces.MineShaftPiece createRandomShaftPiece(
		List<StructurePiece> list, Random random, int i, int j, int k, @Nullable Direction direction, int l, MineshaftFeature.Type type
	) {
		int m = random.nextInt(100);
		if (m >= 80) {
			BoundingBox boundingBox = MineShaftPieces.MineShaftCrossing.findCrossing(list, random, i, j, k, direction);
			if (boundingBox != null) {
				return new MineShaftPieces.MineShaftCrossing(l, boundingBox, direction, type);
			}
		} else if (m >= 70) {
			BoundingBox boundingBox = MineShaftPieces.MineShaftStairs.findStairs(list, random, i, j, k, direction);
			if (boundingBox != null) {
				return new MineShaftPieces.MineShaftStairs(l, boundingBox, direction, type);
			}
		} else {
			BoundingBox boundingBox = MineShaftPieces.MineShaftCorridor.findCorridorSize(list, random, i, j, k, direction);
			if (boundingBox != null) {
				return new MineShaftPieces.MineShaftCorridor(l, random, boundingBox, direction, type);
			}
		}

		return null;
	}

	private static MineShaftPieces.MineShaftPiece generateAndAddPiece(
		StructurePiece structurePiece, List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l
	) {
		if (l > 8) {
			return null;
		} else if (Math.abs(i - structurePiece.getBoundingBox().x0) <= 80 && Math.abs(k - structurePiece.getBoundingBox().z0) <= 80) {
			MineshaftFeature.Type type = ((MineShaftPieces.MineShaftPiece)structurePiece).type;
			MineShaftPieces.MineShaftPiece mineShaftPiece = createRandomShaftPiece(list, random, i, j, k, direction, l + 1, type);
			if (mineShaftPiece != null) {
				list.add(mineShaftPiece);
				mineShaftPiece.addChildren(structurePiece, list, random);
			}

			return mineShaftPiece;
		} else {
			return null;
		}
	}

	public static class MineShaftCorridor extends MineShaftPieces.MineShaftPiece {
		private final boolean hasRails;
		private final boolean spiderCorridor;
		private boolean hasPlacedSpider;
		private final int numSections;

		public MineShaftCorridor(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.MINE_SHAFT_CORRIDOR, compoundTag);
			this.hasRails = compoundTag.getBoolean("hr");
			this.spiderCorridor = compoundTag.getBoolean("sc");
			this.hasPlacedSpider = compoundTag.getBoolean("hps");
			this.numSections = compoundTag.getInt("Num");
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			super.addAdditionalSaveData(compoundTag);
			compoundTag.putBoolean("hr", this.hasRails);
			compoundTag.putBoolean("sc", this.spiderCorridor);
			compoundTag.putBoolean("hps", this.hasPlacedSpider);
			compoundTag.putInt("Num", this.numSections);
		}

		public MineShaftCorridor(int i, Random random, BoundingBox boundingBox, Direction direction, MineshaftFeature.Type type) {
			super(StructurePieceType.MINE_SHAFT_CORRIDOR, i, type);
			this.setOrientation(direction);
			this.boundingBox = boundingBox;
			this.hasRails = random.nextInt(3) == 0;
			this.spiderCorridor = !this.hasRails && random.nextInt(23) == 0;
			if (this.getOrientation().getAxis() == Direction.Axis.Z) {
				this.numSections = boundingBox.getZSpan() / 5;
			} else {
				this.numSections = boundingBox.getXSpan() / 5;
			}
		}

		public static BoundingBox findCorridorSize(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction) {
			BoundingBox boundingBox = new BoundingBox(i, j, k, i, j + 3 - 1, k);

			int l;
			for (l = random.nextInt(3) + 2; l > 0; l--) {
				int m = l * 5;
				switch (direction) {
					case NORTH:
					default:
						boundingBox.x1 = i + 3 - 1;
						boundingBox.z0 = k - (m - 1);
						break;
					case SOUTH:
						boundingBox.x1 = i + 3 - 1;
						boundingBox.z1 = k + m - 1;
						break;
					case WEST:
						boundingBox.x0 = i - (m - 1);
						boundingBox.z1 = k + 3 - 1;
						break;
					case EAST:
						boundingBox.x1 = i + m - 1;
						boundingBox.z1 = k + 3 - 1;
				}

				if (StructurePiece.findCollisionPiece(list, boundingBox) == null) {
					break;
				}
			}

			return l > 0 ? boundingBox : null;
		}

		@Override
		public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
			int i = this.getGenDepth();
			int j = random.nextInt(4);
			Direction direction = this.getOrientation();
			if (direction != null) {
				switch (direction) {
					case NORTH:
					default:
						if (j <= 1) {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x0, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z0 - 1, direction, i
							);
						} else if (j == 2) {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z0, Direction.WEST, i
							);
						} else {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z0, Direction.EAST, i
							);
						}
						break;
					case SOUTH:
						if (j <= 1) {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x0, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z1 + 1, direction, i
							);
						} else if (j == 2) {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z1 - 3, Direction.WEST, i
							);
						} else {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z1 - 3, Direction.EAST, i
							);
						}
						break;
					case WEST:
						if (j <= 1) {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z0, direction, i
							);
						} else if (j == 2) {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x0, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z0 - 1, Direction.NORTH, i
							);
						} else {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x0, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z1 + 1, Direction.SOUTH, i
							);
						}
						break;
					case EAST:
						if (j <= 1) {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z0, direction, i
							);
						} else if (j == 2) {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x1 - 3, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z0 - 1, Direction.NORTH, i
							);
						} else {
							MineShaftPieces.generateAndAddPiece(
								structurePiece, list, random, this.boundingBox.x1 - 3, this.boundingBox.y0 - 1 + random.nextInt(3), this.boundingBox.z1 + 1, Direction.SOUTH, i
							);
						}
				}
			}

			if (i < 8) {
				if (direction != Direction.NORTH && direction != Direction.SOUTH) {
					for (int k = this.boundingBox.x0 + 3; k + 3 <= this.boundingBox.x1; k += 5) {
						int l = random.nextInt(5);
						if (l == 0) {
							MineShaftPieces.generateAndAddPiece(structurePiece, list, random, k, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, i + 1);
						} else if (l == 1) {
							MineShaftPieces.generateAndAddPiece(structurePiece, list, random, k, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, i + 1);
						}
					}
				} else {
					for (int kx = this.boundingBox.z0 + 3; kx + 3 <= this.boundingBox.z1; kx += 5) {
						int l = random.nextInt(5);
						if (l == 0) {
							MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, kx, Direction.WEST, i + 1);
						} else if (l == 1) {
							MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, kx, Direction.EAST, i + 1);
						}
					}
				}
			}
		}

		@Override
		protected boolean createChest(LevelAccessor levelAccessor, BoundingBox boundingBox, Random random, int i, int j, int k, ResourceLocation resourceLocation) {
			BlockPos blockPos = new BlockPos(this.getWorldX(i, k), this.getWorldY(j), this.getWorldZ(i, k));
			if (boundingBox.isInside(blockPos) && levelAccessor.getBlockState(blockPos).isAir() && !levelAccessor.getBlockState(blockPos.below()).isAir()) {
				BlockState blockState = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, random.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
				this.placeBlock(levelAccessor, blockState, i, j, k, boundingBox);
				MinecartChest minecartChest = new MinecartChest(
					levelAccessor.getLevel(), (double)((float)blockPos.getX() + 0.5F), (double)((float)blockPos.getY() + 0.5F), (double)((float)blockPos.getZ() + 0.5F)
				);
				minecartChest.setLootTable(resourceLocation, random.nextLong());
				levelAccessor.addFreshEntity(minecartChest);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean postProcess(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
			if (this.edgesLiquid(levelAccessor, boundingBox)) {
				return false;
			} else {
				int i = 0;
				int j = 2;
				int k = 0;
				int l = 2;
				int m = this.numSections * 5 - 1;
				BlockState blockState = this.getPlanksBlock();
				this.generateBox(levelAccessor, boundingBox, 0, 0, 0, 2, 1, m, CAVE_AIR, CAVE_AIR, false);
				this.generateMaybeBox(levelAccessor, boundingBox, random, 0.8F, 0, 2, 0, 2, 2, m, CAVE_AIR, CAVE_AIR, false, false);
				if (this.spiderCorridor) {
					this.generateMaybeBox(levelAccessor, boundingBox, random, 0.6F, 0, 0, 0, 2, 1, m, Blocks.COBWEB.defaultBlockState(), CAVE_AIR, false, true);
				}

				for (int n = 0; n < this.numSections; n++) {
					int o = 2 + n * 5;
					this.placeSupport(levelAccessor, boundingBox, 0, 0, o, 2, 2, random);
					this.placeCobWeb(levelAccessor, boundingBox, random, 0.1F, 0, 2, o - 1);
					this.placeCobWeb(levelAccessor, boundingBox, random, 0.1F, 2, 2, o - 1);
					this.placeCobWeb(levelAccessor, boundingBox, random, 0.1F, 0, 2, o + 1);
					this.placeCobWeb(levelAccessor, boundingBox, random, 0.1F, 2, 2, o + 1);
					this.placeCobWeb(levelAccessor, boundingBox, random, 0.05F, 0, 2, o - 2);
					this.placeCobWeb(levelAccessor, boundingBox, random, 0.05F, 2, 2, o - 2);
					this.placeCobWeb(levelAccessor, boundingBox, random, 0.05F, 0, 2, o + 2);
					this.placeCobWeb(levelAccessor, boundingBox, random, 0.05F, 2, 2, o + 2);
					if (random.nextInt(100) == 0) {
						this.createChest(levelAccessor, boundingBox, random, 2, 0, o - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
					}

					if (random.nextInt(100) == 0) {
						this.createChest(levelAccessor, boundingBox, random, 0, 0, o + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
					}

					if (this.spiderCorridor && !this.hasPlacedSpider) {
						int p = this.getWorldY(0);
						int q = o - 1 + random.nextInt(3);
						int r = this.getWorldX(1, q);
						int s = this.getWorldZ(1, q);
						BlockPos blockPos = new BlockPos(r, p, s);
						if (boundingBox.isInside(blockPos) && this.isInterior(levelAccessor, 1, 0, q, boundingBox)) {
							this.hasPlacedSpider = true;
							levelAccessor.setBlock(blockPos, Blocks.SPAWNER.defaultBlockState(), 2);
							BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
							if (blockEntity instanceof SpawnerBlockEntity) {
								((SpawnerBlockEntity)blockEntity).getSpawner().setEntityId(EntityType.CAVE_SPIDER);
							}
						}
					}
				}

				for (int n = 0; n <= 2; n++) {
					for (int ox = 0; ox <= m; ox++) {
						int p = -1;
						BlockState blockState2 = this.getBlock(levelAccessor, n, -1, ox, boundingBox);
						if (blockState2.isAir() && this.isInterior(levelAccessor, n, -1, ox, boundingBox)) {
							int r = -1;
							this.placeBlock(levelAccessor, blockState, n, -1, ox, boundingBox);
						}
					}
				}

				if (this.hasRails) {
					BlockState blockState3 = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);

					for (int oxx = 0; oxx <= m; oxx++) {
						BlockState blockState4 = this.getBlock(levelAccessor, 1, -1, oxx, boundingBox);
						if (!blockState4.isAir() && blockState4.isSolidRender(levelAccessor, new BlockPos(this.getWorldX(1, oxx), this.getWorldY(-1), this.getWorldZ(1, oxx)))) {
							float f = this.isInterior(levelAccessor, 1, 0, oxx, boundingBox) ? 0.7F : 0.9F;
							this.maybeGenerateBlock(levelAccessor, boundingBox, random, f, 1, 0, oxx, blockState3);
						}
					}
				}

				return true;
			}
		}

		private void placeSupport(LevelAccessor levelAccessor, BoundingBox boundingBox, int i, int j, int k, int l, int m, Random random) {
			if (this.isSupportingBox(levelAccessor, boundingBox, i, m, l, k)) {
				BlockState blockState = this.getPlanksBlock();
				BlockState blockState2 = this.getFenceBlock();
				this.generateBox(levelAccessor, boundingBox, i, j, k, i, l - 1, k, blockState2.setValue(FenceBlock.WEST, Boolean.valueOf(true)), CAVE_AIR, false);
				this.generateBox(levelAccessor, boundingBox, m, j, k, m, l - 1, k, blockState2.setValue(FenceBlock.EAST, Boolean.valueOf(true)), CAVE_AIR, false);
				if (random.nextInt(4) == 0) {
					this.generateBox(levelAccessor, boundingBox, i, l, k, i, l, k, blockState, CAVE_AIR, false);
					this.generateBox(levelAccessor, boundingBox, m, l, k, m, l, k, blockState, CAVE_AIR, false);
				} else {
					this.generateBox(levelAccessor, boundingBox, i, l, k, m, l, k, blockState, CAVE_AIR, false);
					this.maybeGenerateBlock(
						levelAccessor, boundingBox, random, 0.05F, i + 1, l, k - 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH)
					);
					this.maybeGenerateBlock(
						levelAccessor, boundingBox, random, 0.05F, i + 1, l, k + 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH)
					);
				}
			}
		}

		private void placeCobWeb(LevelAccessor levelAccessor, BoundingBox boundingBox, Random random, float f, int i, int j, int k) {
			if (this.isInterior(levelAccessor, i, j, k, boundingBox)) {
				this.maybeGenerateBlock(levelAccessor, boundingBox, random, f, i, j, k, Blocks.COBWEB.defaultBlockState());
			}
		}
	}

	public static class MineShaftCrossing extends MineShaftPieces.MineShaftPiece {
		private final Direction direction;
		private final boolean isTwoFloored;

		public MineShaftCrossing(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.MINE_SHAFT_CROSSING, compoundTag);
			this.isTwoFloored = compoundTag.getBoolean("tf");
			this.direction = Direction.from2DDataValue(compoundTag.getInt("D"));
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			super.addAdditionalSaveData(compoundTag);
			compoundTag.putBoolean("tf", this.isTwoFloored);
			compoundTag.putInt("D", this.direction.get2DDataValue());
		}

		public MineShaftCrossing(int i, BoundingBox boundingBox, @Nullable Direction direction, MineshaftFeature.Type type) {
			super(StructurePieceType.MINE_SHAFT_CROSSING, i, type);
			this.direction = direction;
			this.boundingBox = boundingBox;
			this.isTwoFloored = boundingBox.getYSpan() > 3;
		}

		public static BoundingBox findCrossing(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction) {
			BoundingBox boundingBox = new BoundingBox(i, j, k, i, j + 3 - 1, k);
			if (random.nextInt(4) == 0) {
				boundingBox.y1 += 4;
			}

			switch (direction) {
				case NORTH:
				default:
					boundingBox.x0 = i - 1;
					boundingBox.x1 = i + 3;
					boundingBox.z0 = k - 4;
					break;
				case SOUTH:
					boundingBox.x0 = i - 1;
					boundingBox.x1 = i + 3;
					boundingBox.z1 = k + 3 + 1;
					break;
				case WEST:
					boundingBox.x0 = i - 4;
					boundingBox.z0 = k - 1;
					boundingBox.z1 = k + 3;
					break;
				case EAST:
					boundingBox.x1 = i + 3 + 1;
					boundingBox.z0 = k - 1;
					boundingBox.z1 = k + 3;
			}

			return StructurePiece.findCollisionPiece(list, boundingBox) != null ? null : boundingBox;
		}

		@Override
		public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
			int i = this.getGenDepth();
			switch (this.direction) {
				case NORTH:
				default:
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, i
					);
					MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.WEST, i);
					MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.EAST, i);
					break;
				case SOUTH:
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, i
					);
					MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.WEST, i);
					MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.EAST, i);
					break;
				case WEST:
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, i
					);
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, i
					);
					MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.WEST, i);
					break;
				case EAST:
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, i
					);
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, i
					);
					MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.EAST, i);
			}

			if (this.isTwoFloored) {
				if (random.nextBoolean()) {
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z0 - 1, Direction.NORTH, i
					);
				}

				if (random.nextBoolean()) {
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z0 + 1, Direction.WEST, i
					);
				}

				if (random.nextBoolean()) {
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z0 + 1, Direction.EAST, i
					);
				}

				if (random.nextBoolean()) {
					MineShaftPieces.generateAndAddPiece(
						structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z1 + 1, Direction.SOUTH, i
					);
				}
			}
		}

		@Override
		public boolean postProcess(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
			if (this.edgesLiquid(levelAccessor, boundingBox)) {
				return false;
			} else {
				BlockState blockState = this.getPlanksBlock();
				if (this.isTwoFloored) {
					this.generateBox(
						levelAccessor,
						boundingBox,
						this.boundingBox.x0 + 1,
						this.boundingBox.y0,
						this.boundingBox.z0,
						this.boundingBox.x1 - 1,
						this.boundingBox.y0 + 3 - 1,
						this.boundingBox.z1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						levelAccessor,
						boundingBox,
						this.boundingBox.x0,
						this.boundingBox.y0,
						this.boundingBox.z0 + 1,
						this.boundingBox.x1,
						this.boundingBox.y0 + 3 - 1,
						this.boundingBox.z1 - 1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						levelAccessor,
						boundingBox,
						this.boundingBox.x0 + 1,
						this.boundingBox.y1 - 2,
						this.boundingBox.z0,
						this.boundingBox.x1 - 1,
						this.boundingBox.y1,
						this.boundingBox.z1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						levelAccessor,
						boundingBox,
						this.boundingBox.x0,
						this.boundingBox.y1 - 2,
						this.boundingBox.z0 + 1,
						this.boundingBox.x1,
						this.boundingBox.y1,
						this.boundingBox.z1 - 1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						levelAccessor,
						boundingBox,
						this.boundingBox.x0 + 1,
						this.boundingBox.y0 + 3,
						this.boundingBox.z0 + 1,
						this.boundingBox.x1 - 1,
						this.boundingBox.y0 + 3,
						this.boundingBox.z1 - 1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
				} else {
					this.generateBox(
						levelAccessor,
						boundingBox,
						this.boundingBox.x0 + 1,
						this.boundingBox.y0,
						this.boundingBox.z0,
						this.boundingBox.x1 - 1,
						this.boundingBox.y1,
						this.boundingBox.z1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
					this.generateBox(
						levelAccessor,
						boundingBox,
						this.boundingBox.x0,
						this.boundingBox.y0,
						this.boundingBox.z0 + 1,
						this.boundingBox.x1,
						this.boundingBox.y1,
						this.boundingBox.z1 - 1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
				}

				this.placeSupportPillar(levelAccessor, boundingBox, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, this.boundingBox.y1);
				this.placeSupportPillar(levelAccessor, boundingBox, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 - 1, this.boundingBox.y1);
				this.placeSupportPillar(levelAccessor, boundingBox, this.boundingBox.x1 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, this.boundingBox.y1);
				this.placeSupportPillar(levelAccessor, boundingBox, this.boundingBox.x1 - 1, this.boundingBox.y0, this.boundingBox.z1 - 1, this.boundingBox.y1);

				for (int i = this.boundingBox.x0; i <= this.boundingBox.x1; i++) {
					for (int j = this.boundingBox.z0; j <= this.boundingBox.z1; j++) {
						if (this.getBlock(levelAccessor, i, this.boundingBox.y0 - 1, j, boundingBox).isAir()
							&& this.isInterior(levelAccessor, i, this.boundingBox.y0 - 1, j, boundingBox)) {
							this.placeBlock(levelAccessor, blockState, i, this.boundingBox.y0 - 1, j, boundingBox);
						}
					}
				}

				return true;
			}
		}

		private void placeSupportPillar(LevelAccessor levelAccessor, BoundingBox boundingBox, int i, int j, int k, int l) {
			if (!this.getBlock(levelAccessor, i, l + 1, k, boundingBox).isAir()) {
				this.generateBox(levelAccessor, boundingBox, i, j, k, i, l, k, this.getPlanksBlock(), CAVE_AIR, false);
			}
		}
	}

	abstract static class MineShaftPiece extends StructurePiece {
		protected MineshaftFeature.Type type;

		public MineShaftPiece(StructurePieceType structurePieceType, int i, MineshaftFeature.Type type) {
			super(structurePieceType, i);
			this.type = type;
		}

		public MineShaftPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
			super(structurePieceType, compoundTag);
			this.type = MineshaftFeature.Type.byId(compoundTag.getInt("MST"));
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			compoundTag.putInt("MST", this.type.ordinal());
		}

		protected BlockState getPlanksBlock() {
			switch (this.type) {
				case NORMAL:
				default:
					return Blocks.OAK_PLANKS.defaultBlockState();
				case MESA:
					return Blocks.DARK_OAK_PLANKS.defaultBlockState();
			}
		}

		protected BlockState getFenceBlock() {
			switch (this.type) {
				case NORMAL:
				default:
					return Blocks.OAK_FENCE.defaultBlockState();
				case MESA:
					return Blocks.DARK_OAK_FENCE.defaultBlockState();
			}
		}

		protected boolean isSupportingBox(BlockGetter blockGetter, BoundingBox boundingBox, int i, int j, int k, int l) {
			for (int m = i; m <= j; m++) {
				if (this.getBlock(blockGetter, m, k + 1, l, boundingBox).isAir()) {
					return false;
				}
			}

			return true;
		}
	}

	public static class MineShaftRoom extends MineShaftPieces.MineShaftPiece {
		private final List<BoundingBox> childEntranceBoxes = Lists.<BoundingBox>newLinkedList();

		public MineShaftRoom(int i, Random random, int j, int k, MineshaftFeature.Type type) {
			super(StructurePieceType.MINE_SHAFT_ROOM, i, type);
			this.type = type;
			this.boundingBox = new BoundingBox(j, 50, k, j + 7 + random.nextInt(6), 54 + random.nextInt(6), k + 7 + random.nextInt(6));
		}

		public MineShaftRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.MINE_SHAFT_ROOM, compoundTag);
			ListTag listTag = compoundTag.getList("Entrances", 11);

			for (int i = 0; i < listTag.size(); i++) {
				this.childEntranceBoxes.add(new BoundingBox(listTag.getIntArray(i)));
			}
		}

		@Override
		public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
			int i = this.getGenDepth();
			int j = this.boundingBox.getYSpan() - 3 - 1;
			if (j <= 0) {
				j = 1;
			}

			int k = 0;

			while (k < this.boundingBox.getXSpan()) {
				k += random.nextInt(this.boundingBox.getXSpan());
				if (k + 3 > this.boundingBox.getXSpan()) {
					break;
				}

				MineShaftPieces.MineShaftPiece mineShaftPiece = MineShaftPieces.generateAndAddPiece(
					structurePiece, list, random, this.boundingBox.x0 + k, this.boundingBox.y0 + random.nextInt(j) + 1, this.boundingBox.z0 - 1, Direction.NORTH, i
				);
				if (mineShaftPiece != null) {
					BoundingBox boundingBox = mineShaftPiece.getBoundingBox();
					this.childEntranceBoxes.add(new BoundingBox(boundingBox.x0, boundingBox.y0, this.boundingBox.z0, boundingBox.x1, boundingBox.y1, this.boundingBox.z0 + 1));
				}

				k += 4;
			}

			k = 0;

			while (k < this.boundingBox.getXSpan()) {
				k += random.nextInt(this.boundingBox.getXSpan());
				if (k + 3 > this.boundingBox.getXSpan()) {
					break;
				}

				MineShaftPieces.MineShaftPiece mineShaftPiece = MineShaftPieces.generateAndAddPiece(
					structurePiece, list, random, this.boundingBox.x0 + k, this.boundingBox.y0 + random.nextInt(j) + 1, this.boundingBox.z1 + 1, Direction.SOUTH, i
				);
				if (mineShaftPiece != null) {
					BoundingBox boundingBox = mineShaftPiece.getBoundingBox();
					this.childEntranceBoxes.add(new BoundingBox(boundingBox.x0, boundingBox.y0, this.boundingBox.z1 - 1, boundingBox.x1, boundingBox.y1, this.boundingBox.z1));
				}

				k += 4;
			}

			k = 0;

			while (k < this.boundingBox.getZSpan()) {
				k += random.nextInt(this.boundingBox.getZSpan());
				if (k + 3 > this.boundingBox.getZSpan()) {
					break;
				}

				MineShaftPieces.MineShaftPiece mineShaftPiece = MineShaftPieces.generateAndAddPiece(
					structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 + random.nextInt(j) + 1, this.boundingBox.z0 + k, Direction.WEST, i
				);
				if (mineShaftPiece != null) {
					BoundingBox boundingBox = mineShaftPiece.getBoundingBox();
					this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.x0, boundingBox.y0, boundingBox.z0, this.boundingBox.x0 + 1, boundingBox.y1, boundingBox.z1));
				}

				k += 4;
			}

			k = 0;

			while (k < this.boundingBox.getZSpan()) {
				k += random.nextInt(this.boundingBox.getZSpan());
				if (k + 3 > this.boundingBox.getZSpan()) {
					break;
				}

				StructurePiece structurePiece2 = MineShaftPieces.generateAndAddPiece(
					structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 + random.nextInt(j) + 1, this.boundingBox.z0 + k, Direction.EAST, i
				);
				if (structurePiece2 != null) {
					BoundingBox boundingBox = structurePiece2.getBoundingBox();
					this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.x1 - 1, boundingBox.y0, boundingBox.z0, this.boundingBox.x1, boundingBox.y1, boundingBox.z1));
				}

				k += 4;
			}
		}

		@Override
		public boolean postProcess(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
			if (this.edgesLiquid(levelAccessor, boundingBox)) {
				return false;
			} else {
				this.generateBox(
					levelAccessor,
					boundingBox,
					this.boundingBox.x0,
					this.boundingBox.y0,
					this.boundingBox.z0,
					this.boundingBox.x1,
					this.boundingBox.y0,
					this.boundingBox.z1,
					Blocks.DIRT.defaultBlockState(),
					CAVE_AIR,
					true
				);
				this.generateBox(
					levelAccessor,
					boundingBox,
					this.boundingBox.x0,
					this.boundingBox.y0 + 1,
					this.boundingBox.z0,
					this.boundingBox.x1,
					Math.min(this.boundingBox.y0 + 3, this.boundingBox.y1),
					this.boundingBox.z1,
					CAVE_AIR,
					CAVE_AIR,
					false
				);

				for (BoundingBox boundingBox2 : this.childEntranceBoxes) {
					this.generateBox(
						levelAccessor,
						boundingBox,
						boundingBox2.x0,
						boundingBox2.y1 - 2,
						boundingBox2.z0,
						boundingBox2.x1,
						boundingBox2.y1,
						boundingBox2.z1,
						CAVE_AIR,
						CAVE_AIR,
						false
					);
				}

				this.generateUpperHalfSphere(
					levelAccessor,
					boundingBox,
					this.boundingBox.x0,
					this.boundingBox.y0 + 4,
					this.boundingBox.z0,
					this.boundingBox.x1,
					this.boundingBox.y1,
					this.boundingBox.z1,
					CAVE_AIR,
					false
				);
				return true;
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
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			super.addAdditionalSaveData(compoundTag);
			ListTag listTag = new ListTag();

			for (BoundingBox boundingBox : this.childEntranceBoxes) {
				listTag.add(boundingBox.createTag());
			}

			compoundTag.put("Entrances", listTag);
		}
	}

	public static class MineShaftStairs extends MineShaftPieces.MineShaftPiece {
		public MineShaftStairs(int i, BoundingBox boundingBox, Direction direction, MineshaftFeature.Type type) {
			super(StructurePieceType.MINE_SHAFT_STAIRS, i, type);
			this.setOrientation(direction);
			this.boundingBox = boundingBox;
		}

		public MineShaftStairs(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.MINE_SHAFT_STAIRS, compoundTag);
		}

		public static BoundingBox findStairs(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction) {
			BoundingBox boundingBox = new BoundingBox(i, j - 5, k, i, j + 3 - 1, k);
			switch (direction) {
				case NORTH:
				default:
					boundingBox.x1 = i + 3 - 1;
					boundingBox.z0 = k - 8;
					break;
				case SOUTH:
					boundingBox.x1 = i + 3 - 1;
					boundingBox.z1 = k + 8;
					break;
				case WEST:
					boundingBox.x0 = i - 8;
					boundingBox.z1 = k + 3 - 1;
					break;
				case EAST:
					boundingBox.x1 = i + 8;
					boundingBox.z1 = k + 3 - 1;
			}

			return StructurePiece.findCollisionPiece(list, boundingBox) != null ? null : boundingBox;
		}

		@Override
		public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
			int i = this.getGenDepth();
			Direction direction = this.getOrientation();
			if (direction != null) {
				switch (direction) {
					case NORTH:
					default:
						MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, i);
						break;
					case SOUTH:
						MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, i);
						break;
					case WEST:
						MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0, Direction.WEST, i);
						break;
					case EAST:
						MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0, Direction.EAST, i);
				}
			}
		}

		@Override
		public boolean postProcess(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
			if (this.edgesLiquid(levelAccessor, boundingBox)) {
				return false;
			} else {
				this.generateBox(levelAccessor, boundingBox, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
				this.generateBox(levelAccessor, boundingBox, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);

				for (int i = 0; i < 5; i++) {
					this.generateBox(levelAccessor, boundingBox, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, CAVE_AIR, CAVE_AIR, false);
				}

				return true;
			}
		}
	}
}
