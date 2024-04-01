package net.minecraft.client.grid;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.grid.GridCarrier;
import net.minecraft.world.grid.SubGridBlocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class SubGridRenderer implements AutoCloseable {
	private static final double CULL_BUFFER_SIZE = 3.0;
	private final Minecraft minecraft = Minecraft.getInstance();
	private final BlockRenderDispatcher blockRenderer;
	private final ClientSubGrid grid;
	private final Reference2ObjectMap<RenderType, VertexBuffer> vertexBuffers = new Reference2ObjectArrayMap<>();
	@Nullable
	private CompletableFuture<SubGridMeshBuilder.Results> meshFuture;
	private boolean needsRebuild = true;

	public SubGridRenderer(ClientSubGrid clientSubGrid) {
		this.grid = clientSubGrid;
		this.blockRenderer = this.minecraft.getBlockRenderer();
	}

	private void prepareMesh() {
		if (this.meshFuture != null) {
			if (this.meshFuture.isDone()) {
				try (SubGridMeshBuilder.Results results = (SubGridMeshBuilder.Results)this.meshFuture.join()) {
					results.uploadTo(this.vertexBuffers);
					VertexBuffer.unbind();
				}

				this.meshFuture = null;
			}
		} else if (this.needsRebuild) {
			this.needsRebuild = false;
			SubGridMeshBuilder.BlockView blockView = SubGridMeshBuilder.BlockView.copyOf(this.grid);
			SubGridMeshBuilder subGridMeshBuilder = new SubGridMeshBuilder(this.blockRenderer, blockView);
			this.meshFuture = CompletableFuture.supplyAsync(subGridMeshBuilder::build, Util.backgroundExecutor());
		}
	}

	public void draw(float f, double d, double e, double g, Frustum frustum, Matrix4f matrix4f, Matrix4f matrix4f2, boolean bl) {
		GridCarrier gridCarrier = this.grid.carrier();
		Vec3 vec3 = new Vec3(
			Mth.lerp((double)f, gridCarrier.xOld, gridCarrier.getX()),
			Mth.lerp((double)f, gridCarrier.yOld, gridCarrier.getY()),
			Mth.lerp((double)f, gridCarrier.zOld, gridCarrier.getZ())
		);
		SubGridBlocks subGridBlocks = this.grid.getBlocks();
		if (frustum.isVisible(
			vec3.x - 3.0,
			vec3.y - 3.0,
			vec3.z - 3.0,
			vec3.x + (double)subGridBlocks.sizeX() + 1.0 + 3.0,
			vec3.y + (double)subGridBlocks.sizeY() + 1.0 + 3.0,
			vec3.z + (double)subGridBlocks.sizeZ() + 1.0 + 3.0
		)) {
			this.prepareMesh();
			if (!this.vertexBuffers.isEmpty()) {
				Window window = this.minecraft.getWindow();
				Vector3f vector3f = new Vector3f((float)(vec3.x - d), (float)(vec3.y - e), (float)(vec3.z - g));
				if (bl) {
					this.drawLayer(RenderType.translucent(), vector3f, matrix4f, matrix4f2, window);
					this.drawLayer(RenderType.tripwire(), vector3f, matrix4f, matrix4f2, window);
				} else {
					this.drawLayer(RenderType.solid(), vector3f, matrix4f, matrix4f2, window);
					this.drawLayer(RenderType.cutoutMipped(), vector3f, matrix4f, matrix4f2, window);
					this.drawLayer(RenderType.cutout(), vector3f, matrix4f, matrix4f2, window);
				}
			}
		}
	}

	private void drawLayer(RenderType renderType, Vector3f vector3f, Matrix4f matrix4f, Matrix4f matrix4f2, Window window) {
		VertexBuffer vertexBuffer = this.vertexBuffers.get(renderType);
		if (vertexBuffer != null) {
			renderType.setupRenderState();
			ShaderInstance shaderInstance = RenderSystem.getShader();
			shaderInstance.setDefaultUniforms(VertexFormat.Mode.QUADS, matrix4f, matrix4f2, window);
			shaderInstance.CHUNK_OFFSET.set(vector3f.x, vector3f.y, vector3f.z);
			shaderInstance.apply();
			vertexBuffer.bind();
			vertexBuffer.draw();
			VertexBuffer.unbind();
			shaderInstance.clear();
			renderType.clearRenderState();
		}
	}

	public void close() {
		this.vertexBuffers.values().forEach(VertexBuffer::close);
		this.vertexBuffers.clear();
		if (this.meshFuture != null) {
			this.meshFuture.thenAcceptAsync(SubGridMeshBuilder.Results::close, runnable -> RenderSystem.recordRenderCall(runnable::run));
			this.meshFuture = null;
		}
	}
}
