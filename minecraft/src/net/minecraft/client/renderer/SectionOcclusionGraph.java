package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SectionOcclusionGraph {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Direction[] DIRECTIONS = Direction.values();
	private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
	private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0) * 16.0);
	private boolean needsFullUpdate = true;
	@Nullable
	private Future<?> fullUpdateTask;
	@Nullable
	private ViewArea viewArea;
	private final AtomicReference<SectionOcclusionGraph.GraphState> currentGraph = new AtomicReference();
	private final AtomicReference<SectionOcclusionGraph.GraphEvents> nextGraphEvents = new AtomicReference();
	private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);

	public void waitAndReset(@Nullable ViewArea viewArea) {
		if (this.fullUpdateTask != null) {
			try {
				this.fullUpdateTask.get();
				this.fullUpdateTask = null;
			} catch (Exception var3) {
				LOGGER.warn("Full update failed", (Throwable)var3);
			}
		}

		this.viewArea = viewArea;
		if (viewArea != null) {
			this.currentGraph.set(new SectionOcclusionGraph.GraphState(viewArea.sections.length));
			this.invalidate();
		} else {
			this.currentGraph.set(null);
		}
	}

	public void invalidate() {
		this.needsFullUpdate = true;
	}

	public void addSectionsInFrustum(Frustum frustum, List<SectionRenderDispatcher.RenderSection> list) {
		for (SectionOcclusionGraph.Node node : ((SectionOcclusionGraph.GraphState)this.currentGraph.get()).storage().renderSections) {
			if (frustum.isVisible(node.section.getBoundingBox())) {
				list.add(node.section);
			}
		}
	}

	public boolean consumeFrustumUpdate() {
		return this.needsFrustumUpdate.compareAndSet(true, false);
	}

	public void onChunkLoaded(ChunkPos chunkPos) {
		SectionOcclusionGraph.GraphEvents graphEvents = (SectionOcclusionGraph.GraphEvents)this.nextGraphEvents.get();
		if (graphEvents != null) {
			this.addNeighbors(graphEvents, chunkPos);
		}

		SectionOcclusionGraph.GraphEvents graphEvents2 = ((SectionOcclusionGraph.GraphState)this.currentGraph.get()).events;
		if (graphEvents2 != graphEvents) {
			this.addNeighbors(graphEvents2, chunkPos);
		}
	}

	public void onSectionCompiled(SectionRenderDispatcher.RenderSection renderSection) {
		SectionOcclusionGraph.GraphEvents graphEvents = (SectionOcclusionGraph.GraphEvents)this.nextGraphEvents.get();
		if (graphEvents != null) {
			graphEvents.sectionsToPropagateFrom.add(renderSection);
		}

		SectionOcclusionGraph.GraphEvents graphEvents2 = ((SectionOcclusionGraph.GraphState)this.currentGraph.get()).events;
		if (graphEvents2 != graphEvents) {
			graphEvents2.sectionsToPropagateFrom.add(renderSection);
		}
	}

	public void update(boolean bl, Camera camera, Frustum frustum, List<SectionRenderDispatcher.RenderSection> list) {
		Vec3 vec3 = camera.getPosition();
		if (this.needsFullUpdate && (this.fullUpdateTask == null || this.fullUpdateTask.isDone())) {
			this.scheduleFullUpdate(bl, camera, vec3);
		}

		this.runPartialUpdate(bl, frustum, list, vec3);
	}

	private void scheduleFullUpdate(boolean bl, Camera camera, Vec3 vec3) {
		this.needsFullUpdate = false;
		this.fullUpdateTask = Util.backgroundExecutor().submit(() -> {
			SectionOcclusionGraph.GraphState graphState = new SectionOcclusionGraph.GraphState(this.viewArea.sections.length);
			this.nextGraphEvents.set(graphState.events);
			Queue<SectionOcclusionGraph.Node> queue = Queues.<SectionOcclusionGraph.Node>newArrayDeque();
			this.initializeQueueForFullUpdate(camera, queue);
			queue.forEach(node -> graphState.storage.sectionToNodeMap.put(node.section, node));
			this.runUpdates(graphState.storage, vec3, queue, bl, renderSection -> {
			});
			this.currentGraph.set(graphState);
			this.nextGraphEvents.set(null);
			this.needsFrustumUpdate.set(true);
		});
	}

	private void runPartialUpdate(boolean bl, Frustum frustum, List<SectionRenderDispatcher.RenderSection> list, Vec3 vec3) {
		SectionOcclusionGraph.GraphState graphState = (SectionOcclusionGraph.GraphState)this.currentGraph.get();
		this.queueSectionsWithNewNeighbors(graphState);
		if (!graphState.events.sectionsToPropagateFrom.isEmpty()) {
			Queue<SectionOcclusionGraph.Node> queue = Queues.<SectionOcclusionGraph.Node>newArrayDeque();

			while (!graphState.events.sectionsToPropagateFrom.isEmpty()) {
				SectionRenderDispatcher.RenderSection renderSection = (SectionRenderDispatcher.RenderSection)graphState.events.sectionsToPropagateFrom.poll();
				SectionOcclusionGraph.Node node = graphState.storage.sectionToNodeMap.get(renderSection);
				if (node != null && node.section == renderSection) {
					queue.add(node);
				}
			}

			Frustum frustum2 = LevelRenderer.offsetFrustum(frustum);
			Consumer<SectionRenderDispatcher.RenderSection> consumer = renderSection -> {
				if (frustum2.isVisible(renderSection.getBoundingBox())) {
					list.add(renderSection);
				}
			};
			this.runUpdates(graphState.storage, vec3, queue, bl, consumer);
		}
	}

	private void queueSectionsWithNewNeighbors(SectionOcclusionGraph.GraphState graphState) {
		LongIterator longIterator = graphState.events.chunksWhichReceivedNeighbors.iterator();

		while (longIterator.hasNext()) {
			long l = longIterator.nextLong();
			List<SectionRenderDispatcher.RenderSection> list = graphState.storage.chunksWaitingForNeighbors.get(l);
			if (list != null && ((SectionRenderDispatcher.RenderSection)list.get(0)).hasAllNeighbors()) {
				graphState.events.sectionsToPropagateFrom.addAll(list);
				graphState.storage.chunksWaitingForNeighbors.remove(l);
			}
		}

		graphState.events.chunksWhichReceivedNeighbors.clear();
	}

	private void addNeighbors(SectionOcclusionGraph.GraphEvents graphEvents, ChunkPos chunkPos) {
		graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x - 1, chunkPos.z));
		graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x, chunkPos.z - 1));
		graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x + 1, chunkPos.z));
		graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x, chunkPos.z + 1));
	}

	private void initializeQueueForFullUpdate(Camera camera, Queue<SectionOcclusionGraph.Node> queue) {
		int i = 16;
		Vec3 vec3 = camera.getPosition();
		BlockPos blockPos = camera.getBlockPosition();
		SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSectionAt(blockPos);
		if (renderSection == null) {
			LevelHeightAccessor levelHeightAccessor = this.viewArea.getLevelHeightAccessor();
			boolean bl = blockPos.getY() > levelHeightAccessor.getMinBuildHeight();
			int j = bl ? levelHeightAccessor.getMaxBuildHeight() - 8 : levelHeightAccessor.getMinBuildHeight() + 8;
			int k = Mth.floor(vec3.x / 16.0) * 16;
			int l = Mth.floor(vec3.z / 16.0) * 16;
			int m = this.viewArea.getViewDistance();
			List<SectionOcclusionGraph.Node> list = Lists.<SectionOcclusionGraph.Node>newArrayList();

			for (int n = -m; n <= m; n++) {
				for (int o = -m; o <= m; o++) {
					SectionRenderDispatcher.RenderSection renderSection2 = this.viewArea
						.getRenderSectionAt(new BlockPos(k + SectionPos.sectionToBlockCoord(n, 8), j, l + SectionPos.sectionToBlockCoord(o, 8)));
					if (renderSection2 != null && this.isInViewDistance(blockPos, renderSection2.getOrigin())) {
						Direction direction = bl ? Direction.DOWN : Direction.UP;
						SectionOcclusionGraph.Node node = new SectionOcclusionGraph.Node(renderSection2, direction, 0);
						node.setDirections(node.directions, direction);
						if (n > 0) {
							node.setDirections(node.directions, Direction.EAST);
						} else if (n < 0) {
							node.setDirections(node.directions, Direction.WEST);
						}

						if (o > 0) {
							node.setDirections(node.directions, Direction.SOUTH);
						} else if (o < 0) {
							node.setDirections(node.directions, Direction.NORTH);
						}

						list.add(node);
					}
				}
			}

			list.sort(Comparator.comparingDouble(nodex -> blockPos.distSqr(nodex.section.getOrigin().offset(8, 8, 8))));
			queue.addAll(list);
		} else {
			queue.add(new SectionOcclusionGraph.Node(renderSection, null, 0));
		}
	}

	private void runUpdates(
		SectionOcclusionGraph.GraphStorage graphStorage,
		Vec3 vec3,
		Queue<SectionOcclusionGraph.Node> queue,
		boolean bl,
		Consumer<SectionRenderDispatcher.RenderSection> consumer
	) {
		int i = 16;
		BlockPos blockPos = new BlockPos(Mth.floor(vec3.x / 16.0) * 16, Mth.floor(vec3.y / 16.0) * 16, Mth.floor(vec3.z / 16.0) * 16);
		BlockPos blockPos2 = blockPos.offset(8, 8, 8);

		while (!queue.isEmpty()) {
			SectionOcclusionGraph.Node node = (SectionOcclusionGraph.Node)queue.poll();
			SectionRenderDispatcher.RenderSection renderSection = node.section;
			if (graphStorage.renderSections.add(node)) {
				consumer.accept(node.section);
			}

			boolean bl2 = Math.abs(renderSection.getOrigin().getX() - blockPos.getX()) > 60
				|| Math.abs(renderSection.getOrigin().getY() - blockPos.getY()) > 60
				|| Math.abs(renderSection.getOrigin().getZ() - blockPos.getZ()) > 60;

			for (Direction direction : DIRECTIONS) {
				SectionRenderDispatcher.RenderSection renderSection2 = this.getRelativeFrom(blockPos, renderSection, direction);
				if (renderSection2 != null && (!bl || !node.hasDirection(direction.getOpposite()))) {
					if (bl && node.hasSourceDirections()) {
						SectionRenderDispatcher.CompiledSection compiledSection = renderSection.getCompiled();
						boolean bl3 = false;

						for (int j = 0; j < DIRECTIONS.length; j++) {
							if (node.hasSourceDirection(j) && compiledSection.facesCanSeeEachother(DIRECTIONS[j].getOpposite(), direction)) {
								bl3 = true;
								break;
							}
						}

						if (!bl3) {
							continue;
						}
					}

					if (bl && bl2) {
						BlockPos blockPos3 = renderSection2.getOrigin();
						BlockPos blockPos4 = blockPos3.offset(
							(direction.getAxis() == Direction.Axis.X ? blockPos2.getX() <= blockPos3.getX() : blockPos2.getX() >= blockPos3.getX()) ? 0 : 16,
							(direction.getAxis() == Direction.Axis.Y ? blockPos2.getY() <= blockPos3.getY() : blockPos2.getY() >= blockPos3.getY()) ? 0 : 16,
							(direction.getAxis() == Direction.Axis.Z ? blockPos2.getZ() <= blockPos3.getZ() : blockPos2.getZ() >= blockPos3.getZ()) ? 0 : 16
						);
						Vec3 vec32 = new Vec3((double)blockPos4.getX(), (double)blockPos4.getY(), (double)blockPos4.getZ());
						Vec3 vec33 = vec3.subtract(vec32).normalize().scale(CEILED_SECTION_DIAGONAL);
						boolean bl4 = true;

						while (vec3.subtract(vec32).lengthSqr() > 3600.0) {
							vec32 = vec32.add(vec33);
							LevelHeightAccessor levelHeightAccessor = this.viewArea.getLevelHeightAccessor();
							if (vec32.y > (double)levelHeightAccessor.getMaxBuildHeight() || vec32.y < (double)levelHeightAccessor.getMinBuildHeight()) {
								break;
							}

							SectionRenderDispatcher.RenderSection renderSection3 = this.viewArea.getRenderSectionAt(BlockPos.containing(vec32.x, vec32.y, vec32.z));
							if (renderSection3 == null || graphStorage.sectionToNodeMap.get(renderSection3) == null) {
								bl4 = false;
								break;
							}
						}

						if (!bl4) {
							continue;
						}
					}

					SectionOcclusionGraph.Node node2 = graphStorage.sectionToNodeMap.get(renderSection2);
					if (node2 != null) {
						node2.addSourceDirection(direction);
					} else {
						SectionOcclusionGraph.Node node3 = new SectionOcclusionGraph.Node(renderSection2, direction, node.step + 1);
						node3.setDirections(node.directions, direction);
						if (renderSection2.hasAllNeighbors()) {
							queue.add(node3);
							graphStorage.sectionToNodeMap.put(renderSection2, node3);
						} else if (this.isInViewDistance(blockPos, renderSection2.getOrigin())) {
							graphStorage.sectionToNodeMap.put(renderSection2, node3);
							graphStorage.chunksWaitingForNeighbors
								.computeIfAbsent(
									ChunkPos.asLong(renderSection2.getOrigin()), (Long2ObjectFunction<? extends List<SectionRenderDispatcher.RenderSection>>)(l -> new ArrayList())
								)
								.add(renderSection2);
						}
					}
				}
			}
		}
	}

	private boolean isInViewDistance(BlockPos blockPos, BlockPos blockPos2) {
		int i = SectionPos.blockToSectionCoord(blockPos.getX());
		int j = SectionPos.blockToSectionCoord(blockPos.getZ());
		int k = SectionPos.blockToSectionCoord(blockPos2.getX());
		int l = SectionPos.blockToSectionCoord(blockPos2.getZ());
		return ChunkTrackingView.isInViewDistance(i, j, this.viewArea.getViewDistance(), k, l);
	}

	@Nullable
	private SectionRenderDispatcher.RenderSection getRelativeFrom(BlockPos blockPos, SectionRenderDispatcher.RenderSection renderSection, Direction direction) {
		BlockPos blockPos2 = renderSection.getRelativeOrigin(direction);
		if (!this.isInViewDistance(blockPos, blockPos2)) {
			return null;
		} else {
			return Mth.abs(blockPos.getY() - blockPos2.getY()) > this.viewArea.getViewDistance() * 16 ? null : this.viewArea.getRenderSectionAt(blockPos2);
		}
	}

	@Nullable
	@VisibleForDebug
	public SectionOcclusionGraph.Node getNode(SectionRenderDispatcher.RenderSection renderSection) {
		return ((SectionOcclusionGraph.GraphState)this.currentGraph.get()).storage.sectionToNodeMap.get(renderSection);
	}

	@Environment(EnvType.CLIENT)
	static record GraphEvents(LongSet chunksWhichReceivedNeighbors, BlockingQueue<SectionRenderDispatcher.RenderSection> sectionsToPropagateFrom) {

		public GraphEvents() {
			this(new LongOpenHashSet(), new LinkedBlockingQueue());
		}
	}

	@Environment(EnvType.CLIENT)
	static record GraphState(SectionOcclusionGraph.GraphStorage storage, SectionOcclusionGraph.GraphEvents events) {

		public GraphState(int i) {
			this(new SectionOcclusionGraph.GraphStorage(i), new SectionOcclusionGraph.GraphEvents());
		}
	}

	@Environment(EnvType.CLIENT)
	static class GraphStorage {
		public final SectionOcclusionGraph.SectionToNodeMap sectionToNodeMap;
		public final LinkedHashSet<SectionOcclusionGraph.Node> renderSections;
		public final Long2ObjectMap<List<SectionRenderDispatcher.RenderSection>> chunksWaitingForNeighbors;

		public GraphStorage(int i) {
			this.sectionToNodeMap = new SectionOcclusionGraph.SectionToNodeMap(i);
			this.renderSections = new LinkedHashSet(i);
			this.chunksWaitingForNeighbors = new Long2ObjectOpenHashMap<>();
		}
	}

	@Environment(EnvType.CLIENT)
	@VisibleForDebug
	public static class Node {
		@VisibleForDebug
		protected final SectionRenderDispatcher.RenderSection section;
		private byte sourceDirections;
		byte directions;
		@VisibleForDebug
		public final int step;

		Node(SectionRenderDispatcher.RenderSection renderSection, @Nullable Direction direction, int i) {
			this.section = renderSection;
			if (direction != null) {
				this.addSourceDirection(direction);
			}

			this.step = i;
		}

		void setDirections(byte b, Direction direction) {
			this.directions = (byte)(this.directions | b | 1 << direction.ordinal());
		}

		boolean hasDirection(Direction direction) {
			return (this.directions & 1 << direction.ordinal()) > 0;
		}

		void addSourceDirection(Direction direction) {
			this.sourceDirections = (byte)(this.sourceDirections | this.sourceDirections | 1 << direction.ordinal());
		}

		@VisibleForDebug
		public boolean hasSourceDirection(int i) {
			return (this.sourceDirections & 1 << i) > 0;
		}

		boolean hasSourceDirections() {
			return this.sourceDirections != 0;
		}

		public int hashCode() {
			return this.section.getOrigin().hashCode();
		}

		public boolean equals(Object object) {
			return !(object instanceof SectionOcclusionGraph.Node node) ? false : this.section.getOrigin().equals(node.section.getOrigin());
		}
	}

	@Environment(EnvType.CLIENT)
	static class SectionToNodeMap {
		private final SectionOcclusionGraph.Node[] nodes;

		SectionToNodeMap(int i) {
			this.nodes = new SectionOcclusionGraph.Node[i];
		}

		public void put(SectionRenderDispatcher.RenderSection renderSection, SectionOcclusionGraph.Node node) {
			this.nodes[renderSection.index] = node;
		}

		@Nullable
		public SectionOcclusionGraph.Node get(SectionRenderDispatcher.RenderSection renderSection) {
			int i = renderSection.index;
			return i >= 0 && i < this.nodes.length ? this.nodes[i] : null;
		}
	}
}
