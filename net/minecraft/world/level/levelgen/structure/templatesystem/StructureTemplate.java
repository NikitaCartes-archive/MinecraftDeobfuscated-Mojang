/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jetbrains.annotations.Nullable;

public class StructureTemplate {
    private final List<Palette> palettes = Lists.newArrayList();
    private final List<StructureEntityInfo> entityInfoList = Lists.newArrayList();
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
        if (blockPos2.getX() < 1 || blockPos2.getY() < 1 || blockPos2.getZ() < 1) {
            return;
        }
        BlockPos blockPos3 = blockPos.offset(blockPos2).offset(-1, -1, -1);
        ArrayList<StructureBlockInfo> list = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list2 = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list3 = Lists.newArrayList();
        BlockPos blockPos4 = new BlockPos(Math.min(blockPos.getX(), blockPos3.getX()), Math.min(blockPos.getY(), blockPos3.getY()), Math.min(blockPos.getZ(), blockPos3.getZ()));
        BlockPos blockPos5 = new BlockPos(Math.max(blockPos.getX(), blockPos3.getX()), Math.max(blockPos.getY(), blockPos3.getY()), Math.max(blockPos.getZ(), blockPos3.getZ()));
        this.size = blockPos2;
        for (BlockPos blockPos6 : BlockPos.betweenClosed(blockPos4, blockPos5)) {
            StructureBlockInfo structureBlockInfo;
            BlockPos blockPos7 = blockPos6.subtract(blockPos4);
            BlockState blockState = level.getBlockState(blockPos6);
            if (block != null && block == blockState.getBlock()) continue;
            BlockEntity blockEntity = level.getBlockEntity(blockPos6);
            if (blockEntity != null) {
                CompoundTag compoundTag = blockEntity.save(new CompoundTag());
                compoundTag.remove("x");
                compoundTag.remove("y");
                compoundTag.remove("z");
                structureBlockInfo = new StructureBlockInfo(blockPos7, blockState, compoundTag.copy());
            } else {
                structureBlockInfo = new StructureBlockInfo(blockPos7, blockState, null);
            }
            StructureTemplate.addToLists(structureBlockInfo, list, list2, list3);
        }
        List<StructureBlockInfo> list4 = StructureTemplate.buildInfoList(list, list2, list3);
        this.palettes.clear();
        this.palettes.add(new Palette(list4));
        if (bl) {
            this.fillEntityList(level, blockPos4, blockPos5.offset(1, 1, 1));
        } else {
            this.entityInfoList.clear();
        }
    }

    private static void addToLists(StructureBlockInfo structureBlockInfo, List<StructureBlockInfo> list, List<StructureBlockInfo> list2, List<StructureBlockInfo> list3) {
        if (structureBlockInfo.nbt != null) {
            list2.add(structureBlockInfo);
        } else if (!structureBlockInfo.state.getBlock().hasDynamicShape() && structureBlockInfo.state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
            list.add(structureBlockInfo);
        } else {
            list3.add(structureBlockInfo);
        }
    }

    private static List<StructureBlockInfo> buildInfoList(List<StructureBlockInfo> list, List<StructureBlockInfo> list2, List<StructureBlockInfo> list3) {
        Comparator<StructureBlockInfo> comparator = Comparator.comparingInt(structureBlockInfo -> structureBlockInfo.pos.getY()).thenComparingInt(structureBlockInfo -> structureBlockInfo.pos.getX()).thenComparingInt(structureBlockInfo -> structureBlockInfo.pos.getZ());
        list.sort(comparator);
        list3.sort(comparator);
        list2.sort(comparator);
        ArrayList<StructureBlockInfo> list4 = Lists.newArrayList();
        list4.addAll(list);
        list4.addAll(list3);
        list4.addAll(list2);
        return list4;
    }

    private void fillEntityList(Level level, BlockPos blockPos, BlockPos blockPos2) {
        List<Entity> list = level.getEntitiesOfClass(Entity.class, new AABB(blockPos, blockPos2), entity -> !(entity instanceof Player));
        this.entityInfoList.clear();
        for (Entity entity2 : list) {
            Vec3 vec3 = new Vec3(entity2.getX() - (double)blockPos.getX(), entity2.getY() - (double)blockPos.getY(), entity2.getZ() - (double)blockPos.getZ());
            CompoundTag compoundTag = new CompoundTag();
            entity2.save(compoundTag);
            BlockPos blockPos3 = entity2 instanceof Painting ? ((Painting)entity2).getPos().subtract(blockPos) : new BlockPos(vec3);
            this.entityInfoList.add(new StructureEntityInfo(vec3, blockPos3, compoundTag.copy()));
        }
    }

    public List<StructureBlockInfo> filterBlocks(BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Block block) {
        return this.filterBlocks(blockPos, structurePlaceSettings, block, true);
    }

    public List<StructureBlockInfo> filterBlocks(BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Block block, boolean bl) {
        ArrayList<StructureBlockInfo> list = Lists.newArrayList();
        BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
        if (this.palettes.isEmpty()) {
            return Collections.emptyList();
        }
        for (StructureBlockInfo structureBlockInfo : structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks(block)) {
            BlockPos blockPos2;
            BlockPos blockPos3 = blockPos2 = bl ? StructureTemplate.calculateRelativePosition(structurePlaceSettings, structureBlockInfo.pos).offset(blockPos) : structureBlockInfo.pos;
            if (boundingBox != null && !boundingBox.isInside(blockPos2)) continue;
            list.add(new StructureBlockInfo(blockPos2, structureBlockInfo.state.rotate(structurePlaceSettings.getRotation()), structureBlockInfo.nbt));
        }
        return list;
    }

    public BlockPos calculateConnectedPosition(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings2, BlockPos blockPos2) {
        BlockPos blockPos3 = StructureTemplate.calculateRelativePosition(structurePlaceSettings, blockPos);
        BlockPos blockPos4 = StructureTemplate.calculateRelativePosition(structurePlaceSettings2, blockPos2);
        return blockPos3.subtract(blockPos4);
    }

    public static BlockPos calculateRelativePosition(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
        return StructureTemplate.transform(blockPos, structurePlaceSettings.getMirror(), structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot());
    }

    public void placeInWorldChunk(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Random random) {
        structurePlaceSettings.updateBoundingBoxFromChunkPos();
        this.placeInWorld(serverLevelAccessor, blockPos, structurePlaceSettings, random);
    }

    public void placeInWorld(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Random random) {
        this.placeInWorld(serverLevelAccessor, blockPos, blockPos, structurePlaceSettings, random, 2);
    }

    public boolean placeInWorld(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, Random random, int i) {
        if (this.palettes.isEmpty()) {
            return false;
        }
        List<StructureBlockInfo> list = structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks();
        if (list.isEmpty() && (structurePlaceSettings.isIgnoreEntities() || this.entityInfoList.isEmpty()) || this.size.getX() < 1 || this.size.getY() < 1 || this.size.getZ() < 1) {
            return false;
        }
        BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
        ArrayList<BlockPos> list2 = Lists.newArrayListWithCapacity(structurePlaceSettings.shouldKeepLiquids() ? list.size() : 0);
        ArrayList<Pair<BlockPos, CompoundTag>> list3 = Lists.newArrayListWithCapacity(list.size());
        int j = Integer.MAX_VALUE;
        int k = Integer.MAX_VALUE;
        int l = Integer.MAX_VALUE;
        int m = Integer.MIN_VALUE;
        int n = Integer.MIN_VALUE;
        int o = Integer.MIN_VALUE;
        List<StructureBlockInfo> list4 = StructureTemplate.processBlockInfos(serverLevelAccessor, blockPos, blockPos2, structurePlaceSettings, list);
        for (StructureBlockInfo structureBlockInfo : list4) {
            BlockEntity blockEntity;
            BlockPos blockPos3 = structureBlockInfo.pos;
            if (boundingBox != null && !boundingBox.isInside(blockPos3)) continue;
            FluidState fluidState = structurePlaceSettings.shouldKeepLiquids() ? serverLevelAccessor.getFluidState(blockPos3) : null;
            BlockState blockState = structureBlockInfo.state.mirror(structurePlaceSettings.getMirror()).rotate(structurePlaceSettings.getRotation());
            if (structureBlockInfo.nbt != null) {
                blockEntity = serverLevelAccessor.getBlockEntity(blockPos3);
                Clearable.tryClear(blockEntity);
                serverLevelAccessor.setBlock(blockPos3, Blocks.BARRIER.defaultBlockState(), 20);
            }
            if (!serverLevelAccessor.setBlock(blockPos3, blockState, i)) continue;
            j = Math.min(j, blockPos3.getX());
            k = Math.min(k, blockPos3.getY());
            l = Math.min(l, blockPos3.getZ());
            m = Math.max(m, blockPos3.getX());
            n = Math.max(n, blockPos3.getY());
            o = Math.max(o, blockPos3.getZ());
            list3.add(Pair.of(blockPos3, structureBlockInfo.nbt));
            if (structureBlockInfo.nbt != null && (blockEntity = serverLevelAccessor.getBlockEntity(blockPos3)) != null) {
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
            if (fluidState == null || !(blockState.getBlock() instanceof LiquidBlockContainer)) continue;
            ((LiquidBlockContainer)((Object)blockState.getBlock())).placeLiquid(serverLevelAccessor, blockPos3, blockState, fluidState);
            if (fluidState.isSource()) continue;
            list2.add(blockPos3);
        }
        boolean bl = true;
        Direction[] directions = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        while (bl && !list2.isEmpty()) {
            bl = false;
            Iterator iterator = list2.iterator();
            while (iterator.hasNext()) {
                BlockState blockState2;
                Block block;
                BlockPos blockPos3;
                BlockPos blockPos5 = blockPos3 = (BlockPos)iterator.next();
                FluidState fluidState2 = serverLevelAccessor.getFluidState(blockPos5);
                for (int p = 0; p < directions.length && !fluidState2.isSource(); ++p) {
                    BlockPos blockPos4 = blockPos5.relative(directions[p]);
                    FluidState fluidState3 = serverLevelAccessor.getFluidState(blockPos4);
                    if (!(fluidState3.getHeight(serverLevelAccessor, blockPos4) > fluidState2.getHeight(serverLevelAccessor, blockPos5)) && (!fluidState3.isSource() || fluidState2.isSource())) continue;
                    fluidState2 = fluidState3;
                    blockPos5 = blockPos4;
                }
                if (!fluidState2.isSource() || !((block = (blockState2 = serverLevelAccessor.getBlockState(blockPos3)).getBlock()) instanceof LiquidBlockContainer)) continue;
                ((LiquidBlockContainer)((Object)block)).placeLiquid(serverLevelAccessor, blockPos3, blockState2, fluidState2);
                bl = true;
                iterator.remove();
            }
        }
        if (j <= m) {
            if (!structurePlaceSettings.getKnownShape()) {
                BitSetDiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(m - j + 1, n - k + 1, o - l + 1);
                int n2 = j;
                int r = k;
                int s = l;
                for (Pair pair : list3) {
                    BlockPos blockPos7 = (BlockPos)pair.getFirst();
                    ((DiscreteVoxelShape)discreteVoxelShape).setFull(blockPos7.getX() - n2, blockPos7.getY() - r, blockPos7.getZ() - s, true, true);
                }
                StructureTemplate.updateShapeAtEdge(serverLevelAccessor, i, discreteVoxelShape, n2, r, s);
            }
            for (Pair pair : list3) {
                BlockEntity blockEntity;
                BlockPos blockPos5 = (BlockPos)pair.getFirst();
                if (!structurePlaceSettings.getKnownShape()) {
                    BlockState blockState2;
                    BlockState blockState3 = serverLevelAccessor.getBlockState(blockPos5);
                    if (blockState3 != (blockState2 = Block.updateFromNeighbourShapes(blockState3, serverLevelAccessor, blockPos5))) {
                        serverLevelAccessor.setBlock(blockPos5, blockState2, i & 0xFFFFFFFE | 0x10);
                    }
                    serverLevelAccessor.blockUpdated(blockPos5, blockState2.getBlock());
                }
                if (pair.getSecond() == null || (blockEntity = serverLevelAccessor.getBlockEntity(blockPos5)) == null) continue;
                blockEntity.setChanged();
            }
        }
        if (!structurePlaceSettings.isIgnoreEntities()) {
            this.placeEntities(serverLevelAccessor, blockPos, structurePlaceSettings.getMirror(), structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot(), boundingBox, structurePlaceSettings.shouldFinalizeEntities());
        }
        return true;
    }

    public static void updateShapeAtEdge(LevelAccessor levelAccessor, int i, DiscreteVoxelShape discreteVoxelShape, int j, int k, int l) {
        discreteVoxelShape.forAllFaces((direction, m, n, o) -> {
            BlockState blockState4;
            BlockState blockState2;
            BlockState blockState3;
            BlockPos blockPos = new BlockPos(j + m, k + n, l + o);
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockState blockState = levelAccessor.getBlockState(blockPos);
            if (blockState != (blockState3 = blockState.updateShape(direction, blockState2 = levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2))) {
                levelAccessor.setBlock(blockPos, blockState3, i & 0xFFFFFFFE);
            }
            if (blockState2 != (blockState4 = blockState2.updateShape(direction.getOpposite(), blockState3, levelAccessor, blockPos2, blockPos))) {
                levelAccessor.setBlock(blockPos2, blockState4, i & 0xFFFFFFFE);
            }
        });
    }

    public static List<StructureBlockInfo> processBlockInfos(LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, List<StructureBlockInfo> list) {
        ArrayList<StructureBlockInfo> list2 = Lists.newArrayList();
        for (StructureBlockInfo structureBlockInfo : list) {
            BlockPos blockPos3 = StructureTemplate.calculateRelativePosition(structurePlaceSettings, structureBlockInfo.pos).offset(blockPos);
            StructureBlockInfo structureBlockInfo2 = new StructureBlockInfo(blockPos3, structureBlockInfo.state, structureBlockInfo.nbt != null ? structureBlockInfo.nbt.copy() : null);
            Iterator<StructureProcessor> iterator = structurePlaceSettings.getProcessors().iterator();
            while (structureBlockInfo2 != null && iterator.hasNext()) {
                structureBlockInfo2 = iterator.next().processBlock(levelAccessor, blockPos, blockPos2, structureBlockInfo, structureBlockInfo2, structurePlaceSettings);
            }
            if (structureBlockInfo2 == null) continue;
            list2.add(structureBlockInfo2);
        }
        return list2;
    }

    private void placeEntities(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2, @Nullable BoundingBox boundingBox, boolean bl) {
        for (StructureEntityInfo structureEntityInfo : this.entityInfoList) {
            BlockPos blockPos3 = StructureTemplate.transform(structureEntityInfo.blockPos, mirror, rotation, blockPos2).offset(blockPos);
            if (boundingBox != null && !boundingBox.isInside(blockPos3)) continue;
            CompoundTag compoundTag = structureEntityInfo.nbt.copy();
            Vec3 vec3 = StructureTemplate.transform(structureEntityInfo.pos, mirror, rotation, blockPos2);
            Vec3 vec32 = vec3.add(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            ListTag listTag = new ListTag();
            listTag.add(DoubleTag.valueOf(vec32.x));
            listTag.add(DoubleTag.valueOf(vec32.y));
            listTag.add(DoubleTag.valueOf(vec32.z));
            compoundTag.put("Pos", listTag);
            compoundTag.remove("UUID");
            StructureTemplate.createEntityIgnoreException(serverLevelAccessor, compoundTag).ifPresent(entity -> {
                float f = entity.mirror(mirror);
                entity.moveTo(vec3.x, vec3.y, vec3.z, f += entity.yRot - entity.rotate(rotation), entity.xRot);
                if (bl && entity instanceof Mob) {
                    ((Mob)entity).finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(new BlockPos(vec32)), MobSpawnType.STRUCTURE, null, compoundTag);
                }
                serverLevelAccessor.addFreshEntity((Entity)entity);
            });
        }
    }

    private static Optional<Entity> createEntityIgnoreException(ServerLevelAccessor serverLevelAccessor, CompoundTag compoundTag) {
        try {
            return EntityType.create(compoundTag, serverLevelAccessor.getLevel());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public BlockPos getSize(Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
            }
        }
        return this.size;
    }

    public static BlockPos transform(BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2) {
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT: {
                k = -k;
                break;
            }
            case FRONT_BACK: {
                i = -i;
                break;
            }
            default: {
                bl = false;
            }
        }
        int l = blockPos2.getX();
        int m = blockPos2.getZ();
        switch (rotation) {
            case CLOCKWISE_180: {
                return new BlockPos(l + l - i, j, m + m - k);
            }
            case COUNTERCLOCKWISE_90: {
                return new BlockPos(l - m + k, j, l + m - i);
            }
            case CLOCKWISE_90: {
                return new BlockPos(l + m - k, j, m - l + i);
            }
        }
        return bl ? new BlockPos(i, j, k) : blockPos;
    }

    public static Vec3 transform(Vec3 vec3, Mirror mirror, Rotation rotation, BlockPos blockPos) {
        double d = vec3.x;
        double e = vec3.y;
        double f = vec3.z;
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT: {
                f = 1.0 - f;
                break;
            }
            case FRONT_BACK: {
                d = 1.0 - d;
                break;
            }
            default: {
                bl = false;
            }
        }
        int i = blockPos.getX();
        int j = blockPos.getZ();
        switch (rotation) {
            case CLOCKWISE_180: {
                return new Vec3((double)(i + i + 1) - d, e, (double)(j + j + 1) - f);
            }
            case COUNTERCLOCKWISE_90: {
                return new Vec3((double)(i - j) + f, e, (double)(i + j + 1) - d);
            }
            case CLOCKWISE_90: {
                return new Vec3((double)(i + j + 1) - f, e, (double)(j - i) + d);
            }
        }
        return bl ? new Vec3(d, e, f) : vec3;
    }

    public BlockPos getZeroPositionWithTransform(BlockPos blockPos, Mirror mirror, Rotation rotation) {
        return StructureTemplate.getZeroPositionWithTransform(blockPos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
    }

    public static BlockPos getZeroPositionWithTransform(BlockPos blockPos, Mirror mirror, Rotation rotation, int i, int j) {
        int k = mirror == Mirror.FRONT_BACK ? --i : 0;
        int l = mirror == Mirror.LEFT_RIGHT ? --j : 0;
        BlockPos blockPos2 = blockPos;
        switch (rotation) {
            case NONE: {
                blockPos2 = blockPos.offset(k, 0, l);
                break;
            }
            case CLOCKWISE_90: {
                blockPos2 = blockPos.offset(j - l, 0, k);
                break;
            }
            case CLOCKWISE_180: {
                blockPos2 = blockPos.offset(i - k, 0, j - l);
                break;
            }
            case COUNTERCLOCKWISE_90: {
                blockPos2 = blockPos.offset(l, 0, i - k);
            }
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
            case NONE: {
                boundingBox = new BoundingBox(0, 0, 0, k, l, m);
                break;
            }
            case CLOCKWISE_180: {
                boundingBox = new BoundingBox(i + i - k, 0, j + j - m, i + i, l, j + j);
                break;
            }
            case COUNTERCLOCKWISE_90: {
                boundingBox = new BoundingBox(i - j, 0, i + j - m, i - j + k, l, i + j);
                break;
            }
            case CLOCKWISE_90: {
                boundingBox = new BoundingBox(i + j - k, 0, j - i, i + j, l, j - i + m);
            }
        }
        switch (mirror) {
            case NONE: {
                break;
            }
            case FRONT_BACK: {
                this.mirrorAABB(rotation, k, m, boundingBox, Direction.WEST, Direction.EAST);
                break;
            }
            case LEFT_RIGHT: {
                this.mirrorAABB(rotation, m, k, boundingBox, Direction.NORTH, Direction.SOUTH);
            }
        }
        boundingBox.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        return boundingBox;
    }

    private void mirrorAABB(Rotation rotation, int i, int j, BoundingBox boundingBox, Direction direction, Direction direction2) {
        BlockPos blockPos = BlockPos.ZERO;
        blockPos = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? blockPos.relative(rotation.rotate(direction), j) : (rotation == Rotation.CLOCKWISE_180 ? blockPos.relative(direction2, i) : blockPos.relative(direction, i));
        boundingBox.move(blockPos.getX(), 0, blockPos.getZ());
    }

    public CompoundTag save(CompoundTag compoundTag) {
        if (this.palettes.isEmpty()) {
            compoundTag.put("blocks", new ListTag());
            compoundTag.put("palette", new ListTag());
        } else {
            ListTag listTag2;
            ArrayList<SimplePalette> list = Lists.newArrayList();
            SimplePalette simplePalette = new SimplePalette();
            list.add(simplePalette);
            for (int i = 1; i < this.palettes.size(); ++i) {
                list.add(new SimplePalette());
            }
            ListTag listTag = new ListTag();
            List<StructureBlockInfo> list2 = this.palettes.get(0).blocks();
            for (int j = 0; j < list2.size(); ++j) {
                StructureBlockInfo structureBlockInfo = list2.get(j);
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.put("pos", this.newIntegerList(structureBlockInfo.pos.getX(), structureBlockInfo.pos.getY(), structureBlockInfo.pos.getZ()));
                int k = simplePalette.idFor(structureBlockInfo.state);
                compoundTag2.putInt("state", k);
                if (structureBlockInfo.nbt != null) {
                    compoundTag2.put("nbt", structureBlockInfo.nbt);
                }
                listTag.add(compoundTag2);
                for (int l = 1; l < this.palettes.size(); ++l) {
                    SimplePalette simplePalette2 = (SimplePalette)list.get(l);
                    simplePalette2.addMapping(this.palettes.get((int)l).blocks().get((int)j).state, k);
                }
            }
            compoundTag.put("blocks", listTag);
            if (list.size() == 1) {
                listTag2 = new ListTag();
                for (BlockState blockState : simplePalette) {
                    listTag2.add(NbtUtils.writeBlockState(blockState));
                }
                compoundTag.put("palette", listTag2);
            } else {
                listTag2 = new ListTag();
                for (SimplePalette simplePalette3 : list) {
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
        for (StructureEntityInfo structureEntityInfo : this.entityInfoList) {
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.put("pos", this.newDoubleList(structureEntityInfo.pos.x, structureEntityInfo.pos.y, structureEntityInfo.pos.z));
            compoundTag3.put("blockPos", this.newIntegerList(structureEntityInfo.blockPos.getX(), structureEntityInfo.blockPos.getY(), structureEntityInfo.blockPos.getZ()));
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
        int i;
        ListTag listTag3;
        this.palettes.clear();
        this.entityInfoList.clear();
        ListTag listTag = compoundTag.getList("size", 3);
        this.size = new BlockPos(listTag.getInt(0), listTag.getInt(1), listTag.getInt(2));
        ListTag listTag2 = compoundTag.getList("blocks", 10);
        if (compoundTag.contains("palettes", 9)) {
            listTag3 = compoundTag.getList("palettes", 9);
            for (i = 0; i < listTag3.size(); ++i) {
                this.loadPalette(listTag3.getList(i), listTag2);
            }
        } else {
            this.loadPalette(compoundTag.getList("palette", 10), listTag2);
        }
        listTag3 = compoundTag.getList("entities", 10);
        for (i = 0; i < listTag3.size(); ++i) {
            CompoundTag compoundTag2 = listTag3.getCompound(i);
            ListTag listTag4 = compoundTag2.getList("pos", 6);
            Vec3 vec3 = new Vec3(listTag4.getDouble(0), listTag4.getDouble(1), listTag4.getDouble(2));
            ListTag listTag5 = compoundTag2.getList("blockPos", 3);
            BlockPos blockPos = new BlockPos(listTag5.getInt(0), listTag5.getInt(1), listTag5.getInt(2));
            if (!compoundTag2.contains("nbt")) continue;
            CompoundTag compoundTag3 = compoundTag2.getCompound("nbt");
            this.entityInfoList.add(new StructureEntityInfo(vec3, blockPos, compoundTag3));
        }
    }

    private void loadPalette(ListTag listTag, ListTag listTag2) {
        SimplePalette simplePalette = new SimplePalette();
        for (int i = 0; i < listTag.size(); ++i) {
            simplePalette.addMapping(NbtUtils.readBlockState(listTag.getCompound(i)), i);
        }
        ArrayList<StructureBlockInfo> list = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list2 = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list3 = Lists.newArrayList();
        for (int j = 0; j < listTag2.size(); ++j) {
            CompoundTag compoundTag = listTag2.getCompound(j);
            ListTag listTag3 = compoundTag.getList("pos", 3);
            BlockPos blockPos = new BlockPos(listTag3.getInt(0), listTag3.getInt(1), listTag3.getInt(2));
            BlockState blockState = simplePalette.stateFor(compoundTag.getInt("state"));
            CompoundTag compoundTag2 = compoundTag.contains("nbt") ? compoundTag.getCompound("nbt") : null;
            StructureBlockInfo structureBlockInfo = new StructureBlockInfo(blockPos, blockState, compoundTag2);
            StructureTemplate.addToLists(structureBlockInfo, list, list2, list3);
        }
        List<StructureBlockInfo> list4 = StructureTemplate.buildInfoList(list, list2, list3);
        this.palettes.add(new Palette(list4));
    }

    private ListTag newIntegerList(int ... is) {
        ListTag listTag = new ListTag();
        for (int i : is) {
            listTag.add(IntTag.valueOf(i));
        }
        return listTag;
    }

    private ListTag newDoubleList(double ... ds) {
        ListTag listTag = new ListTag();
        for (double d : ds) {
            listTag.add(DoubleTag.valueOf(d));
        }
        return listTag;
    }

    public static final class Palette {
        private final List<StructureBlockInfo> blocks;
        private final Map<Block, List<StructureBlockInfo>> cache = Maps.newHashMap();

        private Palette(List<StructureBlockInfo> list) {
            this.blocks = list;
        }

        public List<StructureBlockInfo> blocks() {
            return this.blocks;
        }

        public List<StructureBlockInfo> blocks(Block block2) {
            return this.cache.computeIfAbsent(block2, block -> this.blocks.stream().filter(structureBlockInfo -> structureBlockInfo.state.is((Block)block)).collect(Collectors.toList()));
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

    static class SimplePalette
    implements Iterable<BlockState> {
        public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
        private final IdMapper<BlockState> ids = new IdMapper(16);
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

        @Override
        public Iterator<BlockState> iterator() {
            return this.ids.iterator();
        }

        public void addMapping(BlockState blockState, int i) {
            this.ids.addMapping(blockState, i);
        }
    }
}

