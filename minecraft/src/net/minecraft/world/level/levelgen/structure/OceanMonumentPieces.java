package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanMonumentPieces {
	static class FitDoubleXRoom implements OceanMonumentPieces.MonumentRoomFitter {
		private FitDoubleXRoom() {
		}

		@Override
		public boolean fits(OceanMonumentPieces.RoomDefinition roomDefinition) {
			return roomDefinition.hasOpening[Direction.EAST.get3DDataValue()] && !roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed;
		}

		@Override
		public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, Random random) {
			roomDefinition.claimed = true;
			roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
			return new OceanMonumentPieces.OceanMonumentDoubleXRoom(direction, roomDefinition);
		}
	}

	static class FitDoubleXYRoom implements OceanMonumentPieces.MonumentRoomFitter {
		private FitDoubleXYRoom() {
		}

		@Override
		public boolean fits(OceanMonumentPieces.RoomDefinition roomDefinition) {
			if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]
				&& !roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed
				&& roomDefinition.hasOpening[Direction.UP.get3DDataValue()]
				&& !roomDefinition.connections[Direction.UP.get3DDataValue()].claimed) {
				OceanMonumentPieces.RoomDefinition roomDefinition2 = roomDefinition.connections[Direction.EAST.get3DDataValue()];
				return roomDefinition2.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition2.connections[Direction.UP.get3DDataValue()].claimed;
			} else {
				return false;
			}
		}

		@Override
		public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, Random random) {
			roomDefinition.claimed = true;
			roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
			roomDefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
			roomDefinition.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
			return new OceanMonumentPieces.OceanMonumentDoubleXYRoom(direction, roomDefinition);
		}
	}

	static class FitDoubleYRoom implements OceanMonumentPieces.MonumentRoomFitter {
		private FitDoubleYRoom() {
		}

		@Override
		public boolean fits(OceanMonumentPieces.RoomDefinition roomDefinition) {
			return roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition.connections[Direction.UP.get3DDataValue()].claimed;
		}

		@Override
		public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, Random random) {
			roomDefinition.claimed = true;
			roomDefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
			return new OceanMonumentPieces.OceanMonumentDoubleYRoom(direction, roomDefinition);
		}
	}

	static class FitDoubleYZRoom implements OceanMonumentPieces.MonumentRoomFitter {
		private FitDoubleYZRoom() {
		}

		@Override
		public boolean fits(OceanMonumentPieces.RoomDefinition roomDefinition) {
			if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]
				&& !roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed
				&& roomDefinition.hasOpening[Direction.UP.get3DDataValue()]
				&& !roomDefinition.connections[Direction.UP.get3DDataValue()].claimed) {
				OceanMonumentPieces.RoomDefinition roomDefinition2 = roomDefinition.connections[Direction.NORTH.get3DDataValue()];
				return roomDefinition2.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition2.connections[Direction.UP.get3DDataValue()].claimed;
			} else {
				return false;
			}
		}

		@Override
		public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, Random random) {
			roomDefinition.claimed = true;
			roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed = true;
			roomDefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
			roomDefinition.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
			return new OceanMonumentPieces.OceanMonumentDoubleYZRoom(direction, roomDefinition);
		}
	}

	static class FitDoubleZRoom implements OceanMonumentPieces.MonumentRoomFitter {
		private FitDoubleZRoom() {
		}

		@Override
		public boolean fits(OceanMonumentPieces.RoomDefinition roomDefinition) {
			return roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] && !roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed;
		}

		@Override
		public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, Random random) {
			OceanMonumentPieces.RoomDefinition roomDefinition2 = roomDefinition;
			if (!roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] || roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed) {
				roomDefinition2 = roomDefinition.connections[Direction.SOUTH.get3DDataValue()];
			}

			roomDefinition2.claimed = true;
			roomDefinition2.connections[Direction.NORTH.get3DDataValue()].claimed = true;
			return new OceanMonumentPieces.OceanMonumentDoubleZRoom(direction, roomDefinition2);
		}
	}

	static class FitSimpleRoom implements OceanMonumentPieces.MonumentRoomFitter {
		private FitSimpleRoom() {
		}

		@Override
		public boolean fits(OceanMonumentPieces.RoomDefinition roomDefinition) {
			return true;
		}

		@Override
		public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, Random random) {
			roomDefinition.claimed = true;
			return new OceanMonumentPieces.OceanMonumentSimpleRoom(direction, roomDefinition, random);
		}
	}

	static class FitSimpleTopRoom implements OceanMonumentPieces.MonumentRoomFitter {
		private FitSimpleTopRoom() {
		}

		@Override
		public boolean fits(OceanMonumentPieces.RoomDefinition roomDefinition) {
			return !roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]
				&& !roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]
				&& !roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]
				&& !roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]
				&& !roomDefinition.hasOpening[Direction.UP.get3DDataValue()];
		}

		@Override
		public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, Random random) {
			roomDefinition.claimed = true;
			return new OceanMonumentPieces.OceanMonumentSimpleTopRoom(direction, roomDefinition);
		}
	}

	public static class MonumentBuilding extends OceanMonumentPieces.OceanMonumentPiece {
		private OceanMonumentPieces.RoomDefinition sourceRoom;
		private OceanMonumentPieces.RoomDefinition coreRoom;
		private final List<OceanMonumentPieces.OceanMonumentPiece> childPieces = Lists.<OceanMonumentPieces.OceanMonumentPiece>newArrayList();

		public MonumentBuilding(Random random, int i, int j, Direction direction) {
			super(StructurePieceType.OCEAN_MONUMENT_BUILDING, 0);
			this.setOrientation(direction);
			Direction direction2 = this.getOrientation();
			if (direction2.getAxis() == Direction.Axis.Z) {
				this.boundingBox = new BoundingBox(i, 39, j, i + 58 - 1, 61, j + 58 - 1);
			} else {
				this.boundingBox = new BoundingBox(i, 39, j, i + 58 - 1, 61, j + 58 - 1);
			}

			List<OceanMonumentPieces.RoomDefinition> list = this.generateRoomGraph(random);
			this.sourceRoom.claimed = true;
			this.childPieces.add(new OceanMonumentPieces.OceanMonumentEntryRoom(direction2, this.sourceRoom));
			this.childPieces.add(new OceanMonumentPieces.OceanMonumentCoreRoom(direction2, this.coreRoom));
			List<OceanMonumentPieces.MonumentRoomFitter> list2 = Lists.<OceanMonumentPieces.MonumentRoomFitter>newArrayList();
			list2.add(new OceanMonumentPieces.FitDoubleXYRoom());
			list2.add(new OceanMonumentPieces.FitDoubleYZRoom());
			list2.add(new OceanMonumentPieces.FitDoubleZRoom());
			list2.add(new OceanMonumentPieces.FitDoubleXRoom());
			list2.add(new OceanMonumentPieces.FitDoubleYRoom());
			list2.add(new OceanMonumentPieces.FitSimpleTopRoom());
			list2.add(new OceanMonumentPieces.FitSimpleRoom());

			for (OceanMonumentPieces.RoomDefinition roomDefinition : list) {
				if (!roomDefinition.claimed && !roomDefinition.isSpecial()) {
					for (OceanMonumentPieces.MonumentRoomFitter monumentRoomFitter : list2) {
						if (monumentRoomFitter.fits(roomDefinition)) {
							this.childPieces.add(monumentRoomFitter.create(direction2, roomDefinition, random));
							break;
						}
					}
				}
			}

			int k = this.boundingBox.y0;
			int l = this.getWorldX(9, 22);
			int m = this.getWorldZ(9, 22);

			for (OceanMonumentPieces.OceanMonumentPiece oceanMonumentPiece : this.childPieces) {
				oceanMonumentPiece.getBoundingBox().move(l, k, m);
			}

			BoundingBox boundingBox = BoundingBox.createProper(
				this.getWorldX(1, 1), this.getWorldY(1), this.getWorldZ(1, 1), this.getWorldX(23, 21), this.getWorldY(8), this.getWorldZ(23, 21)
			);
			BoundingBox boundingBox2 = BoundingBox.createProper(
				this.getWorldX(34, 1), this.getWorldY(1), this.getWorldZ(34, 1), this.getWorldX(56, 21), this.getWorldY(8), this.getWorldZ(56, 21)
			);
			BoundingBox boundingBox3 = BoundingBox.createProper(
				this.getWorldX(22, 22), this.getWorldY(13), this.getWorldZ(22, 22), this.getWorldX(35, 35), this.getWorldY(17), this.getWorldZ(35, 35)
			);
			int n = random.nextInt();
			this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom(direction2, boundingBox, n++));
			this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom(direction2, boundingBox2, n++));
			this.childPieces.add(new OceanMonumentPieces.OceanMonumentPenthouse(direction2, boundingBox3));
		}

		public MonumentBuilding(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_BUILDING, compoundTag);
		}

		private List<OceanMonumentPieces.RoomDefinition> generateRoomGraph(Random random) {
			OceanMonumentPieces.RoomDefinition[] roomDefinitions = new OceanMonumentPieces.RoomDefinition[75];

			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 4; j++) {
					int k = 0;
					int l = getRoomIndex(i, 0, j);
					roomDefinitions[l] = new OceanMonumentPieces.RoomDefinition(l);
				}
			}

			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 4; j++) {
					int k = 1;
					int l = getRoomIndex(i, 1, j);
					roomDefinitions[l] = new OceanMonumentPieces.RoomDefinition(l);
				}
			}

			for (int i = 1; i < 4; i++) {
				for (int j = 0; j < 2; j++) {
					int k = 2;
					int l = getRoomIndex(i, 2, j);
					roomDefinitions[l] = new OceanMonumentPieces.RoomDefinition(l);
				}
			}

			this.sourceRoom = roomDefinitions[GRIDROOM_SOURCE_INDEX];

			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					for (int k = 0; k < 3; k++) {
						int l = getRoomIndex(i, k, j);
						if (roomDefinitions[l] != null) {
							for (Direction direction : Direction.values()) {
								int m = i + direction.getStepX();
								int n = k + direction.getStepY();
								int o = j + direction.getStepZ();
								if (m >= 0 && m < 5 && o >= 0 && o < 5 && n >= 0 && n < 3) {
									int p = getRoomIndex(m, n, o);
									if (roomDefinitions[p] != null) {
										if (o == j) {
											roomDefinitions[l].setConnection(direction, roomDefinitions[p]);
										} else {
											roomDefinitions[l].setConnection(direction.getOpposite(), roomDefinitions[p]);
										}
									}
								}
							}
						}
					}
				}
			}

			OceanMonumentPieces.RoomDefinition roomDefinition = new OceanMonumentPieces.RoomDefinition(1003);
			OceanMonumentPieces.RoomDefinition roomDefinition2 = new OceanMonumentPieces.RoomDefinition(1001);
			OceanMonumentPieces.RoomDefinition roomDefinition3 = new OceanMonumentPieces.RoomDefinition(1002);
			roomDefinitions[GRIDROOM_TOP_CONNECT_INDEX].setConnection(Direction.UP, roomDefinition);
			roomDefinitions[GRIDROOM_LEFTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, roomDefinition2);
			roomDefinitions[GRIDROOM_RIGHTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, roomDefinition3);
			roomDefinition.claimed = true;
			roomDefinition2.claimed = true;
			roomDefinition3.claimed = true;
			this.sourceRoom.isSource = true;
			this.coreRoom = roomDefinitions[getRoomIndex(random.nextInt(4), 0, 2)];
			this.coreRoom.claimed = true;
			this.coreRoom.connections[Direction.EAST.get3DDataValue()].claimed = true;
			this.coreRoom.connections[Direction.NORTH.get3DDataValue()].claimed = true;
			this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].claimed = true;
			this.coreRoom.connections[Direction.UP.get3DDataValue()].claimed = true;
			this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
			this.coreRoom.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
			this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
			List<OceanMonumentPieces.RoomDefinition> list = Lists.<OceanMonumentPieces.RoomDefinition>newArrayList();

			for (OceanMonumentPieces.RoomDefinition roomDefinition4 : roomDefinitions) {
				if (roomDefinition4 != null) {
					roomDefinition4.updateOpenings();
					list.add(roomDefinition4);
				}
			}

			roomDefinition.updateOpenings();
			Collections.shuffle(list, random);
			int q = 1;

			for (OceanMonumentPieces.RoomDefinition roomDefinition5 : list) {
				int r = 0;
				int m = 0;

				while (r < 2 && m < 5) {
					m++;
					int n = random.nextInt(6);
					if (roomDefinition5.hasOpening[n]) {
						int o = Direction.from3DDataValue(n).getOpposite().get3DDataValue();
						roomDefinition5.hasOpening[n] = false;
						roomDefinition5.connections[n].hasOpening[o] = false;
						if (roomDefinition5.findSource(q++) && roomDefinition5.connections[n].findSource(q++)) {
							r++;
						} else {
							roomDefinition5.hasOpening[n] = true;
							roomDefinition5.connections[n].hasOpening[o] = true;
						}
					}
				}
			}

			list.add(roomDefinition);
			list.add(roomDefinition2);
			list.add(roomDefinition3);
			return list;
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
			int i = Math.max(worldGenLevel.getSeaLevel(), 64) - this.boundingBox.y0;
			this.generateWaterBox(worldGenLevel, boundingBox, 0, 0, 0, 58, i, 58);
			this.generateWing(false, 0, worldGenLevel, random, boundingBox);
			this.generateWing(true, 33, worldGenLevel, random, boundingBox);
			this.generateEntranceArchs(worldGenLevel, random, boundingBox);
			this.generateEntranceWall(worldGenLevel, random, boundingBox);
			this.generateRoofPiece(worldGenLevel, random, boundingBox);
			this.generateLowerWall(worldGenLevel, random, boundingBox);
			this.generateMiddleWall(worldGenLevel, random, boundingBox);
			this.generateUpperWall(worldGenLevel, random, boundingBox);

			for (int j = 0; j < 7; j++) {
				int k = 0;

				while (k < 7) {
					if (k == 0 && j == 3) {
						k = 6;
					}

					int l = j * 9;
					int m = k * 9;

					for (int n = 0; n < 4; n++) {
						for (int o = 0; o < 4; o++) {
							this.placeBlock(worldGenLevel, BASE_LIGHT, l + n, 0, m + o, boundingBox);
							this.fillColumnDown(worldGenLevel, BASE_LIGHT, l + n, -1, m + o, boundingBox);
						}
					}

					if (j != 0 && j != 6) {
						k += 6;
					} else {
						k++;
					}
				}
			}

			for (int j = 0; j < 5; j++) {
				this.generateWaterBox(worldGenLevel, boundingBox, -1 - j, 0 + j * 2, -1 - j, -1 - j, 23, 58 + j);
				this.generateWaterBox(worldGenLevel, boundingBox, 58 + j, 0 + j * 2, -1 - j, 58 + j, 23, 58 + j);
				this.generateWaterBox(worldGenLevel, boundingBox, 0 - j, 0 + j * 2, -1 - j, 57 + j, 23, -1 - j);
				this.generateWaterBox(worldGenLevel, boundingBox, 0 - j, 0 + j * 2, 58 + j, 57 + j, 23, 58 + j);
			}

			for (OceanMonumentPieces.OceanMonumentPiece oceanMonumentPiece : this.childPieces) {
				if (oceanMonumentPiece.getBoundingBox().intersects(boundingBox)) {
					oceanMonumentPiece.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
				}
			}

			return true;
		}

		private void generateWing(boolean bl, int i, WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
			int j = 24;
			if (this.chunkIntersects(boundingBox, i, 0, i + 23, 20)) {
				this.generateBox(worldGenLevel, boundingBox, i + 0, 0, 0, i + 24, 0, 20, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, i + 0, 1, 0, i + 24, 10, 20);

				for (int k = 0; k < 4; k++) {
					this.generateBox(worldGenLevel, boundingBox, i + k, k + 1, k, i + k, k + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, i + k + 7, k + 5, k + 7, i + k + 7, k + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, i + 17 - k, k + 5, k + 7, i + 17 - k, k + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, i + 24 - k, k + 1, k, i + 24 - k, k + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, i + k + 1, k + 1, k, i + 23 - k, k + 1, k, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, i + k + 8, k + 5, k + 7, i + 16 - k, k + 5, k + 7, BASE_LIGHT, BASE_LIGHT, false);
				}

				this.generateBox(worldGenLevel, boundingBox, i + 4, 4, 4, i + 6, 4, 20, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, i + 7, 4, 4, i + 17, 4, 6, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, i + 18, 4, 4, i + 20, 4, 20, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, i + 11, 8, 11, i + 13, 8, 20, BASE_GRAY, BASE_GRAY, false);
				this.placeBlock(worldGenLevel, DOT_DECO_DATA, i + 12, 9, 12, boundingBox);
				this.placeBlock(worldGenLevel, DOT_DECO_DATA, i + 12, 9, 15, boundingBox);
				this.placeBlock(worldGenLevel, DOT_DECO_DATA, i + 12, 9, 18, boundingBox);
				int k = i + (bl ? 19 : 5);
				int l = i + (bl ? 5 : 19);

				for (int m = 20; m >= 5; m -= 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, k, 5, m, boundingBox);
				}

				for (int m = 19; m >= 7; m -= 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, l, 5, m, boundingBox);
				}

				for (int m = 0; m < 4; m++) {
					int n = bl ? i + 24 - (17 - m * 3) : i + 17 - m * 3;
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 5, 5, boundingBox);
				}

				this.placeBlock(worldGenLevel, DOT_DECO_DATA, l, 5, 5, boundingBox);
				this.generateBox(worldGenLevel, boundingBox, i + 11, 1, 12, i + 13, 7, 12, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, i + 12, 1, 11, i + 12, 7, 13, BASE_GRAY, BASE_GRAY, false);
			}
		}

		private void generateEntranceArchs(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
			if (this.chunkIntersects(boundingBox, 22, 5, 35, 17)) {
				this.generateWaterBox(worldGenLevel, boundingBox, 25, 0, 0, 32, 8, 20);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, 24, 2, 5 + i * 4, 24, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 22, 4, 5 + i * 4, 23, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
					this.placeBlock(worldGenLevel, BASE_LIGHT, 25, 5, 5 + i * 4, boundingBox);
					this.placeBlock(worldGenLevel, BASE_LIGHT, 26, 6, 5 + i * 4, boundingBox);
					this.placeBlock(worldGenLevel, LAMP_BLOCK, 26, 5, 5 + i * 4, boundingBox);
					this.generateBox(worldGenLevel, boundingBox, 33, 2, 5 + i * 4, 33, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 34, 4, 5 + i * 4, 35, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
					this.placeBlock(worldGenLevel, BASE_LIGHT, 32, 5, 5 + i * 4, boundingBox);
					this.placeBlock(worldGenLevel, BASE_LIGHT, 31, 6, 5 + i * 4, boundingBox);
					this.placeBlock(worldGenLevel, LAMP_BLOCK, 31, 5, 5 + i * 4, boundingBox);
					this.generateBox(worldGenLevel, boundingBox, 27, 6, 5 + i * 4, 30, 6, 5 + i * 4, BASE_GRAY, BASE_GRAY, false);
				}
			}
		}

		private void generateEntranceWall(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
			if (this.chunkIntersects(boundingBox, 15, 20, 42, 21)) {
				this.generateBox(worldGenLevel, boundingBox, 15, 0, 21, 42, 0, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 26, 1, 21, 31, 3, 21);
				this.generateBox(worldGenLevel, boundingBox, 21, 12, 21, 36, 12, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 17, 11, 21, 40, 11, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 16, 10, 21, 41, 10, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 15, 7, 21, 42, 9, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 16, 6, 21, 41, 6, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 17, 5, 21, 40, 5, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 21, 4, 21, 36, 4, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 22, 3, 21, 26, 3, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 31, 3, 21, 35, 3, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 23, 2, 21, 25, 2, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 32, 2, 21, 34, 2, 21, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 28, 4, 20, 29, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 27, 3, 21, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 30, 3, 21, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 26, 2, 21, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 31, 2, 21, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 25, 1, 21, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 32, 1, 21, boundingBox);

				for (int i = 0; i < 7; i++) {
					this.placeBlock(worldGenLevel, BASE_BLACK, 28 - i, 6 + i, 21, boundingBox);
					this.placeBlock(worldGenLevel, BASE_BLACK, 29 + i, 6 + i, 21, boundingBox);
				}

				for (int i = 0; i < 4; i++) {
					this.placeBlock(worldGenLevel, BASE_BLACK, 28 - i, 9 + i, 21, boundingBox);
					this.placeBlock(worldGenLevel, BASE_BLACK, 29 + i, 9 + i, 21, boundingBox);
				}

				this.placeBlock(worldGenLevel, BASE_BLACK, 28, 12, 21, boundingBox);
				this.placeBlock(worldGenLevel, BASE_BLACK, 29, 12, 21, boundingBox);

				for (int i = 0; i < 3; i++) {
					this.placeBlock(worldGenLevel, BASE_BLACK, 22 - i * 2, 8, 21, boundingBox);
					this.placeBlock(worldGenLevel, BASE_BLACK, 22 - i * 2, 9, 21, boundingBox);
					this.placeBlock(worldGenLevel, BASE_BLACK, 35 + i * 2, 8, 21, boundingBox);
					this.placeBlock(worldGenLevel, BASE_BLACK, 35 + i * 2, 9, 21, boundingBox);
				}

				this.generateWaterBox(worldGenLevel, boundingBox, 15, 13, 21, 42, 15, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 15, 1, 21, 15, 6, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 16, 1, 21, 16, 5, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 17, 1, 21, 20, 4, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 21, 1, 21, 21, 3, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 22, 1, 21, 22, 2, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 23, 1, 21, 24, 1, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 42, 1, 21, 42, 6, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 41, 1, 21, 41, 5, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 37, 1, 21, 40, 4, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 36, 1, 21, 36, 3, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 33, 1, 21, 34, 1, 21);
				this.generateWaterBox(worldGenLevel, boundingBox, 35, 1, 21, 35, 2, 21);
			}
		}

		private void generateRoofPiece(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
			if (this.chunkIntersects(boundingBox, 21, 21, 36, 36)) {
				this.generateBox(worldGenLevel, boundingBox, 21, 0, 22, 36, 0, 36, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 21, 1, 22, 36, 23, 36);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, 21 + i, 13 + i, 21 + i, 36 - i, 13 + i, 21 + i, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 21 + i, 13 + i, 36 - i, 36 - i, 13 + i, 36 - i, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 21 + i, 13 + i, 22 + i, 21 + i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 36 - i, 13 + i, 22 + i, 36 - i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
				}

				this.generateBox(worldGenLevel, boundingBox, 25, 16, 25, 32, 16, 32, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 25, 17, 25, 25, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 32, 17, 25, 32, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 25, 17, 32, 25, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 32, 17, 32, 32, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 26, 20, 26, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 27, 21, 27, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 27, 20, 27, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 26, 20, 31, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 27, 21, 30, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 27, 20, 30, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 31, 20, 31, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 30, 21, 30, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 30, 20, 30, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 31, 20, 26, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 30, 21, 27, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 30, 20, 27, boundingBox);
				this.generateBox(worldGenLevel, boundingBox, 28, 21, 27, 29, 21, 27, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 27, 21, 28, 27, 21, 29, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 28, 21, 30, 29, 21, 30, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 30, 21, 28, 30, 21, 29, BASE_GRAY, BASE_GRAY, false);
			}
		}

		private void generateLowerWall(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
			if (this.chunkIntersects(boundingBox, 0, 21, 6, 58)) {
				this.generateBox(worldGenLevel, boundingBox, 0, 0, 21, 6, 0, 57, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 21, 6, 7, 57);
				this.generateBox(worldGenLevel, boundingBox, 4, 4, 21, 6, 4, 53, BASE_GRAY, BASE_GRAY, false);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, i, i + 1, 21, i, i + 1, 57 - i, BASE_LIGHT, BASE_LIGHT, false);
				}

				for (int i = 23; i < 53; i += 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, 5, 5, i, boundingBox);
				}

				this.placeBlock(worldGenLevel, DOT_DECO_DATA, 5, 5, 52, boundingBox);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, i, i + 1, 21, i, i + 1, 57 - i, BASE_LIGHT, BASE_LIGHT, false);
				}

				this.generateBox(worldGenLevel, boundingBox, 4, 1, 52, 6, 3, 52, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 5, 1, 51, 5, 3, 53, BASE_GRAY, BASE_GRAY, false);
			}

			if (this.chunkIntersects(boundingBox, 51, 21, 58, 58)) {
				this.generateBox(worldGenLevel, boundingBox, 51, 0, 21, 57, 0, 57, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 51, 1, 21, 57, 7, 57);
				this.generateBox(worldGenLevel, boundingBox, 51, 4, 21, 53, 4, 53, BASE_GRAY, BASE_GRAY, false);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, 57 - i, i + 1, 21, 57 - i, i + 1, 57 - i, BASE_LIGHT, BASE_LIGHT, false);
				}

				for (int i = 23; i < 53; i += 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, 52, 5, i, boundingBox);
				}

				this.placeBlock(worldGenLevel, DOT_DECO_DATA, 52, 5, 52, boundingBox);
				this.generateBox(worldGenLevel, boundingBox, 51, 1, 52, 53, 3, 52, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 52, 1, 51, 52, 3, 53, BASE_GRAY, BASE_GRAY, false);
			}

			if (this.chunkIntersects(boundingBox, 0, 51, 57, 57)) {
				this.generateBox(worldGenLevel, boundingBox, 7, 0, 51, 50, 0, 57, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 51, 50, 10, 57);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, i + 1, i + 1, 57 - i, 56 - i, i + 1, 57 - i, BASE_LIGHT, BASE_LIGHT, false);
				}
			}
		}

		private void generateMiddleWall(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
			if (this.chunkIntersects(boundingBox, 7, 21, 13, 50)) {
				this.generateBox(worldGenLevel, boundingBox, 7, 0, 21, 13, 0, 50, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 21, 13, 10, 50);
				this.generateBox(worldGenLevel, boundingBox, 11, 8, 21, 13, 8, 53, BASE_GRAY, BASE_GRAY, false);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, i + 7, i + 5, 21, i + 7, i + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
				}

				for (int i = 21; i <= 45; i += 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, 12, 9, i, boundingBox);
				}
			}

			if (this.chunkIntersects(boundingBox, 44, 21, 50, 54)) {
				this.generateBox(worldGenLevel, boundingBox, 44, 0, 21, 50, 0, 50, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 44, 1, 21, 50, 10, 50);
				this.generateBox(worldGenLevel, boundingBox, 44, 8, 21, 46, 8, 53, BASE_GRAY, BASE_GRAY, false);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, 50 - i, i + 5, 21, 50 - i, i + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
				}

				for (int i = 21; i <= 45; i += 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, 45, 9, i, boundingBox);
				}
			}

			if (this.chunkIntersects(boundingBox, 8, 44, 49, 54)) {
				this.generateBox(worldGenLevel, boundingBox, 14, 0, 44, 43, 0, 50, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 14, 1, 44, 43, 10, 50);

				for (int i = 12; i <= 45; i += 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 9, 45, boundingBox);
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 9, 52, boundingBox);
					if (i == 12 || i == 18 || i == 24 || i == 33 || i == 39 || i == 45) {
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 9, 47, boundingBox);
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 9, 50, boundingBox);
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 10, 45, boundingBox);
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 10, 46, boundingBox);
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 10, 51, boundingBox);
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 10, 52, boundingBox);
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 11, 47, boundingBox);
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 11, 50, boundingBox);
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 12, 48, boundingBox);
						this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 12, 49, boundingBox);
					}
				}

				for (int ix = 0; ix < 3; ix++) {
					this.generateBox(worldGenLevel, boundingBox, 8 + ix, 5 + ix, 54, 49 - ix, 5 + ix, 54, BASE_GRAY, BASE_GRAY, false);
				}

				this.generateBox(worldGenLevel, boundingBox, 11, 8, 54, 46, 8, 54, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 14, 8, 44, 43, 8, 53, BASE_GRAY, BASE_GRAY, false);
			}
		}

		private void generateUpperWall(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
			if (this.chunkIntersects(boundingBox, 14, 21, 20, 43)) {
				this.generateBox(worldGenLevel, boundingBox, 14, 0, 21, 20, 0, 43, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 14, 1, 22, 20, 14, 43);
				this.generateBox(worldGenLevel, boundingBox, 18, 12, 22, 20, 12, 39, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 18, 12, 21, 20, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, i + 14, i + 9, 21, i + 14, i + 9, 43 - i, BASE_LIGHT, BASE_LIGHT, false);
				}

				for (int i = 23; i <= 39; i += 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, 19, 13, i, boundingBox);
				}
			}

			if (this.chunkIntersects(boundingBox, 37, 21, 43, 43)) {
				this.generateBox(worldGenLevel, boundingBox, 37, 0, 21, 43, 0, 43, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 37, 1, 22, 43, 14, 43);
				this.generateBox(worldGenLevel, boundingBox, 37, 12, 22, 39, 12, 39, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 37, 12, 21, 39, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, 43 - i, i + 9, 21, 43 - i, i + 9, 43 - i, BASE_LIGHT, BASE_LIGHT, false);
				}

				for (int i = 23; i <= 39; i += 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, 38, 13, i, boundingBox);
				}
			}

			if (this.chunkIntersects(boundingBox, 15, 37, 42, 43)) {
				this.generateBox(worldGenLevel, boundingBox, 21, 0, 37, 36, 0, 43, BASE_GRAY, BASE_GRAY, false);
				this.generateWaterBox(worldGenLevel, boundingBox, 21, 1, 37, 36, 14, 43);
				this.generateBox(worldGenLevel, boundingBox, 21, 12, 37, 36, 12, 39, BASE_GRAY, BASE_GRAY, false);

				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, 15 + i, i + 9, 43 - i, 42 - i, i + 9, 43 - i, BASE_LIGHT, BASE_LIGHT, false);
				}

				for (int i = 21; i <= 36; i += 3) {
					this.placeBlock(worldGenLevel, DOT_DECO_DATA, i, 13, 38, boundingBox);
				}
			}
		}
	}

	interface MonumentRoomFitter {
		boolean fits(OceanMonumentPieces.RoomDefinition roomDefinition);

		OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, Random random);
	}

	public static class OceanMonumentCoreRoom extends OceanMonumentPieces.OceanMonumentPiece {
		public OceanMonumentCoreRoom(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition) {
			super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, 1, direction, roomDefinition, 2, 2, 2);
		}

		public OceanMonumentCoreRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, compoundTag);
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
			this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 0, 14, 8, 14, BASE_GRAY);
			int i = 7;
			BlockState blockState = BASE_LIGHT;
			this.generateBox(worldGenLevel, boundingBox, 0, 7, 0, 0, 7, 15, blockState, blockState, false);
			this.generateBox(worldGenLevel, boundingBox, 15, 7, 0, 15, 7, 15, blockState, blockState, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 7, 0, 15, 7, 0, blockState, blockState, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 7, 15, 14, 7, 15, blockState, blockState, false);

			for (int ix = 1; ix <= 6; ix++) {
				blockState = BASE_LIGHT;
				if (ix == 2 || ix == 6) {
					blockState = BASE_GRAY;
				}

				for (int j = 0; j <= 15; j += 15) {
					this.generateBox(worldGenLevel, boundingBox, j, ix, 0, j, ix, 1, blockState, blockState, false);
					this.generateBox(worldGenLevel, boundingBox, j, ix, 6, j, ix, 9, blockState, blockState, false);
					this.generateBox(worldGenLevel, boundingBox, j, ix, 14, j, ix, 15, blockState, blockState, false);
				}

				this.generateBox(worldGenLevel, boundingBox, 1, ix, 0, 1, ix, 0, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 6, ix, 0, 9, ix, 0, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 14, ix, 0, 14, ix, 0, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 1, ix, 15, 14, ix, 15, blockState, blockState, false);
			}

			this.generateBox(worldGenLevel, boundingBox, 6, 3, 6, 9, 6, 9, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.GOLD_BLOCK.defaultBlockState(), false);

			for (int ix = 3; ix <= 6; ix += 3) {
				for (int k = 6; k <= 9; k += 3) {
					this.placeBlock(worldGenLevel, LAMP_BLOCK, k, ix, 6, boundingBox);
					this.placeBlock(worldGenLevel, LAMP_BLOCK, k, ix, 9, boundingBox);
				}
			}

			this.generateBox(worldGenLevel, boundingBox, 5, 1, 6, 5, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 1, 9, 5, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 1, 6, 10, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 1, 9, 10, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 1, 5, 6, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 9, 1, 5, 9, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 1, 10, 6, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 9, 1, 10, 9, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 2, 5, 5, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 2, 10, 5, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 2, 5, 10, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 2, 10, 10, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 7, 1, 5, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 7, 1, 10, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 7, 9, 5, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 7, 9, 10, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 7, 5, 6, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 7, 10, 6, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 9, 7, 5, 14, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 9, 7, 10, 14, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 2, 1, 2, 2, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 3, 1, 2, 3, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 13, 1, 2, 13, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 12, 1, 2, 12, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 2, 1, 12, 2, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 3, 1, 13, 3, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 13, 1, 12, 13, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 12, 1, 13, 12, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
			return true;
		}
	}

	public static class OceanMonumentDoubleXRoom extends OceanMonumentPieces.OceanMonumentPiece {
		public OceanMonumentDoubleXRoom(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, 1, direction, roomDefinition, 2, 1, 1);
		}

		public OceanMonumentDoubleXRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, compoundTag);
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
			OceanMonumentPieces.RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
			OceanMonumentPieces.RoomDefinition roomDefinition2 = this.roomDefinition;
			if (this.roomDefinition.index / 25 > 0) {
				this.generateDefaultFloor(worldGenLevel, boundingBox, 8, 0, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
				this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
			}

			if (roomDefinition2.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 7, 4, 6, BASE_GRAY);
			}

			if (roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 8, 4, 1, 14, 4, 6, BASE_GRAY);
			}

			this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 15, 3, 0, 15, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 15, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 3, 7, 14, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 15, 2, 0, 15, 2, 7, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 15, 2, 0, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 14, 2, 7, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 15, 1, 0, 15, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 15, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 1, 0, 10, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 2, 0, 9, 2, 3, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 3, 0, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 6, 2, 3, boundingBox);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 9, 2, 3, boundingBox);
			if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
			}

			if (roomDefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
			}

			if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
			}

			if (roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 11, 1, 0, 12, 2, 0);
			}

			if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 11, 1, 7, 12, 2, 7);
			}

			if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 15, 1, 3, 15, 2, 4);
			}

			return true;
		}
	}

	public static class OceanMonumentDoubleXYRoom extends OceanMonumentPieces.OceanMonumentPiece {
		public OceanMonumentDoubleXYRoom(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, 1, direction, roomDefinition, 2, 2, 1);
		}

		public OceanMonumentDoubleXYRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, compoundTag);
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
			OceanMonumentPieces.RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
			OceanMonumentPieces.RoomDefinition roomDefinition2 = this.roomDefinition;
			OceanMonumentPieces.RoomDefinition roomDefinition3 = roomDefinition2.connections[Direction.UP.get3DDataValue()];
			OceanMonumentPieces.RoomDefinition roomDefinition4 = roomDefinition.connections[Direction.UP.get3DDataValue()];
			if (this.roomDefinition.index / 25 > 0) {
				this.generateDefaultFloor(worldGenLevel, boundingBox, 8, 0, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
				this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
			}

			if (roomDefinition3.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 1, 7, 8, 6, BASE_GRAY);
			}

			if (roomDefinition4.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 8, 8, 1, 14, 8, 6, BASE_GRAY);
			}

			for (int i = 1; i <= 7; i++) {
				BlockState blockState = BASE_LIGHT;
				if (i == 2 || i == 6) {
					blockState = BASE_GRAY;
				}

				this.generateBox(worldGenLevel, boundingBox, 0, i, 0, 0, i, 7, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 15, i, 0, 15, i, 7, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 1, i, 0, 15, i, 0, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 1, i, 7, 14, i, 7, blockState, blockState, false);
			}

			this.generateBox(worldGenLevel, boundingBox, 2, 1, 3, 2, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 3, 1, 2, 4, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 3, 1, 5, 4, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 13, 1, 3, 13, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 11, 1, 2, 12, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 11, 1, 5, 12, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 1, 3, 5, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 1, 3, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 7, 2, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 5, 2, 5, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 5, 2, 10, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 5, 5, 5, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 5, 5, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 6, 6, 2, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 9, 6, 2, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 6, 6, 5, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 9, 6, 5, boundingBox);
			this.generateBox(worldGenLevel, boundingBox, 5, 4, 3, 6, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 9, 4, 3, 10, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 4, 2, boundingBox);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 4, 5, boundingBox);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 10, 4, 2, boundingBox);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 10, 4, 5, boundingBox);
			if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
			}

			if (roomDefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
			}

			if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
			}

			if (roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 11, 1, 0, 12, 2, 0);
			}

			if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 11, 1, 7, 12, 2, 7);
			}

			if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 15, 1, 3, 15, 2, 4);
			}

			if (roomDefinition3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 5, 0, 4, 6, 0);
			}

			if (roomDefinition3.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 5, 7, 4, 6, 7);
			}

			if (roomDefinition3.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 5, 3, 0, 6, 4);
			}

			if (roomDefinition4.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 11, 5, 0, 12, 6, 0);
			}

			if (roomDefinition4.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 11, 5, 7, 12, 6, 7);
			}

			if (roomDefinition4.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 15, 5, 3, 15, 6, 4);
			}

			return true;
		}
	}

	public static class OceanMonumentDoubleYRoom extends OceanMonumentPieces.OceanMonumentPiece {
		public OceanMonumentDoubleYRoom(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, 1, direction, roomDefinition, 1, 2, 1);
		}

		public OceanMonumentDoubleYRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, compoundTag);
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
			if (this.roomDefinition.index / 25 > 0) {
				this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
			}

			OceanMonumentPieces.RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.UP.get3DDataValue()];
			if (roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 1, 6, 8, 6, BASE_GRAY);
			}

			this.generateBox(worldGenLevel, boundingBox, 0, 4, 0, 0, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 4, 0, 7, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 4, 0, 6, 4, 0, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 4, 7, 6, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 2, 4, 1, 2, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 4, 2, 1, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 4, 1, 5, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 4, 2, 6, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 2, 4, 5, 2, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 4, 5, 1, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 4, 5, 5, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 4, 5, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
			OceanMonumentPieces.RoomDefinition roomDefinition2 = this.roomDefinition;

			for (int i = 1; i <= 5; i += 4) {
				int j = 0;
				if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 2, i, j, 2, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 5, i, j, 5, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 3, i + 2, j, 4, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
				} else {
					this.generateBox(worldGenLevel, boundingBox, 0, i, j, 7, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 0, i + 1, j, 7, i + 1, j, BASE_GRAY, BASE_GRAY, false);
				}

				int var13 = 7;
				if (roomDefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 2, i, var13, 2, i + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 5, i, var13, 5, i + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 3, i + 2, var13, 4, i + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
				} else {
					this.generateBox(worldGenLevel, boundingBox, 0, i, var13, 7, i + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 0, i + 1, var13, 7, i + 1, var13, BASE_GRAY, BASE_GRAY, false);
				}

				int k = 0;
				if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, k, i, 2, k, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, k, i, 5, k, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, k, i + 2, 3, k, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
				} else {
					this.generateBox(worldGenLevel, boundingBox, k, i, 0, k, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, k, i + 1, 0, k, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
				}

				int var14 = 7;
				if (roomDefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, var14, i, 2, var14, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, var14, i, 5, var14, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, var14, i + 2, 3, var14, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
				} else {
					this.generateBox(worldGenLevel, boundingBox, var14, i, 0, var14, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, var14, i + 1, 0, var14, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
				}

				roomDefinition2 = roomDefinition;
			}

			return true;
		}
	}

	public static class OceanMonumentDoubleYZRoom extends OceanMonumentPieces.OceanMonumentPiece {
		public OceanMonumentDoubleYZRoom(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, 1, direction, roomDefinition, 1, 2, 2);
		}

		public OceanMonumentDoubleYZRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, compoundTag);
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
			OceanMonumentPieces.RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
			OceanMonumentPieces.RoomDefinition roomDefinition2 = this.roomDefinition;
			OceanMonumentPieces.RoomDefinition roomDefinition3 = roomDefinition.connections[Direction.UP.get3DDataValue()];
			OceanMonumentPieces.RoomDefinition roomDefinition4 = roomDefinition2.connections[Direction.UP.get3DDataValue()];
			if (this.roomDefinition.index / 25 > 0) {
				this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 8, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
				this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
			}

			if (roomDefinition4.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 1, 6, 8, 7, BASE_GRAY);
			}

			if (roomDefinition3.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 8, 6, 8, 14, BASE_GRAY);
			}

			for (int i = 1; i <= 7; i++) {
				BlockState blockState = BASE_LIGHT;
				if (i == 2 || i == 6) {
					blockState = BASE_GRAY;
				}

				this.generateBox(worldGenLevel, boundingBox, 0, i, 0, 0, i, 15, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 7, i, 0, 7, i, 15, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 1, i, 0, 6, i, 0, blockState, blockState, false);
				this.generateBox(worldGenLevel, boundingBox, 1, i, 15, 6, i, 15, blockState, blockState, false);
			}

			for (int i = 1; i <= 7; i++) {
				BlockState blockState = BASE_BLACK;
				if (i == 2 || i == 6) {
					blockState = LAMP_BLOCK;
				}

				this.generateBox(worldGenLevel, boundingBox, 3, i, 7, 4, i, 8, blockState, blockState, false);
			}

			if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
			}

			if (roomDefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4);
			}

			if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
			}

			if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 15, 4, 2, 15);
			}

			if (roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 11, 0, 2, 12);
			}

			if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 11, 7, 2, 12);
			}

			if (roomDefinition4.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 5, 0, 4, 6, 0);
			}

			if (roomDefinition4.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 7, 5, 3, 7, 6, 4);
				this.generateBox(worldGenLevel, boundingBox, 5, 4, 2, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 6, 1, 2, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 6, 1, 5, 6, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
			}

			if (roomDefinition4.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 5, 3, 0, 6, 4);
				this.generateBox(worldGenLevel, boundingBox, 1, 4, 2, 2, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 1, 2, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 1, 5, 1, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
			}

			if (roomDefinition3.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 5, 15, 4, 6, 15);
			}

			if (roomDefinition3.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 5, 11, 0, 6, 12);
				this.generateBox(worldGenLevel, boundingBox, 1, 4, 10, 2, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 1, 10, 1, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 1, 13, 1, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
			}

			if (roomDefinition3.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 7, 5, 11, 7, 6, 12);
				this.generateBox(worldGenLevel, boundingBox, 5, 4, 10, 6, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 6, 1, 10, 6, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 6, 1, 13, 6, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
			}

			return true;
		}
	}

	public static class OceanMonumentDoubleZRoom extends OceanMonumentPieces.OceanMonumentPiece {
		public OceanMonumentDoubleZRoom(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, 1, direction, roomDefinition, 1, 1, 2);
		}

		public OceanMonumentDoubleZRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, compoundTag);
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
			OceanMonumentPieces.RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
			OceanMonumentPieces.RoomDefinition roomDefinition2 = this.roomDefinition;
			if (this.roomDefinition.index / 25 > 0) {
				this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 8, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
				this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
			}

			if (roomDefinition2.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 6, 4, 7, BASE_GRAY);
			}

			if (roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 8, 6, 4, 14, BASE_GRAY);
			}

			this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 3, 15, 6, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 15, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 15, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 7, 2, 0, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 2, 15, 6, 2, 15, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 7, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 15, 6, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 1, 1, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 1, 1, 6, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 3, 1, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 3, 1, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 13, 1, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 1, 13, 6, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 3, 13, 1, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 3, 13, 6, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 2, 1, 6, 2, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 1, 6, 5, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 2, 1, 9, 2, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 1, 9, 5, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 3, 2, 6, 4, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 3, 2, 9, 4, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 2, 2, 7, 2, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 2, 7, 5, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 5, boundingBox);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 5, boundingBox);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 10, boundingBox);
			this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 10, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 2, 3, 5, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 5, 3, 5, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 2, 3, 10, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 5, 3, 10, boundingBox);
			if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
			}

			if (roomDefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4);
			}

			if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
			}

			if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 15, 4, 2, 15);
			}

			if (roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 11, 0, 2, 12);
			}

			if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 11, 7, 2, 12);
			}

			return true;
		}
	}

	public static class OceanMonumentEntryRoom extends OceanMonumentPieces.OceanMonumentPiece {
		public OceanMonumentEntryRoom(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition) {
			super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, 1, direction, roomDefinition, 1, 1, 1);
		}

		public OceanMonumentEntryRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, compoundTag);
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
			this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 1, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 2, 0, 7, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 2, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 5, 1, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
			if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
			}

			if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 1, 2, 4);
			}

			if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 6, 1, 3, 7, 2, 4);
			}

			return true;
		}
	}

	public static class OceanMonumentPenthouse extends OceanMonumentPieces.OceanMonumentPiece {
		public OceanMonumentPenthouse(Direction direction, BoundingBox boundingBox) {
			super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, direction, boundingBox);
		}

		public OceanMonumentPenthouse(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, compoundTag);
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
			this.generateBox(worldGenLevel, boundingBox, 2, -1, 2, 11, -1, 11, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 0, -1, 0, 1, -1, 11, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 12, -1, 0, 13, -1, 11, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 2, -1, 0, 11, -1, 1, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 2, -1, 12, 11, -1, 13, BASE_GRAY, BASE_GRAY, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 0, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 13, 0, 0, 13, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 0, 0, 12, 0, 0, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 0, 13, 12, 0, 13, BASE_LIGHT, BASE_LIGHT, false);

			for (int i = 2; i <= 11; i += 3) {
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 0, 0, i, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 13, 0, i, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, i, 0, 0, boundingBox);
			}

			this.generateBox(worldGenLevel, boundingBox, 2, 0, 3, 4, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 9, 0, 3, 11, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 4, 0, 9, 9, 0, 11, BASE_LIGHT, BASE_LIGHT, false);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 5, 0, 8, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 8, 0, 8, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 10, 0, 10, boundingBox);
			this.placeBlock(worldGenLevel, BASE_LIGHT, 3, 0, 10, boundingBox);
			this.generateBox(worldGenLevel, boundingBox, 3, 0, 3, 3, 0, 7, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 10, 0, 3, 10, 0, 7, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 6, 0, 10, 7, 0, 10, BASE_BLACK, BASE_BLACK, false);
			int i = 3;

			for (int j = 0; j < 2; j++) {
				for (int k = 2; k <= 8; k += 3) {
					this.generateBox(worldGenLevel, boundingBox, i, 0, k, i, 2, k, BASE_LIGHT, BASE_LIGHT, false);
				}

				i = 10;
			}

			this.generateBox(worldGenLevel, boundingBox, 5, 0, 10, 5, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 8, 0, 10, 8, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 6, -1, 7, 7, -1, 8, BASE_BLACK, BASE_BLACK, false);
			this.generateWaterBox(worldGenLevel, boundingBox, 6, -1, 3, 7, -1, 4);
			this.spawnElder(worldGenLevel, boundingBox, 6, 1, 6);
			return true;
		}
	}

	public abstract static class OceanMonumentPiece extends StructurePiece {
		protected static final BlockState BASE_GRAY = Blocks.PRISMARINE.defaultBlockState();
		protected static final BlockState BASE_LIGHT = Blocks.PRISMARINE_BRICKS.defaultBlockState();
		protected static final BlockState BASE_BLACK = Blocks.DARK_PRISMARINE.defaultBlockState();
		protected static final BlockState DOT_DECO_DATA = BASE_LIGHT;
		protected static final BlockState LAMP_BLOCK = Blocks.SEA_LANTERN.defaultBlockState();
		protected static final BlockState FILL_BLOCK = Blocks.WATER.defaultBlockState();
		protected static final Set<Block> FILL_KEEP = ImmutableSet.<Block>builder()
			.add(Blocks.ICE)
			.add(Blocks.PACKED_ICE)
			.add(Blocks.BLUE_ICE)
			.add(FILL_BLOCK.getBlock())
			.build();
		protected static final int GRIDROOM_SOURCE_INDEX = getRoomIndex(2, 0, 0);
		protected static final int GRIDROOM_TOP_CONNECT_INDEX = getRoomIndex(2, 2, 0);
		protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX = getRoomIndex(0, 1, 0);
		protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX = getRoomIndex(4, 1, 0);
		protected OceanMonumentPieces.RoomDefinition roomDefinition;

		protected static final int getRoomIndex(int i, int j, int k) {
			return j * 25 + k * 5 + i;
		}

		public OceanMonumentPiece(StructurePieceType structurePieceType, int i) {
			super(structurePieceType, i);
		}

		public OceanMonumentPiece(StructurePieceType structurePieceType, Direction direction, BoundingBox boundingBox) {
			super(structurePieceType, 1);
			this.setOrientation(direction);
			this.boundingBox = boundingBox;
		}

		protected OceanMonumentPiece(
			StructurePieceType structurePieceType, int i, Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, int j, int k, int l
		) {
			super(structurePieceType, i);
			this.setOrientation(direction);
			this.roomDefinition = roomDefinition;
			int m = roomDefinition.index;
			int n = m % 5;
			int o = m / 5 % 5;
			int p = m / 25;
			if (direction != Direction.NORTH && direction != Direction.SOUTH) {
				this.boundingBox = new BoundingBox(0, 0, 0, l * 8 - 1, k * 4 - 1, j * 8 - 1);
			} else {
				this.boundingBox = new BoundingBox(0, 0, 0, j * 8 - 1, k * 4 - 1, l * 8 - 1);
			}

			switch (direction) {
				case NORTH:
					this.boundingBox.move(n * 8, p * 4, -(o + l) * 8 + 1);
					break;
				case SOUTH:
					this.boundingBox.move(n * 8, p * 4, o * 8);
					break;
				case WEST:
					this.boundingBox.move(-(o + l) * 8 + 1, p * 4, n * 8);
					break;
				default:
					this.boundingBox.move(o * 8, p * 4, n * 8);
			}
		}

		public OceanMonumentPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
			super(structurePieceType, compoundTag);
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
		}

		protected void generateWaterBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k, int l, int m, int n) {
			for (int o = j; o <= m; o++) {
				for (int p = i; p <= l; p++) {
					for (int q = k; q <= n; q++) {
						BlockState blockState = this.getBlock(worldGenLevel, p, o, q, boundingBox);
						if (!FILL_KEEP.contains(blockState.getBlock())) {
							if (this.getWorldY(o) >= worldGenLevel.getSeaLevel() && blockState != FILL_BLOCK) {
								this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), p, o, q, boundingBox);
							} else {
								this.placeBlock(worldGenLevel, FILL_BLOCK, p, o, q, boundingBox);
							}
						}
					}
				}
			}
		}

		protected void generateDefaultFloor(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, boolean bl) {
			if (bl) {
				this.generateBox(worldGenLevel, boundingBox, i + 0, 0, j + 0, i + 2, 0, j + 8 - 1, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, i + 5, 0, j + 0, i + 8 - 1, 0, j + 8 - 1, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, i + 3, 0, j + 0, i + 4, 0, j + 2, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, i + 3, 0, j + 5, i + 4, 0, j + 8 - 1, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, i + 3, 0, j + 2, i + 4, 0, j + 2, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, i + 3, 0, j + 5, i + 4, 0, j + 5, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, i + 2, 0, j + 3, i + 2, 0, j + 4, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, i + 5, 0, j + 3, i + 5, 0, j + 4, BASE_LIGHT, BASE_LIGHT, false);
			} else {
				this.generateBox(worldGenLevel, boundingBox, i + 0, 0, j + 0, i + 8 - 1, 0, j + 8 - 1, BASE_GRAY, BASE_GRAY, false);
			}
		}

		protected void generateBoxOnFillOnly(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k, int l, int m, int n, BlockState blockState) {
			for (int o = j; o <= m; o++) {
				for (int p = i; p <= l; p++) {
					for (int q = k; q <= n; q++) {
						if (this.getBlock(worldGenLevel, p, o, q, boundingBox) == FILL_BLOCK) {
							this.placeBlock(worldGenLevel, blockState, p, o, q, boundingBox);
						}
					}
				}
			}
		}

		protected boolean chunkIntersects(BoundingBox boundingBox, int i, int j, int k, int l) {
			int m = this.getWorldX(i, j);
			int n = this.getWorldZ(i, j);
			int o = this.getWorldX(k, l);
			int p = this.getWorldZ(k, l);
			return boundingBox.intersects(Math.min(m, o), Math.min(n, p), Math.max(m, o), Math.max(n, p));
		}

		protected boolean spawnElder(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k) {
			int l = this.getWorldX(i, k);
			int m = this.getWorldY(j);
			int n = this.getWorldZ(i, k);
			if (boundingBox.isInside(new BlockPos(l, m, n))) {
				ElderGuardian elderGuardian = EntityType.ELDER_GUARDIAN.create(worldGenLevel.getLevel());
				elderGuardian.heal(elderGuardian.getMaxHealth());
				elderGuardian.moveTo((double)l + 0.5, (double)m, (double)n + 0.5, 0.0F, 0.0F);
				elderGuardian.finalizeSpawn(worldGenLevel, worldGenLevel.getCurrentDifficultyAt(elderGuardian.blockPosition()), MobSpawnType.STRUCTURE, null, null);
				worldGenLevel.addFreshEntityWithPassengers(elderGuardian);
				return true;
			} else {
				return false;
			}
		}
	}

	public static class OceanMonumentSimpleRoom extends OceanMonumentPieces.OceanMonumentPiece {
		private int mainDesign;

		public OceanMonumentSimpleRoom(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition, Random random) {
			super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, 1, direction, roomDefinition, 1, 1, 1);
			this.mainDesign = random.nextInt(3);
		}

		public OceanMonumentSimpleRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, compoundTag);
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
			if (this.roomDefinition.index / 25 > 0) {
				this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
			}

			if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
			}

			boolean bl = this.mainDesign != 0
				&& random.nextBoolean()
				&& !this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]
				&& !this.roomDefinition.hasOpening[Direction.UP.get3DDataValue()]
				&& this.roomDefinition.countOpenings() > 1;
			if (this.mainDesign == 0) {
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 2, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 2, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 2, 2, 0, BASE_GRAY, BASE_GRAY, false);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 1, 2, 1, boundingBox);
				this.generateBox(worldGenLevel, boundingBox, 5, 1, 0, 7, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 5, 3, 0, 7, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 2, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 5, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 6, 2, 1, boundingBox);
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 5, 2, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 3, 5, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 2, 5, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 2, 2, 7, BASE_GRAY, BASE_GRAY, false);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 1, 2, 6, boundingBox);
				this.generateBox(worldGenLevel, boundingBox, 5, 1, 5, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 5, 3, 5, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 2, 5, 7, 2, 7, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 5, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 6, 2, 6, boundingBox);
				if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 3, 3, 0, 4, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
				} else {
					this.generateBox(worldGenLevel, boundingBox, 3, 3, 0, 4, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 3, 2, 0, 4, 2, 0, BASE_GRAY, BASE_GRAY, false);
					this.generateBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 1, 1, BASE_LIGHT, BASE_LIGHT, false);
				}

				if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 3, 3, 7, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
				} else {
					this.generateBox(worldGenLevel, boundingBox, 3, 3, 6, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 3, 2, 7, 4, 2, 7, BASE_GRAY, BASE_GRAY, false);
					this.generateBox(worldGenLevel, boundingBox, 3, 1, 6, 4, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
				}

				if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 0, 3, 3, 0, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
				} else {
					this.generateBox(worldGenLevel, boundingBox, 0, 3, 3, 1, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 0, 2, 3, 0, 2, 4, BASE_GRAY, BASE_GRAY, false);
					this.generateBox(worldGenLevel, boundingBox, 0, 1, 3, 1, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
				}

				if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 7, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
				} else {
					this.generateBox(worldGenLevel, boundingBox, 6, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 7, 2, 3, 7, 2, 4, BASE_GRAY, BASE_GRAY, false);
					this.generateBox(worldGenLevel, boundingBox, 6, 1, 3, 7, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
				}
			} else if (this.mainDesign == 1) {
				this.generateBox(worldGenLevel, boundingBox, 2, 1, 2, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 2, 1, 5, 2, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 5, 1, 5, 5, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 5, 1, 2, 5, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 2, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 5, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 5, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 2, boundingBox);
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 1, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 7, 1, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 6, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 6, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 1, 6, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 6, 1, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 1, 1, 7, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
				this.placeBlock(worldGenLevel, BASE_GRAY, 1, 2, 0, boundingBox);
				this.placeBlock(worldGenLevel, BASE_GRAY, 0, 2, 1, boundingBox);
				this.placeBlock(worldGenLevel, BASE_GRAY, 1, 2, 7, boundingBox);
				this.placeBlock(worldGenLevel, BASE_GRAY, 0, 2, 6, boundingBox);
				this.placeBlock(worldGenLevel, BASE_GRAY, 6, 2, 7, boundingBox);
				this.placeBlock(worldGenLevel, BASE_GRAY, 7, 2, 6, boundingBox);
				this.placeBlock(worldGenLevel, BASE_GRAY, 6, 2, 0, boundingBox);
				this.placeBlock(worldGenLevel, BASE_GRAY, 7, 2, 1, boundingBox);
				if (!this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
					this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
				}

				if (!this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
					this.generateBox(worldGenLevel, boundingBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
				}

				if (!this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 0, 2, 1, 0, 2, 6, BASE_GRAY, BASE_GRAY, false);
					this.generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
				}

				if (!this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
					this.generateBox(worldGenLevel, boundingBox, 7, 3, 1, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, 7, 2, 1, 7, 2, 6, BASE_GRAY, BASE_GRAY, false);
					this.generateBox(worldGenLevel, boundingBox, 7, 1, 1, 7, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
				}
			} else if (this.mainDesign == 2) {
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
				if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
					this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
				}

				if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
					this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
				}

				if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
					this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
				}

				if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
					this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4);
				}
			}

			if (bl) {
				this.generateBox(worldGenLevel, boundingBox, 3, 1, 3, 4, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 3, 2, 3, 4, 2, 4, BASE_GRAY, BASE_GRAY, false);
				this.generateBox(worldGenLevel, boundingBox, 3, 3, 3, 4, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
			}

			return true;
		}
	}

	public static class OceanMonumentSimpleTopRoom extends OceanMonumentPieces.OceanMonumentPiece {
		public OceanMonumentSimpleTopRoom(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition) {
			super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, 1, direction, roomDefinition, 1, 1, 1);
		}

		public OceanMonumentSimpleTopRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, compoundTag);
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
			if (this.roomDefinition.index / 25 > 0) {
				this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
			}

			if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
				this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
			}

			for (int i = 1; i <= 6; i++) {
				for (int j = 1; j <= 6; j++) {
					if (random.nextInt(3) != 0) {
						int k = 2 + (random.nextInt(4) == 0 ? 0 : 1);
						BlockState blockState = Blocks.WET_SPONGE.defaultBlockState();
						this.generateBox(worldGenLevel, boundingBox, i, k, j, i, 3, j, blockState, blockState, false);
					}
				}
			}

			this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
			this.generateBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
			this.generateBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
			if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
				this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
			}

			return true;
		}
	}

	public static class OceanMonumentWingRoom extends OceanMonumentPieces.OceanMonumentPiece {
		private int mainDesign;

		public OceanMonumentWingRoom(Direction direction, BoundingBox boundingBox, int i) {
			super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, direction, boundingBox);
			this.mainDesign = i & 1;
		}

		public OceanMonumentWingRoom(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, compoundTag);
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
			if (this.mainDesign == 0) {
				for (int i = 0; i < 4; i++) {
					this.generateBox(worldGenLevel, boundingBox, 10 - i, 3 - i, 20 - i, 12 + i, 3 - i, 20, BASE_LIGHT, BASE_LIGHT, false);
				}

				this.generateBox(worldGenLevel, boundingBox, 7, 0, 6, 15, 0, 16, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 6, 0, 6, 6, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 16, 0, 6, 16, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 1, 7, 7, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 15, 1, 7, 15, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 7, 1, 6, 9, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 13, 1, 6, 15, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 8, 1, 7, 9, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 13, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 9, 0, 5, 13, 0, 5, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 10, 0, 7, 12, 0, 7, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 8, 0, 10, 8, 0, 12, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 14, 0, 10, 14, 0, 12, BASE_BLACK, BASE_BLACK, false);

				for (int i = 18; i >= 7; i -= 3) {
					this.placeBlock(worldGenLevel, LAMP_BLOCK, 6, 3, i, boundingBox);
					this.placeBlock(worldGenLevel, LAMP_BLOCK, 16, 3, i, boundingBox);
				}

				this.placeBlock(worldGenLevel, LAMP_BLOCK, 10, 0, 10, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 12, 0, 10, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 10, 0, 12, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 12, 0, 12, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 8, 3, 6, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 14, 3, 6, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 4, 2, 4, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 4, 1, 4, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 4, 0, 4, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 18, 2, 4, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 18, 1, 4, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 18, 0, 4, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 4, 2, 18, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 4, 1, 18, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 4, 0, 18, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 18, 2, 18, boundingBox);
				this.placeBlock(worldGenLevel, LAMP_BLOCK, 18, 1, 18, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 18, 0, 18, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 9, 7, 20, boundingBox);
				this.placeBlock(worldGenLevel, BASE_LIGHT, 13, 7, 20, boundingBox);
				this.generateBox(worldGenLevel, boundingBox, 6, 0, 21, 7, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 15, 0, 21, 16, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
				this.spawnElder(worldGenLevel, boundingBox, 11, 2, 16);
			} else if (this.mainDesign == 1) {
				this.generateBox(worldGenLevel, boundingBox, 9, 3, 18, 13, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 9, 0, 18, 9, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
				this.generateBox(worldGenLevel, boundingBox, 13, 0, 18, 13, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
				int i = 9;
				int j = 20;
				int k = 5;

				for (int l = 0; l < 2; l++) {
					this.placeBlock(worldGenLevel, BASE_LIGHT, i, 6, 20, boundingBox);
					this.placeBlock(worldGenLevel, LAMP_BLOCK, i, 5, 20, boundingBox);
					this.placeBlock(worldGenLevel, BASE_LIGHT, i, 4, 20, boundingBox);
					i = 13;
				}

				this.generateBox(worldGenLevel, boundingBox, 7, 3, 7, 15, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
				int var14 = 10;

				for (int l = 0; l < 2; l++) {
					this.generateBox(worldGenLevel, boundingBox, var14, 0, 10, var14, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, var14, 0, 12, var14, 6, 12, BASE_LIGHT, BASE_LIGHT, false);
					this.placeBlock(worldGenLevel, LAMP_BLOCK, var14, 0, 10, boundingBox);
					this.placeBlock(worldGenLevel, LAMP_BLOCK, var14, 0, 12, boundingBox);
					this.placeBlock(worldGenLevel, LAMP_BLOCK, var14, 4, 10, boundingBox);
					this.placeBlock(worldGenLevel, LAMP_BLOCK, var14, 4, 12, boundingBox);
					var14 = 12;
				}

				var14 = 8;

				for (int l = 0; l < 2; l++) {
					this.generateBox(worldGenLevel, boundingBox, var14, 0, 7, var14, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
					this.generateBox(worldGenLevel, boundingBox, var14, 0, 14, var14, 2, 14, BASE_LIGHT, BASE_LIGHT, false);
					var14 = 14;
				}

				this.generateBox(worldGenLevel, boundingBox, 8, 3, 8, 8, 3, 13, BASE_BLACK, BASE_BLACK, false);
				this.generateBox(worldGenLevel, boundingBox, 14, 3, 8, 14, 3, 13, BASE_BLACK, BASE_BLACK, false);
				this.spawnElder(worldGenLevel, boundingBox, 11, 5, 13);
			}

			return true;
		}
	}

	static class RoomDefinition {
		private final int index;
		private final OceanMonumentPieces.RoomDefinition[] connections = new OceanMonumentPieces.RoomDefinition[6];
		private final boolean[] hasOpening = new boolean[6];
		private boolean claimed;
		private boolean isSource;
		private int scanIndex;

		public RoomDefinition(int i) {
			this.index = i;
		}

		public void setConnection(Direction direction, OceanMonumentPieces.RoomDefinition roomDefinition) {
			this.connections[direction.get3DDataValue()] = roomDefinition;
			roomDefinition.connections[direction.getOpposite().get3DDataValue()] = this;
		}

		public void updateOpenings() {
			for (int i = 0; i < 6; i++) {
				this.hasOpening[i] = this.connections[i] != null;
			}
		}

		public boolean findSource(int i) {
			if (this.isSource) {
				return true;
			} else {
				this.scanIndex = i;

				for (int j = 0; j < 6; j++) {
					if (this.connections[j] != null && this.hasOpening[j] && this.connections[j].scanIndex != i && this.connections[j].findSource(i)) {
						return true;
					}
				}

				return false;
			}
		}

		public boolean isSpecial() {
			return this.index >= 75;
		}

		public int countOpenings() {
			int i = 0;

			for (int j = 0; j < 6; j++) {
				if (this.hasOpening[j]) {
					i++;
				}
			}

			return i;
		}
	}
}
