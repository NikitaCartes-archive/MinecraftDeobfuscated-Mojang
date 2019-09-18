package net.minecraft.client.model.geom;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ModelPart {
	private static final BufferBuilder COMPILE_BUFFER = new BufferBuilder(256);
	private float xTexSize = 64.0F;
	private float yTexSize = 32.0F;
	private int xTexOffs;
	private int yTexOffs;
	public float x;
	public float y;
	public float z;
	public float xRot;
	public float yRot;
	public float zRot;
	@Nullable
	private ByteBuffer compiled;
	public boolean mirror;
	public boolean visible = true;
	private final List<ModelPart.Cube> cubes = Lists.<ModelPart.Cube>newArrayList();
	private final List<ModelPart> children = Lists.<ModelPart>newArrayList();

	public ModelPart(Model model) {
		model.accept(this);
		this.setTexSize(model.texWidth, model.texHeight);
	}

	public ModelPart(Model model, int i, int j) {
		this(model.texWidth, model.texHeight, i, j);
		model.accept(this);
	}

	public ModelPart(int i, int j, int k, int l) {
		this.setTexSize(i, j);
		this.texOffs(k, l);
	}

	public void copyFrom(ModelPart modelPart) {
		this.xRot = modelPart.xRot;
		this.yRot = modelPart.yRot;
		this.zRot = modelPart.zRot;
		this.x = modelPart.x;
		this.y = modelPart.y;
		this.z = modelPart.z;
	}

	public void addChild(ModelPart modelPart) {
		this.children.add(modelPart);
	}

	public ModelPart texOffs(int i, int j) {
		this.xTexOffs = i;
		this.yTexOffs = j;
		return this;
	}

	public ModelPart addBox(String string, float f, float g, float h, int i, int j, int k, float l, int m, int n) {
		this.texOffs(m, n);
		this.cubes.add(new ModelPart.Cube(this.xTexOffs, this.yTexOffs, f, g, h, (float)i, (float)j, (float)k, l, this.mirror, this.xTexSize, this.yTexSize));
		return this;
	}

	public ModelPart addBox(float f, float g, float h, float i, float j, float k) {
		this.cubes.add(new ModelPart.Cube(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, 0.0F, this.mirror, this.xTexSize, this.yTexSize));
		return this;
	}

	public ModelPart addBox(float f, float g, float h, float i, float j, float k, boolean bl) {
		this.cubes.add(new ModelPart.Cube(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, 0.0F, bl, this.xTexSize, this.yTexSize));
		return this;
	}

	public void addBox(float f, float g, float h, float i, float j, float k, float l) {
		this.cubes.add(new ModelPart.Cube(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l, this.mirror, this.xTexSize, this.yTexSize));
	}

	public void addBox(float f, float g, float h, float i, float j, float k, float l, boolean bl) {
		this.cubes.add(new ModelPart.Cube(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l, bl, this.xTexSize, this.yTexSize));
	}

	public void setPos(float f, float g, float h) {
		this.x = f;
		this.y = g;
		this.z = h;
	}

	public void render(float f) {
		if (this.visible) {
			this.compile(f);
			if (this.compiled != null) {
				RenderSystem.pushMatrix();
				this.translateAndRotate(f);
				this.compiled.clear();
				int i = this.compiled.remaining() / DefaultVertexFormat.ENTITY.getVertexSize();
				BufferUploader.end(this.compiled, 7, DefaultVertexFormat.ENTITY, i);

				for (ModelPart modelPart : this.children) {
					modelPart.render(f);
				}

				RenderSystem.popMatrix();
			}
		}
	}

	public void render(BufferBuilder bufferBuilder, float f, int i, int j, TextureAtlasSprite textureAtlasSprite) {
		this.render(bufferBuilder, f, i, j, textureAtlasSprite, 1.0F, 1.0F, 1.0F);
	}

	public void render(BufferBuilder bufferBuilder, float f, int i, int j, TextureAtlasSprite textureAtlasSprite, float g, float h, float k) {
		if (this.visible) {
			if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
				bufferBuilder.pushPose();
				bufferBuilder.translate((double)(this.x * f), (double)(this.y * f), (double)(this.z * f));
				if (this.zRot != 0.0F) {
					bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, this.zRot, false));
				}

				if (this.yRot != 0.0F) {
					bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, this.yRot, false));
				}

				if (this.xRot != 0.0F) {
					bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, this.xRot, false));
				}

				this.compile(bufferBuilder, f, i, j, textureAtlasSprite, g, h, k);

				for (ModelPart modelPart : this.children) {
					modelPart.render(bufferBuilder, f, i, j, textureAtlasSprite);
				}

				bufferBuilder.popPose();
			}
		}
	}

	private void compile(float f) {
		if (this.visible) {
			if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
				if (this.compiled == null) {
					COMPILE_BUFFER.begin(7, DefaultVertexFormat.ENTITY);
					this.compile(COMPILE_BUFFER, f, 240, 240, null);
					COMPILE_BUFFER.end();
					Pair<BufferBuilder.DrawState, ByteBuffer> pair = COMPILE_BUFFER.popNextBuffer();
					ByteBuffer byteBuffer = pair.getSecond();
					this.compiled = MemoryTracker.createByteBuffer(byteBuffer.remaining());
					this.compiled.put(byteBuffer);
				}
			}
		}
	}

	public void translateTo(float f) {
		if (this.visible) {
			this.translateAndRotate(f);
		}
	}

	private void translateAndRotate(float f) {
		RenderSystem.translatef(this.x * f, this.y * f, this.z * f);
		if (this.zRot != 0.0F) {
			RenderSystem.rotatef(this.zRot * (180.0F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
		}

		if (this.yRot != 0.0F) {
			RenderSystem.rotatef(this.yRot * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
		}

		if (this.xRot != 0.0F) {
			RenderSystem.rotatef(this.xRot * (180.0F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
		}
	}

	private void compile(BufferBuilder bufferBuilder, float f, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite) {
		this.compile(bufferBuilder, f, i, j, textureAtlasSprite, 1.0F, 1.0F, 1.0F);
	}

	private void compile(BufferBuilder bufferBuilder, float f, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite, float g, float h, float k) {
		Matrix4f matrix4f = bufferBuilder.getPose();
		VertexFormat vertexFormat = bufferBuilder.getVertexFormat();

		for (ModelPart.Cube cube : this.cubes) {
			for (ModelPart.Polygon polygon : cube.polygons) {
				Vec3 vec3 = polygon.vertices[1].pos.vectorTo(polygon.vertices[0].pos);
				Vec3 vec32 = polygon.vertices[1].pos.vectorTo(polygon.vertices[2].pos);
				Vec3 vec33 = vec32.cross(vec3).normalize();
				float l = (float)vec33.x;
				float m = (float)vec33.y;
				float n = (float)vec33.z;

				for (int o = 0; o < 4; o++) {
					ModelPart.Vertex vertex = polygon.vertices[o];
					Vector4f vector4f = new Vector4f((float)vertex.pos.x * f, (float)vertex.pos.y * f, (float)vertex.pos.z * f, 1.0F);
					vector4f.transform(matrix4f);
					bufferBuilder.vertex((double)vector4f.x(), (double)vector4f.y(), (double)vector4f.z());
					if (vertexFormat.hasColor()) {
						float p = Mth.diffuseLight(l, m, n);
						bufferBuilder.color(p * g, p * h, p * k, 1.0F);
					}

					if (textureAtlasSprite == null) {
						bufferBuilder.uv((double)vertex.u, (double)vertex.v);
					} else {
						bufferBuilder.uv((double)textureAtlasSprite.getU((double)(vertex.u * 16.0F)), (double)textureAtlasSprite.getV((double)(vertex.v * 16.0F)));
					}

					if (vertexFormat.hasUv(1)) {
						bufferBuilder.uv2(i, j);
					}

					bufferBuilder.normal(l, m, n).endVertex();
				}
			}
		}
	}

	public ModelPart setTexSize(int i, int j) {
		this.xTexSize = (float)i;
		this.yTexSize = (float)j;
		return this;
	}

	public ModelPart.Cube getRandomCube(Random random) {
		return (ModelPart.Cube)this.cubes.get(random.nextInt(this.cubes.size()));
	}

	@Environment(EnvType.CLIENT)
	public static class Cube {
		private final ModelPart.Polygon[] polygons;
		public final float minX;
		public final float minY;
		public final float minZ;
		public final float maxX;
		public final float maxY;
		public final float maxZ;

		public Cube(int i, int j, float f, float g, float h, float k, float l, float m, float n, boolean bl, float o, float p) {
			this.minX = f;
			this.minY = g;
			this.minZ = h;
			this.maxX = f + k;
			this.maxY = g + l;
			this.maxZ = h + m;
			this.polygons = new ModelPart.Polygon[6];
			float q = f + k;
			float r = g + l;
			float s = h + m;
			f -= n;
			g -= n;
			h -= n;
			q += n;
			r += n;
			s += n;
			if (bl) {
				float t = q;
				q = f;
				f = t;
			}

			ModelPart.Vertex vertex = new ModelPart.Vertex(f, g, h, 0.0F, 0.0F);
			ModelPart.Vertex vertex2 = new ModelPart.Vertex(q, g, h, 0.0F, 8.0F);
			ModelPart.Vertex vertex3 = new ModelPart.Vertex(q, r, h, 8.0F, 8.0F);
			ModelPart.Vertex vertex4 = new ModelPart.Vertex(f, r, h, 8.0F, 0.0F);
			ModelPart.Vertex vertex5 = new ModelPart.Vertex(f, g, s, 0.0F, 0.0F);
			ModelPart.Vertex vertex6 = new ModelPart.Vertex(q, g, s, 0.0F, 8.0F);
			ModelPart.Vertex vertex7 = new ModelPart.Vertex(q, r, s, 8.0F, 8.0F);
			ModelPart.Vertex vertex8 = new ModelPart.Vertex(f, r, s, 8.0F, 0.0F);
			this.polygons[0] = new ModelPart.Polygon(
				new ModelPart.Vertex[]{vertex6, vertex2, vertex3, vertex7}, (float)i + m + k, (float)j + m, (float)i + m + k + m, (float)j + m + l, o, p
			);
			this.polygons[1] = new ModelPart.Polygon(
				new ModelPart.Vertex[]{vertex, vertex5, vertex8, vertex4}, (float)i, (float)j + m, (float)i + m, (float)j + m + l, o, p
			);
			this.polygons[2] = new ModelPart.Polygon(
				new ModelPart.Vertex[]{vertex6, vertex5, vertex, vertex2}, (float)i + m, (float)j, (float)i + m + k, (float)j + m, o, p
			);
			this.polygons[3] = new ModelPart.Polygon(
				new ModelPart.Vertex[]{vertex3, vertex4, vertex8, vertex7}, (float)i + m + k, (float)j + m, (float)i + m + k + k, (float)j, o, p
			);
			this.polygons[4] = new ModelPart.Polygon(
				new ModelPart.Vertex[]{vertex2, vertex, vertex4, vertex3}, (float)i + m, (float)j + m, (float)i + m + k, (float)j + m + l, o, p
			);
			this.polygons[5] = new ModelPart.Polygon(
				new ModelPart.Vertex[]{vertex5, vertex6, vertex7, vertex8}, (float)i + m + k + m, (float)j + m, (float)i + m + k + m + k, (float)j + m + l, o, p
			);
			if (bl) {
				for (ModelPart.Polygon polygon : this.polygons) {
					polygon.mirror();
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class Polygon {
		public ModelPart.Vertex[] vertices;

		public Polygon(ModelPart.Vertex[] vertexs) {
			this.vertices = vertexs;
		}

		public Polygon(ModelPart.Vertex[] vertexs, float f, float g, float h, float i, float j, float k) {
			this(vertexs);
			float l = 0.0F / j;
			float m = 0.0F / k;
			vertexs[0] = vertexs[0].remap(h / j - l, g / k + m);
			vertexs[1] = vertexs[1].remap(f / j + l, g / k + m);
			vertexs[2] = vertexs[2].remap(f / j + l, i / k - m);
			vertexs[3] = vertexs[3].remap(h / j - l, i / k - m);
		}

		public void mirror() {
			ModelPart.Vertex[] vertexs = new ModelPart.Vertex[this.vertices.length];

			for (int i = 0; i < this.vertices.length; i++) {
				vertexs[i] = this.vertices[this.vertices.length - i - 1];
			}

			this.vertices = vertexs;
		}
	}

	@Environment(EnvType.CLIENT)
	static class Vertex {
		public final Vec3 pos;
		public final float u;
		public final float v;

		public Vertex(float f, float g, float h, float i, float j) {
			this(new Vec3((double)f, (double)g, (double)h), i, j);
		}

		public ModelPart.Vertex remap(float f, float g) {
			return new ModelPart.Vertex(this.pos, f, g);
		}

		public Vertex(Vec3 vec3, float f, float g) {
			this.pos = vec3;
			this.u = f;
			this.v = g;
		}
	}
}
