/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
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
    public static final String PALETTE_TAG = "palette";
    public static final String PALETTE_LIST_TAG = "palettes";
    public static final String ENTITIES_TAG = "entities";
    public static final String BLOCKS_TAG = "blocks";
    public static final String BLOCK_TAG_POS = "pos";
    public static final String BLOCK_TAG_STATE = "state";
    public static final String BLOCK_TAG_NBT = "nbt";
    public static final String ENTITY_TAG_POS = "pos";
    public static final String ENTITY_TAG_BLOCKPOS = "blockPos";
    public static final String ENTITY_TAG_NBT = "nbt";
    public static final String SIZE_TAG = "size";
    private final List<Palette> palettes = Lists.newArrayList();
    private final List<StructureEntityInfo> entityInfoList = Lists.newArrayList();
    private Vec3i size = Vec3i.ZERO;
    private String author = "?";

    public Vec3i getSize() {
        return this.size;
    }

    public void setAuthor(String string) {
        this.author = string;
    }

    public String getAuthor() {
        return this.author;
    }

    public void fillFromWorld(Level level, BlockPos blockPos, Vec3i vec3i, boolean bl, @Nullable Block block) {
        if (vec3i.getX() < 1 || vec3i.getY() < 1 || vec3i.getZ() < 1) {
            return;
        }
        BlockPos blockPos2 = blockPos.offset(vec3i).offset(-1, -1, -1);
        ArrayList<StructureBlockInfo> list = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list2 = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list3 = Lists.newArrayList();
        BlockPos blockPos3 = new BlockPos(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()));
        BlockPos blockPos4 = new BlockPos(Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
        this.size = vec3i;
        for (BlockPos blockPos5 : BlockPos.betweenClosed(blockPos3, blockPos4)) {
            BlockPos blockPos6 = blockPos5.subtract(blockPos3);
            BlockState blockState = level.getBlockState(blockPos5);
            if (block != null && blockState.is(block)) continue;
            BlockEntity blockEntity = level.getBlockEntity(blockPos5);
            StructureBlockInfo structureBlockInfo = blockEntity != null ? new StructureBlockInfo(blockPos6, blockState, blockEntity.saveWithId()) : new StructureBlockInfo(blockPos6, blockState, null);
            StructureTemplate.addToLists(structureBlockInfo, list, list2, list3);
        }
        List<StructureBlockInfo> list4 = StructureTemplate.buildInfoList(list, list2, list3);
        this.palettes.clear();
        this.palettes.add(new Palette(list4));
        if (bl) {
            this.fillEntityList(level, blockPos3, blockPos4.offset(1, 1, 1));
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

    public ObjectArrayList<StructureBlockInfo> filterBlocks(BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Block block, boolean bl) {
        ObjectArrayList<StructureBlockInfo> objectArrayList = new ObjectArrayList<StructureBlockInfo>();
        BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
        if (this.palettes.isEmpty()) {
            return objectArrayList;
        }
        for (StructureBlockInfo structureBlockInfo : structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks(block)) {
            BlockPos blockPos2;
            BlockPos blockPos3 = blockPos2 = bl ? StructureTemplate.calculateRelativePosition(structurePlaceSettings, structureBlockInfo.pos).offset(blockPos) : structureBlockInfo.pos;
            if (boundingBox != null && !boundingBox.isInside(blockPos2)) continue;
            objectArrayList.add(new StructureBlockInfo(blockPos2, structureBlockInfo.state.rotate(structurePlaceSettings.getRotation()), structureBlockInfo.nbt));
        }
        return objectArrayList;
    }

    public BlockPos calculateConnectedPosition(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings2, BlockPos blockPos2) {
        BlockPos blockPos3 = StructureTemplate.calculateRelativePosition(structurePlaceSettings, blockPos);
        BlockPos blockPos4 = StructureTemplate.calculateRelativePosition(structurePlaceSettings2, blockPos2);
        return blockPos3.subtract(blockPos4);
    }

    public static BlockPos calculateRelativePosition(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
        return StructureTemplate.transform(blockPos, structurePlaceSettings.getMirror(), structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot());
    }

    public boolean placeInWorld(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int i) {
        if (this.palettes.isEmpty()) {
            return false;
        }
        List<StructureBlockInfo> list = structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks();
        if (list.isEmpty() && (structurePlaceSettings.isIgnoreEntities() || this.entityInfoList.isEmpty()) || this.size.getX() < 1 || this.size.getY() < 1 || this.size.getZ() < 1) {
            return false;
        }
        BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
        ArrayList<BlockPos> list2 = Lists.newArrayListWithCapacity(structurePlaceSettings.shouldKeepLiquids() ? list.size() : 0);
        ArrayList<BlockPos> list3 = Lists.newArrayListWithCapacity(structurePlaceSettings.shouldKeepLiquids() ? list.size() : 0);
        ArrayList<Pair<BlockPos, CompoundTag>> list4 = Lists.newArrayListWithCapacity(list.size());
        int j = Integer.MAX_VALUE;
        int k = Integer.MAX_VALUE;
        int l = Integer.MAX_VALUE;
        int m = Integer.MIN_VALUE;
        int n = Integer.MIN_VALUE;
        int o = Integer.MIN_VALUE;
        List<StructureBlockInfo> list5 = StructureTemplate.processBlockInfos(serverLevelAccessor, blockPos, blockPos2, structurePlaceSettings, list);
        for (StructureBlockInfo structureBlockInfo : list5) {
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
            list4.add(Pair.of(blockPos3, structureBlockInfo.nbt));
            if (structureBlockInfo.nbt != null && (blockEntity = serverLevelAccessor.getBlockEntity(blockPos3)) != null) {
                if (blockEntity instanceof RandomizableContainerBlockEntity) {
                    structureBlockInfo.nbt.putLong("LootTableSeed", randomSource.nextLong());
                }
                blockEntity.load(structureBlockInfo.nbt);
            }
            if (fluidState == null) continue;
            if (blockState.getFluidState().isSource()) {
                list3.add(blockPos3);
                continue;
            }
            if (!(blockState.getBlock() instanceof LiquidBlockContainer)) continue;
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
                Object block;
                BlockPos blockPos3 = (BlockPos)iterator.next();
                FluidState fluidState2 = serverLevelAccessor.getFluidState(blockPos3);
                for (int p = 0; p < directions.length && !fluidState2.isSource(); ++p) {
                    BlockPos blockPos5 = blockPos3.relative(directions[p]);
                    FluidState fluidState = serverLevelAccessor.getFluidState(blockPos5);
                    if (!fluidState.isSource() || list3.contains(blockPos5)) continue;
                    fluidState2 = fluidState;
                }
                if (!fluidState2.isSource() || !((block = (blockState2 = serverLevelAccessor.getBlockState(blockPos3)).getBlock()) instanceof LiquidBlockContainer)) continue;
                ((LiquidBlockContainer)block).placeLiquid(serverLevelAccessor, blockPos3, blockState2, fluidState2);
                bl = true;
                iterator.remove();
            }
        }
        if (j <= m) {
            if (!structurePlaceSettings.getKnownShape()) {
                BitSetDiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(m - j + 1, n - k + 1, o - l + 1);
                int n2 = j;
                int r = k;
                int p = l;
                for (Pair pair : list4) {
                    BlockPos blockPos6 = (BlockPos)pair.getFirst();
                    ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos6.getX() - n2, blockPos6.getY() - r, blockPos6.getZ() - p);
                }
                StructureTemplate.updateShapeAtEdge(serverLevelAccessor, i, discreteVoxelShape, n2, r, p);
            }
            for (Pair pair : list4) {
                BlockEntity blockEntity;
                BlockPos blockPos7 = (BlockPos)pair.getFirst();
                if (!structurePlaceSettings.getKnownShape()) {
                    BlockState blockState3;
                    BlockState blockState2 = serverLevelAccessor.getBlockState(blockPos7);
                    if (blockState2 != (blockState3 = Block.updateFromNeighbourShapes(blockState2, serverLevelAccessor, blockPos7))) {
                        serverLevelAccessor.setBlock(blockPos7, blockState3, i & 0xFFFFFFFE | 0x10);
                    }
                    serverLevelAccessor.blockUpdated(blockPos7, blockState3.getBlock());
                }
                if (pair.getSecond() == null || (blockEntity = serverLevelAccessor.getBlockEntity(blockPos7)) == null) continue;
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
        for (StructureProcessor structureProcessor : structurePlaceSettings.getProcessors()) {
            structureProcessor.finalizeStructure(levelAccessor, blockPos, blockPos2, structurePlaceSettings, list2);
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
                float f = entity.rotate(rotation);
                entity.moveTo(vec3.x, vec3.y, vec3.z, f += entity.mirror(mirror) - entity.getYRot(), entity.getXRot());
                if (bl && entity instanceof Mob) {
                    ((Mob)entity).finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(new BlockPos(vec32)), MobSpawnType.STRUCTURE, null, compoundTag);
                }
                serverLevelAccessor.addFreshEntityWithPassengers((Entity)entity);
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

    public Vec3i getSize(Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                return new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
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
        return StructureTemplate.getBoundingBox(blockPos, rotation, blockPos2, mirror, this.size);
    }

    @VisibleForTesting
    protected static BoundingBox getBoundingBox(BlockPos blockPos, Rotation rotation, BlockPos blockPos2, Mirror mirror, Vec3i vec3i) {
        Vec3i vec3i2 = vec3i.offset(-1, -1, -1);
        BlockPos blockPos3 = StructureTemplate.transform(BlockPos.ZERO, mirror, rotation, blockPos2);
        BlockPos blockPos4 = StructureTemplate.transform(BlockPos.ZERO.offset(vec3i2), mirror, rotation, blockPos2);
        return BoundingBox.fromCorners(blockPos3, blockPos4).move(blockPos);
    }

    public CompoundTag save(CompoundTag compoundTag) {
        if (this.palettes.isEmpty()) {
            compoundTag.put(BLOCKS_TAG, new ListTag());
            compoundTag.put(PALETTE_TAG, new ListTag());
        } else {
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
                compoundTag2.putInt(BLOCK_TAG_STATE, k);
                if (structureBlockInfo.nbt != null) {
                    compoundTag2.put("nbt", structureBlockInfo.nbt);
                }
                listTag.add(compoundTag2);
                for (int l = 1; l < this.palettes.size(); ++l) {
                    SimplePalette simplePalette2 = (SimplePalette)list.get(l);
                    simplePalette2.addMapping(this.palettes.get((int)l).blocks().get((int)j).state, k);
                }
            }
            compoundTag.put(BLOCKS_TAG, listTag);
            if (list.size() == 1) {
                listTag2 = new ListTag();
                for (BlockState blockState : simplePalette) {
                    listTag2.add(NbtUtils.writeBlockState(blockState));
                }
                compoundTag.put(PALETTE_TAG, listTag2);
            } else {
                listTag2 = new ListTag();
                for (SimplePalette simplePalette3 : list) {
                    ListTag listTag3 = new ListTag();
                    for (BlockState blockState2 : simplePalette3) {
                        listTag3.add(NbtUtils.writeBlockState(blockState2));
                    }
                    listTag2.add(listTag3);
                }
                compoundTag.put(PALETTE_LIST_TAG, listTag2);
            }
        }
        ListTag listTag4 = new ListTag();
        for (StructureEntityInfo structureEntityInfo : this.entityInfoList) {
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.put("pos", this.newDoubleList(structureEntityInfo.pos.x, structureEntityInfo.pos.y, structureEntityInfo.pos.z));
            compoundTag3.put(ENTITY_TAG_BLOCKPOS, this.newIntegerList(structureEntityInfo.blockPos.getX(), structureEntityInfo.blockPos.getY(), structureEntityInfo.blockPos.getZ()));
            if (structureEntityInfo.nbt != null) {
                compoundTag3.put("nbt", structureEntityInfo.nbt);
            }
            listTag4.add(compoundTag3);
        }
        compoundTag.put(ENTITIES_TAG, listTag4);
        compoundTag.put(SIZE_TAG, this.newIntegerList(this.size.getX(), this.size.getY(), this.size.getZ()));
        return NbtUtils.addCurrentDataVersion(compoundTag);
    }

    public void load(HolderGetter<Block> holderGetter, CompoundTag compoundTag) {
        int i;
        ListTag listTag3;
        this.palettes.clear();
        this.entityInfoList.clear();
        ListTag listTag = compoundTag.getList(SIZE_TAG, 3);
        this.size = new Vec3i(listTag.getInt(0), listTag.getInt(1), listTag.getInt(2));
        ListTag listTag2 = compoundTag.getList(BLOCKS_TAG, 10);
        if (compoundTag.contains(PALETTE_LIST_TAG, 9)) {
            listTag3 = compoundTag.getList(PALETTE_LIST_TAG, 9);
            for (i = 0; i < listTag3.size(); ++i) {
                this.loadPalette(holderGetter, listTag3.getList(i), listTag2);
            }
        } else {
            this.loadPalette(holderGetter, compoundTag.getList(PALETTE_TAG, 10), listTag2);
        }
        listTag3 = compoundTag.getList(ENTITIES_TAG, 10);
        for (i = 0; i < listTag3.size(); ++i) {
            CompoundTag compoundTag2 = listTag3.getCompound(i);
            ListTag listTag4 = compoundTag2.getList("pos", 6);
            Vec3 vec3 = new Vec3(listTag4.getDouble(0), listTag4.getDouble(1), listTag4.getDouble(2));
            ListTag listTag5 = compoundTag2.getList(ENTITY_TAG_BLOCKPOS, 3);
            BlockPos blockPos = new BlockPos(listTag5.getInt(0), listTag5.getInt(1), listTag5.getInt(2));
            if (!compoundTag2.contains("nbt")) continue;
            CompoundTag compoundTag3 = compoundTag2.getCompound("nbt");
            this.entityInfoList.add(new StructureEntityInfo(vec3, blockPos, compoundTag3));
        }
    }

    private void loadPalette(HolderGetter<Block> holderGetter, ListTag listTag, ListTag listTag2) {
        SimplePalette simplePalette = new SimplePalette();
        for (int i = 0; i < listTag.size(); ++i) {
            simplePalette.addMapping(NbtUtils.readBlockState(holderGetter, listTag.getCompound(i)), i);
        }
        ArrayList<StructureBlockInfo> list = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list2 = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list3 = Lists.newArrayList();
        for (int j = 0; j < listTag2.size(); ++j) {
            CompoundTag compoundTag = listTag2.getCompound(j);
            ListTag listTag3 = compoundTag.getList("pos", 3);
            BlockPos blockPos = new BlockPos(listTag3.getInt(0), listTag3.getInt(1), listTag3.getInt(2));
            BlockState blockState = simplePalette.stateFor(compoundTag.getInt(BLOCK_TAG_STATE));
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
            return String.format(Locale.ROOT, "<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
        }
    }

    public static final class Palette {
        private final List<StructureBlockInfo> blocks;
        private final Map<Block, List<StructureBlockInfo>> cache = Maps.newHashMap();

        Palette(List<StructureBlockInfo> list) {
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

    static class SimplePalette
    implements Iterable<BlockState> {
        public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
        private final IdMapper<BlockState> ids = new IdMapper(16);
        private int lastId;

        SimplePalette() {
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

