package net.minecraft.client.renderer.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class DebugRenderer {
	public final PathfindingRenderer pathfindingRenderer;
	public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
	public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
	public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
	public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
	public final DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
	public final CaveDebugRenderer caveRenderer;
	public final StructureRenderer structureRenderer;
	public final DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
	public final DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
	public final DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
	public final DebugRenderer.SimpleDebugRenderer chunkRenderer;
	public final VillageDebugRenderer villageDebugRenderer;
	public final RaidDebugRenderer raidDebugRenderer;
	public final GoalSelectorDebugRenderer goalSelectorRenderer;
	public final GameTestDebugRenderer gameTestDebugRenderer;
	private boolean renderChunkborder;

	public DebugRenderer(Minecraft minecraft) {
		this.pathfindingRenderer = new PathfindingRenderer(minecraft);
		this.waterDebugRenderer = new WaterDebugRenderer(minecraft);
		this.chunkBorderRenderer = new ChunkBorderRenderer(minecraft);
		this.heightMapRenderer = new HeightMapRenderer(minecraft);
		this.collisionBoxRenderer = new CollisionBoxRenderer(minecraft);
		this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(minecraft);
		this.caveRenderer = new CaveDebugRenderer(minecraft);
		this.structureRenderer = new StructureRenderer(minecraft);
		this.lightDebugRenderer = new LightDebugRenderer(minecraft);
		this.worldGenAttemptRenderer = new WorldGenAttemptRenderer(minecraft);
		this.solidFaceRenderer = new SolidFaceRenderer(minecraft);
		this.chunkRenderer = new ChunkDebugRenderer(minecraft);
		this.villageDebugRenderer = new VillageDebugRenderer(minecraft);
		this.raidDebugRenderer = new RaidDebugRenderer(minecraft);
		this.goalSelectorRenderer = new GoalSelectorDebugRenderer(minecraft);
		this.gameTestDebugRenderer = new GameTestDebugRenderer();
	}

	public void clear() {
		this.pathfindingRenderer.clear();
		this.waterDebugRenderer.clear();
		this.chunkBorderRenderer.clear();
		this.heightMapRenderer.clear();
		this.collisionBoxRenderer.clear();
		this.neighborsUpdateRenderer.clear();
		this.caveRenderer.clear();
		this.structureRenderer.clear();
		this.lightDebugRenderer.clear();
		this.worldGenAttemptRenderer.clear();
		this.solidFaceRenderer.clear();
		this.chunkRenderer.clear();
		this.villageDebugRenderer.clear();
		this.raidDebugRenderer.clear();
		this.goalSelectorRenderer.clear();
		this.gameTestDebugRenderer.clear();
	}

	public boolean switchRenderChunkborder() {
		this.renderChunkborder = !this.renderChunkborder;
		return this.renderChunkborder;
	}

	@Environment(EnvType.CLIENT)
	public interface SimpleDebugRenderer {
		default void clear() {
		}
	}
}
