/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

public class StructureUtils {
    public static String testStructuresDir = "gameteststructures";

    public static AABB getStructureBounds(StructureBlockEntity structureBlockEntity) {
        BlockPos blockPos = structureBlockEntity.getBlockPos().offset(structureBlockEntity.getStructurePos());
        return new AABB(blockPos, blockPos.offset(structureBlockEntity.getStructureSize()));
    }

    public static void addCommandBlockAndButtonToStartTest(BlockPos blockPos, ServerLevel serverLevel) {
        serverLevel.setBlockAndUpdate(blockPos, Blocks.COMMAND_BLOCK.defaultBlockState());
        CommandBlockEntity commandBlockEntity = (CommandBlockEntity)serverLevel.getBlockEntity(blockPos);
        commandBlockEntity.getCommandBlock().setCommand("test runthis");
        serverLevel.setBlockAndUpdate(blockPos.offset(0, 0, -1), Blocks.STONE_BUTTON.defaultBlockState());
    }

    public static void createNewEmptyStructureBlock(String string, BlockPos blockPos, BlockPos blockPos2, int i, ServerLevel serverLevel) {
        BoundingBox boundingBox = StructureUtils.createStructureBoundingBox(blockPos, blockPos2, i);
        StructureUtils.clearSpaceForStructure(boundingBox, blockPos.getY(), serverLevel);
        serverLevel.setBlockAndUpdate(blockPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
        structureBlockEntity.setIgnoreEntities(false);
        structureBlockEntity.setStructureName(new ResourceLocation(string));
        structureBlockEntity.setStructureSize(blockPos2);
        structureBlockEntity.setMode(StructureMode.SAVE);
        structureBlockEntity.setShowBoundingBox(true);
    }

    public static StructureBlockEntity spawnStructure(String string, BlockPos blockPos, int i, ServerLevel serverLevel, boolean bl) {
        BoundingBox boundingBox = StructureUtils.createStructureBoundingBox(blockPos, StructureUtils.getStructureTemplate(string, serverLevel).getSize(), i);
        StructureUtils.forceLoadChunks(blockPos, serverLevel);
        StructureUtils.clearSpaceForStructure(boundingBox, blockPos.getY(), serverLevel);
        StructureBlockEntity structureBlockEntity = StructureUtils.createStructureBlock(string, blockPos, serverLevel, bl);
        ((ServerTickList)serverLevel.getBlockTicks()).fetchTicksInArea(boundingBox, true, false);
        serverLevel.clearBlockEvents(boundingBox);
        return structureBlockEntity;
    }

    private static void forceLoadChunks(BlockPos blockPos, ServerLevel serverLevel) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        for (int i = -1; i < 4; ++i) {
            for (int j = -1; j < 4; ++j) {
                int k = chunkPos.x + i;
                int l = chunkPos.z + j;
                serverLevel.setChunkForced(k, l, true);
            }
        }
    }

    public static void clearSpaceForStructure(BoundingBox boundingBox, int i, ServerLevel serverLevel) {
        BlockPos.betweenClosedStream(boundingBox).forEach(blockPos -> StructureUtils.clearBlock(i, blockPos, serverLevel));
        ((ServerTickList)serverLevel.getBlockTicks()).fetchTicksInArea(boundingBox, true, false);
        serverLevel.clearBlockEvents(boundingBox);
        AABB aABB = new AABB(boundingBox.x0, boundingBox.y0, boundingBox.z0, boundingBox.x1, boundingBox.y1, boundingBox.z1);
        List<Entity> list = serverLevel.getEntitiesOfClass(Entity.class, aABB, entity -> !(entity instanceof Player));
        list.forEach(Entity::remove);
    }

    public static BoundingBox createStructureBoundingBox(BlockPos blockPos, BlockPos blockPos2, int i) {
        BlockPos blockPos3 = blockPos.offset(-i, -3, -i);
        BlockPos blockPos4 = blockPos.offset(blockPos2).offset(i - 1, 30, i - 1);
        return BoundingBox.createProper(blockPos3.getX(), blockPos3.getY(), blockPos3.getZ(), blockPos4.getX(), blockPos4.getY(), blockPos4.getZ());
    }

    public static Optional<BlockPos> findStructureBlockContainingPos(BlockPos blockPos, int i, ServerLevel serverLevel) {
        return StructureUtils.findStructureBlocks(blockPos, i, serverLevel).stream().filter(blockPos2 -> StructureUtils.doesStructureContain(blockPos2, blockPos, serverLevel)).findFirst();
    }

    @Nullable
    public static BlockPos findNearestStructureBlock(BlockPos blockPos, int i, ServerLevel serverLevel) {
        Comparator<BlockPos> comparator = Comparator.comparingInt(blockPos2 -> blockPos2.distManhattan(blockPos));
        Collection<BlockPos> collection = StructureUtils.findStructureBlocks(blockPos, i, serverLevel);
        Optional<BlockPos> optional = collection.stream().min(comparator);
        return optional.orElse(null);
    }

    public static Collection<BlockPos> findStructureBlocks(BlockPos blockPos, int i, ServerLevel serverLevel) {
        ArrayList<BlockPos> collection = Lists.newArrayList();
        AABB aABB = new AABB(blockPos);
        aABB = aABB.inflate(i);
        for (int j = (int)aABB.minX; j <= (int)aABB.maxX; ++j) {
            for (int k = (int)aABB.minY; k <= (int)aABB.maxY; ++k) {
                for (int l = (int)aABB.minZ; l <= (int)aABB.maxZ; ++l) {
                    BlockPos blockPos2 = new BlockPos(j, k, l);
                    BlockState blockState = serverLevel.getBlockState(blockPos2);
                    if (!blockState.is(Blocks.STRUCTURE_BLOCK)) continue;
                    collection.add(blockPos2);
                }
            }
        }
        return collection;
    }

    private static StructureTemplate getStructureTemplate(String string, ServerLevel serverLevel) {
        StructureManager structureManager = serverLevel.getStructureManager();
        StructureTemplate structureTemplate = structureManager.get(new ResourceLocation(string));
        if (structureTemplate != null) {
            return structureTemplate;
        }
        String string2 = string + ".snbt";
        Path path = Paths.get(testStructuresDir, string2);
        CompoundTag compoundTag = StructureUtils.tryLoadStructure(path);
        if (compoundTag == null) {
            throw new RuntimeException("Could not find structure file " + path + ", and the structure is not available in the world structures either.");
        }
        return structureManager.readStructure(compoundTag);
    }

    private static StructureBlockEntity createStructureBlock(String string, BlockPos blockPos, ServerLevel serverLevel, boolean bl) {
        serverLevel.setBlockAndUpdate(blockPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
        structureBlockEntity.setMode(StructureMode.LOAD);
        structureBlockEntity.setIgnoreEntities(false);
        structureBlockEntity.setStructureName(new ResourceLocation(string));
        structureBlockEntity.loadStructure(bl);
        if (structureBlockEntity.getStructureSize() != BlockPos.ZERO) {
            return structureBlockEntity;
        }
        StructureTemplate structureTemplate = StructureUtils.getStructureTemplate(string, serverLevel);
        structureBlockEntity.loadStructure(bl, structureTemplate);
        if (structureBlockEntity.getStructureSize() == BlockPos.ZERO) {
            throw new RuntimeException("Failed to load structure " + string);
        }
        return structureBlockEntity;
    }

    @Nullable
    private static CompoundTag tryLoadStructure(Path path) {
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            String string = IOUtils.toString(bufferedReader);
            return TagParser.parseTag(string);
        } catch (IOException iOException) {
            return null;
        } catch (CommandSyntaxException commandSyntaxException) {
            throw new RuntimeException("Error while trying to load structure " + path, commandSyntaxException);
        }
    }

    private static void clearBlock(int i, BlockPos blockPos, ServerLevel serverLevel) {
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = FlatLevelGeneratorSettings.getDefault();
        BlockState[] blockStates = flatLevelGeneratorSettings.getLayers();
        BlockState blockState = blockPos.getY() < i ? blockStates[blockPos.getY() - 1] : Blocks.AIR.defaultBlockState();
        BlockInput blockInput = new BlockInput(blockState, Collections.emptySet(), null);
        blockInput.place(serverLevel, blockPos, 2);
        serverLevel.blockUpdated(blockPos, blockState.getBlock());
    }

    private static boolean doesStructureContain(BlockPos blockPos, BlockPos blockPos2, ServerLevel serverLevel) {
        StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
        AABB aABB = StructureUtils.getStructureBounds(structureBlockEntity);
        return aABB.contains(Vec3.atLowerCornerOf(blockPos2));
    }
}

