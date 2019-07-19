/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePools;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureFeatureIO;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JigsawPlacement {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureTemplatePools POOLS = new StructureTemplatePools();

    public static void addPieces(ResourceLocation resourceLocation, int i, PieceFactory pieceFactory, ChunkGenerator<?> chunkGenerator, StructureManager structureManager, BlockPos blockPos, List<StructurePiece> list, Random random) {
        StructureFeatureIO.bootstrap();
        new Placer(resourceLocation, i, pieceFactory, chunkGenerator, structureManager, blockPos, list, random);
    }

    static {
        POOLS.register(StructureTemplatePool.EMPTY);
    }

    public static interface PieceFactory {
        public PoolElementStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, Rotation var5, BoundingBox var6);
    }

    static final class Placer {
        private final int maxDepth;
        private final PieceFactory factory;
        private final ChunkGenerator<?> chunkGenerator;
        private final StructureManager structureManager;
        private final List<StructurePiece> pieces;
        private final Random random;
        private final Deque<PieceState> placing = Queues.newArrayDeque();

        public Placer(ResourceLocation resourceLocation, int i, PieceFactory pieceFactory, ChunkGenerator<?> chunkGenerator, StructureManager structureManager, BlockPos blockPos, List<StructurePiece> list, Random random) {
            this.maxDepth = i;
            this.factory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.pieces = list;
            this.random = random;
            Rotation rotation = Rotation.getRandom(random);
            StructureTemplatePool structureTemplatePool = POOLS.getPool(resourceLocation);
            StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(random);
            PoolElementStructurePiece poolElementStructurePiece = pieceFactory.create(structureManager, structurePoolElement, blockPos, structurePoolElement.getGroundLevelDelta(), rotation, structurePoolElement.getBoundingBox(structureManager, blockPos, rotation));
            BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
            int j = (boundingBox.x1 + boundingBox.x0) / 2;
            int k = (boundingBox.z1 + boundingBox.z0) / 2;
            int l = chunkGenerator.getFirstFreeHeight(j, k, Heightmap.Types.WORLD_SURFACE_WG);
            poolElementStructurePiece.move(0, l - (boundingBox.y0 + poolElementStructurePiece.getGroundLevelDelta()), 0);
            list.add(poolElementStructurePiece);
            if (i <= 0) {
                return;
            }
            int m = 80;
            AABB aABB = new AABB(j - 80, l - 80, k - 80, j + 80 + 1, l + 80 + 1, k + 80 + 1);
            this.placing.addLast(new PieceState(poolElementStructurePiece, new AtomicReference<VoxelShape>(Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST)), l + 80, 0));
            while (!this.placing.isEmpty()) {
                PieceState pieceState = this.placing.removeFirst();
                this.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.boundsTop, pieceState.depth);
            }
        }

        private void tryPlacingChildren(PoolElementStructurePiece poolElementStructurePiece, AtomicReference<VoxelShape> atomicReference, int i, int j) {
            StructurePoolElement structurePoolElement = poolElementStructurePiece.getElement();
            BlockPos blockPos = poolElementStructurePiece.getPosition();
            Rotation rotation = poolElementStructurePiece.getRotation();
            StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
            boolean bl = projection == StructureTemplatePool.Projection.RIGID;
            AtomicReference<VoxelShape> atomicReference2 = new AtomicReference<VoxelShape>();
            BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
            int k = boundingBox.y0;
            block0: for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : structurePoolElement.getShuffledJigsawBlocks(this.structureManager, blockPos, rotation, this.random)) {
                StructurePoolElement structurePoolElement2;
                int n;
                AtomicReference<Object> atomicReference3;
                Direction direction = structureBlockInfo2.state.getValue(JigsawBlock.FACING);
                BlockPos blockPos2 = structureBlockInfo2.pos;
                BlockPos blockPos3 = blockPos2.relative(direction);
                int l = blockPos2.getY() - k;
                int m = -1;
                StructureTemplatePool structureTemplatePool = POOLS.getPool(new ResourceLocation(structureBlockInfo2.nbt.getString("target_pool")));
                StructureTemplatePool structureTemplatePool2 = POOLS.getPool(structureTemplatePool.getFallback());
                if (structureTemplatePool == StructureTemplatePool.INVALID || structureTemplatePool.size() == 0 && structureTemplatePool != StructureTemplatePool.EMPTY) {
                    LOGGER.warn("Empty or none existent pool: {}", (Object)structureBlockInfo2.nbt.getString("target_pool"));
                    continue;
                }
                boolean bl2 = boundingBox.isInside(blockPos3);
                if (bl2) {
                    atomicReference3 = atomicReference2;
                    n = k;
                    if (atomicReference2.get() == null) {
                        atomicReference2.set(Shapes.create(AABB.of(boundingBox)));
                    }
                } else {
                    atomicReference3 = atomicReference;
                    n = i;
                }
                ArrayList<StructurePoolElement> list = Lists.newArrayList();
                if (j != this.maxDepth) {
                    list.addAll(structureTemplatePool.getShuffledTemplates(this.random));
                }
                list.addAll(structureTemplatePool2.getShuffledTemplates(this.random));
                Iterator iterator = list.iterator();
                while (iterator.hasNext() && (structurePoolElement2 = (StructurePoolElement)iterator.next()) != EmptyPoolElement.INSTANCE) {
                    for (Rotation rotation2 : Rotation.getShuffled(this.random)) {
                        List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation2, this.random);
                        BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation2);
                        int o = boundingBox2.getYSpan() > 16 ? 0 : list2.stream().mapToInt(structureBlockInfo -> {
                            if (!boundingBox2.isInside(structureBlockInfo.pos.relative(structureBlockInfo.state.getValue(JigsawBlock.FACING)))) {
                                return 0;
                            }
                            ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfo.nbt.getString("target_pool"));
                            StructureTemplatePool structureTemplatePool = POOLS.getPool(resourceLocation);
                            StructureTemplatePool structureTemplatePool2 = POOLS.getPool(structureTemplatePool.getFallback());
                            return Math.max(structureTemplatePool.getMaxSize(this.structureManager), structureTemplatePool2.getMaxSize(this.structureManager));
                        }).max().orElse(0);
                        for (StructureTemplate.StructureBlockInfo structureBlockInfo22 : list2) {
                            int w;
                            int u;
                            int s;
                            if (!JigsawBlock.canAttach(structureBlockInfo2, structureBlockInfo22)) continue;
                            BlockPos blockPos4 = structureBlockInfo22.pos;
                            BlockPos blockPos5 = new BlockPos(blockPos3.getX() - blockPos4.getX(), blockPos3.getY() - blockPos4.getY(), blockPos3.getZ() - blockPos4.getZ());
                            BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureManager, blockPos5, rotation2);
                            int p = boundingBox3.y0;
                            StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl3 = projection2 == StructureTemplatePool.Projection.RIGID;
                            int q = blockPos4.getY();
                            int r = l - q + structureBlockInfo2.state.getValue(JigsawBlock.FACING).getStepY();
                            if (bl && bl3) {
                                s = k + r;
                            } else {
                                if (m == -1) {
                                    m = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG);
                                }
                                s = m - q;
                            }
                            int t = s - p;
                            BoundingBox boundingBox4 = boundingBox3.moved(0, t, 0);
                            BlockPos blockPos6 = blockPos5.offset(0, t, 0);
                            if (o > 0) {
                                u = Math.max(o + 1, boundingBox4.y1 - boundingBox4.y0);
                                boundingBox4.y1 = boundingBox4.y0 + u;
                            }
                            if (Shapes.joinIsNotEmpty((VoxelShape)atomicReference3.get(), Shapes.create(AABB.of(boundingBox4).deflate(0.25)), BooleanOp.ONLY_SECOND)) continue;
                            atomicReference3.set(Shapes.joinUnoptimized((VoxelShape)atomicReference3.get(), Shapes.create(AABB.of(boundingBox4)), BooleanOp.ONLY_FIRST));
                            u = poolElementStructurePiece.getGroundLevelDelta();
                            int v = bl3 ? u - r : structurePoolElement2.getGroundLevelDelta();
                            PoolElementStructurePiece poolElementStructurePiece2 = this.factory.create(this.structureManager, structurePoolElement2, blockPos6, v, rotation2, boundingBox4);
                            if (bl) {
                                w = k + l;
                            } else if (bl3) {
                                w = s + q;
                            } else {
                                if (m == -1) {
                                    m = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG);
                                }
                                w = m + r / 2;
                            }
                            poolElementStructurePiece.addJunction(new JigsawJunction(blockPos3.getX(), w - l + u, blockPos3.getZ(), r, projection2));
                            poolElementStructurePiece2.addJunction(new JigsawJunction(blockPos2.getX(), w - q + v, blockPos2.getZ(), -r, projection));
                            this.pieces.add(poolElementStructurePiece2);
                            if (j + 1 > this.maxDepth) continue block0;
                            this.placing.addLast(new PieceState(poolElementStructurePiece2, atomicReference3, n, j + 1));
                            continue block0;
                        }
                    }
                }
            }
        }
    }

    static final class PieceState {
        private final PoolElementStructurePiece piece;
        private final AtomicReference<VoxelShape> free;
        private final int boundsTop;
        private final int depth;

        private PieceState(PoolElementStructurePiece poolElementStructurePiece, AtomicReference<VoxelShape> atomicReference, int i, int j) {
            this.piece = poolElementStructurePiece;
            this.free = atomicReference;
            this.boundsTop = i;
            this.depth = j;
        }
    }
}

