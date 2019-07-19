/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.jetbrains.annotations.Nullable;

public class PortalForcer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final NetherPortalBlock PORTAL_BLOCK = (NetherPortalBlock)Blocks.NETHER_PORTAL;
    private final ServerLevel level;
    private final Random random;
    private final Map<ColumnPos, PortalPosition> cachedPortals = Maps.newHashMapWithExpectedSize(4096);
    private final Object2LongMap<ColumnPos> negativeChecks = new Object2LongOpenHashMap<ColumnPos>();

    public PortalForcer(ServerLevel serverLevel) {
        this.level = serverLevel;
        this.random = new Random(serverLevel.getSeed());
    }

    public boolean findAndMoveToPortal(Entity entity, float f) {
        Vec3 vec3 = entity.getPortalEntranceOffset();
        Direction direction = entity.getPortalEntranceForwards();
        BlockPattern.PortalInfo portalInfo = this.findPortal(new BlockPos(entity), entity.getDeltaMovement(), direction, vec3.x, vec3.y, entity instanceof Player);
        if (portalInfo == null) {
            return false;
        }
        Vec3 vec32 = portalInfo.pos;
        Vec3 vec33 = portalInfo.speed;
        entity.setDeltaMovement(vec33);
        entity.yRot = f + (float)portalInfo.angle;
        if (entity instanceof ServerPlayer) {
            ((ServerPlayer)entity).connection.teleport(vec32.x, vec32.y, vec32.z, entity.yRot, entity.xRot);
            ((ServerPlayer)entity).connection.resetPosition();
        } else {
            entity.moveTo(vec32.x, vec32.y, vec32.z, entity.yRot, entity.xRot);
        }
        return true;
    }

    @Nullable
    public BlockPattern.PortalInfo findPortal(BlockPos blockPos, Vec3 vec3, Direction direction, double d, double e, boolean bl) {
        int i = 128;
        boolean bl2 = true;
        BlockPos blockPos2 = null;
        ColumnPos columnPos = new ColumnPos(blockPos);
        if (!bl && this.negativeChecks.containsKey(columnPos)) {
            return null;
        }
        PortalPosition portalPosition = this.cachedPortals.get(columnPos);
        if (portalPosition != null) {
            blockPos2 = portalPosition.pos;
            portalPosition.lastUsed = this.level.getGameTime();
            bl2 = false;
        } else {
            double f = Double.MAX_VALUE;
            for (int j = -128; j <= 128; ++j) {
                for (int k = -128; k <= 128; ++k) {
                    BlockPos blockPos3 = blockPos.offset(j, this.level.getHeight() - 1 - blockPos.getY(), k);
                    while (blockPos3.getY() >= 0) {
                        BlockPos blockPos4 = blockPos3.below();
                        if (this.level.getBlockState(blockPos3).getBlock() == PORTAL_BLOCK) {
                            blockPos4 = blockPos3.below();
                            while (this.level.getBlockState(blockPos4).getBlock() == PORTAL_BLOCK) {
                                blockPos3 = blockPos4;
                                blockPos4 = blockPos3.below();
                            }
                            double g = blockPos3.distSqr(blockPos);
                            if (f < 0.0 || g < f) {
                                f = g;
                                blockPos2 = blockPos3;
                            }
                        }
                        blockPos3 = blockPos4;
                    }
                }
            }
        }
        if (blockPos2 == null) {
            long l = this.level.getGameTime() + 300L;
            this.negativeChecks.put(columnPos, l);
            return null;
        }
        if (bl2) {
            this.cachedPortals.put(columnPos, new PortalPosition(blockPos2, this.level.getGameTime()));
            Supplier[] supplierArray = new Supplier[2];
            supplierArray[0] = this.level.getDimension()::getType;
            supplierArray[1] = () -> columnPos;
            LOGGER.debug("Adding nether portal ticket for {}:{}", supplierArray);
            this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPos2), 3, columnPos);
        }
        BlockPattern.BlockPatternMatch blockPatternMatch = PORTAL_BLOCK.getPortalShape(this.level, blockPos2);
        return blockPatternMatch.getPortalOutput(direction, blockPos2, e, vec3, d);
    }

    public boolean createPortal(Entity entity) {
        int ab;
        int aa;
        int y;
        int x;
        int w;
        int v;
        int u;
        int t;
        double f;
        int s;
        double e;
        int r;
        int i = 16;
        double d = -1.0;
        int j = Mth.floor(entity.x);
        int k = Mth.floor(entity.y);
        int l = Mth.floor(entity.z);
        int m = j;
        int n = k;
        int o = l;
        int p = 0;
        int q = this.random.nextInt(4);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (r = j - 16; r <= j + 16; ++r) {
            e = (double)r + 0.5 - entity.x;
            for (s = l - 16; s <= l + 16; ++s) {
                f = (double)s + 0.5 - entity.z;
                block2: for (t = this.level.getHeight() - 1; t >= 0; --t) {
                    if (!this.level.isEmptyBlock(mutableBlockPos.set(r, t, s))) continue;
                    while (t > 0 && this.level.isEmptyBlock(mutableBlockPos.set(r, t - 1, s))) {
                        --t;
                    }
                    for (u = q; u < q + 4; ++u) {
                        v = u % 2;
                        w = 1 - v;
                        if (u % 4 >= 2) {
                            v = -v;
                            w = -w;
                        }
                        for (x = 0; x < 3; ++x) {
                            for (y = 0; y < 4; ++y) {
                                for (int z = -1; z < 4; ++z) {
                                    aa = r + (y - 1) * v + x * w;
                                    ab = t + z;
                                    int ac = s + (y - 1) * w - x * v;
                                    mutableBlockPos.set(aa, ab, ac);
                                    if (z < 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().isSolid() || z >= 0 && !this.level.isEmptyBlock(mutableBlockPos)) continue block2;
                                }
                            }
                        }
                        double g = (double)t + 0.5 - entity.y;
                        double h = e * e + g * g + f * f;
                        if (!(d < 0.0) && !(h < d)) continue;
                        d = h;
                        m = r;
                        n = t;
                        o = s;
                        p = u % 4;
                    }
                }
            }
        }
        if (d < 0.0) {
            for (r = j - 16; r <= j + 16; ++r) {
                e = (double)r + 0.5 - entity.x;
                for (s = l - 16; s <= l + 16; ++s) {
                    f = (double)s + 0.5 - entity.z;
                    block10: for (t = this.level.getHeight() - 1; t >= 0; --t) {
                        if (!this.level.isEmptyBlock(mutableBlockPos.set(r, t, s))) continue;
                        while (t > 0 && this.level.isEmptyBlock(mutableBlockPos.set(r, t - 1, s))) {
                            --t;
                        }
                        for (u = q; u < q + 2; ++u) {
                            v = u % 2;
                            w = 1 - v;
                            for (int x2 = 0; x2 < 4; ++x2) {
                                for (y = -1; y < 4; ++y) {
                                    int z = r + (x2 - 1) * v;
                                    aa = t + y;
                                    ab = s + (x2 - 1) * w;
                                    mutableBlockPos.set(z, aa, ab);
                                    if (y < 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().isSolid() || y >= 0 && !this.level.isEmptyBlock(mutableBlockPos)) continue block10;
                                }
                            }
                            double g = (double)t + 0.5 - entity.y;
                            double h = e * e + g * g + f * f;
                            if (!(d < 0.0) && !(h < d)) continue;
                            d = h;
                            m = r;
                            n = t;
                            o = s;
                            p = u % 2;
                        }
                    }
                }
            }
        }
        r = p;
        int ad = m;
        int ae = n;
        s = o;
        int af = r % 2;
        int ag = 1 - af;
        if (r % 4 >= 2) {
            af = -af;
            ag = -ag;
        }
        if (d < 0.0) {
            ae = n = Mth.clamp(n, 70, this.level.getHeight() - 10);
            for (t = -1; t <= 1; ++t) {
                for (u = 1; u < 3; ++u) {
                    for (v = -1; v < 3; ++v) {
                        w = ad + (u - 1) * af + t * ag;
                        x = ae + v;
                        y = s + (u - 1) * ag - t * af;
                        boolean bl = v < 0;
                        mutableBlockPos.set(w, x, y);
                        this.level.setBlockAndUpdate(mutableBlockPos, bl ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        for (t = -1; t < 3; ++t) {
            for (u = -1; u < 4; ++u) {
                if (t != -1 && t != 2 && u != -1 && u != 3) continue;
                mutableBlockPos.set(ad + t * af, ae + u, s + t * ag);
                this.level.setBlock(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }
        BlockState blockState = (BlockState)PORTAL_BLOCK.defaultBlockState().setValue(NetherPortalBlock.AXIS, af == 0 ? Direction.Axis.Z : Direction.Axis.X);
        for (u = 0; u < 2; ++u) {
            for (v = 0; v < 3; ++v) {
                mutableBlockPos.set(ad + u * af, ae + v, s + u * ag);
                this.level.setBlock(mutableBlockPos, blockState, 18);
            }
        }
        return true;
    }

    public void tick(long l) {
        if (l % 100L == 0L) {
            this.purgeNegativeChecks(l);
            this.clearStaleCacheEntries(l);
        }
    }

    private void purgeNegativeChecks(long l) {
        LongIterator longIterator = this.negativeChecks.values().iterator();
        while (longIterator.hasNext()) {
            long m = longIterator.nextLong();
            if (m > l) continue;
            longIterator.remove();
        }
    }

    private void clearStaleCacheEntries(long l) {
        long m = l - 300L;
        Iterator<Map.Entry<ColumnPos, PortalPosition>> iterator = this.cachedPortals.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ColumnPos, PortalPosition> entry = iterator.next();
            PortalPosition portalPosition = entry.getValue();
            if (portalPosition.lastUsed >= m) continue;
            ColumnPos columnPos = entry.getKey();
            Supplier[] supplierArray = new Supplier[2];
            supplierArray[0] = this.level.getDimension()::getType;
            supplierArray[1] = () -> columnPos;
            LOGGER.debug("Removing nether portal ticket for {}:{}", supplierArray);
            this.level.getChunkSource().removeRegionTicket(TicketType.PORTAL, new ChunkPos(portalPosition.pos), 3, columnPos);
            iterator.remove();
        }
    }

    static class PortalPosition {
        public final BlockPos pos;
        public long lastUsed;

        public PortalPosition(BlockPos blockPos, long l) {
            this.pos = blockPos;
            this.lastUsed = l;
        }
    }
}

