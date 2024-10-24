package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SequencedPriorityIterator;
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
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
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
	private static final int UNSET_HEIGHT = Integer.MIN_VALUE;

	public static Optional<Structure.GenerationStub> addPieces(
		Structure.GenerationContext generationContext,
		Holder<StructureTemplatePool> holder,
		Optional<ResourceLocation> optional,
		int i,
		BlockPos blockPos,
		boolean bl,
		Optional<Heightmap.Types> optional2,
		int j,
		PoolAliasLookup poolAliasLookup,
		DimensionPadding dimensionPadding,
		LiquidSettings liquidSettings
	) {
		RegistryAccess registryAccess = generationContext.registryAccess();
		ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
		StructureTemplateManager structureTemplateManager = generationContext.structureTemplateManager();
		LevelHeightAccessor levelHeightAccessor = generationContext.heightAccessor();
		WorldgenRandom worldgenRandom = generationContext.random();
		Registry<StructureTemplatePool> registry = registryAccess.lookupOrThrow(Registries.TEMPLATE_POOL);
		Rotation rotation = Rotation.getRandom(worldgenRandom);
		StructureTemplatePool structureTemplatePool = (StructureTemplatePool)holder.unwrapKey()
			.flatMap(resourceKey -> registry.getOptional(poolAliasLookup.lookup(resourceKey)))
			.orElse(holder.value());
		StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom);
		if (structurePoolElement == EmptyPoolElement.INSTANCE) {
			return Optional.empty();
		} else {
			BlockPos blockPos2;
			if (optional.isPresent()) {
				ResourceLocation resourceLocation = (ResourceLocation)optional.get();
				Optional<BlockPos> optional3 = getRandomNamedJigsaw(structurePoolElement, resourceLocation, blockPos, rotation, structureTemplateManager, worldgenRandom);
				if (optional3.isEmpty()) {
					LOGGER.error(
						"No starting jigsaw {} found in start pool {}",
						resourceLocation,
						holder.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("<unregistered>")
					);
					return Optional.empty();
				}

				blockPos2 = (BlockPos)optional3.get();
			} else {
				blockPos2 = blockPos;
			}

			Vec3i vec3i = blockPos2.subtract(blockPos);
			BlockPos blockPos3 = blockPos.subtract(vec3i);
			PoolElementStructurePiece poolElementStructurePiece = new PoolElementStructurePiece(
				structureTemplateManager,
				structurePoolElement,
				blockPos3,
				structurePoolElement.getGroundLevelDelta(),
				rotation,
				structurePoolElement.getBoundingBox(structureTemplateManager, blockPos3, rotation),
				liquidSettings
			);
			BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
			int k = (boundingBox.maxX() + boundingBox.minX()) / 2;
			int l = (boundingBox.maxZ() + boundingBox.minZ()) / 2;
			int m;
			if (optional2.isPresent()) {
				m = blockPos.getY() + chunkGenerator.getFirstFreeHeight(k, l, (Heightmap.Types)optional2.get(), levelHeightAccessor, generationContext.randomState());
			} else {
				m = blockPos3.getY();
			}

			int n = boundingBox.minY() + poolElementStructurePiece.getGroundLevelDelta();
			poolElementStructurePiece.move(0, m - n, 0);
			int o = m + vec3i.getY();
			return Optional.of(
				new Structure.GenerationStub(
					new BlockPos(k, o, l),
					structurePiecesBuilder -> {
						List<PoolElementStructurePiece> list = Lists.<PoolElementStructurePiece>newArrayList();
						list.add(poolElementStructurePiece);
						if (i > 0) {
							AABB aABB = new AABB(
								(double)(k - j),
								(double)Math.max(o - j, levelHeightAccessor.getMinY() + dimensionPadding.bottom()),
								(double)(l - j),
								(double)(k + j + 1),
								(double)Math.min(o + j + 1, levelHeightAccessor.getMaxY() + 1 - dimensionPadding.top()),
								(double)(l + j + 1)
							);
							VoxelShape voxelShape = Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST);
							addPieces(
								generationContext.randomState(),
								i,
								bl,
								chunkGenerator,
								structureTemplateManager,
								levelHeightAccessor,
								worldgenRandom,
								registry,
								poolElementStructurePiece,
								list,
								voxelShape,
								poolAliasLookup,
								liquidSettings
							);
							list.forEach(structurePiecesBuilder::addPiece);
						}
					}
				)
			);
		}
	}

	private static Optional<BlockPos> getRandomNamedJigsaw(
		StructurePoolElement structurePoolElement,
		ResourceLocation resourceLocation,
		BlockPos blockPos,
		Rotation rotation,
		StructureTemplateManager structureTemplateManager,
		WorldgenRandom worldgenRandom
	) {
		for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo : structurePoolElement.getShuffledJigsawBlocks(
			structureTemplateManager, blockPos, rotation, worldgenRandom
		)) {
			if (resourceLocation.equals(jigsawBlockInfo.name())) {
				return Optional.of(jigsawBlockInfo.info().pos());
			}
		}

		return Optional.empty();
	}

	private static void addPieces(
		RandomState randomState,
		int i,
		boolean bl,
		ChunkGenerator chunkGenerator,
		StructureTemplateManager structureTemplateManager,
		LevelHeightAccessor levelHeightAccessor,
		RandomSource randomSource,
		Registry<StructureTemplatePool> registry,
		PoolElementStructurePiece poolElementStructurePiece,
		List<PoolElementStructurePiece> list,
		VoxelShape voxelShape,
		PoolAliasLookup poolAliasLookup,
		LiquidSettings liquidSettings
	) {
		JigsawPlacement.Placer placer = new JigsawPlacement.Placer(registry, i, chunkGenerator, structureTemplateManager, list, randomSource);
		placer.tryPlacingChildren(
			poolElementStructurePiece, new MutableObject<>(voxelShape), 0, bl, levelHeightAccessor, randomState, poolAliasLookup, liquidSettings
		);

		while (placer.placing.hasNext()) {
			JigsawPlacement.PieceState pieceState = placer.placing.next();
			placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, bl, levelHeightAccessor, randomState, poolAliasLookup, liquidSettings);
		}
	}

	public static boolean generateJigsaw(
		ServerLevel serverLevel, Holder<StructureTemplatePool> holder, ResourceLocation resourceLocation, int i, BlockPos blockPos, boolean bl
	) {
		ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
		StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
		StructureManager structureManager = serverLevel.structureManager();
		RandomSource randomSource = serverLevel.getRandom();
		Structure.GenerationContext generationContext = new Structure.GenerationContext(
			serverLevel.registryAccess(),
			chunkGenerator,
			chunkGenerator.getBiomeSource(),
			serverLevel.getChunkSource().randomState(),
			structureTemplateManager,
			serverLevel.getSeed(),
			new ChunkPos(blockPos),
			serverLevel,
			holderx -> true
		);
		Optional<Structure.GenerationStub> optional = addPieces(
			generationContext,
			holder,
			Optional.of(resourceLocation),
			i,
			blockPos,
			false,
			Optional.empty(),
			128,
			PoolAliasLookup.EMPTY,
			JigsawStructure.DEFAULT_DIMENSION_PADDING,
			JigsawStructure.DEFAULT_LIQUID_SETTINGS
		);
		if (optional.isPresent()) {
			StructurePiecesBuilder structurePiecesBuilder = ((Structure.GenerationStub)optional.get()).getPiecesBuilder();

			for (StructurePiece structurePiece : structurePiecesBuilder.build().pieces()) {
				if (structurePiece instanceof PoolElementStructurePiece poolElementStructurePiece) {
					poolElementStructurePiece.place(serverLevel, structureManager, chunkGenerator, randomSource, BoundingBox.infinite(), blockPos, bl);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	static record PieceState(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth) {
	}

	static final class Placer {
		private final Registry<StructureTemplatePool> pools;
		private final int maxDepth;
		private final ChunkGenerator chunkGenerator;
		private final StructureTemplateManager structureTemplateManager;
		private final List<? super PoolElementStructurePiece> pieces;
		private final RandomSource random;
		final SequencedPriorityIterator<JigsawPlacement.PieceState> placing = new SequencedPriorityIterator<>();

		Placer(
			Registry<StructureTemplatePool> registry,
			int i,
			ChunkGenerator chunkGenerator,
			StructureTemplateManager structureTemplateManager,
			List<? super PoolElementStructurePiece> list,
			RandomSource randomSource
		) {
			this.pools = registry;
			this.maxDepth = i;
			this.chunkGenerator = chunkGenerator;
			this.structureTemplateManager = structureTemplateManager;
			this.pieces = list;
			this.random = randomSource;
		}

		void tryPlacingChildren(
			PoolElementStructurePiece poolElementStructurePiece,
			MutableObject<VoxelShape> mutableObject,
			int i,
			boolean bl,
			LevelHeightAccessor levelHeightAccessor,
			RandomState randomState,
			PoolAliasLookup poolAliasLookup,
			LiquidSettings liquidSettings
		) {
			StructurePoolElement structurePoolElement = poolElementStructurePiece.getElement();
			BlockPos blockPos = poolElementStructurePiece.getPosition();
			Rotation rotation = poolElementStructurePiece.getRotation();
			StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
			boolean bl2 = projection == StructureTemplatePool.Projection.RIGID;
			MutableObject<VoxelShape> mutableObject2 = new MutableObject<>();
			BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
			int j = boundingBox.minY();

			label129:
			for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo : structurePoolElement.getShuffledJigsawBlocks(
				this.structureTemplateManager, blockPos, rotation, this.random
			)) {
				StructureTemplate.StructureBlockInfo structureBlockInfo = jigsawBlockInfo.info();
				Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo.state());
				BlockPos blockPos2 = structureBlockInfo.pos();
				BlockPos blockPos3 = blockPos2.relative(direction);
				int k = blockPos2.getY() - j;
				int l = Integer.MIN_VALUE;
				ResourceKey<StructureTemplatePool> resourceKey = readPoolKey(jigsawBlockInfo, poolAliasLookup);
				Optional<? extends Holder<StructureTemplatePool>> optional = this.pools.get(resourceKey);
				if (optional.isEmpty()) {
					JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", resourceKey.location());
				} else {
					Holder<StructureTemplatePool> holder = (Holder<StructureTemplatePool>)optional.get();
					if (holder.value().size() == 0 && !holder.is(Pools.EMPTY)) {
						JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", resourceKey.location());
					} else {
						Holder<StructureTemplatePool> holder2 = holder.value().getFallback();
						if (holder2.value().size() == 0 && !holder2.is(Pools.EMPTY)) {
							JigsawPlacement.LOGGER
								.warn("Empty or non-existent fallback pool: {}", holder2.unwrapKey().map(resourceKeyx -> resourceKeyx.location().toString()).orElse("<unregistered>"));
						} else {
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
								list.addAll(holder.value().getShuffledTemplates(this.random));
							}

							list.addAll(holder2.value().getShuffledTemplates(this.random));
							int m = jigsawBlockInfo.placementPriority();

							for (StructurePoolElement structurePoolElement2 : list) {
								if (structurePoolElement2 == EmptyPoolElement.INSTANCE) {
									break;
								}

								for (Rotation rotation2 : Rotation.getShuffled(this.random)) {
									List<StructureTemplate.JigsawBlockInfo> list2 = structurePoolElement2.getShuffledJigsawBlocks(
										this.structureTemplateManager, BlockPos.ZERO, rotation2, this.random
									);
									BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, rotation2);
									int n;
									if (bl && boundingBox2.getYSpan() <= 16) {
										n = list2.stream().mapToInt(jigsawBlockInfox -> {
											StructureTemplate.StructureBlockInfo structureBlockInfox = jigsawBlockInfox.info();
											if (!boundingBox2.isInside(structureBlockInfox.pos().relative(JigsawBlock.getFrontFacing(structureBlockInfox.state())))) {
												return 0;
											} else {
												ResourceKey<StructureTemplatePool> resourceKeyx = readPoolKey(jigsawBlockInfox, poolAliasLookup);
												Optional<? extends Holder<StructureTemplatePool>> optionalx = this.pools.get(resourceKeyx);
												Optional<Holder<StructureTemplatePool>> optional2 = optionalx.map(holderx -> ((StructureTemplatePool)holderx.value()).getFallback());
												int ix = (Integer)optionalx.map(holderx -> ((StructureTemplatePool)holderx.value()).getMaxSize(this.structureTemplateManager)).orElse(0);
												int jx = (Integer)optional2.map(holderx -> ((StructureTemplatePool)holderx.value()).getMaxSize(this.structureTemplateManager)).orElse(0);
												return Math.max(ix, jx);
											}
										}).max().orElse(0);
									} else {
										n = 0;
									}

									for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo2 : list2) {
										if (JigsawBlock.canAttach(jigsawBlockInfo, jigsawBlockInfo2)) {
											BlockPos blockPos4 = jigsawBlockInfo2.info().pos();
											BlockPos blockPos5 = blockPos3.subtract(blockPos4);
											BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, blockPos5, rotation2);
											int o = boundingBox3.minY();
											StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
											boolean bl4 = projection2 == StructureTemplatePool.Projection.RIGID;
											int p = blockPos4.getY();
											int q = k - p + JigsawBlock.getFrontFacing(structureBlockInfo.state()).getStepY();
											int r;
											if (bl2 && bl4) {
												r = j + q;
											} else {
												if (l == Integer.MIN_VALUE) {
													l = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
												}

												r = l - p;
											}

											int s = r - o;
											BoundingBox boundingBox4 = boundingBox3.moved(0, s, 0);
											BlockPos blockPos6 = blockPos5.offset(0, s, 0);
											if (n > 0) {
												int t = Math.max(n + 1, boundingBox4.maxY() - boundingBox4.minY());
												boundingBox4.encapsulate(new BlockPos(boundingBox4.minX(), boundingBox4.minY() + t, boundingBox4.minZ()));
											}

											if (!Shapes.joinIsNotEmpty(mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
												mutableObject3.setValue(Shapes.joinUnoptimized(mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4)), BooleanOp.ONLY_FIRST));
												int t = poolElementStructurePiece.getGroundLevelDelta();
												int u;
												if (bl4) {
													u = t - q;
												} else {
													u = structurePoolElement2.getGroundLevelDelta();
												}

												PoolElementStructurePiece poolElementStructurePiece2 = new PoolElementStructurePiece(
													this.structureTemplateManager, structurePoolElement2, blockPos6, u, rotation2, boundingBox4, liquidSettings
												);
												int v;
												if (bl2) {
													v = j + k;
												} else if (bl4) {
													v = r + p;
												} else {
													if (l == Integer.MIN_VALUE) {
														l = this.chunkGenerator
															.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
													}

													v = l + q / 2;
												}

												poolElementStructurePiece.addJunction(new JigsawJunction(blockPos3.getX(), v - k + t, blockPos3.getZ(), q, projection2));
												poolElementStructurePiece2.addJunction(new JigsawJunction(blockPos2.getX(), v - p + u, blockPos2.getZ(), -q, projection));
												this.pieces.add(poolElementStructurePiece2);
												if (i + 1 <= this.maxDepth) {
													JigsawPlacement.PieceState pieceState = new JigsawPlacement.PieceState(poolElementStructurePiece2, mutableObject3, i + 1);
													this.placing.add(pieceState, m);
												}
												continue label129;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		private static ResourceKey<StructureTemplatePool> readPoolKey(StructureTemplate.JigsawBlockInfo jigsawBlockInfo, PoolAliasLookup poolAliasLookup) {
			return poolAliasLookup.lookup(Pools.createKey(jigsawBlockInfo.pool()));
		}
	}
}
