/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PortalForcer {
    private final ServerLevel level;
    private final Random random;

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
        PoiManager poiManager = this.level.getPoiManager();
        poiManager.ensureLoadedAndValid(this.level, blockPos, 128);
        List list = poiManager.getInSquare(poiType -> poiType == PoiType.NETHER_PORTAL, blockPos, 128, PoiManager.Occupancy.ANY).collect(Collectors.toList());
        Optional<PoiRecord> optional = list.stream().min(Comparator.comparingDouble(poiRecord -> poiRecord.getPos().distSqr(blockPos)).thenComparingInt(poiRecord -> poiRecord.getPos().getY()));
        return optional.map(poiRecord -> {
            BlockPos blockPos = poiRecord.getPos();
            this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPos), 3, blockPos);
            BlockPattern.BlockPatternMatch blockPatternMatch = NetherPortalBlock.getPortalShape(this.level, blockPos);
            return blockPatternMatch.getPortalOutput(direction, blockPos, e, vec3, d);
        }).orElse(null);
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
        int j = Mth.floor(entity.getX());
        int k = Mth.floor(entity.getY());
        int l = Mth.floor(entity.getZ());
        int m = j;
        int n = k;
        int o = l;
        int p = 0;
        int q = this.random.nextInt(4);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (r = j - 16; r <= j + 16; ++r) {
            e = (double)r + 0.5 - entity.getX();
            for (s = l - 16; s <= l + 16; ++s) {
                f = (double)s + 0.5 - entity.getZ();
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
                        double g = (double)t + 0.5 - entity.getY();
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
                e = (double)r + 0.5 - entity.getX();
                for (s = l - 16; s <= l + 16; ++s) {
                    f = (double)s + 0.5 - entity.getZ();
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
                            double g = (double)t + 0.5 - entity.getY();
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
        BlockState blockState = (BlockState)Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, af == 0 ? Direction.Axis.Z : Direction.Axis.X);
        for (u = 0; u < 2; ++u) {
            for (v = 0; v < 3; ++v) {
                mutableBlockPos.set(ad + u * af, ae + v, s + u * ag);
                this.level.setBlock(mutableBlockPos, blockState, 18);
            }
        }
        return true;
    }
}

