package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Deque;
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
import net.minecraft.world.level.levelgen.feature.BastionPieces;
import net.minecraft.world.level.levelgen.feature.VillagePieces;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PillagerOutpostPieces;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureFeatureIO;
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

	public static void bootstrap() {
		BastionPieces.bootstrap();
		VillagePieces.bootstrap();
		PillagerOutpostPieces.bootstrap();
	}

	public static void addPieces(
		ResourceLocation resourceLocation,
		int i,
		JigsawPlacement.PieceFactory pieceFactory,
		ChunkGenerator<?> chunkGenerator,
		StructureManager structureManager,
		BlockPos blockPos,
		List<? super PoolElementStructurePiece> list,
		Random random,
		boolean bl,
		boolean bl2
	) {
		StructureFeatureIO.bootstrap();
		Rotation rotation = Rotation.getRandom(random);
		StructureTemplatePool structureTemplatePool = POOLS.getPool(resourceLocation);
		StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(random);
		PoolElementStructurePiece poolElementStructurePiece = pieceFactory.create(
			structureManager,
			structurePoolElement,
			blockPos,
			structurePoolElement.getGroundLevelDelta(),
			rotation,
			structurePoolElement.getBoundingBox(structureManager, blockPos, rotation)
		);
		BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
		int j = (boundingBox.x1 + boundingBox.x0) / 2;
		int k = (boundingBox.z1 + boundingBox.z0) / 2;
		int l;
		if (bl2) {
			l = blockPos.getY() + chunkGenerator.getFirstFreeHeight(j, k, Heightmap.Types.WORLD_SURFACE_WG);
		} else {
			l = blockPos.getY();
		}

		int m = boundingBox.y0 + poolElementStructurePiece.getGroundLevelDelta();
		poolElementStructurePiece.move(0, l - m, 0);
		list.add(poolElementStructurePiece);
		if (i > 0) {
			int n = 80;
			AABB aABB = new AABB((double)(j - 80), (double)(l - 80), (double)(k - 80), (double)(j + 80 + 1), (double)(l + 80 + 1), (double)(k + 80 + 1));
			JigsawPlacement.Placer placer = new JigsawPlacement.Placer(i, pieceFactory, chunkGenerator, structureManager, list, random);
			placer.placing
				.addLast(
					new JigsawPlacement.PieceState(
						poolElementStructurePiece, new AtomicReference(Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST)), l + 80, 0
					)
				);

			while (!placer.placing.isEmpty()) {
				JigsawPlacement.PieceState pieceState = (JigsawPlacement.PieceState)placer.placing.removeFirst();
				placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.boundsTop, pieceState.depth, bl);
			}
		}
	}

	public static void addPieces(
		PoolElementStructurePiece poolElementStructurePiece,
		int i,
		JigsawPlacement.PieceFactory pieceFactory,
		ChunkGenerator<?> chunkGenerator,
		StructureManager structureManager,
		List<? super PoolElementStructurePiece> list,
		Random random
	) {
		bootstrap();
		JigsawPlacement.Placer placer = new JigsawPlacement.Placer(i, pieceFactory, chunkGenerator, structureManager, list, random);
		placer.placing.addLast(new JigsawPlacement.PieceState(poolElementStructurePiece, new AtomicReference(Shapes.INFINITY), 0, 0));

		while (!placer.placing.isEmpty()) {
			JigsawPlacement.PieceState pieceState = (JigsawPlacement.PieceState)placer.placing.removeFirst();
			placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.boundsTop, pieceState.depth, false);
		}
	}

	static {
		POOLS.register(StructureTemplatePool.EMPTY);
	}

	public interface PieceFactory {
		PoolElementStructurePiece create(
			StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, Rotation rotation, BoundingBox boundingBox
		);
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

	static final class Placer {
		private final int maxDepth;
		private final JigsawPlacement.PieceFactory factory;
		private final ChunkGenerator<?> chunkGenerator;
		private final StructureManager structureManager;
		private final List<? super PoolElementStructurePiece> pieces;
		private final Random random;
		private final Deque<JigsawPlacement.PieceState> placing = Queues.<JigsawPlacement.PieceState>newArrayDeque();

		private Placer(
			int i,
			JigsawPlacement.PieceFactory pieceFactory,
			ChunkGenerator<?> chunkGenerator,
			StructureManager structureManager,
			List<? super PoolElementStructurePiece> list,
			Random random
		) {
			this.maxDepth = i;
			this.factory = pieceFactory;
			this.chunkGenerator = chunkGenerator;
			this.structureManager = structureManager;
			this.pieces = list;
			this.random = random;
		}

		private void tryPlacingChildren(PoolElementStructurePiece poolElementStructurePiece, AtomicReference<VoxelShape> atomicReference, int i, int j, boolean bl) {
			StructurePoolElement structurePoolElement = poolElementStructurePiece.getElement();
			BlockPos blockPos = poolElementStructurePiece.getPosition();
			Rotation rotation = poolElementStructurePiece.getRotation();
			StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
			boolean bl2 = projection == StructureTemplatePool.Projection.RIGID;
			AtomicReference<VoxelShape> atomicReference2 = new AtomicReference();
			BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
			int k = boundingBox.y0;

			label125:
			for (StructureTemplate.StructureBlockInfo structureBlockInfo : structurePoolElement.getShuffledJigsawBlocks(
				this.structureManager, blockPos, rotation, this.random
			)) {
				Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo.state);
				BlockPos blockPos2 = structureBlockInfo.pos;
				BlockPos blockPos3 = blockPos2.relative(direction);
				int l = blockPos2.getY() - k;
				int m = -1;
				StructureTemplatePool structureTemplatePool = JigsawPlacement.POOLS.getPool(new ResourceLocation(structureBlockInfo.nbt.getString("pool")));
				StructureTemplatePool structureTemplatePool2 = JigsawPlacement.POOLS.getPool(structureTemplatePool.getFallback());
				if (structureTemplatePool != StructureTemplatePool.INVALID && (structureTemplatePool.size() != 0 || structureTemplatePool == StructureTemplatePool.EMPTY)) {
					boolean bl3 = boundingBox.isInside(blockPos3);
					AtomicReference<VoxelShape> atomicReference3;
					int n;
					if (bl3) {
						atomicReference3 = atomicReference2;
						n = k;
						if (atomicReference2.get() == null) {
							atomicReference2.set(Shapes.create(AABB.of(boundingBox)));
						}
					} else {
						atomicReference3 = atomicReference;
						n = i;
					}

					List<StructurePoolElement> list = Lists.<StructurePoolElement>newArrayList();
					if (j != this.maxDepth) {
						list.addAll(structureTemplatePool.getShuffledTemplates(this.random));
					}

					list.addAll(structureTemplatePool2.getShuffledTemplates(this.random));

					for (StructurePoolElement structurePoolElement2 : list) {
						if (structurePoolElement2 == EmptyPoolElement.INSTANCE) {
							break;
						}

						for (Rotation rotation2 : Rotation.getShuffled(this.random)) {
							List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getShuffledJigsawBlocks(
								this.structureManager, BlockPos.ZERO, rotation2, this.random
							);
							BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation2);
							int o;
							if (bl && boundingBox2.getYSpan() <= 16) {
								o = list2.stream().mapToInt(structureBlockInfox -> {
									if (!boundingBox2.isInside(structureBlockInfox.pos.relative(JigsawBlock.getFrontFacing(structureBlockInfox.state)))) {
										return 0;
									} else {
										ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfox.nbt.getString("pool"));
										StructureTemplatePool structureTemplatePoolx = JigsawPlacement.POOLS.getPool(resourceLocation);
										StructureTemplatePool structureTemplatePool2x = JigsawPlacement.POOLS.getPool(structureTemplatePoolx.getFallback());
										return Math.max(structureTemplatePoolx.getMaxSize(this.structureManager), structureTemplatePool2x.getMaxSize(this.structureManager));
									}
								}).max().orElse(0);
							} else {
								o = 0;
							}

							for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : list2) {
								if (JigsawBlock.canAttach(structureBlockInfo, structureBlockInfo2)) {
									BlockPos blockPos4 = structureBlockInfo2.pos;
									BlockPos blockPos5 = new BlockPos(blockPos3.getX() - blockPos4.getX(), blockPos3.getY() - blockPos4.getY(), blockPos3.getZ() - blockPos4.getZ());
									BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureManager, blockPos5, rotation2);
									int p = boundingBox3.y0;
									StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
									boolean bl4 = projection2 == StructureTemplatePool.Projection.RIGID;
									int q = blockPos4.getY();
									int r = l - q + JigsawBlock.getFrontFacing(structureBlockInfo.state).getStepY();
									int s;
									if (bl2 && bl4) {
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
										int u = Math.max(o + 1, boundingBox4.y1 - boundingBox4.y0);
										boundingBox4.y1 = boundingBox4.y0 + u;
									}

									if (!Shapes.joinIsNotEmpty((VoxelShape)atomicReference3.get(), Shapes.create(AABB.of(boundingBox4).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
										atomicReference3.set(Shapes.joinUnoptimized((VoxelShape)atomicReference3.get(), Shapes.create(AABB.of(boundingBox4)), BooleanOp.ONLY_FIRST));
										int u = poolElementStructurePiece.getGroundLevelDelta();
										int v;
										if (bl4) {
											v = u - r;
										} else {
											v = structurePoolElement2.getGroundLevelDelta();
										}

										PoolElementStructurePiece poolElementStructurePiece2 = this.factory
											.create(this.structureManager, structurePoolElement2, blockPos6, v, rotation2, boundingBox4);
										int w;
										if (bl2) {
											w = k + l;
										} else if (bl4) {
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
										if (j + 1 <= this.maxDepth) {
											this.placing.addLast(new JigsawPlacement.PieceState(poolElementStructurePiece2, atomicReference3, n, j + 1));
										}
										continue label125;
									}
								}
							}
						}
					}
				} else {
					JigsawPlacement.LOGGER.warn("Empty or none existent pool: {}", structureBlockInfo.nbt.getString("pool"));
				}
			}
		}
	}
}
