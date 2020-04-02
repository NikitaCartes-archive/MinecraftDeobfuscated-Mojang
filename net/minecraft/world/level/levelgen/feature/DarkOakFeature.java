/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class DarkOakFeature
extends AbstractTreeFeature<MegaTreeConfiguration> {
    public DarkOakFeature(Function<Dynamic<?>, ? extends MegaTreeConfiguration> function) {
        super(function);
    }

    @Override
    public boolean doPlace(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, Set<BlockPos> set2, BoundingBox boundingBox, MegaTreeConfiguration megaTreeConfiguration) {
        int s;
        int r;
        int i = random.nextInt(3) + random.nextInt(2) + megaTreeConfiguration.baseHeight;
        int j = blockPos.getX();
        int k = blockPos.getY();
        int l = blockPos.getZ();
        if (k < 1 || k + i + 1 >= 256) {
            return false;
        }
        BlockPos blockPos2 = blockPos.below();
        if (!DarkOakFeature.isGrassOrDirt(levelSimulatedRW, blockPos2)) {
            return false;
        }
        if (!this.canPlaceTreeOfHeight(levelSimulatedRW, blockPos, i)) {
            return false;
        }
        this.setDirtAt(levelSimulatedRW, blockPos2);
        this.setDirtAt(levelSimulatedRW, blockPos2.east());
        this.setDirtAt(levelSimulatedRW, blockPos2.south());
        this.setDirtAt(levelSimulatedRW, blockPos2.south().east());
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int m = i - random.nextInt(4);
        int n = 2 - random.nextInt(3);
        int o = j;
        int p = l;
        int q = k + i - 1;
        for (r = 0; r < i; ++r) {
            BlockPos blockPos3;
            if (r >= m && n > 0) {
                o += direction.getStepX();
                p += direction.getStepZ();
                --n;
            }
            if (!DarkOakFeature.isAirOrLeaves(levelSimulatedRW, blockPos3 = new BlockPos(o, s = k + r, p))) continue;
            DarkOakFeature.placeLog(levelSimulatedRW, random, blockPos3, set, boundingBox, megaTreeConfiguration);
            DarkOakFeature.placeLog(levelSimulatedRW, random, blockPos3.east(), set, boundingBox, megaTreeConfiguration);
            DarkOakFeature.placeLog(levelSimulatedRW, random, blockPos3.south(), set, boundingBox, megaTreeConfiguration);
            DarkOakFeature.placeLog(levelSimulatedRW, random, blockPos3.east().south(), set, boundingBox, megaTreeConfiguration);
        }
        for (r = -2; r <= 0; ++r) {
            for (s = -2; s <= 0; ++s) {
                int t = -1;
                this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q + t, p + s), set2, boundingBox, megaTreeConfiguration);
                this.placeLeaf(levelSimulatedRW, random, new BlockPos(1 + o - r, q + t, p + s), set2, boundingBox, megaTreeConfiguration);
                this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q + t, 1 + p - s), set2, boundingBox, megaTreeConfiguration);
                this.placeLeaf(levelSimulatedRW, random, new BlockPos(1 + o - r, q + t, 1 + p - s), set2, boundingBox, megaTreeConfiguration);
                if (r <= -2 && s <= -1 || r == -1 && s == -2) continue;
                t = 1;
                this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q + t, p + s), set2, boundingBox, megaTreeConfiguration);
                this.placeLeaf(levelSimulatedRW, random, new BlockPos(1 + o - r, q + t, p + s), set2, boundingBox, megaTreeConfiguration);
                this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q + t, 1 + p - s), set2, boundingBox, megaTreeConfiguration);
                this.placeLeaf(levelSimulatedRW, random, new BlockPos(1 + o - r, q + t, 1 + p - s), set2, boundingBox, megaTreeConfiguration);
            }
        }
        if (random.nextBoolean()) {
            this.placeLeaf(levelSimulatedRW, random, new BlockPos(o, q + 2, p), set2, boundingBox, megaTreeConfiguration);
            this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + 1, q + 2, p), set2, boundingBox, megaTreeConfiguration);
            this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + 1, q + 2, p + 1), set2, boundingBox, megaTreeConfiguration);
            this.placeLeaf(levelSimulatedRW, random, new BlockPos(o, q + 2, p + 1), set2, boundingBox, megaTreeConfiguration);
        }
        for (r = -3; r <= 4; ++r) {
            for (s = -3; s <= 4; ++s) {
                if (r == -3 && s == -3 || r == -3 && s == 4 || r == 4 && s == -3 || r == 4 && s == 4 || Math.abs(r) >= 3 && Math.abs(s) >= 3) continue;
                this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q, p + s), set2, boundingBox, megaTreeConfiguration);
            }
        }
        for (r = -1; r <= 2; ++r) {
            for (s = -1; s <= 2; ++s) {
                int v;
                int u;
                if (r >= 0 && r <= 1 && s >= 0 && s <= 1 || random.nextInt(3) > 0) continue;
                int t = random.nextInt(3) + 2;
                for (u = 0; u < t; ++u) {
                    DarkOakFeature.placeLog(levelSimulatedRW, random, new BlockPos(j + r, q - u - 1, l + s), set, boundingBox, megaTreeConfiguration);
                }
                for (u = -1; u <= 1; ++u) {
                    for (v = -1; v <= 1; ++v) {
                        this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r + u, q, p + s + v), set2, boundingBox, megaTreeConfiguration);
                    }
                }
                for (u = -2; u <= 2; ++u) {
                    for (v = -2; v <= 2; ++v) {
                        if (Math.abs(u) == 2 && Math.abs(v) == 2) continue;
                        this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r + u, q - 1, p + s + v), set2, boundingBox, megaTreeConfiguration);
                    }
                }
            }
        }
        return true;
    }

    private boolean canPlaceTreeOfHeight(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, int i) {
        int j = blockPos.getX();
        int k = blockPos.getY();
        int l = blockPos.getZ();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int m = 0; m <= i + 1; ++m) {
            int n = 1;
            if (m == 0) {
                n = 0;
            }
            if (m >= i - 1) {
                n = 2;
            }
            for (int o = -n; o <= n; ++o) {
                for (int p = -n; p <= n; ++p) {
                    if (DarkOakFeature.isFree(levelSimulatedReader, mutableBlockPos.set(j + o, k + m, l + p))) continue;
                    return false;
                }
            }
        }
        return true;
    }
}

