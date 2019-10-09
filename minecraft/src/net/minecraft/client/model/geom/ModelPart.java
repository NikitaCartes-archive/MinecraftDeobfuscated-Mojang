package net.minecraft.client.model.geom;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ModelPart {
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
		this.addBox(this.xTexOffs, this.yTexOffs, f, g, h, (float)i, (float)j, (float)k, l, l, l, this.mirror, false);
		return this;
	}

	public ModelPart addBox(float f, float g, float h, float i, float j, float k) {
		this.addBox(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, 0.0F, 0.0F, 0.0F, this.mirror, false);
		return this;
	}

	public ModelPart addBox(float f, float g, float h, float i, float j, float k, boolean bl) {
		this.addBox(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, 0.0F, 0.0F, 0.0F, bl, false);
		return this;
	}

	public void addBox(float f, float g, float h, float i, float j, float k, float l) {
		this.addBox(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l, l, l, this.mirror, false);
	}

	public void addBox(float f, float g, float h, float i, float j, float k, float l, float m, float n) {
		this.addBox(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l, m, n, this.mirror, false);
	}

	public void addBox(float f, float g, float h, float i, float j, float k, float l, boolean bl) {
		this.addBox(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l, l, l, bl, false);
	}

	private void addBox(int i, int j, float f, float g, float h, float k, float l, float m, float n, float o, float p, boolean bl, boolean bl2) {
		this.cubes.add(new ModelPart.Cube(i, j, f, g, h, k, l, m, n, o, p, bl, this.xTexSize, this.yTexSize));
	}

	public void setPos(float f, float g, float h) {
		this.x = f;
		this.y = g;
		this.z = h;
	}

	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, float f, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite) {
		this.render(poseStack, vertexConsumer, f, i, j, textureAtlasSprite, 1.0F, 1.0F, 1.0F);
	}

	public void render(
		PoseStack poseStack, VertexConsumer vertexConsumer, float f, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite, float g, float h, float k
	) {
		if (this.visible) {
			if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
				poseStack.pushPose();
				this.translateAndRotate(poseStack, f);
				this.compile(poseStack.getPose(), vertexConsumer, f, i, j, textureAtlasSprite, g, h, k);

				for (ModelPart modelPart : this.children) {
					modelPart.render(poseStack, vertexConsumer, f, i, j, textureAtlasSprite, g, h, k);
				}

				poseStack.popPose();
			}
		}
	}

	public void translateAndRotate(PoseStack poseStack, float f) {
		poseStack.translate((double)(this.x * f), (double)(this.y * f), (double)(this.z * f));
		if (this.zRot != 0.0F) {
			poseStack.mulPose(Vector3f.ZP.rotation(this.zRot));
		}

		if (this.yRot != 0.0F) {
			poseStack.mulPose(Vector3f.YP.rotation(this.yRot));
		}

		if (this.xRot != 0.0F) {
			poseStack.mulPose(Vector3f.XP.rotation(this.xRot));
		}
	}

	private void compile(
		Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite, float g, float h, float k
	) {
		Matrix3f matrix3f = new Matrix3f(matrix4f);

		for (ModelPart.Cube cube : this.cubes) {
			for (ModelPart.Polygon polygon : cube.polygons) {
				Vector3f vector3f = new Vector3f(polygon.vertices[1].pos.vectorTo(polygon.vertices[0].pos));
				Vector3f vector3f2 = new Vector3f(polygon.vertices[1].pos.vectorTo(polygon.vertices[2].pos));
				vector3f.transform(matrix3f);
				vector3f2.transform(matrix3f);
				vector3f2.cross(vector3f);
				vector3f2.normalize();
				float l = vector3f2.x();
				float m = vector3f2.y();
				float n = vector3f2.z();

				for (int o = 0; o < 4; o++) {
					ModelPart.Vertex vertex = polygon.vertices[o];
					Vector4f vector4f = new Vector4f((float)vertex.pos.x * f, (float)vertex.pos.y * f, (float)vertex.pos.z * f, 1.0F);
					vector4f.transform(matrix4f);
					float p;
					float q;
					if (textureAtlasSprite == null) {
						p = vertex.u;
						q = vertex.v;
					} else {
						p = textureAtlasSprite.getU((double)(vertex.u * 16.0F));
						q = textureAtlasSprite.getV((double)(vertex.v * 16.0F));
					}

					vertexConsumer.vertex((double)vector4f.x(), (double)vector4f.y(), (double)vector4f.z())
						.color(g, h, k, 1.0F)
						.uv(p, q)
						.overlayCoords(j)
						.uv2(i)
						.normal(l, m, n)
						.endVertex();
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

		public Cube(int i, int j, float f, float g, float h, float k, float l, float m, float n, float o, float p, boolean bl, float q, float r) {
			this.minX = f;
			this.minY = g;
			this.minZ = h;
			this.maxX = f + k;
			this.maxY = g + l;
			this.maxZ = h + m;
			this.polygons = new ModelPart.Polygon[6];
			float s = f + k;
			float t = g + l;
			float u = h + m;
			f -= n;
			g -= o;
			h -= p;
			s += n;
			t += o;
			u += p;
			if (bl) {
				float v = s;
				s = f;
				f = v;
			}

			ModelPart.Vertex vertex = new ModelPart.Vertex(f, g, h, 0.0F, 0.0F);
			ModelPart.Vertex vertex2 = new ModelPart.Vertex(s, g, h, 0.0F, 8.0F);
			ModelPart.Vertex vertex3 = new ModelPart.Vertex(s, t, h, 8.0F, 8.0F);
			ModelPart.Vertex vertex4 = new ModelPart.Vertex(f, t, h, 8.0F, 0.0F);
			ModelPart.Vertex vertex5 = new ModelPart.Vertex(f, g, u, 0.0F, 0.0F);
			ModelPart.Vertex vertex6 = new ModelPart.Vertex(s, g, u, 0.0F, 8.0F);
			ModelPart.Vertex vertex7 = new ModelPart.Vertex(s, t, u, 8.0F, 8.0F);
			ModelPart.Vertex vertex8 = new ModelPart.Vertex(f, t, u, 8.0F, 0.0F);
			float w = (float)i;
			float x = (float)i + m;
			float y = (float)i + m + k;
			float z = (float)i + m + k + k;
			float aa = (float)i + m + k + m;
			float ab = (float)i + m + k + m + k;
			float ac = (float)j;
			float ad = (float)j + m;
			float ae = (float)j + m + l;
			this.polygons[2] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex6, vertex5, vertex, vertex2}, x, ac, y, ad, q, r);
			this.polygons[3] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex3, vertex4, vertex8, vertex7}, y, ad, z, ac, q, r);
			this.polygons[1] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex, vertex5, vertex8, vertex4}, w, ad, x, ae, q, r);
			this.polygons[4] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex2, vertex, vertex4, vertex3}, x, ad, y, ae, q, r);
			this.polygons[0] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex6, vertex2, vertex3, vertex7}, y, ad, aa, ae, q, r);
			this.polygons[5] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex5, vertex6, vertex7, vertex8}, aa, ad, ab, ae, q, r);
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
