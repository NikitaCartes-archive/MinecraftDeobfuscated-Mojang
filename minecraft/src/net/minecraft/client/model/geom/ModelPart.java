package net.minecraft.client.model.geom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;

@Environment(EnvType.CLIENT)
public final class ModelPart {
	public float x;
	public float y;
	public float z;
	public float xRot;
	public float yRot;
	public float zRot;
	public boolean visible = true;
	private final List<ModelPart.Cube> cubes;
	private final Map<String, ModelPart> children;

	public ModelPart(List<ModelPart.Cube> list, Map<String, ModelPart> map) {
		this.cubes = list;
		this.children = map;
	}

	public PartPose storePose() {
		return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
	}

	public void loadPose(PartPose partPose) {
		this.x = partPose.x;
		this.y = partPose.y;
		this.z = partPose.z;
		this.xRot = partPose.xRot;
		this.yRot = partPose.yRot;
		this.zRot = partPose.zRot;
	}

	public void copyFrom(ModelPart modelPart) {
		this.xRot = modelPart.xRot;
		this.yRot = modelPart.yRot;
		this.zRot = modelPart.zRot;
		this.x = modelPart.x;
		this.y = modelPart.y;
		this.z = modelPart.z;
	}

	public ModelPart getChild(String string) {
		ModelPart modelPart = (ModelPart)this.children.get(string);
		if (modelPart == null) {
			throw new NoSuchElementException("Can't find part " + string);
		} else {
			return modelPart;
		}
	}

	public void setPos(float f, float g, float h) {
		this.x = f;
		this.y = g;
		this.z = h;
	}

	public void setRotation(float f, float g, float h) {
		this.xRot = f;
		this.yRot = g;
		this.zRot = h;
	}

	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
		this.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		if (this.visible) {
			if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
				poseStack.pushPose();
				this.translateAndRotate(poseStack);
				this.compile(poseStack.last(), vertexConsumer, i, j, f, g, h, k);

				for (ModelPart modelPart : this.children.values()) {
					modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k);
				}

				poseStack.popPose();
			}
		}
	}

	public void visit(PoseStack poseStack, ModelPart.Visitor visitor) {
		this.visit(poseStack, visitor, "");
	}

	private void visit(PoseStack poseStack, ModelPart.Visitor visitor, String string) {
		if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
			poseStack.pushPose();
			this.translateAndRotate(poseStack);
			PoseStack.Pose pose = poseStack.last();

			for (int i = 0; i < this.cubes.size(); i++) {
				visitor.visit(pose, string, i, (ModelPart.Cube)this.cubes.get(i));
			}

			String string2 = string + "/";
			this.children.forEach((string2x, modelPart) -> modelPart.visit(poseStack, visitor, string2 + string2x));
			poseStack.popPose();
		}
	}

	public void translateAndRotate(PoseStack poseStack) {
		poseStack.translate((double)(this.x / 16.0F), (double)(this.y / 16.0F), (double)(this.z / 16.0F));
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

	private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		for (ModelPart.Cube cube : this.cubes) {
			cube.compile(pose, vertexConsumer, i, j, f, g, h, k);
		}
	}

	public ModelPart.Cube getRandomCube(Random random) {
		return (ModelPart.Cube)this.cubes.get(random.nextInt(this.cubes.size()));
	}

	public boolean isEmpty() {
		return this.cubes.isEmpty();
	}

	public Stream<ModelPart> getAllParts() {
		return Stream.concat(Stream.of(this), this.children.values().stream().flatMap(ModelPart::getAllParts));
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
			this.polygons[2] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex6, vertex5, vertex, vertex2}, x, ac, y, ad, q, r, bl, Direction.DOWN);
			this.polygons[3] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex3, vertex4, vertex8, vertex7}, y, ad, z, ac, q, r, bl, Direction.UP);
			this.polygons[1] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex, vertex5, vertex8, vertex4}, w, ad, x, ae, q, r, bl, Direction.WEST);
			this.polygons[4] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex2, vertex, vertex4, vertex3}, x, ad, y, ae, q, r, bl, Direction.NORTH);
			this.polygons[0] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex6, vertex2, vertex3, vertex7}, y, ad, aa, ae, q, r, bl, Direction.EAST);
			this.polygons[5] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex5, vertex6, vertex7, vertex8}, aa, ad, ab, ae, q, r, bl, Direction.SOUTH);
		}

		public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
			Matrix4f matrix4f = pose.pose();
			Matrix3f matrix3f = pose.normal();

			for (ModelPart.Polygon polygon : this.polygons) {
				Vector3f vector3f = polygon.normal.copy();
				vector3f.transform(matrix3f);
				float l = vector3f.x();
				float m = vector3f.y();
				float n = vector3f.z();

				for (ModelPart.Vertex vertex : polygon.vertices) {
					float o = vertex.pos.x() / 16.0F;
					float p = vertex.pos.y() / 16.0F;
					float q = vertex.pos.z() / 16.0F;
					Vector4f vector4f = new Vector4f(o, p, q, 1.0F);
					vector4f.transform(matrix4f);
					vertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), f, g, h, k, vertex.u, vertex.v, j, i, l, m, n);
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class Polygon {
		public final ModelPart.Vertex[] vertices;
		public final Vector3f normal;

		public Polygon(ModelPart.Vertex[] vertexs, float f, float g, float h, float i, float j, float k, boolean bl, Direction direction) {
			this.vertices = vertexs;
			float l = 0.0F / j;
			float m = 0.0F / k;
			vertexs[0] = vertexs[0].remap(h / j - l, g / k + m);
			vertexs[1] = vertexs[1].remap(f / j + l, g / k + m);
			vertexs[2] = vertexs[2].remap(f / j + l, i / k - m);
			vertexs[3] = vertexs[3].remap(h / j - l, i / k - m);
			if (bl) {
				int n = vertexs.length;

				for (int o = 0; o < n / 2; o++) {
					ModelPart.Vertex vertex = vertexs[o];
					vertexs[o] = vertexs[n - 1 - o];
					vertexs[n - 1 - o] = vertex;
				}
			}

			this.normal = direction.step();
			if (bl) {
				this.normal.mul(-1.0F, 1.0F, 1.0F);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class Vertex {
		public final Vector3f pos;
		public final float u;
		public final float v;

		public Vertex(float f, float g, float h, float i, float j) {
			this(new Vector3f(f, g, h), i, j);
		}

		public ModelPart.Vertex remap(float f, float g) {
			return new ModelPart.Vertex(this.pos, f, g);
		}

		public Vertex(Vector3f vector3f, float f, float g) {
			this.pos = vector3f;
			this.u = f;
			this.v = g;
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Visitor {
		void visit(PoseStack.Pose pose, String string, int i, ModelPart.Cube cube);
	}
}
