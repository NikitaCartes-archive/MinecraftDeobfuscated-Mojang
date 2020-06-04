package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class StructureTemplate {
	private final List<StructureTemplate.Palette> palettes = Lists.<StructureTemplate.Palette>newArrayList();
	private final List<StructureTemplate.StructureEntityInfo> entityInfoList = Lists.<StructureTemplate.StructureEntityInfo>newArrayList();
	private BlockPos size = BlockPos.ZERO;
	private String author = "?";

	public BlockPos getSize() {
		return this.size;
	}

	public void setAuthor(String string) {
		this.author = string;
	}

	public String getAuthor() {
		return this.author;
	}

	public void fillFromWorld(Level level, BlockPos blockPos, BlockPos blockPos2, boolean bl, @Nullable Block block) {
		if (blockPos2.getX() >= 1 && blockPos2.getY() >= 1 && blockPos2.getZ() >= 1) {
			BlockPos blockPos3 = blockPos.offset(blockPos2).offset(-1, -1, -1);
			List<StructureTemplate.StructureBlockInfo> list = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();
			List<StructureTemplate.StructureBlockInfo> list2 = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();
			List<StructureTemplate.StructureBlockInfo> list3 = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();
			BlockPos blockPos4 = new BlockPos(
				Math.min(blockPos.getX(), blockPos3.getX()), Math.min(blockPos.getY(), blockPos3.getY()), Math.min(blockPos.getZ(), blockPos3.getZ())
			);
			BlockPos blockPos5 = new BlockPos(
				Math.max(blockPos.getX(), blockPos3.getX()), Math.max(blockPos.getY(), blockPos3.getY()), Math.max(blockPos.getZ(), blockPos3.getZ())
			);
			this.size = blockPos2;

			for (BlockPos blockPos6 : BlockPos.betweenClosed(blockPos4, blockPos5)) {
				BlockPos blockPos7 = blockPos6.subtract(blockPos4);
				BlockState blockState = level.getBlockState(blockPos6);
				if (block == null || block != blockState.getBlock()) {
					BlockEntity blockEntity = level.getBlockEntity(blockPos6);
					StructureTemplate.StructureBlockInfo structureBlockInfo;
					if (blockEntity != null) {
						CompoundTag compoundTag = blockEntity.save(new CompoundTag());
						compoundTag.remove("x");
						compoundTag.remove("y");
						compoundTag.remove("z");
						structureBlockInfo = new StructureTemplate.StructureBlockInfo(blockPos7, blockState, compoundTag);
					} else {
						structureBlockInfo = new StructureTemplate.StructureBlockInfo(blockPos7, blockState, null);
					}

					addToLists(structureBlockInfo, list, list2, list3);
				}
			}

			List<StructureTemplate.StructureBlockInfo> list4 = buildInfoList(list, list2, list3);
			this.palettes.clear();
			this.palettes.add(new StructureTemplate.Palette(list4));
			if (bl) {
				this.fillEntityList(level, blockPos4, blockPos5.offset(1, 1, 1));
			} else {
				this.entityInfoList.clear();
			}
		}
	}

	private static void addToLists(
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		List<StructureTemplate.StructureBlockInfo> list,
		List<StructureTemplate.StructureBlockInfo> list2,
		List<StructureTemplate.StructureBlockInfo> list3
	) {
		if (structureBlockInfo.nbt != null) {
			list2.add(structureBlockInfo);
		} else if (!structureBlockInfo.state.getBlock().hasDynamicShape()
			&& structureBlockInfo.state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
			list.add(structureBlockInfo);
		} else {
			list3.add(structureBlockInfo);
		}
	}

	private static List<StructureTemplate.StructureBlockInfo> buildInfoList(
		List<StructureTemplate.StructureBlockInfo> list, List<StructureTemplate.StructureBlockInfo> list2, List<StructureTemplate.StructureBlockInfo> list3
	) {
		Comparator<StructureTemplate.StructureBlockInfo> comparator = Comparator.comparingInt(structureBlockInfo -> structureBlockInfo.pos.getY())
			.thenComparingInt(structureBlockInfo -> structureBlockInfo.pos.getX())
			.thenComparingInt(structureBlockInfo -> structureBlockInfo.pos.getZ());
		list.sort(comparator);
		list3.sort(comparator);
		list2.sort(comparator);
		List<StructureTemplate.StructureBlockInfo> list4 = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();
		list4.addAll(list);
		list4.addAll(list3);
		list4.addAll(list2);
		return list4;
	}

	private void fillEntityList(Level level, BlockPos blockPos, BlockPos blockPos2) {
		List<Entity> list = level.getEntitiesOfClass(Entity.class, new AABB(blockPos, blockPos2), entityx -> !(entityx instanceof Player));
		this.entityInfoList.clear();

		for (Entity entity : list) {
			Vec3 vec3 = new Vec3(entity.getX() - (double)blockPos.getX(), entity.getY() - (double)blockPos.getY(), entity.getZ() - (double)blockPos.getZ());
			CompoundTag compoundTag = new CompoundTag();
			entity.save(compoundTag);
			BlockPos blockPos3;
			if (entity instanceof Painting) {
				blockPos3 = ((Painting)entity).getPos().subtract(blockPos);
			} else {
				blockPos3 = new BlockPos(vec3);
			}

			this.entityInfoList.add(new StructureTemplate.StructureEntityInfo(vec3, blockPos3, compoundTag));
		}
	}

	public List<StructureTemplate.StructureBlockInfo> filterBlocks(BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Block block) {
		return this.filterBlocks(blockPos, structurePlaceSettings, block, true);
	}

	public List<StructureTemplate.StructureBlockInfo> filterBlocks(BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Block block, boolean bl) {
		List<StructureTemplate.StructureBlockInfo> list = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();
		BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
		if (this.palettes.isEmpty()) {
			return Collections.emptyList();
		} else {
			for (StructureTemplate.StructureBlockInfo structureBlockInfo : structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks(block)) {
				BlockPos blockPos2 = bl ? calculateRelativePosition(structurePlaceSettings, structureBlockInfo.pos).offset(blockPos) : structureBlockInfo.pos;
				if (boundingBox == null || boundingBox.isInside(blockPos2)) {
					list.add(
						new StructureTemplate.StructureBlockInfo(blockPos2, structureBlockInfo.state.rotate(structurePlaceSettings.getRotation()), structureBlockInfo.nbt)
					);
				}
			}

			return list;
		}
	}

	public BlockPos calculateConnectedPosition(
		StructurePlaceSettings structurePlaceSettings, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings2, BlockPos blockPos2
	) {
		BlockPos blockPos3 = calculateRelativePosition(structurePlaceSettings, blockPos);
		BlockPos blockPos4 = calculateRelativePosition(structurePlaceSettings2, blockPos2);
		return blockPos3.subtract(blockPos4);
	}

	public static BlockPos calculateRelativePosition(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
		return transform(blockPos, structurePlaceSettings.getMirror(), structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot());
	}

	public void placeInWorldChunk(LevelAccessor levelAccessor, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Random random) {
		structurePlaceSettings.updateBoundingBoxFromChunkPos();
		this.placeInWorld(levelAccessor, blockPos, structurePlaceSettings, random);
	}

	public void placeInWorld(LevelAccessor levelAccessor, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Random random) {
		this.placeInWorld(levelAccessor, blockPos, blockPos, structurePlaceSettings, random, 2);
	}

	public boolean placeInWorld(
		LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, Random random, int i
	) {
		if (this.palettes.isEmpty()) {
			return false;
		} else {
			List<StructureTemplate.StructureBlockInfo> list = structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks();
			if ((!list.isEmpty() || !structurePlaceSettings.isIgnoreEntities() && !this.entityInfoList.isEmpty())
				&& this.size.getX() >= 1
				&& this.size.getY() >= 1
				&& this.size.getZ() >= 1) {
				BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
				List<BlockPos> list2 = Lists.<BlockPos>newArrayListWithCapacity(structurePlaceSettings.shouldKeepLiquids() ? list.size() : 0);
				List<Pair<BlockPos, CompoundTag>> list3 = Lists.<Pair<BlockPos, CompoundTag>>newArrayListWithCapacity(list.size());
				int j = Integer.MAX_VALUE;
				int k = Integer.MAX_VALUE;
				int l = Integer.MAX_VALUE;
				int m = Integer.MIN_VALUE;
				int n = Integer.MIN_VALUE;
				int o = Integer.MIN_VALUE;

				for (StructureTemplate.StructureBlockInfo structureBlockInfo : processBlockInfos(levelAccessor, blockPos, blockPos2, structurePlaceSettings, list)) {
					BlockPos blockPos3 = structureBlockInfo.pos;
					if (boundingBox == null || boundingBox.isInside(blockPos3)) {
						FluidState fluidState = structurePlaceSettings.shouldKeepLiquids() ? levelAccessor.getFluidState(blockPos3) : null;
						BlockState blockState = structureBlockInfo.state.mirror(structurePlaceSettings.getMirror()).rotate(structurePlaceSettings.getRotation());
						if (structureBlockInfo.nbt != null) {
							BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos3);
							Clearable.tryClear(blockEntity);
							levelAccessor.setBlock(blockPos3, Blocks.BARRIER.defaultBlockState(), 20);
						}

						if (levelAccessor.setBlock(blockPos3, blockState, i)) {
							j = Math.min(j, blockPos3.getX());
							k = Math.min(k, blockPos3.getY());
							l = Math.min(l, blockPos3.getZ());
							m = Math.max(m, blockPos3.getX());
							n = Math.max(n, blockPos3.getY());
							o = Math.max(o, blockPos3.getZ());
							list3.add(Pair.of(blockPos3, structureBlockInfo.nbt));
							if (structureBlockInfo.nbt != null) {
								BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos3);
								if (blockEntity != null) {
									structureBlockInfo.nbt.putInt("x", blockPos3.getX());
									structureBlockInfo.nbt.putInt("y", blockPos3.getY());
									structureBlockInfo.nbt.putInt("z", blockPos3.getZ());
									if (blockEntity instanceof RandomizableContainerBlockEntity) {
										structureBlockInfo.nbt.putLong("LootTableSeed", random.nextLong());
									}

									blockEntity.load(structureBlockInfo.state, structureBlockInfo.nbt);
									blockEntity.mirror(structurePlaceSettings.getMirror());
									blockEntity.rotate(structurePlaceSettings.getRotation());
								}
							}

							if (fluidState != null && blockState.getBlock() instanceof LiquidBlockContainer) {
								((LiquidBlockContainer)blockState.getBlock()).placeLiquid(levelAccessor, blockPos3, blockState, fluidState);
								if (!fluidState.isSource()) {
									list2.add(blockPos3);
								}
							}
						}
					}
				}

				boolean bl = true;
				Direction[] directions = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

				while (bl && !list2.isEmpty()) {
					bl = false;
					Iterator<BlockPos> iterator = list2.iterator();

					while (iterator.hasNext()) {
						BlockPos blockPos4 = (BlockPos)iterator.next();
						BlockPos blockPos5 = blockPos4;
						FluidState fluidState2 = levelAccessor.getFluidState(blockPos4);

						for (int p = 0; p < directions.length && !fluidState2.isSource(); p++) {
							BlockPos blockPos6 = blockPos5.relative(directions[p]);
							FluidState fluidState3 = levelAccessor.getFluidState(blockPos6);
							if (fluidState3.getHeight(levelAccessor, blockPos6) > fluidState2.getHeight(levelAccessor, blockPos5)
								|| fluidState3.isSource() && !fluidState2.isSource()) {
								fluidState2 = fluidState3;
								blockPos5 = blockPos6;
							}
						}

						if (fluidState2.isSource()) {
							BlockState blockState2 = levelAccessor.getBlockState(blockPos4);
							Block block = blockState2.getBlock();
							if (block instanceof LiquidBlockContainer) {
								((LiquidBlockContainer)block).placeLiquid(levelAccessor, blockPos4, blockState2, fluidState2);
								bl = true;
								iterator.remove();
							}
						}
					}
				}

				if (j <= m) {
					if (!structurePlaceSettings.getKnownShape()) {
						DiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(m - j + 1, n - k + 1, o - l + 1);
						int q = j;
						int r = k;
						int s = l;

						for (Pair<BlockPos, CompoundTag> pair : list3) {
							BlockPos blockPos7 = pair.getFirst();
							discreteVoxelShape.setFull(blockPos7.getX() - q, blockPos7.getY() - r, blockPos7.getZ() - s, true, true);
						}

						updateShapeAtEdge(levelAccessor, i, discreteVoxelShape, q, r, s);
					}

					for (Pair<BlockPos, CompoundTag> pair2 : list3) {
						BlockPos blockPos5 = pair2.getFirst();
						if (!structurePlaceSettings.getKnownShape()) {
							BlockState blockState3 = levelAccessor.getBlockState(blockPos5);
							BlockState blockState2 = Block.updateFromNeighbourShapes(blockState3, levelAccessor, blockPos5);
							if (blockState3 != blockState2) {
								levelAccessor.setBlock(blockPos5, blockState2, i & -2 | 16);
							}

							levelAccessor.blockUpdated(blockPos5, blockState2.getBlock());
						}

						if (pair2.getSecond() != null) {
							BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos5);
							if (blockEntity != null) {
								blockEntity.setChanged();
							}
						}
					}
				}

				if (!structurePlaceSettings.isIgnoreEntities()) {
					this.placeEntities(
						levelAccessor,
						blockPos,
						structurePlaceSettings.getMirror(),
						structurePlaceSettings.getRotation(),
						structurePlaceSettings.getRotationPivot(),
						boundingBox,
						structurePlaceSettings.shouldFinalizeEntities()
					);
				}

				return true;
			} else {
				return false;
			}
		}
	}

	public static void updateShapeAtEdge(LevelAccessor levelAccessor, int i, DiscreteVoxelShape discreteVoxelShape, int j, int k, int l) {
		discreteVoxelShape.forAllFaces((direction, m, n, o) -> {
			BlockPos blockPos = new BlockPos(j + m, k + n, l + o);
			BlockPos blockPos2 = blockPos.relative(direction);
			BlockState blockState = levelAccessor.getBlockState(blockPos);
			BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
			BlockState blockState3 = blockState.updateShape(direction, blockState2, levelAccessor, blockPos, blockPos2);
			if (blockState != blockState3) {
				levelAccessor.setBlock(blockPos, blockState3, i & -2);
			}

			BlockState blockState4 = blockState2.updateShape(direction.getOpposite(), blockState3, levelAccessor, blockPos2, blockPos);
			if (blockState2 != blockState4) {
				levelAccessor.setBlock(blockPos2, blockState4, i & -2);
			}
		});
	}

	public static List<StructureTemplate.StructureBlockInfo> processBlockInfos(
		LevelAccessor levelAccessor,
		BlockPos blockPos,
		BlockPos blockPos2,
		StructurePlaceSettings structurePlaceSettings,
		List<StructureTemplate.StructureBlockInfo> list
	) {
		List<StructureTemplate.StructureBlockInfo> list2 = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();

		for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
			BlockPos blockPos3 = calculateRelativePosition(structurePlaceSettings, structureBlockInfo.pos).offset(blockPos);
			StructureTemplate.StructureBlockInfo structureBlockInfo2 = new StructureTemplate.StructureBlockInfo(
				blockPos3, structureBlockInfo.state, structureBlockInfo.nbt
			);
			Iterator<StructureProcessor> iterator = structurePlaceSettings.getProcessors().iterator();

			while (structureBlockInfo2 != null && iterator.hasNext()) {
				structureBlockInfo2 = ((StructureProcessor)iterator.next())
					.processBlock(levelAccessor, blockPos, blockPos2, structureBlockInfo, structureBlockInfo2, structurePlaceSettings);
			}

			if (structureBlockInfo2 != null) {
				list2.add(structureBlockInfo2);
			}
		}

		return list2;
	}

	private void placeEntities(
		LevelAccessor levelAccessor, BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2, @Nullable BoundingBox boundingBox, boolean bl
	) {
		for (StructureTemplate.StructureEntityInfo structureEntityInfo : this.entityInfoList) {
			BlockPos blockPos3 = transform(structureEntityInfo.blockPos, mirror, rotation, blockPos2).offset(blockPos);
			if (boundingBox == null || boundingBox.isInside(blockPos3)) {
				CompoundTag compoundTag = structureEntityInfo.nbt;
				Vec3 vec3 = transform(structureEntityInfo.pos, mirror, rotation, blockPos2);
				Vec3 vec32 = vec3.add((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
				ListTag listTag = new ListTag();
				listTag.add(DoubleTag.valueOf(vec32.x));
				listTag.add(DoubleTag.valueOf(vec32.y));
				listTag.add(DoubleTag.valueOf(vec32.z));
				compoundTag.put("Pos", listTag);
				compoundTag.remove("UUID");
				createEntityIgnoreException(levelAccessor, compoundTag).ifPresent(entity -> {
					float f = entity.mirror(mirror);
					f += entity.yRot - entity.rotate(rotation);
					entity.moveTo(vec32.x, vec32.y, vec32.z, f, entity.xRot);
					if (bl && entity instanceof Mob) {
						((Mob)entity).finalizeSpawn(levelAccessor, levelAccessor.getCurrentDifficultyAt(new BlockPos(vec32)), MobSpawnType.STRUCTURE, null, compoundTag);
					}

					levelAccessor.addFreshEntity(entity);
				});
			}
		}
	}

	private static Optional<Entity> createEntityIgnoreException(LevelAccessor levelAccessor, CompoundTag compoundTag) {
		try {
			return EntityType.create(compoundTag, levelAccessor.getLevel());
		} catch (Exception var3) {
			return Optional.empty();
		}
	}

	public BlockPos getSize(Rotation rotation) {
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:
				return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
			default:
				return this.size;
		}
	}

	public static BlockPos transform(BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		boolean bl = true;
		switch (mirror) {
			case LEFT_RIGHT:
				k = -k;
				break;
			case FRONT_BACK:
				i = -i;
				break;
			default:
				bl = false;
		}

		int l = blockPos2.getX();
		int m = blockPos2.getZ();
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
				return new BlockPos(l - m + k, j, l + m - i);
			case CLOCKWISE_90:
				return new BlockPos(l + m - k, j, m - l + i);
			case CLOCKWISE_180:
				return new BlockPos(l + l - i, j, m + m - k);
			default:
				return bl ? new BlockPos(i, j, k) : blockPos;
		}
	}

	public static Vec3 transform(Vec3 vec3, Mirror mirror, Rotation rotation, BlockPos blockPos) {
		double d = vec3.x;
		double e = vec3.y;
		double f = vec3.z;
		boolean bl = true;
		switch (mirror) {
			case LEFT_RIGHT:
				f = 1.0 - f;
				break;
			case FRONT_BACK:
				d = 1.0 - d;
				break;
			default:
				bl = false;
		}

		int i = blockPos.getX();
		int j = blockPos.getZ();
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
				return new Vec3((double)(i - j) + f, e, (double)(i + j + 1) - d);
			case CLOCKWISE_90:
				return new Vec3((double)(i + j + 1) - f, e, (double)(j - i) + d);
			case CLOCKWISE_180:
				return new Vec3((double)(i + i + 1) - d, e, (double)(j + j + 1) - f);
			default:
				return bl ? new Vec3(d, e, f) : vec3;
		}
	}

	public BlockPos getZeroPositionWithTransform(BlockPos blockPos, Mirror mirror, Rotation rotation) {
		return getZeroPositionWithTransform(blockPos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
	}

	public static BlockPos getZeroPositionWithTransform(BlockPos blockPos, Mirror mirror, Rotation rotation, int i, int j) {
		i--;
		j--;
		int k = mirror == Mirror.FRONT_BACK ? i : 0;
		int l = mirror == Mirror.LEFT_RIGHT ? j : 0;
		BlockPos blockPos2 = blockPos;
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
				blockPos2 = blockPos.offset(l, 0, i - k);
				break;
			case CLOCKWISE_90:
				blockPos2 = blockPos.offset(j - l, 0, k);
				break;
			case CLOCKWISE_180:
				blockPos2 = blockPos.offset(i - k, 0, j - l);
				break;
			case NONE:
				blockPos2 = blockPos.offset(k, 0, l);
		}

		return blockPos2;
	}

	public BoundingBox getBoundingBox(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
		return this.getBoundingBox(blockPos, structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot(), structurePlaceSettings.getMirror());
	}

	public BoundingBox getBoundingBox(BlockPos blockPos, Rotation rotation, BlockPos blockPos2, Mirror mirror) {
		BlockPos blockPos3 = this.getSize(rotation);
		int i = blockPos2.getX();
		int j = blockPos2.getZ();
		int k = blockPos3.getX() - 1;
		int l = blockPos3.getY() - 1;
		int m = blockPos3.getZ() - 1;
		BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
				boundingBox = new BoundingBox(i - j, 0, i + j - m, i - j + k, l, i + j);
				break;
			case CLOCKWISE_90:
				boundingBox = new BoundingBox(i + j - k, 0, j - i, i + j, l, j - i + m);
				break;
			case CLOCKWISE_180:
				boundingBox = new BoundingBox(i + i - k, 0, j + j - m, i + i, l, j + j);
				break;
			case NONE:
				boundingBox = new BoundingBox(0, 0, 0, k, l, m);
		}

		switch (mirror) {
			case LEFT_RIGHT:
				this.mirrorAABB(rotation, m, k, boundingBox, Direction.NORTH, Direction.SOUTH);
				break;
			case FRONT_BACK:
				this.mirrorAABB(rotation, k, m, boundingBox, Direction.WEST, Direction.EAST);
			case NONE:
		}

		boundingBox.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		return boundingBox;
	}

	private void mirrorAABB(Rotation rotation, int i, int j, BoundingBox boundingBox, Direction direction, Direction direction2) {
		BlockPos blockPos = BlockPos.ZERO;
		if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
			blockPos = blockPos.relative(rotation.rotate(direction), j);
		} else if (rotation == Rotation.CLOCKWISE_180) {
			blockPos = blockPos.relative(direction2, i);
		} else {
			blockPos = blockPos.relative(direction, i);
		}

		boundingBox.move(blockPos.getX(), 0, blockPos.getZ());
	}

	public CompoundTag save(CompoundTag compoundTag) {
		if (this.palettes.isEmpty()) {
			compoundTag.put("blocks", new ListTag());
			compoundTag.put("palette", new ListTag());
		} else {
			List<StructureTemplate.SimplePalette> list = Lists.<StructureTemplate.SimplePalette>newArrayList();
			StructureTemplate.SimplePalette simplePalette = new StructureTemplate.SimplePalette();
			list.add(simplePalette);

			for (int i = 1; i < this.palettes.size(); i++) {
				list.add(new StructureTemplate.SimplePalette());
			}

			ListTag listTag = new ListTag();
			List<StructureTemplate.StructureBlockInfo> list2 = ((StructureTemplate.Palette)this.palettes.get(0)).blocks();

			for (int j = 0; j < list2.size(); j++) {
				StructureTemplate.StructureBlockInfo structureBlockInfo = (StructureTemplate.StructureBlockInfo)list2.get(j);
				CompoundTag compoundTag2 = new CompoundTag();
				compoundTag2.put("pos", this.newIntegerList(structureBlockInfo.pos.getX(), structureBlockInfo.pos.getY(), structureBlockInfo.pos.getZ()));
				int k = simplePalette.idFor(structureBlockInfo.state);
				compoundTag2.putInt("state", k);
				if (structureBlockInfo.nbt != null) {
					compoundTag2.put("nbt", structureBlockInfo.nbt);
				}

				listTag.add(compoundTag2);

				for (int l = 1; l < this.palettes.size(); l++) {
					StructureTemplate.SimplePalette simplePalette2 = (StructureTemplate.SimplePalette)list.get(l);
					simplePalette2.addMapping(((StructureTemplate.StructureBlockInfo)((StructureTemplate.Palette)this.palettes.get(l)).blocks().get(j)).state, k);
				}
			}

			compoundTag.put("blocks", listTag);
			if (list.size() == 1) {
				ListTag listTag2 = new ListTag();

				for (BlockState blockState : simplePalette) {
					listTag2.add(NbtUtils.writeBlockState(blockState));
				}

				compoundTag.put("palette", listTag2);
			} else {
				ListTag listTag2 = new ListTag();

				for (StructureTemplate.SimplePalette simplePalette3 : list) {
					ListTag listTag3 = new ListTag();

					for (BlockState blockState2 : simplePalette3) {
						listTag3.add(NbtUtils.writeBlockState(blockState2));
					}

					listTag2.add(listTag3);
				}

				compoundTag.put("palettes", listTag2);
			}
		}

		ListTag listTag4 = new ListTag();

		for (StructureTemplate.StructureEntityInfo structureEntityInfo : this.entityInfoList) {
			CompoundTag compoundTag3 = new CompoundTag();
			compoundTag3.put("pos", this.newDoubleList(structureEntityInfo.pos.x, structureEntityInfo.pos.y, structureEntityInfo.pos.z));
			compoundTag3.put(
				"blockPos", this.newIntegerList(structureEntityInfo.blockPos.getX(), structureEntityInfo.blockPos.getY(), structureEntityInfo.blockPos.getZ())
			);
			if (structureEntityInfo.nbt != null) {
				compoundTag3.put("nbt", structureEntityInfo.nbt);
			}

			listTag4.add(compoundTag3);
		}

		compoundTag.put("entities", listTag4);
		compoundTag.put("size", this.newIntegerList(this.size.getX(), this.size.getY(), this.size.getZ()));
		compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
		return compoundTag;
	}

	public void load(CompoundTag compoundTag) {
		this.palettes.clear();
		this.entityInfoList.clear();
		ListTag listTag = compoundTag.getList("size", 3);
		this.size = new BlockPos(listTag.getInt(0), listTag.getInt(1), listTag.getInt(2));
		ListTag listTag2 = compoundTag.getList("blocks", 10);
		if (compoundTag.contains("palettes", 9)) {
			ListTag listTag3 = compoundTag.getList("palettes", 9);

			for (int i = 0; i < listTag3.size(); i++) {
				this.loadPalette(listTag3.getList(i), listTag2);
			}
		} else {
			this.loadPalette(compoundTag.getList("palette", 10), listTag2);
		}

		ListTag listTag3 = compoundTag.getList("entities", 10);

		for (int i = 0; i < listTag3.size(); i++) {
			CompoundTag compoundTag2 = listTag3.getCompound(i);
			ListTag listTag4 = compoundTag2.getList("pos", 6);
			Vec3 vec3 = new Vec3(listTag4.getDouble(0), listTag4.getDouble(1), listTag4.getDouble(2));
			ListTag listTag5 = compoundTag2.getList("blockPos", 3);
			BlockPos blockPos = new BlockPos(listTag5.getInt(0), listTag5.getInt(1), listTag5.getInt(2));
			if (compoundTag2.contains("nbt")) {
				CompoundTag compoundTag3 = compoundTag2.getCompound("nbt");
				this.entityInfoList.add(new StructureTemplate.StructureEntityInfo(vec3, blockPos, compoundTag3));
			}
		}
	}

	private void loadPalette(ListTag listTag, ListTag listTag2) {
		StructureTemplate.SimplePalette simplePalette = new StructureTemplate.SimplePalette();

		for (int i = 0; i < listTag.size(); i++) {
			simplePalette.addMapping(NbtUtils.readBlockState(listTag.getCompound(i)), i);
		}

		List<StructureTemplate.StructureBlockInfo> list = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();
		List<StructureTemplate.StructureBlockInfo> list2 = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();
		List<StructureTemplate.StructureBlockInfo> list3 = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();

		for (int j = 0; j < listTag2.size(); j++) {
			CompoundTag compoundTag = listTag2.getCompound(j);
			ListTag listTag3 = compoundTag.getList("pos", 3);
			BlockPos blockPos = new BlockPos(listTag3.getInt(0), listTag3.getInt(1), listTag3.getInt(2));
			BlockState blockState = simplePalette.stateFor(compoundTag.getInt("state"));
			CompoundTag compoundTag2;
			if (compoundTag.contains("nbt")) {
				compoundTag2 = compoundTag.getCompound("nbt");
			} else {
				compoundTag2 = null;
			}

			StructureTemplate.StructureBlockInfo structureBlockInfo = new StructureTemplate.StructureBlockInfo(blockPos, blockState, compoundTag2);
			addToLists(structureBlockInfo, list, list2, list3);
		}

		List<StructureTemplate.StructureBlockInfo> list4 = buildInfoList(list, list2, list3);
		this.palettes.add(new StructureTemplate.Palette(list4));
	}

	private ListTag newIntegerList(int... is) {
		ListTag listTag = new ListTag();

		for (int i : is) {
			listTag.add(IntTag.valueOf(i));
		}

		return listTag;
	}

	private ListTag newDoubleList(double... ds) {
		ListTag listTag = new ListTag();

		for (double d : ds) {
			listTag.add(DoubleTag.valueOf(d));
		}

		return listTag;
	}

	public static final class Palette {
		private final List<StructureTemplate.StructureBlockInfo> blocks;
		private final Map<Block, List<StructureTemplate.StructureBlockInfo>> cache = Maps.<Block, List<StructureTemplate.StructureBlockInfo>>newHashMap();

		private Palette(List<StructureTemplate.StructureBlockInfo> list) {
			this.blocks = list;
		}

		public List<StructureTemplate.StructureBlockInfo> blocks() {
			return this.blocks;
		}

		public List<StructureTemplate.StructureBlockInfo> blocks(Block block) {
			return (List<StructureTemplate.StructureBlockInfo>)this.cache
				.computeIfAbsent(block, blockx -> (List)this.blocks.stream().filter(structureBlockInfo -> structureBlockInfo.state.is(blockx)).collect(Collectors.toList()));
		}
	}

	static class SimplePalette implements Iterable<BlockState> {
		public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
		private final IdMapper<BlockState> ids = new IdMapper<>(16);
		private int lastId;

		private SimplePalette() {
		}

		public int idFor(BlockState blockState) {
			int i = this.ids.getId(blockState);
			if (i == -1) {
				i = this.lastId++;
				this.ids.addMapping(blockState, i);
			}

			return i;
		}

		@Nullable
		public BlockState stateFor(int i) {
			BlockState blockState = this.ids.byId(i);
			return blockState == null ? DEFAULT_BLOCK_STATE : blockState;
		}

		public Iterator<BlockState> iterator() {
			return this.ids.iterator();
		}

		public void addMapping(BlockState blockState, int i) {
			this.ids.addMapping(blockState, i);
		}
	}

	public static class StructureBlockInfo {
		public final BlockPos pos;
		public final BlockState state;
		public final CompoundTag nbt;

		public StructureBlockInfo(BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag) {
			this.pos = blockPos;
			this.state = blockState;
			this.nbt = compoundTag;
		}

		public String toString() {
			return String.format("<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
		}
	}

	public static class StructureEntityInfo {
		public final Vec3 pos;
		public final BlockPos blockPos;
		public final CompoundTag nbt;

		public StructureEntityInfo(Vec3 vec3, BlockPos blockPos, CompoundTag compoundTag) {
			this.pos = vec3;
			this.blockPos = blockPos;
			this.nbt = compoundTag;
		}
	}
}
