/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class JigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<Structure.GenerationStub> addPieces(Structure.GenerationContext generationContext, Holder<StructureTemplatePool> holder, Optional<ResourceLocation> optional, int i, BlockPos blockPos, boolean bl, Optional<Heightmap.Types> optional2, int j) {
        BlockPos blockPos2;
        RegistryAccess registryAccess = generationContext.registryAccess();
        ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
        StructureTemplateManager structureTemplateManager = generationContext.structureTemplateManager();
        LevelHeightAccessor levelHeightAccessor = generationContext.heightAccessor();
        WorldgenRandom worldgenRandom = generationContext.random();
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Rotation rotation = Rotation.getRandom(worldgenRandom);
        StructureTemplatePool structureTemplatePool = holder.value();
        StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        if (optional.isPresent()) {
            ResourceLocation resourceLocation = optional.get();
            Optional<BlockPos> optional3 = JigsawPlacement.getRandomNamedJigsaw(structurePoolElement, resourceLocation, blockPos, rotation, structureTemplateManager, worldgenRandom);
            if (optional3.isEmpty()) {
                LOGGER.error("No starting jigsaw {} found in start pool {}", (Object)resourceLocation, (Object)holder.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("<unregistered>"));
                return Optional.empty();
            }
            blockPos2 = optional3.get();
        } else {
            blockPos2 = blockPos;
        }
        BlockPos vec3i = blockPos2.subtract(blockPos);
        BlockPos blockPos3 = blockPos.subtract(vec3i);
        PoolElementStructurePiece poolElementStructurePiece = new PoolElementStructurePiece(structureTemplateManager, structurePoolElement, blockPos3, structurePoolElement.getGroundLevelDelta(), rotation, structurePoolElement.getBoundingBox(structureTemplateManager, blockPos3, rotation));
        BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
        int k = (boundingBox.maxX() + boundingBox.minX()) / 2;
        int l = (boundingBox.maxZ() + boundingBox.minZ()) / 2;
        int m = optional2.isPresent() ? blockPos.getY() + chunkGenerator.getFirstFreeHeight(k, l, optional2.get(), levelHeightAccessor, generationContext.randomState()) : blockPos3.getY();
        int n = boundingBox.minY() + poolElementStructurePiece.getGroundLevelDelta();
        poolElementStructurePiece.move(0, m - n, 0);
        int o = m + vec3i.getY();
        return Optional.of(new Structure.GenerationStub(new BlockPos(k, o, l), structurePiecesBuilder -> {
            ArrayList<PoolElementStructurePiece> list = Lists.newArrayList();
            list.add(poolElementStructurePiece);
            if (i <= 0) {
                return;
            }
            AABB aABB = new AABB(k - j, o - j, l - j, k + j + 1, o + j + 1, l + j + 1);
            VoxelShape voxelShape = Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST);
            JigsawPlacement.addPieces(generationContext.randomState(), i, bl, chunkGenerator, structureTemplateManager, levelHeightAccessor, worldgenRandom, registry, poolElementStructurePiece, list, voxelShape);
            list.forEach(structurePiecesBuilder::addPiece);
        }));
    }

    private static Optional<BlockPos> getRandomNamedJigsaw(StructurePoolElement structurePoolElement, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, StructureTemplateManager structureTemplateManager, WorldgenRandom worldgenRandom) {
        List<StructureTemplate.StructureBlockInfo> list = structurePoolElement.getShuffledJigsawBlocks(structureTemplateManager, blockPos, rotation, worldgenRandom);
        Optional<BlockPos> optional = Optional.empty();
        for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
            ResourceLocation resourceLocation2 = ResourceLocation.tryParse(structureBlockInfo.nbt.getString("name"));
            if (!resourceLocation.equals(resourceLocation2)) continue;
            optional = Optional.of(structureBlockInfo.pos);
            break;
        }
        return optional;
    }

    private static void addPieces(RandomState randomState, int i, boolean bl, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, LevelHeightAccessor levelHeightAccessor, RandomSource randomSource, Registry<StructureTemplatePool> registry, PoolElementStructurePiece poolElementStructurePiece, List<PoolElementStructurePiece> list, VoxelShape voxelShape) {
        Placer placer = new Placer(registry, i, chunkGenerator, structureTemplateManager, list, randomSource);
        placer.placing.addLast(new PieceState(poolElementStructurePiece, new MutableObject<VoxelShape>(voxelShape), 0));
        while (!placer.placing.isEmpty()) {
            PieceState pieceState = placer.placing.removeFirst();
            placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, bl, levelHeightAccessor, randomState);
        }
    }

    public static boolean generateJigsaw(ServerLevel serverLevel, Holder<StructureTemplatePool> holder2, ResourceLocation resourceLocation, int i, BlockPos blockPos, boolean bl) {
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
        StructureManager structureManager = serverLevel.structureManager();
        RandomSource randomSource = serverLevel.getRandom();
        Structure.GenerationContext generationContext = new Structure.GenerationContext(serverLevel.registryAccess(), chunkGenerator, chunkGenerator.getBiomeSource(), serverLevel.getChunkSource().randomState(), structureTemplateManager, serverLevel.getSeed(), new ChunkPos(blockPos), serverLevel, holder -> true);
        Optional<Structure.GenerationStub> optional = JigsawPlacement.addPieces(generationContext, holder2, Optional.of(resourceLocation), i, blockPos, false, Optional.empty(), 128);
        if (optional.isPresent()) {
            StructurePiecesBuilder structurePiecesBuilder = optional.get().getPiecesBuilder();
            for (StructurePiece structurePiece : structurePiecesBuilder.build().pieces()) {
                if (!(structurePiece instanceof PoolElementStructurePiece)) continue;
                PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
                poolElementStructurePiece.place(serverLevel, structureManager, chunkGenerator, randomSource, BoundingBox.infinite(), blockPos, bl);
            }
            return true;
        }
        return false;
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final RandomSource random;
        final Deque<PieceState> placing = Queues.newArrayDeque();

        Placer(Registry<StructureTemplatePool> registry, int i, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, List<? super PoolElementStructurePiece> list, RandomSource randomSource) {
            this.pools = registry;
            this.maxDepth = i;
            this.chunkGenerator = chunkGenerator;
            this.structureTemplateManager = structureTemplateManager;
            this.pieces = list;
            this.random = randomSource;
        }

        void tryPlacingChildren(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i, boolean bl, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
            StructurePoolElement structurePoolElement = poolElementStructurePiece.getElement();
            BlockPos blockPos = poolElementStructurePiece.getPosition();
            Rotation rotation = poolElementStructurePiece.getRotation();
            StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
            boolean bl2 = projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject2 = new MutableObject<VoxelShape>();
            BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
            int j = boundingBox.minY();
            block0: for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : structurePoolElement.getShuffledJigsawBlocks(this.structureTemplateManager, blockPos, rotation, this.random)) {
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
                        List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getShuffledJigsawBlocks(this.structureTemplateManager, BlockPos.ZERO, rotation2, this.random);
                        BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, rotation2);
                        int m = !bl || boundingBox2.getYSpan() > 16 ? 0 : list2.stream().mapToInt(structureBlockInfo -> {
                            if (!boundingBox2.isInside(structureBlockInfo.pos.relative(JigsawBlock.getFrontFacing(structureBlockInfo.state)))) {
                                return 0;
                            }
                            ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfo.nbt.getString("pool"));
                            Optional<StructureTemplatePool> optional = this.pools.getOptional(resourceLocation);
                            Optional<Integer> optional2 = optional.flatMap(structureTemplatePool -> this.pools.getOptional(structureTemplatePool.getFallback()));
                            int i = optional.map(structureTemplatePool -> structureTemplatePool.getMaxSize(this.structureTemplateManager)).orElse(0);
                            int j = optional2.map(structureTemplatePool -> structureTemplatePool.getMaxSize(this.structureTemplateManager)).orElse(0);
                            return Math.max(i, j);
                        }).max().orElse(0);
                        for (StructureTemplate.StructureBlockInfo structureBlockInfo22 : list2) {
                            int u;
                            int s;
                            int q;
                            if (!JigsawBlock.canAttach(structureBlockInfo2, structureBlockInfo22)) continue;
                            BlockPos blockPos4 = structureBlockInfo22.pos;
                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                            BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, blockPos5, rotation2);
                            int n = boundingBox3.minY();
                            StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl4 = projection2 == StructureTemplatePool.Projection.RIGID;
                            int o = blockPos4.getY();
                            int p = k - o + JigsawBlock.getFrontFacing(structureBlockInfo2.state).getStepY();
                            if (bl2 && bl4) {
                                q = j + p;
                            } else {
                                if (l == -1) {
                                    l = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
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
                            PoolElementStructurePiece poolElementStructurePiece2 = new PoolElementStructurePiece(this.structureTemplateManager, structurePoolElement2, blockPos6, t, rotation2, boundingBox4);
                            if (bl2) {
                                u = j + k;
                            } else if (bl4) {
                                u = q + o;
                            } else {
                                if (l == -1) {
                                    l = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
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

