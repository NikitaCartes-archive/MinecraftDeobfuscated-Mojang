/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.lighting;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public abstract class LayerLightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>>
extends DynamicGraphMinFixedPoint
implements LayerLightEventListener {
    private static final Direction[] DIRECTIONS = Direction.values();
    protected final LightChunkGetter chunkSource;
    protected final LightLayer layer;
    protected final S storage;
    private boolean runningLightUpdates;
    protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private final long[] lastChunkPos = new long[2];
    private final BlockGetter[] lastChunk = new BlockGetter[2];

    public LayerLightEngine(LightChunkGetter lightChunkGetter, LightLayer lightLayer, S layerLightSectionStorage) {
        super(16, 256, 8192);
        this.chunkSource = lightChunkGetter;
        this.layer = lightLayer;
        this.storage = layerLightSectionStorage;
        this.clearCache();
    }

    @Override
    protected void checkNode(long l) {
        ((LayerLightSectionStorage)this.storage).runAllUpdates();
        if (((LayerLightSectionStorage)this.storage).storingLightForSection(SectionPos.blockToSection(l))) {
            super.checkNode(l);
        }
    }

    @Nullable
    private BlockGetter getChunk(int i, int j) {
        long l = ChunkPos.asLong(i, j);
        for (int k = 0; k < 2; ++k) {
            if (l != this.lastChunkPos[k]) continue;
            return this.lastChunk[k];
        }
        BlockGetter blockGetter = this.chunkSource.getChunkForLighting(i, j);
        for (int m = 1; m > 0; --m) {
            this.lastChunkPos[m] = this.lastChunkPos[m - 1];
            this.lastChunk[m] = this.lastChunk[m - 1];
        }
        this.lastChunkPos[0] = l;
        this.lastChunk[0] = blockGetter;
        return blockGetter;
    }

    private void clearCache() {
        Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunk, null);
    }

    protected BlockState getStateAndOpacity(long l, @Nullable MutableInt mutableInt) {
        boolean bl;
        int j;
        if (l == Long.MAX_VALUE) {
            if (mutableInt != null) {
                mutableInt.setValue(0);
            }
            return Blocks.AIR.defaultBlockState();
        }
        int i = SectionPos.blockToSectionCoord(BlockPos.getX(l));
        BlockGetter blockGetter = this.getChunk(i, j = SectionPos.blockToSectionCoord(BlockPos.getZ(l)));
        if (blockGetter == null) {
            if (mutableInt != null) {
                mutableInt.setValue(16);
            }
            return Blocks.BEDROCK.defaultBlockState();
        }
        this.pos.set(l);
        BlockState blockState = blockGetter.getBlockState(this.pos);
        boolean bl2 = bl = blockState.canOcclude() && blockState.useShapeForLightOcclusion();
        if (mutableInt != null) {
            mutableInt.setValue(blockState.getLightBlock(this.chunkSource.getLevel(), this.pos));
        }
        return bl ? blockState : Blocks.AIR.defaultBlockState();
    }

    protected VoxelShape getShape(BlockState blockState, long l, Direction direction) {
        return blockState.canOcclude() ? blockState.getFaceOcclusionShape(this.chunkSource.getLevel(), this.pos.set(l), direction) : Shapes.empty();
    }

    public static int getLightBlockInto(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos, BlockState blockState2, BlockPos blockPos2, Direction direction, int i) {
        VoxelShape voxelShape2;
        boolean bl2;
        boolean bl = blockState.canOcclude() && blockState.useShapeForLightOcclusion();
        boolean bl3 = bl2 = blockState2.canOcclude() && blockState2.useShapeForLightOcclusion();
        if (!bl && !bl2) {
            return i;
        }
        VoxelShape voxelShape = bl ? blockState.getOcclusionShape(blockGetter, blockPos) : Shapes.empty();
        VoxelShape voxelShape3 = voxelShape2 = bl2 ? blockState2.getOcclusionShape(blockGetter, blockPos2) : Shapes.empty();
        if (Shapes.mergedFaceOccludes(voxelShape, voxelShape2, direction)) {
            return 16;
        }
        return i;
    }

    @Override
    protected boolean isSource(long l) {
        return l == Long.MAX_VALUE;
    }

    @Override
    protected int getComputedLevel(long l, long m, int i) {
        return 0;
    }

    @Override
    protected int getLevel(long l) {
        if (l == Long.MAX_VALUE) {
            return 0;
        }
        return 15 - ((LayerLightSectionStorage)this.storage).getStoredLevel(l);
    }

    protected int getLevel(DataLayer dataLayer, long l) {
        return 15 - dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
    }

    @Override
    protected void setLevel(long l, int i) {
        ((LayerLightSectionStorage)this.storage).setStoredLevel(l, Math.min(15, 15 - i));
    }

    @Override
    protected int computeLevelFromNeighbor(long l, long m, int i) {
        return 0;
    }

    public boolean hasLightWork() {
        return this.hasWork() || ((DynamicGraphMinFixedPoint)this.storage).hasWork() || ((LayerLightSectionStorage)this.storage).hasInconsistencies();
    }

    public int runUpdates(int i, boolean bl, boolean bl2) {
        if (!this.runningLightUpdates) {
            if (((DynamicGraphMinFixedPoint)this.storage).hasWork() && (i = ((DynamicGraphMinFixedPoint)this.storage).runUpdates(i)) == 0) {
                return i;
            }
            ((LayerLightSectionStorage)this.storage).markNewInconsistencies(this, bl, bl2);
        }
        this.runningLightUpdates = true;
        if (this.hasWork()) {
            i = this.runUpdates(i);
            this.clearCache();
            if (i == 0) {
                return i;
            }
        }
        this.runningLightUpdates = false;
        ((LayerLightSectionStorage)this.storage).swapSectionMap();
        return i;
    }

    protected void queueSectionData(long l, @Nullable DataLayer dataLayer) {
        ((LayerLightSectionStorage)this.storage).queueSectionData(l, dataLayer);
    }

    @Override
    @Nullable
    public DataLayer getDataLayerData(SectionPos sectionPos) {
        return ((LayerLightSectionStorage)this.storage).getDataLayerData(sectionPos.asLong());
    }

    @Override
    public int getLightValue(BlockPos blockPos) {
        return ((LayerLightSectionStorage)this.storage).getLightValue(blockPos.asLong());
    }

    @Environment(value=EnvType.CLIENT)
    public String getDebugData(long l) {
        return "" + ((LayerLightSectionStorage)this.storage).getLevel(l);
    }

    public void checkBlock(BlockPos blockPos) {
        long l = blockPos.asLong();
        this.checkNode(l);
        for (Direction direction : DIRECTIONS) {
            this.checkNode(BlockPos.offset(l, direction));
        }
    }

    public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
    }

    @Override
    public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
        ((LayerLightSectionStorage)this.storage).updateSectionStatus(sectionPos.asLong(), bl);
    }

    public void enableLightSources(ChunkPos chunkPos, boolean bl) {
        long l = SectionPos.getZeroNode(SectionPos.asLong(chunkPos.x, 0, chunkPos.z));
        ((LayerLightSectionStorage)this.storage).enableLightSources(l, bl);
    }

    public void retainData(ChunkPos chunkPos, boolean bl) {
        long l = SectionPos.getZeroNode(SectionPos.asLong(chunkPos.x, 0, chunkPos.z));
        ((LayerLightSectionStorage)this.storage).retainData(l, bl);
    }
}

