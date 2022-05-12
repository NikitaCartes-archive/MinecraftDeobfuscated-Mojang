/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.portal;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

public class PortalForcer {
    private static final int TICKET_RADIUS = 3;
    private static final int SEARCH_RADIUS = 128;
    private static final int CREATE_RADIUS = 16;
    private static final int FRAME_HEIGHT = 5;
    private static final int FRAME_WIDTH = 4;
    private static final int FRAME_BOX = 3;
    private static final int FRAME_HEIGHT_START = -1;
    private static final int FRAME_HEIGHT_END = 4;
    private static final int FRAME_WIDTH_START = -1;
    private static final int FRAME_WIDTH_END = 3;
    private static final int FRAME_BOX_START = -1;
    private static final int FRAME_BOX_END = 2;
    private static final int NOTHING_FOUND = -1;
    private final ServerLevel level;

    public PortalForcer(ServerLevel serverLevel) {
        this.level = serverLevel;
    }

    public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos blockPos, boolean bl, WorldBorder worldBorder) {
        PoiManager poiManager = this.level.getPoiManager();
        int i = bl ? 16 : 128;
        poiManager.ensureLoadedAndValid(this.level, blockPos, i);
        Optional<PoiRecord> optional = poiManager.getInSquare(holder -> holder.is(PoiTypes.NETHER_PORTAL), blockPos, i, PoiManager.Occupancy.ANY).filter(poiRecord -> worldBorder.isWithinBounds(poiRecord.getPos())).sorted(Comparator.comparingDouble(poiRecord -> poiRecord.getPos().distSqr(blockPos)).thenComparingInt(poiRecord -> poiRecord.getPos().getY())).filter(poiRecord -> this.level.getBlockState(poiRecord.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS)).findFirst();
        return optional.map(poiRecord -> {
            BlockPos blockPos2 = poiRecord.getPos();
            this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPos2), 3, blockPos2);
            BlockState blockState = this.level.getBlockState(blockPos2);
            return BlockUtil.getLargestRectangleAround(blockPos2, blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, blockPos -> this.level.getBlockState((BlockPos)blockPos) == blockState);
        });
    }

    public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos blockPos, Direction.Axis axis) {
        int m;
        int l;
        int k;
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double d = -1.0;
        BlockPos blockPos2 = null;
        double e = -1.0;
        BlockPos blockPos3 = null;
        WorldBorder worldBorder = this.level.getWorldBorder();
        int i = Math.min(this.level.getMaxBuildHeight(), this.level.getMinBuildHeight() + this.level.getLogicalHeight()) - 1;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (BlockPos.MutableBlockPos mutableBlockPos2 : BlockPos.spiralAround(blockPos, 16, Direction.EAST, Direction.SOUTH)) {
            int j = Math.min(i, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos2.getX(), mutableBlockPos2.getZ()));
            k = 1;
            if (!worldBorder.isWithinBounds(mutableBlockPos2) || !worldBorder.isWithinBounds(mutableBlockPos2.move(direction, 1))) continue;
            mutableBlockPos2.move(direction.getOpposite(), 1);
            for (l = j; l >= this.level.getMinBuildHeight(); --l) {
                int n;
                mutableBlockPos2.setY(l);
                if (!this.level.isEmptyBlock(mutableBlockPos2)) continue;
                m = l;
                while (l > this.level.getMinBuildHeight() && this.level.isEmptyBlock(mutableBlockPos2.move(Direction.DOWN))) {
                    --l;
                }
                if (l + 4 > i || (n = m - l) > 0 && n < 3) continue;
                mutableBlockPos2.setY(l);
                if (!this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, 0)) continue;
                double f = blockPos.distSqr(mutableBlockPos2);
                if (this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, -1) && this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, 1) && (d == -1.0 || d > f)) {
                    d = f;
                    blockPos2 = mutableBlockPos2.immutable();
                }
                if (d != -1.0 || e != -1.0 && !(e > f)) continue;
                e = f;
                blockPos3 = mutableBlockPos2.immutable();
            }
        }
        if (d == -1.0 && e != -1.0) {
            blockPos2 = blockPos3;
            d = e;
        }
        if (d == -1.0) {
            int p = i - 9;
            int o = Math.max(this.level.getMinBuildHeight() - -1, 70);
            if (p < o) {
                return Optional.empty();
            }
            blockPos2 = new BlockPos(blockPos.getX(), Mth.clamp(blockPos.getY(), o, p), blockPos.getZ()).immutable();
            Direction direction2 = direction.getClockWise();
            if (!worldBorder.isWithinBounds(blockPos2)) {
                return Optional.empty();
            }
            for (k = -1; k < 2; ++k) {
                for (l = 0; l < 2; ++l) {
                    for (m = -1; m < 3; ++m) {
                        BlockState blockState = m < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        mutableBlockPos.setWithOffset(blockPos2, l * direction.getStepX() + k * direction2.getStepX(), m, l * direction.getStepZ() + k * direction2.getStepZ());
                        this.level.setBlockAndUpdate(mutableBlockPos, blockState);
                    }
                }
            }
        }
        for (int o = -1; o < 3; ++o) {
            for (int p = -1; p < 4; ++p) {
                if (o != -1 && o != 2 && p != -1 && p != 3) continue;
                mutableBlockPos.setWithOffset(blockPos2, o * direction.getStepX(), p, o * direction.getStepZ());
                this.level.setBlock(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }
        BlockState blockState2 = (BlockState)Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        for (int p = 0; p < 2; ++p) {
            for (int j = 0; j < 3; ++j) {
                mutableBlockPos.setWithOffset(blockPos2, p * direction.getStepX(), j, p * direction.getStepZ());
                this.level.setBlock(mutableBlockPos, blockState2, 18);
            }
        }
        return Optional.of(new BlockUtil.FoundRectangle(blockPos2.immutable(), 2, 3));
    }

    private boolean canHostFrame(BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos, Direction direction, int i) {
        Direction direction2 = direction.getClockWise();
        for (int j = -1; j < 3; ++j) {
            for (int k = -1; k < 4; ++k) {
                mutableBlockPos.setWithOffset(blockPos, direction.getStepX() * j + direction2.getStepX() * i, k, direction.getStepZ() * j + direction2.getStepZ() * i);
                if (k < 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().isSolid()) {
                    return false;
                }
                if (k < 0 || this.level.isEmptyBlock(mutableBlockPos)) continue;
                return false;
            }
        }
        return true;
    }
}

