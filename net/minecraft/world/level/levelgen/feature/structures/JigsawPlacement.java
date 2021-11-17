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
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JigsawPlacement {
    static final Logger LOGGER = LogManager.getLogger();

    public static Optional<PieceGenerator<JigsawConfiguration>> addPieces(PieceGeneratorSupplier.Context<JigsawConfiguration> context2, PieceFactory pieceFactory, BlockPos blockPos, boolean bl, boolean bl2) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(context2.seed(), context2.chunkPos().x, context2.chunkPos().z);
        RegistryAccess registryAccess = context2.registryAccess();
        JigsawConfiguration jigsawConfiguration = (JigsawConfiguration)context2.config();
        ChunkGenerator chunkGenerator = context2.chunkGenerator();
        StructureManager structureManager = context2.structureManager();
        LevelHeightAccessor levelHeightAccessor = context2.heightAccessor();
        Predicate<Biome> predicate = context2.validBiome();
        StructureFeature.bootstrap();
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Rotation rotation = Rotation.getRandom(worldgenRandom);
        StructureTemplatePool structureTemplatePool = jigsawConfiguration.startPool().get();
        StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        PoolElementStructurePiece poolElementStructurePiece = pieceFactory.create(structureManager, structurePoolElement, blockPos, structurePoolElement.getGroundLevelDelta(), rotation, structurePoolElement.getBoundingBox(structureManager, blockPos, rotation));
        BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
        int i = (boundingBox.maxX() + boundingBox.minX()) / 2;
        int j = (boundingBox.maxZ() + boundingBox.minZ()) / 2;
        int k = bl2 ? blockPos.getY() + chunkGenerator.getFirstFreeHeight(i, j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor) : blockPos.getY();
        if (!predicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j)))) {
            return Optional.empty();
        }
        int l = boundingBox.minY() + poolElementStructurePiece.getGroundLevelDelta();
        poolElementStructurePiece.move(0, k - l, 0);
        return Optional.of((structurePiecesBuilder, context) -> {
            ArrayList<PoolElementStructurePiece> list = Lists.newArrayList();
            list.add(poolElementStructurePiece);
            if (jigsawConfiguration.maxDepth() <= 0) {
                return;
            }
            int l = 80;
            AABB aABB = new AABB(i - 80, k - 80, j - 80, i + 80 + 1, k + 80 + 1, j + 80 + 1);
            Placer placer = new Placer(registry, jigsawConfiguration.maxDepth(), pieceFactory, chunkGenerator, structureManager, list, worldgenRandom);
            placer.placing.addLast(new PieceState(poolElementStructurePiece, new MutableObject<VoxelShape>(Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST)), 0));
            while (!placer.placing.isEmpty()) {
                PieceState pieceState = placer.placing.removeFirst();
                placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, bl, levelHeightAccessor);
            }
            list.forEach(structurePiecesBuilder::addPiece);
        });
    }

    public static void addPieces(RegistryAccess registryAccess, PoolElementStructurePiece poolElementStructurePiece, int i, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolElementStructurePiece> list, Random random, LevelHeightAccessor levelHeightAccessor) {
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Placer placer = new Placer(registry, i, pieceFactory, chunkGenerator, structureManager, list, random);
        placer.placing.addLast(new PieceState(poolElementStructurePiece, new MutableObject<VoxelShape>(Shapes.INFINITY), 0));
        while (!placer.placing.isEmpty()) {
            PieceState pieceState = placer.placing.removeFirst();
            placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, false, levelHeightAccessor);
        }
    }

    public static interface PieceFactory {
        public PoolElementStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, Rotation var5, BoundingBox var6);
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final PieceFactory factory;
        private final ChunkGenerator chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final Random random;
        final Deque<PieceState> placing = Queues.newArrayDeque();

        Placer(Registry<StructureTemplatePool> registry, int i, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolElementStructurePiece> list, Random random) {
            this.pools = registry;
            this.maxDepth = i;
            this.factory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.pieces = list;
            this.random = random;
        }

        void tryPlacingChildren(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i, boolean bl, LevelHeightAccessor levelHeightAccessor) {
            StructurePoolElement structurePoolElement = poolElementStructurePiece.getElement();
            BlockPos blockPos = poolElementStructurePiece.getPosition();
            Rotation rotation = poolElementStructurePiece.getRotation();
            StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
            boolean bl2 = projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject2 = new MutableObject<VoxelShape>();
            BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
            int j = boundingBox.minY();
            block0: for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : structurePoolElement.getShuffledJigsawBlocks(this.structureManager, blockPos, rotation, this.random)) {
                StructurePoolElement structurePoolElement2;
                MutableObject<Object> mutableObject3;
                Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo2.state);
                BlockPos blockPos2 = structureBlockInfo2.pos;
                BlockPos blockPos3 = blockPos2.relative(direction);
                int k = blockPos2.getY() - j;
                int l = -1;
                ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfo2.nbt.getString("pool"));
                Optional<StructureTemplatePool> optional = this.pools.getOptional(resourceLocation);
                if (!optional.isPresent() || optional.get().size() == 0 && !Objects.equals(resourceLocation, Pools.EMPTY.location())) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)resourceLocation);
                    continue;
                }
                ResourceLocation resourceLocation2 = optional.get().getFallback();
                Optional<StructureTemplatePool> optional2 = this.pools.getOptional(resourceLocation2);
                if (!optional2.isPresent() || optional2.get().size() == 0 && !Objects.equals(resourceLocation2, Pools.EMPTY.location())) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)resourceLocation2);
                    continue;
                }
                boolean bl3 = boundingBox.isInside(blockPos3);
                if (bl3) {
                    mutableObject3 = mutableObject2;
                    if (mutableObject2.getValue() == null) {
                        mutableObject2.setValue(Shapes.create(AABB.of(boundingBox)));
                    }
                } else {
                    mutableObject3 = mutableObject;
                }
                ArrayList<StructurePoolElement> list = Lists.newArrayList();
                if (i != this.maxDepth) {
                    list.addAll(optional.get().getShuffledTemplates(this.random));
                }
                list.addAll(optional2.get().getShuffledTemplates(this.random));
                Iterator iterator = list.iterator();
                while (iterator.hasNext() && (structurePoolElement2 = (StructurePoolElement)iterator.next()) != EmptyPoolElement.INSTANCE) {
                    for (Rotation rotation2 : Rotation.getShuffled(this.random)) {
                        List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation2, this.random);
                        BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation2);
                        int m = !bl || boundingBox2.getYSpan() > 16 ? 0 : list2.stream().mapToInt(structureBlockInfo -> {
                            if (!boundingBox2.isInside(structureBlockInfo.pos.relative(JigsawBlock.getFrontFacing(structureBlockInfo.state)))) {
                                return 0;
                            }
                            ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfo.nbt.getString("pool"));
                            Optional<StructureTemplatePool> optional = this.pools.getOptional(resourceLocation);
                            Optional<Integer> optional2 = optional.flatMap(structureTemplatePool -> this.pools.getOptional(structureTemplatePool.getFallback()));
                            int i = optional.map(structureTemplatePool -> structureTemplatePool.getMaxSize(this.structureManager)).orElse(0);
                            int j = optional2.map(structureTemplatePool -> structureTemplatePool.getMaxSize(this.structureManager)).orElse(0);
                            return Math.max(i, j);
                        }).max().orElse(0);
                        for (StructureTemplate.StructureBlockInfo structureBlockInfo22 : list2) {
                            int u;
                            int s;
                            int q;
                            if (!JigsawBlock.canAttach(structureBlockInfo2, structureBlockInfo22)) continue;
                            BlockPos blockPos4 = structureBlockInfo22.pos;
                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                            BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureManager, blockPos5, rotation2);
                            int n = boundingBox3.minY();
                            StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl4 = projection2 == StructureTemplatePool.Projection.RIGID;
                            int o = blockPos4.getY();
                            int p = k - o + JigsawBlock.getFrontFacing(structureBlockInfo2.state).getStepY();
                            if (bl2 && bl4) {
                                q = j + p;
                            } else {
                                if (l == -1) {
                                    l = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
                                }
                                q = l - o;
                            }
                            int r = q - n;
                            BoundingBox boundingBox4 = boundingBox3.moved(0, r, 0);
                            BlockPos blockPos6 = blockPos5.offset(0, r, 0);
                            if (m > 0) {
                                s = Math.max(m + 1, boundingBox4.maxY() - boundingBox4.minY());
                                boundingBox4.encapsulate(new BlockPos(boundingBox4.minX(), boundingBox4.minY() + s, boundingBox4.minZ()));
                            }
                            if (Shapes.joinIsNotEmpty((VoxelShape)mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4).deflate(0.25)), BooleanOp.ONLY_SECOND)) continue;
                            mutableObject3.setValue(Shapes.joinUnoptimized((VoxelShape)mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4)), BooleanOp.ONLY_FIRST));
                            s = poolElementStructurePiece.getGroundLevelDelta();
                            int t = bl4 ? s - p : structurePoolElement2.getGroundLevelDelta();
                            PoolElementStructurePiece poolElementStructurePiece2 = this.factory.create(this.structureManager, structurePoolElement2, blockPos6, t, rotation2, boundingBox4);
                            if (bl2) {
                                u = j + k;
                            } else if (bl4) {
                                u = q + o;
                            } else {
                                if (l == -1) {
                                    l = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
                                }
                                u = l + p / 2;
                            }
                            poolElementStructurePiece.addJunction(new JigsawJunction(blockPos3.getX(), u - k + s, blockPos3.getZ(), p, projection2));
                            poolElementStructurePiece2.addJunction(new JigsawJunction(blockPos2.getX(), u - o + t, blockPos2.getZ(), -p, projection));
                            this.pieces.add(poolElementStructurePiece2);
                            if (i + 1 > this.maxDepth) continue block0;
                            this.placing.addLast(new PieceState(poolElementStructurePiece2, mutableObject3, i + 1));
                            continue block0;
                        }
                    }
                }
            }
        }
    }

    static final class PieceState {
        final PoolElementStructurePiece piece;
        final MutableObject<VoxelShape> free;
        final int depth;

        PieceState(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i) {
            this.piece = poolElementStructurePiece;
            this.free = mutableObject;
            this.depth = i;
        }
    }
}

