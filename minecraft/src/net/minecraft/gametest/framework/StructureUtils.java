package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureUtils {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String DEFAULT_TEST_STRUCTURES_DIR = "gameteststructures";
	public static String testStructuresDir = "gameteststructures";
	private static final int HOW_MANY_CHUNKS_TO_LOAD_IN_EACH_DIRECTION_OF_STRUCTURE = 4;

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
		BlockPos blockPos = structureBlockEntity.getBlockPos();
		BlockPos blockPos2 = blockPos.offset(structureBlockEntity.getStructureSize().offset(-1, -1, -1));
		BlockPos blockPos3 = StructureTemplate.transform(blockPos2, Mirror.NONE, structureBlockEntity.getRotation(), blockPos);
		return new AABB(blockPos, blockPos3);
	}

	public static BoundingBox getStructureBoundingBox(StructureBlockEntity structureBlockEntity) {
		BlockPos blockPos = structureBlockEntity.getBlockPos();
		BlockPos blockPos2 = blockPos.offset(structureBlockEntity.getStructureSize().offset(-1, -1, -1));
		BlockPos blockPos3 = StructureTemplate.transform(blockPos2, Mirror.NONE, structureBlockEntity.getRotation(), blockPos);
		return BoundingBox.fromCorners(blockPos, blockPos3);
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
		BoundingBox boundingBox = getStructureBoundingBox(blockPos, vec3i, rotation);
		clearSpaceForStructure(boundingBox, blockPos.getY(), serverLevel);
		serverLevel.setBlockAndUpdate(blockPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
		StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
		structureBlockEntity.setIgnoreEntities(false);
		structureBlockEntity.setStructureName(new ResourceLocation(string));
		structureBlockEntity.setStructureSize(vec3i);
		structureBlockEntity.setMode(StructureMode.SAVE);
		structureBlockEntity.setShowBoundingBox(true);
	}

	public static StructureBlockEntity spawnStructure(String string, BlockPos blockPos, Rotation rotation, int i, ServerLevel serverLevel, boolean bl) {
		Vec3i vec3i = getStructureTemplate(string, serverLevel).getSize();
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

		forceLoadChunks(blockPos, serverLevel);
		clearSpaceForStructure(boundingBox, blockPos.getY(), serverLevel);
		StructureBlockEntity structureBlockEntity = createStructureBlock(string, blockPos2, rotation, serverLevel, bl);
		serverLevel.getBlockTicks().clearArea(boundingBox);
		serverLevel.clearBlockEvents(boundingBox);
		return structureBlockEntity;
	}

	private static void forceLoadChunks(BlockPos blockPos, ServerLevel serverLevel) {
		ChunkPos chunkPos = new ChunkPos(blockPos);

		for (int i = -1; i < 4; i++) {
			for (int j = -1; j < 4; j++) {
				int k = chunkPos.x + i;
				int l = chunkPos.z + j;
				serverLevel.setChunkForced(k, l, true);
			}
		}
	}

	public static void clearSpaceForStructure(BoundingBox boundingBox, int i, ServerLevel serverLevel) {
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

	public static BoundingBox getStructureBoundingBox(BlockPos blockPos, Vec3i vec3i, Rotation rotation) {
		BlockPos blockPos2 = blockPos.offset(vec3i).offset(-1, -1, -1);
		BlockPos blockPos3 = StructureTemplate.transform(blockPos2, Mirror.NONE, rotation, blockPos);
		BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos3);
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
		AABB aABB = new AABB(blockPos);
		aABB = aABB.inflate((double)i);

		for (int j = (int)aABB.minX; j <= (int)aABB.maxX; j++) {
			for (int k = (int)aABB.minY; k <= (int)aABB.maxY; k++) {
				for (int l = (int)aABB.minZ; l <= (int)aABB.maxZ; l++) {
					BlockPos blockPos2 = new BlockPos(j, k, l);
					BlockState blockState = serverLevel.getBlockState(blockPos2);
					if (blockState.is(Blocks.STRUCTURE_BLOCK)) {
						collection.add(blockPos2);
					}
				}
			}
		}

		return collection;
	}

	private static StructureTemplate getStructureTemplate(String string, ServerLevel serverLevel) {
		StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
		Optional<StructureTemplate> optional = structureTemplateManager.get(new ResourceLocation(string));
		if (optional.isPresent()) {
			return (StructureTemplate)optional.get();
		} else {
			String string2 = string + ".snbt";
			Path path = Paths.get(testStructuresDir, string2);
			CompoundTag compoundTag = tryLoadStructure(path);
			if (compoundTag == null) {
				throw new RuntimeException("Could not find structure file " + path + ", and the structure is not available in the world structures either.");
			} else {
				return structureTemplateManager.readStructure(compoundTag);
			}
		}
	}

	private static StructureBlockEntity createStructureBlock(String string, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel, boolean bl) {
		serverLevel.setBlockAndUpdate(blockPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
		StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
		structureBlockEntity.setMode(StructureMode.LOAD);
		structureBlockEntity.setRotation(rotation);
		structureBlockEntity.setIgnoreEntities(false);
		structureBlockEntity.setStructureName(new ResourceLocation(string));
		structureBlockEntity.loadStructure(serverLevel, bl);
		if (structureBlockEntity.getStructureSize() != Vec3i.ZERO) {
			return structureBlockEntity;
		} else {
			StructureTemplate structureTemplate = getStructureTemplate(string, serverLevel);
			structureBlockEntity.loadStructure(serverLevel, bl, structureTemplate);
			if (structureBlockEntity.getStructureSize() == Vec3i.ZERO) {
				throw new RuntimeException("Failed to load structure " + string);
			} else {
				return structureBlockEntity;
			}
		}
	}

	@Nullable
	private static CompoundTag tryLoadStructure(Path path) {
		try {
			BufferedReader bufferedReader = Files.newBufferedReader(path);
			String string = IOUtils.toString(bufferedReader);
			return NbtUtils.snbtToStructure(string);
		} catch (IOException var3) {
			return null;
		} catch (CommandSyntaxException var4) {
			throw new RuntimeException("Error while trying to load structure " + path, var4);
		}
	}

	private static void clearBlock(int i, BlockPos blockPos, ServerLevel serverLevel) {
		BlockState blockState = null;
		RegistryAccess registryAccess = serverLevel.registryAccess();
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = FlatLevelGeneratorSettings.getDefault(
			registryAccess.lookupOrThrow(Registries.BIOME),
			registryAccess.lookupOrThrow(Registries.STRUCTURE_SET),
			registryAccess.lookupOrThrow(Registries.PLACED_FEATURE)
		);
		List<BlockState> list = flatLevelGeneratorSettings.getLayers();
		int j = blockPos.getY() - serverLevel.getMinBuildHeight();
		if (blockPos.getY() < i && j > 0 && j <= list.size()) {
			blockState = (BlockState)list.get(j - 1);
		}

		if (blockState == null) {
			blockState = Blocks.AIR.defaultBlockState();
		}

		BlockInput blockInput = new BlockInput(blockState, Collections.emptySet(), null);
		blockInput.place(serverLevel, blockPos, 2);
		serverLevel.blockUpdated(blockPos, blockState.getBlock());
	}

	private static boolean doesStructureContain(BlockPos blockPos, BlockPos blockPos2, ServerLevel serverLevel) {
		StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
		AABB aABB = getStructureBounds(structureBlockEntity).inflate(1.0);
		return aABB.contains(Vec3.atCenterOf(blockPos2));
	}
}
