package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class StructureUtils {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String DEFAULT_TEST_STRUCTURES_DIR = "gameteststructures";
	public static String testStructuresDir = "gameteststructures";

	public static Rotation getRotationForRotationSteps(int i) {
		switch (i) {
			case 0:
				return Rotation.NONE;
			case 1:
				return Rotation.CLOCKWISE_90;
			case 2:
				return Rotation.CLOCKWISE_180;
			case 3:
				return Rotation.COUNTERCLOCKWISE_90;
			default:
				throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + i);
		}
	}

	public static int getRotationStepsForRotation(Rotation rotation) {
		switch (rotation) {
			case NONE:
				return 0;
			case CLOCKWISE_90:
				return 1;
			case CLOCKWISE_180:
				return 2;
			case COUNTERCLOCKWISE_90:
				return 3;
			default:
				throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + rotation);
		}
	}

	public static AABB getStructureBounds(StructureBlockEntity structureBlockEntity) {
		return AABB.of(getStructureBoundingBox(structureBlockEntity));
	}

	public static BoundingBox getStructureBoundingBox(StructureBlockEntity structureBlockEntity) {
		BlockPos blockPos = getStructureOrigin(structureBlockEntity);
		BlockPos blockPos2 = getTransformedFarCorner(blockPos, structureBlockEntity.getStructureSize(), structureBlockEntity.getRotation());
		return BoundingBox.fromCorners(blockPos, blockPos2);
	}

	public static BlockPos getStructureOrigin(StructureBlockEntity structureBlockEntity) {
		return structureBlockEntity.getBlockPos().offset(structureBlockEntity.getStructurePos());
	}

	public static void addCommandBlockAndButtonToStartTest(BlockPos blockPos, BlockPos blockPos2, Rotation rotation, ServerLevel serverLevel) {
		BlockPos blockPos3 = StructureTemplate.transform(blockPos.offset(blockPos2), Mirror.NONE, rotation, blockPos);
		serverLevel.setBlockAndUpdate(blockPos3, Blocks.COMMAND_BLOCK.defaultBlockState());
		CommandBlockEntity commandBlockEntity = (CommandBlockEntity)serverLevel.getBlockEntity(blockPos3);
		commandBlockEntity.getCommandBlock().setCommand("test runthis");
		BlockPos blockPos4 = StructureTemplate.transform(blockPos3.offset(0, 0, -1), Mirror.NONE, rotation, blockPos3);
		serverLevel.setBlockAndUpdate(blockPos4, Blocks.STONE_BUTTON.defaultBlockState().rotate(rotation));
	}

	public static void createNewEmptyStructureBlock(String string, BlockPos blockPos, Vec3i vec3i, Rotation rotation, ServerLevel serverLevel) {
		BoundingBox boundingBox = getStructureBoundingBox(blockPos.above(), vec3i, rotation);
		clearSpaceForStructure(boundingBox, serverLevel);
		serverLevel.setBlockAndUpdate(blockPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
		StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
		structureBlockEntity.setIgnoreEntities(false);
		structureBlockEntity.setStructureName(new ResourceLocation(string));
		structureBlockEntity.setStructureSize(vec3i);
		structureBlockEntity.setMode(StructureMode.SAVE);
		structureBlockEntity.setShowBoundingBox(true);
	}

	public static StructureBlockEntity prepareTestStructure(String string, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel) {
		Vec3i vec3i = ((StructureTemplate)serverLevel.getStructureManager()
				.get(new ResourceLocation(string))
				.orElseThrow(() -> new IllegalStateException("Missing test structure: " + string)))
			.getSize();
		BoundingBox boundingBox = getStructureBoundingBox(blockPos, vec3i, rotation);
		BlockPos blockPos2;
		if (rotation == Rotation.NONE) {
			blockPos2 = blockPos;
		} else if (rotation == Rotation.CLOCKWISE_90) {
			blockPos2 = blockPos.offset(vec3i.getZ() - 1, 0, 0);
		} else if (rotation == Rotation.CLOCKWISE_180) {
			blockPos2 = blockPos.offset(vec3i.getX() - 1, 0, vec3i.getZ() - 1);
		} else {
			if (rotation != Rotation.COUNTERCLOCKWISE_90) {
				throw new IllegalArgumentException("Invalid rotation: " + rotation);
			}

			blockPos2 = blockPos.offset(0, 0, vec3i.getX() - 1);
		}

		forceLoadChunks(boundingBox, serverLevel);
		clearSpaceForStructure(boundingBox, serverLevel);
		return createStructureBlock(string, blockPos2.below(), rotation, serverLevel);
	}

	private static void forceLoadChunks(BoundingBox boundingBox, ServerLevel serverLevel) {
		boundingBox.intersectingChunks().forEach(chunkPos -> serverLevel.setChunkForced(chunkPos.x, chunkPos.z, true));
	}

	public static void clearSpaceForStructure(BoundingBox boundingBox, ServerLevel serverLevel) {
		int i = boundingBox.minY() - 1;
		BoundingBox boundingBox2 = new BoundingBox(
			boundingBox.minX() - 2, boundingBox.minY() - 3, boundingBox.minZ() - 3, boundingBox.maxX() + 3, boundingBox.maxY() + 20, boundingBox.maxZ() + 3
		);
		BlockPos.betweenClosedStream(boundingBox2).forEach(blockPos -> clearBlock(i, blockPos, serverLevel));
		serverLevel.getBlockTicks().clearArea(boundingBox2);
		serverLevel.clearBlockEvents(boundingBox2);
		AABB aABB = new AABB(
			(double)boundingBox2.minX(),
			(double)boundingBox2.minY(),
			(double)boundingBox2.minZ(),
			(double)boundingBox2.maxX(),
			(double)boundingBox2.maxY(),
			(double)boundingBox2.maxZ()
		);
		List<Entity> list = serverLevel.getEntitiesOfClass(Entity.class, aABB, entity -> !(entity instanceof Player));
		list.forEach(Entity::discard);
	}

	public static BlockPos getTransformedFarCorner(BlockPos blockPos, Vec3i vec3i, Rotation rotation) {
		BlockPos blockPos2 = blockPos.offset(vec3i).offset(-1, -1, -1);
		return StructureTemplate.transform(blockPos2, Mirror.NONE, rotation, blockPos);
	}

	public static BoundingBox getStructureBoundingBox(BlockPos blockPos, Vec3i vec3i, Rotation rotation) {
		BlockPos blockPos2 = getTransformedFarCorner(blockPos, vec3i, rotation);
		BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos2);
		int i = Math.min(boundingBox.minX(), boundingBox.maxX());
		int j = Math.min(boundingBox.minZ(), boundingBox.maxZ());
		return boundingBox.move(blockPos.getX() - i, 0, blockPos.getZ() - j);
	}

	public static Optional<BlockPos> findStructureBlockContainingPos(BlockPos blockPos, int i, ServerLevel serverLevel) {
		return findStructureBlocks(blockPos, i, serverLevel).stream().filter(blockPos2 -> doesStructureContain(blockPos2, blockPos, serverLevel)).findFirst();
	}

	@Nullable
	public static BlockPos findNearestStructureBlock(BlockPos blockPos, int i, ServerLevel serverLevel) {
		Comparator<BlockPos> comparator = Comparator.comparingInt(blockPos2 -> blockPos2.distManhattan(blockPos));
		Collection<BlockPos> collection = findStructureBlocks(blockPos, i, serverLevel);
		Optional<BlockPos> optional = collection.stream().min(comparator);
		return (BlockPos)optional.orElse(null);
	}

	public static Collection<BlockPos> findStructureBlocks(BlockPos blockPos, int i, ServerLevel serverLevel) {
		Collection<BlockPos> collection = Lists.<BlockPos>newArrayList();
		BoundingBox boundingBox = new BoundingBox(blockPos).inflatedBy(i);
		BlockPos.betweenClosedStream(boundingBox).forEach(blockPosx -> {
			if (serverLevel.getBlockState(blockPosx).is(Blocks.STRUCTURE_BLOCK)) {
				collection.add(blockPosx.immutable());
			}
		});
		return collection;
	}

	private static StructureBlockEntity createStructureBlock(String string, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel) {
		serverLevel.setBlockAndUpdate(blockPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
		StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
		structureBlockEntity.setMode(StructureMode.LOAD);
		structureBlockEntity.setRotation(rotation);
		structureBlockEntity.setIgnoreEntities(false);
		structureBlockEntity.setStructureName(new ResourceLocation(string));
		if (!structureBlockEntity.loadStructureInfo(serverLevel)) {
			throw new RuntimeException("Failed to load structure info " + string);
		} else {
			return structureBlockEntity;
		}
	}

	private static void clearBlock(int i, BlockPos blockPos, ServerLevel serverLevel) {
		BlockState blockState;
		if (blockPos.getY() < i) {
			blockState = Blocks.STONE.defaultBlockState();
		} else {
			blockState = Blocks.AIR.defaultBlockState();
		}

		BlockInput blockInput = new BlockInput(blockState, Collections.emptySet(), null);
		blockInput.place(serverLevel, blockPos, 2);
		serverLevel.blockUpdated(blockPos, blockState.getBlock());
	}

	private static boolean doesStructureContain(BlockPos blockPos, BlockPos blockPos2, ServerLevel serverLevel) {
		StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
		return getStructureBoundingBox(structureBlockEntity).isInside(blockPos2);
	}
}
