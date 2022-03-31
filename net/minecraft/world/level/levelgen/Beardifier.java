/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Beardifier
implements DensityFunctions.BeardifierOrMarker {
    public static final int BEARD_KERNEL_RADIUS = 12;
    private static final int BEARD_KERNEL_SIZE = 24;
    private static final float[] BEARD_KERNEL = Util.make(new float[13824], fs -> {
        for (int i = 0; i < 24; ++i) {
            for (int j = 0; j < 24; ++j) {
                for (int k = 0; k < 24; ++k) {
                    fs[i * 24 * 24 + j * 24 + k] = (float)Beardifier.computeBeardContribution(j - 12, k - 12, i - 12);
                }
            }
        }
    });
    private final ObjectListIterator<Rigid> pieceIterator;
    private final ObjectListIterator<JigsawJunction> junctionIterator;

    public static Beardifier forStructuresInChunk(StructureManager structureManager, ChunkPos chunkPos) {
        int i = chunkPos.getMinBlockX();
        int j = chunkPos.getMinBlockZ();
        ObjectArrayList objectList = new ObjectArrayList(10);
        ObjectArrayList objectList2 = new ObjectArrayList(32);
        structureManager.startsForStructure(chunkPos, structure -> structure.terrainAdaptation() != TerrainAdjustment.NONE).forEach(structureStart -> {
            TerrainAdjustment terrainAdjustment = structureStart.getStructure().terrainAdaptation();
            for (StructurePiece structurePiece : structureStart.getPieces()) {
                if (!structurePiece.isCloseToChunk(chunkPos, 12)) continue;
                if (structurePiece instanceof PoolElementStructurePiece) {
                    PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
                    StructureTemplatePool.Projection projection = poolElementStructurePiece.getElement().getProjection();
                    if (projection == StructureTemplatePool.Projection.RIGID) {
                        objectList.add(new Rigid(poolElementStructurePiece.getBoundingBox(), terrainAdjustment, poolElementStructurePiece.getGroundLevelDelta()));
                    }
                    for (JigsawJunction jigsawJunction : poolElementStructurePiece.getJunctions()) {
                        int k = jigsawJunction.getSourceX();
                        int l = jigsawJunction.getSourceZ();
                        if (k <= i - 12 || l <= j - 12 || k >= i + 15 + 12 || l >= j + 15 + 12) continue;
                        objectList2.add(jigsawJunction);
                    }
                    continue;
                }
                objectList.add(new Rigid(structurePiece.getBoundingBox(), terrainAdjustment, 0));
            }
        });
        return new Beardifier((ObjectListIterator<Rigid>)objectList.iterator(), (ObjectListIterator<JigsawJunction>)objectList2.iterator());
    }

    @VisibleForTesting
    public Beardifier(ObjectListIterator<Rigid> objectListIterator, ObjectListIterator<JigsawJunction> objectListIterator2) {
        this.pieceIterator = objectListIterator;
        this.junctionIterator = objectListIterator2;
    }

    @Override
    public double compute(DensityFunction.FunctionContext functionContext) {
        int m;
        int l;
        int i = functionContext.blockX();
        int j = functionContext.blockY();
        int k = functionContext.blockZ();
        double d = 0.0;
        while (this.pieceIterator.hasNext()) {
            Rigid rigid = (Rigid)this.pieceIterator.next();
            BoundingBox boundingBox = rigid.box();
            l = rigid.groundLevelDelta();
            m = Math.max(0, Math.max(boundingBox.minX() - i, i - boundingBox.maxX()));
            int n = Math.max(0, Math.max(boundingBox.minZ() - k, k - boundingBox.maxZ()));
            int o = boundingBox.minY() + l;
            int p = j - o;
            int q = switch (rigid.terrainAdjustment()) {
                default -> throw new IncompatibleClassChangeError();
                case TerrainAdjustment.NONE -> 0;
                case TerrainAdjustment.BURY, TerrainAdjustment.BEARD_THIN -> p;
                case TerrainAdjustment.BEARD_BOX -> Math.max(0, Math.max(o - j, j - boundingBox.maxY()));
            };
            d += (switch (rigid.terrainAdjustment()) {
                default -> throw new IncompatibleClassChangeError();
                case TerrainAdjustment.NONE -> 0.0;
                case TerrainAdjustment.BURY -> Beardifier.getBuryContribution(m, q, n);
                case TerrainAdjustment.BEARD_THIN, TerrainAdjustment.BEARD_BOX -> Beardifier.getBeardContribution(m, q, n, p) * 0.8;
            });
        }
        this.pieceIterator.back(Integer.MAX_VALUE);
        while (this.junctionIterator.hasNext()) {
            JigsawJunction jigsawJunction = (JigsawJunction)this.junctionIterator.next();
            int r = i - jigsawJunction.getSourceX();
            l = j - jigsawJunction.getSourceGroundY();
            m = k - jigsawJunction.getSourceZ();
            d += Beardifier.getBeardContribution(r, l, m, l) * 0.4;
        }
        this.junctionIterator.back(Integer.MAX_VALUE);
        return d;
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getBuryContribution(int i, int j, int k) {
        double d = Mth.length(i, (double)j / 2.0, k);
        return Mth.clampedMap(d, 0.0, 6.0, 1.0, 0.0);
    }

    private static double getBeardContribution(int i, int j, int k, int l) {
        int m = i + 12;
        int n = j + 12;
        int o = k + 12;
        if (!(Beardifier.isInKernelRange(m) && Beardifier.isInKernelRange(n) && Beardifier.isInKernelRange(o))) {
            return 0.0;
        }
        double d = (double)l + 0.5;
        double e = Mth.lengthSquared(i, d, k);
        double f = -d * Mth.fastInvSqrt(e / 2.0) / 2.0;
        return f * (double)BEARD_KERNEL[o * 24 * 24 + m * 24 + n];
    }

    private static boolean isInKernelRange(int i) {
        return i >= 0 && i < 24;
    }

    private static double computeBeardContribution(int i, int j, int k) {
        return Beardifier.computeBeardContribution(i, (double)j + 0.5, k);
    }

    private static double computeBeardContribution(int i, double d, int j) {
        double e = Mth.lengthSquared(i, d, j);
        double f = Math.pow(Math.E, -e / 16.0);
        return f;
    }

    @VisibleForTesting
    public record Rigid(BoundingBox box, TerrainAdjustment terrainAdjustment, int groundLevelDelta) {
    }
}

