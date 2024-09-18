package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CloudStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class CloudRenderer extends SimplePreparableReloadListener<Optional<CloudRenderer.TextureData>> implements AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/clouds.png");
	private static final float CELL_SIZE_IN_BLOCKS = 12.0F;
	private static final float HEIGHT_IN_BLOCKS = 4.0F;
	private static final float BLOCKS_PER_SECOND = 0.6F;
	private static final long EMPTY_CELL = 0L;
	private static final int COLOR_OFFSET = 4;
	private static final int NORTH_OFFSET = 3;
	private static final int EAST_OFFSET = 2;
	private static final int SOUTH_OFFSET = 1;
	private static final int WEST_OFFSET = 0;
	private boolean needsRebuild = true;
	private int prevCellX = Integer.MIN_VALUE;
	private int prevCellZ = Integer.MIN_VALUE;
	private CloudRenderer.RelativeCameraPos prevRelativeCameraPos = CloudRenderer.RelativeCameraPos.INSIDE_CLOUDS;
	@Nullable
	private CloudStatus prevType;
	@Nullable
	private CloudRenderer.TextureData texture;
	private final VertexBuffer vertexBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
	private boolean vertexBufferEmpty;

	protected Optional<CloudRenderer.TextureData> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		try {
			InputStream inputStream = resourceManager.open(TEXTURE_LOCATION);

			Optional var20;
			try (NativeImage nativeImage = NativeImage.read(inputStream)) {
				int i = nativeImage.getWidth();
				int j = nativeImage.getHeight();
				long[] ls = new long[i * j];

				for (int k = 0; k < j; k++) {
					for (int l = 0; l < i; l++) {
						int m = nativeImage.getPixel(l, k);
						if (isCellEmpty(m)) {
							ls[l + k * i] = 0L;
						} else {
							boolean bl = isCellEmpty(nativeImage.getPixel(l, Math.floorMod(k - 1, j)));
							boolean bl2 = isCellEmpty(nativeImage.getPixel(Math.floorMod(l + 1, j), k));
							boolean bl3 = isCellEmpty(nativeImage.getPixel(l, Math.floorMod(k + 1, j)));
							boolean bl4 = isCellEmpty(nativeImage.getPixel(Math.floorMod(l - 1, j), k));
							ls[l + k * i] = packCellData(m, bl, bl2, bl3, bl4);
						}
					}
				}

				var20 = Optional.of(new CloudRenderer.TextureData(ls, i, j));
			} catch (Throwable var18) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var15) {
						var18.addSuppressed(var15);
					}
				}

				throw var18;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var20;
		} catch (IOException var19) {
			LOGGER.error("Failed to load cloud texture", (Throwable)var19);
			return Optional.empty();
		}
	}

	protected void apply(Optional<CloudRenderer.TextureData> optional, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		this.texture = (CloudRenderer.TextureData)optional.orElse(null);
		this.needsRebuild = true;
	}

	private static boolean isCellEmpty(int i) {
		return ARGB.alpha(i) < 10;
	}

	private static long packCellData(int i, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		return (long)i << 4 | (long)((bl ? 1 : 0) << 3) | (long)((bl2 ? 1 : 0) << 2) | (long)((bl3 ? 1 : 0) << 1) | (long)((bl4 ? 1 : 0) << 0);
	}

	private static int getColor(long l) {
		return (int)(l >> 4 & 4294967295L);
	}

	private static boolean isNorthEmpty(long l) {
		return (l >> 3 & 1L) != 0L;
	}

	private static boolean isEastEmpty(long l) {
		return (l >> 2 & 1L) != 0L;
	}

	private static boolean isSouthEmpty(long l) {
		return (l >> 1 & 1L) != 0L;
	}

	private static boolean isWestEmpty(long l) {
		return (l >> 0 & 1L) != 0L;
	}

	public void render(int i, CloudStatus cloudStatus, float f, Matrix4f matrix4f, Matrix4f matrix4f2, Vec3 vec3, float g) {
		if (this.texture != null) {
			float h = (float)((double)f - vec3.y);
			float j = h + 4.0F;
			CloudRenderer.RelativeCameraPos relativeCameraPos;
			if (j < 0.0F) {
				relativeCameraPos = CloudRenderer.RelativeCameraPos.ABOVE_CLOUDS;
			} else if (h > 0.0F) {
				relativeCameraPos = CloudRenderer.RelativeCameraPos.BELOW_CLOUDS;
			} else {
				relativeCameraPos = CloudRenderer.RelativeCameraPos.INSIDE_CLOUDS;
			}

			double d = vec3.x + (double)(g * 0.030000001F);
			double e = vec3.z + 3.96F;
			double k = (double)this.texture.width * 12.0;
			double l = (double)this.texture.height * 12.0;
			d -= (double)Mth.floor(d / k) * k;
			e -= (double)Mth.floor(e / l) * l;
			int m = Mth.floor(d / 12.0);
			int n = Mth.floor(e / 12.0);
			float o = (float)(d - (double)((float)m * 12.0F));
			float p = (float)(e - (double)((float)n * 12.0F));
			RenderType renderType = cloudStatus == CloudStatus.FANCY ? RenderType.clouds() : RenderType.flatClouds();
			this.vertexBuffer.bind();
			if (this.needsRebuild || m != this.prevCellX || n != this.prevCellZ || relativeCameraPos != this.prevRelativeCameraPos || cloudStatus != this.prevType) {
				this.needsRebuild = false;
				this.prevCellX = m;
				this.prevCellZ = n;
				this.prevRelativeCameraPos = relativeCameraPos;
				this.prevType = cloudStatus;
				MeshData meshData = this.buildMesh(Tesselator.getInstance(), m, n, cloudStatus, relativeCameraPos, renderType);
				if (meshData != null) {
					this.vertexBuffer.upload(meshData);
					this.vertexBufferEmpty = false;
				} else {
					this.vertexBufferEmpty = true;
				}
			}

			if (!this.vertexBufferEmpty) {
				RenderSystem.setShaderColor(ARGB.from8BitChannel(ARGB.red(i)), ARGB.from8BitChannel(ARGB.green(i)), ARGB.from8BitChannel(ARGB.blue(i)), 1.0F);
				if (cloudStatus == CloudStatus.FANCY) {
					this.drawWithRenderType(RenderType.cloudsDepthOnly(), matrix4f, matrix4f2, o, h, p);
				}

				this.drawWithRenderType(renderType, matrix4f, matrix4f2, o, h, p);
				VertexBuffer.unbind();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
	}

	private void drawWithRenderType(RenderType renderType, Matrix4f matrix4f, Matrix4f matrix4f2, float f, float g, float h) {
		renderType.setupRenderState();
		CompiledShaderProgram compiledShaderProgram = RenderSystem.getShader();
		if (compiledShaderProgram != null && compiledShaderProgram.MODEL_OFFSET != null) {
			compiledShaderProgram.MODEL_OFFSET.set(-f, g, -h);
		}

		this.vertexBuffer.drawWithShader(matrix4f, matrix4f2, compiledShaderProgram);
		renderType.clearRenderState();
	}

	@Nullable
	private MeshData buildMesh(
		Tesselator tesselator, int i, int j, CloudStatus cloudStatus, CloudRenderer.RelativeCameraPos relativeCameraPos, RenderType renderType
	) {
		float f = 0.8F;
		int k = ARGB.colorFromFloat(0.8F, 1.0F, 1.0F, 1.0F);
		int l = ARGB.colorFromFloat(0.8F, 0.9F, 0.9F, 0.9F);
		int m = ARGB.colorFromFloat(0.8F, 0.7F, 0.7F, 0.7F);
		int n = ARGB.colorFromFloat(0.8F, 0.8F, 0.8F, 0.8F);
		BufferBuilder bufferBuilder = tesselator.begin(renderType.mode(), renderType.format());
		this.buildMesh(relativeCameraPos, bufferBuilder, i, j, m, k, l, n, cloudStatus == CloudStatus.FANCY);
		return bufferBuilder.build();
	}

	private void buildMesh(CloudRenderer.RelativeCameraPos relativeCameraPos, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, boolean bl) {
		if (this.texture != null) {
			int o = 32;
			long[] ls = this.texture.cells;
			int p = this.texture.width;
			int q = this.texture.height;

			for (int r = -32; r <= 32; r++) {
				for (int s = -32; s <= 32; s++) {
					int t = Math.floorMod(i + s, p);
					int u = Math.floorMod(j + r, q);
					long v = ls[t + u * p];
					if (v != 0L) {
						int w = getColor(v);
						if (bl) {
							this.buildExtrudedCell(relativeCameraPos, bufferBuilder, ARGB.multiply(k, w), ARGB.multiply(l, w), ARGB.multiply(m, w), ARGB.multiply(n, w), s, r, v);
						} else {
							this.buildFlatCell(bufferBuilder, ARGB.multiply(l, w), s, r);
						}
					}
				}
			}
		}
	}

	private void buildFlatCell(BufferBuilder bufferBuilder, int i, int j, int k) {
		float f = (float)j * 12.0F;
		float g = f + 12.0F;
		float h = (float)k * 12.0F;
		float l = h + 12.0F;
		bufferBuilder.addVertex(f, 0.0F, h).setColor(i);
		bufferBuilder.addVertex(f, 0.0F, l).setColor(i);
		bufferBuilder.addVertex(g, 0.0F, l).setColor(i);
		bufferBuilder.addVertex(g, 0.0F, h).setColor(i);
	}

	private void buildExtrudedCell(
		CloudRenderer.RelativeCameraPos relativeCameraPos, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, long o
	) {
		float f = (float)m * 12.0F;
		float g = f + 12.0F;
		float h = 0.0F;
		float p = 4.0F;
		float q = (float)n * 12.0F;
		float r = q + 12.0F;
		if (relativeCameraPos != CloudRenderer.RelativeCameraPos.BELOW_CLOUDS) {
			bufferBuilder.addVertex(f, 4.0F, q).setColor(j);
			bufferBuilder.addVertex(f, 4.0F, r).setColor(j);
			bufferBuilder.addVertex(g, 4.0F, r).setColor(j);
			bufferBuilder.addVertex(g, 4.0F, q).setColor(j);
		}

		if (relativeCameraPos != CloudRenderer.RelativeCameraPos.ABOVE_CLOUDS) {
			bufferBuilder.addVertex(g, 0.0F, q).setColor(i);
			bufferBuilder.addVertex(g, 0.0F, r).setColor(i);
			bufferBuilder.addVertex(f, 0.0F, r).setColor(i);
			bufferBuilder.addVertex(f, 0.0F, q).setColor(i);
		}

		if (isNorthEmpty(o) && n > 0) {
			bufferBuilder.addVertex(f, 0.0F, q).setColor(l);
			bufferBuilder.addVertex(f, 4.0F, q).setColor(l);
			bufferBuilder.addVertex(g, 4.0F, q).setColor(l);
			bufferBuilder.addVertex(g, 0.0F, q).setColor(l);
		}

		if (isSouthEmpty(o) && n < 0) {
			bufferBuilder.addVertex(g, 0.0F, r).setColor(l);
			bufferBuilder.addVertex(g, 4.0F, r).setColor(l);
			bufferBuilder.addVertex(f, 4.0F, r).setColor(l);
			bufferBuilder.addVertex(f, 0.0F, r).setColor(l);
		}

		if (isWestEmpty(o) && m > 0) {
			bufferBuilder.addVertex(f, 0.0F, r).setColor(k);
			bufferBuilder.addVertex(f, 4.0F, r).setColor(k);
			bufferBuilder.addVertex(f, 4.0F, q).setColor(k);
			bufferBuilder.addVertex(f, 0.0F, q).setColor(k);
		}

		if (isEastEmpty(o) && m < 0) {
			bufferBuilder.addVertex(g, 0.0F, q).setColor(k);
			bufferBuilder.addVertex(g, 4.0F, q).setColor(k);
			bufferBuilder.addVertex(g, 4.0F, r).setColor(k);
			bufferBuilder.addVertex(g, 0.0F, r).setColor(k);
		}

		boolean bl = Math.abs(m) <= 1 && Math.abs(n) <= 1;
		if (bl) {
			bufferBuilder.addVertex(g, 4.0F, q).setColor(j);
			bufferBuilder.addVertex(g, 4.0F, r).setColor(j);
			bufferBuilder.addVertex(f, 4.0F, r).setColor(j);
			bufferBuilder.addVertex(f, 4.0F, q).setColor(j);
			bufferBuilder.addVertex(f, 0.0F, q).setColor(i);
			bufferBuilder.addVertex(f, 0.0F, r).setColor(i);
			bufferBuilder.addVertex(g, 0.0F, r).setColor(i);
			bufferBuilder.addVertex(g, 0.0F, q).setColor(i);
			bufferBuilder.addVertex(g, 0.0F, q).setColor(l);
			bufferBuilder.addVertex(g, 4.0F, q).setColor(l);
			bufferBuilder.addVertex(f, 4.0F, q).setColor(l);
			bufferBuilder.addVertex(f, 0.0F, q).setColor(l);
			bufferBuilder.addVertex(f, 0.0F, r).setColor(l);
			bufferBuilder.addVertex(f, 4.0F, r).setColor(l);
			bufferBuilder.addVertex(g, 4.0F, r).setColor(l);
			bufferBuilder.addVertex(g, 0.0F, r).setColor(l);
			bufferBuilder.addVertex(f, 0.0F, q).setColor(k);
			bufferBuilder.addVertex(f, 4.0F, q).setColor(k);
			bufferBuilder.addVertex(f, 4.0F, r).setColor(k);
			bufferBuilder.addVertex(f, 0.0F, r).setColor(k);
			bufferBuilder.addVertex(g, 0.0F, r).setColor(k);
			bufferBuilder.addVertex(g, 4.0F, r).setColor(k);
			bufferBuilder.addVertex(g, 4.0F, q).setColor(k);
			bufferBuilder.addVertex(g, 0.0F, q).setColor(k);
		}
	}

	public void markForRebuild() {
		this.needsRebuild = true;
	}

	public void close() {
		this.vertexBuffer.close();
	}

	@Environment(EnvType.CLIENT)
	static enum RelativeCameraPos {
		ABOVE_CLOUDS,
		INSIDE_CLOUDS,
		BELOW_CLOUDS;
	}

	@Environment(EnvType.CLIENT)
	public static record TextureData(long[] cells, int width, int height) {
	}
}
