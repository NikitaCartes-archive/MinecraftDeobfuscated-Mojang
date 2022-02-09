package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
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
import org.slf4j.Logger;

public class JigsawPlacement {
	static final Logger LOGGER = LogUtils.getLogger();

	public static Optional<PieceGenerator<JigsawConfiguration>> addPieces(
		PieceGeneratorSupplier.Context<JigsawConfiguration> context, JigsawPlacement.PieceFactory pieceFactory, BlockPos blockPos, boolean bl, boolean bl2
	) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
		RegistryAccess registryAccess = context.registryAccess();
		JigsawConfiguration jigsawConfiguration = context.config();
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		StructureManager structureManager = context.structureManager();
		LevelHeightAccessor levelHeightAccessor = context.heightAccessor();
		Predicate<Holder<Biome>> predicate = context.validBiome();
		StructureFeature.bootstrap();
		Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
		Rotation rotation = Rotation.getRandom(worldgenRandom);
		StructureTemplatePool structureTemplatePool = jigsawConfiguration.startPool().value();
		StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom);
		if (structurePoolElement == EmptyPoolElement.INSTANCE) {
			return Optional.empty();
		} else {
			PoolElementStructurePiece poolElementStructurePiece = pieceFactory.create(
				structureManager,
				structurePoolElement,
				blockPos,
				structurePoolElement.getGroundLevelDelta(),
				rotation,
				structurePoolElement.getBoundingBox(structureManager, blockPos, rotation)
			);
			BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
			int i = (boundingBox.maxX() + boundingBox.minX()) / 2;
			int j = (boundingBox.maxZ() + boundingBox.minZ()) / 2;
			int k;
			if (bl2) {
				k = blockPos.getY() + chunkGenerator.getFirstFreeHeight(i, j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
			} else {
				k = blockPos.getY();
			}

			if (!predicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j)))) {
				return Optional.empty();
			} else {
				int l = boundingBox.minY() + poolElementStructurePiece.getGroundLevelDelta();
				poolElementStructurePiece.move(0, k - l, 0);
				return Optional.of(
					(PieceGenerator<>)(structurePiecesBuilder, contextx) -> {
						List<PoolElementStructurePiece> list = Lists.<PoolElementStructurePiece>newArrayList();
						list.add(poolElementStructurePiece);
						if (jigsawConfiguration.maxDepth() > 0) {
							int lx = 80;
							AABB aABB = new AABB((double)(i - 80), (double)(k - 80), (double)(j - 80), (double)(i + 80 + 1), (double)(k + 80 + 1), (double)(j + 80 + 1));
							JigsawPlacement.Placer placer = new JigsawPlacement.Placer(
								registry, jigsawConfiguration.maxDepth(), pieceFactory, chunkGenerator, structureManager, list, worldgenRandom
							);
							placer.placing
								.addLast(
									new JigsawPlacement.PieceState(
										poolElementStructurePiece, new MutableObject<>(Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST)), 0
									)
								);

							while (!placer.placing.isEmpty()) {
								JigsawPlacement.PieceState pieceState = (JigsawPlacement.PieceState)placer.placing.removeFirst();
								placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, bl, levelHeightAccessor);
							}

							list.forEach(structurePiecesBuilder::addPiece);
						}
					}
				);
			}
		}
	}

	public static void addPieces(
		RegistryAccess registryAccess,
		PoolElementStructurePiece poolElementStructurePiece,
		int i,
		JigsawPlacement.PieceFactory pieceFactory,
		ChunkGenerator chunkGenerator,
		StructureManager structureManager,
		List<? super PoolElementStructurePiece> list,
		Random random,
		LevelHeightAccessor levelHeightAccessor
	) {
		Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
		JigsawPlacement.Placer placer = new JigsawPlacement.Placer(registry, i, pieceFactory, chunkGenerator, structureManager, list, random);
		placer.placing.addLast(new JigsawPlacement.PieceState(poolElementStructurePiece, new MutableObject<>(Shapes.INFINITY), 0));

		while (!placer.placing.isEmpty()) {
			JigsawPlacement.PieceState pieceState = (JigsawPlacement.PieceState)placer.placing.removeFirst();
			placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, false, levelHeightAccessor);
		}
	}

	public interface PieceFactory {
		PoolElementStructurePiece create(
			StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, Rotation rotation, BoundingBox boundingBox
		);
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

	static final class Placer {
		private final Registry<StructureTemplatePool> pools;
		private final int maxDepth;
		private final JigsawPlacement.PieceFactory factory;
		private final ChunkGenerator chunkGenerator;
		private final StructureManager structureManager;
		private final List<? super PoolElementStructurePiece> pieces;
		private final Random random;
		final Deque<JigsawPlacement.PieceState> placing = Queues.<JigsawPlacement.PieceState>newArrayDeque();

		Placer(
			Registry<StructureTemplatePool> registry,
			int i,
			JigsawPlacement.PieceFactory pieceFactory,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			List<? super PoolElementStructurePiece> list,
			Random random
		) {
			this.pools = registry;
			this.maxDepth = i;
			this.factory = pieceFactory;
			this.chunkGenerator = chunkGenerator;
			this.structureManager = structureManager;
			this.pieces = list;
			this.random = random;
		}

		void tryPlacingChildren(
			PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i, boolean bl, LevelHeightAccessor levelHeightAccessor
		) {
			StructurePoolElement structurePoolElement = poolElementStructurePiece.getElement();
			BlockPos blockPos = poolElementStructurePiece.getPosition();
			Rotation rotation = poolElementStructurePiece.getRotation();
			StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
			boolean bl2 = projection == StructureTemplatePool.Projection.RIGID;
			MutableObject<VoxelShape> mutableObject2 = new MutableObject<>();
			BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
			int j = boundingBox.minY();

			label137:
			for (StructureTemplate.StructureBlockInfo structureBlockInfo : structurePoolElement.getShuffledJigsawBlocks(
				this.structureManager, blockPos, rotation, this.random
			)) {
				Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo.state);
				BlockPos blockPos2 = structureBlockInfo.pos;
				BlockPos blockPos3 = blockPos2.relative(direction);
				int k = blockPos2.getY() - j;
				int l = -1;
				ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfo.nbt.getString("pool"));
				Optional<StructureTemplatePool> optional = this.pools.getOptional(resourceLocation);
				if (optional.isPresent() && (((StructureTemplatePool)optional.get()).size() != 0 || Objects.equals(resourceLocation, Pools.EMPTY.location()))) {
					ResourceLocation resourceLocation2 = ((StructureTemplatePool)optional.get()).getFallback();
					Optional<StructureTemplatePool> optional2 = this.pools.getOptional(resourceLocation2);
					if (optional2.isPresent() && (((StructureTemplatePool)optional2.get()).size() != 0 || Objects.equals(resourceLocation2, Pools.EMPTY.location()))) {
						boolean bl3 = boundingBox.isInside(blockPos3);
						MutableObject<VoxelShape> mutableObject3;
						if (bl3) {
							mutableObject3 = mutableObject2;
							if (mutableObject2.getValue() == null) {
								mutableObject2.setValue(Shapes.create(AABB.of(boundingBox)));
							}
						} else {
							mutableObject3 = mutableObject;
						}

						List<StructurePoolElement> list = Lists.<StructurePoolElement>newArrayList();
						if (i != this.maxDepth) {
							list.addAll(((StructureTemplatePool)optional.get()).getShuffledTemplates(this.random));
						}

						list.addAll(((StructureTemplatePool)optional2.get()).getShuffledTemplates(this.random));

						for (StructurePoolElement structurePoolElement2 : list) {
							if (structurePoolElement2 == EmptyPoolElement.INSTANCE) {
								break;
							}

							for (Rotation rotation2 : Rotation.getShuffled(this.random)) {
								List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getShuffledJigsawBlocks(
									this.structureManager, BlockPos.ZERO, rotation2, this.random
								);
								BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation2);
								int m;
								if (bl && boundingBox2.getYSpan() <= 16) {
									m = list2.stream().mapToInt(structureBlockInfox -> {
										if (!boundingBox2.isInside(structureBlockInfox.pos.relative(JigsawBlock.getFrontFacing(structureBlockInfox.state)))) {
											return 0;
										} else {
											ResourceLocation resourceLocationx = new ResourceLocation(structureBlockInfox.nbt.getString("pool"));
											Optional<StructureTemplatePool> optionalx = this.pools.getOptional(resourceLocationx);
											Optional<StructureTemplatePool> optional2x = optionalx.flatMap(structureTemplatePool -> this.pools.getOptional(structureTemplatePool.getFallback()));
											int ix = (Integer)optionalx.map(structureTemplatePool -> structureTemplatePool.getMaxSize(this.structureManager)).orElse(0);
											int jx = (Integer)optional2x.map(structureTemplatePool -> structureTemplatePool.getMaxSize(this.structureManager)).orElse(0);
											return Math.max(ix, jx);
										}
									}).max().orElse(0);
								} else {
									m = 0;
								}

								for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : list2) {
									if (JigsawBlock.canAttach(structureBlockInfo, structureBlockInfo2)) {
										BlockPos blockPos4 = structureBlockInfo2.pos;
										BlockPos blockPos5 = blockPos3.subtract(blockPos4);
										BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureManager, blockPos5, rotation2);
										int n = boundingBox3.minY();
										StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
										boolean bl4 = projection2 == StructureTemplatePool.Projection.RIGID;
										int o = blockPos4.getY();
										int p = k - o + JigsawBlock.getFrontFacing(structureBlockInfo.state).getStepY();
										int q;
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
											int s = Math.max(m + 1, boundingBox4.maxY() - boundingBox4.minY());
											boundingBox4.encapsulate(new BlockPos(boundingBox4.minX(), boundingBox4.minY() + s, boundingBox4.minZ()));
										}

										if (!Shapes.joinIsNotEmpty(mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
											mutableObject3.setValue(Shapes.joinUnoptimized(mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4)), BooleanOp.ONLY_FIRST));
											int s = poolElementStructurePiece.getGroundLevelDelta();
											int t;
											if (bl4) {
												t = s - p;
											} else {
												t = structurePoolElement2.getGroundLevelDelta();
											}

											PoolElementStructurePiece poolElementStructurePiece2 = this.factory
												.create(this.structureManager, structurePoolElement2, blockPos6, t, rotation2, boundingBox4);
											int u;
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
											if (i + 1 <= this.maxDepth) {
												this.placing.addLast(new JigsawPlacement.PieceState(poolElementStructurePiece2, mutableObject3, i + 1));
											}
											continue label137;
										}
									}
								}
							}
						}
					} else {
						JigsawPlacement.LOGGER.warn("Empty or non-existent fallback pool: {}", resourceLocation2);
					}
				} else {
					JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", resourceLocation);
				}
			}
		}
	}
}
