package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collection;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class StencilRenderer {
	private static final RenderType[] DYNAMIC_LIGHT = new RenderType[]{RenderType.dynamicLightStencil(), RenderType.dynamicLightColor()};

	public static void render(StencilRenderer.Triangle[] triangles, Matrix4f matrix4f, MultiBufferSource multiBufferSource, int i) {
		int j = FastColor.ARGB32.red(i);
		int k = FastColor.ARGB32.green(i);
		int l = FastColor.ARGB32.blue(i);
		int m = FastColor.ARGB32.alpha(i);

		for (RenderType renderType : DYNAMIC_LIGHT) {
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);

			for (StencilRenderer.Triangle triangle : triangles) {
				vertexConsumer.vertex(matrix4f, triangle.p0.x, triangle.p0.y, triangle.p0.z).color(j, k, l, m).endVertex();
				vertexConsumer.vertex(matrix4f, triangle.p2.x, triangle.p2.y, triangle.p2.z).color(j, k, l, m).endVertex();
				vertexConsumer.vertex(matrix4f, triangle.p1.x, triangle.p1.y, triangle.p1.z).color(j, k, l, m).endVertex();
			}
		}
	}

	private static List<StencilRenderer.Triangle> makeOctahedron() {
		float f = (float)(1.0 / Math.sqrt(2.0));
		Vector3f[] vector3fs = new Vector3f[]{
			new Vector3f(0.0F, 1.0F, 0.0F),
			new Vector3f(0.0F, -1.0F, 0.0F),
			new Vector3f(-1.0F, 0.0F, -1.0F).mul(f),
			new Vector3f(1.0F, 0.0F, -1.0F).mul(f),
			new Vector3f(1.0F, 0.0F, 1.0F).mul(f),
			new Vector3f(-1.0F, 0.0F, 1.0F).mul(f)
		};
		List<StencilRenderer.Triangle> list = Lists.<StencilRenderer.Triangle>newArrayList();
		list.add(new StencilRenderer.Triangle(vector3fs[0], vector3fs[3], vector3fs[4]));
		list.add(new StencilRenderer.Triangle(vector3fs[0], vector3fs[4], vector3fs[5]));
		list.add(new StencilRenderer.Triangle(vector3fs[0], vector3fs[5], vector3fs[2]));
		list.add(new StencilRenderer.Triangle(vector3fs[0], vector3fs[2], vector3fs[3]));
		list.add(new StencilRenderer.Triangle(vector3fs[1], vector3fs[4], vector3fs[3]));
		list.add(new StencilRenderer.Triangle(vector3fs[1], vector3fs[5], vector3fs[4]));
		list.add(new StencilRenderer.Triangle(vector3fs[1], vector3fs[2], vector3fs[5]));
		list.add(new StencilRenderer.Triangle(vector3fs[1], vector3fs[3], vector3fs[2]));
		return list;
	}

	public static StencilRenderer.Triangle[] createNSphere(int i) {
		List<StencilRenderer.Triangle> list = makeOctahedron();

		for (int j = 0; j < i; j++) {
			int k = list.size();

			for (int l = 0; l < k; l++) {
				list.addAll(((StencilRenderer.Triangle)list.remove(0)).subdivideSpherical());
			}
		}

		return (StencilRenderer.Triangle[])list.toArray(new StencilRenderer.Triangle[0]);
	}

	public static StencilRenderer.Triangle[] createNCone(int i) {
		float f = (float) (Math.PI * 2) / (float)i;
		List<Vector3f> list = Lists.<Vector3f>newArrayList();

		for (int j = 0; j < i; j++) {
			list.add(new Vector3f(Mth.cos((float)j * f), 0.0F, Mth.sin((float)j * f)));
		}

		Vector3f vector3f = new Vector3f(0.0F, 0.0F, 0.0F);
		Vector3f vector3f2 = new Vector3f(0.0F, -1.0F, 0.0F);
		List<StencilRenderer.Triangle> list2 = Lists.<StencilRenderer.Triangle>newArrayList();

		for (int k = 0; k < i; k++) {
			list2.add(new StencilRenderer.Triangle((Vector3f)list.get(k), (Vector3f)list.get((k + 1) % i), vector3f));
			list2.add(new StencilRenderer.Triangle((Vector3f)list.get((k + 1) % i), (Vector3f)list.get(k), vector3f2));
		}

		return (StencilRenderer.Triangle[])list2.toArray(new StencilRenderer.Triangle[0]);
	}

	@Environment(EnvType.CLIENT)
	public static record Triangle(Vector3f p0, Vector3f p1, Vector3f p2) {

		Collection<StencilRenderer.Triangle> subdivideSpherical() {
			Vector3f vector3f = this.p0.add(this.p1, new Vector3f()).div(2.0F).normalize();
			Vector3f vector3f2 = this.p1.add(this.p2, new Vector3f()).div(2.0F).normalize();
			Vector3f vector3f3 = this.p2.add(this.p0, new Vector3f()).div(2.0F).normalize();
			return List.of(
				new StencilRenderer.Triangle(this.p0, vector3f, vector3f3),
				new StencilRenderer.Triangle(vector3f, this.p1, vector3f2),
				new StencilRenderer.Triangle(vector3f2, this.p2, vector3f3),
				new StencilRenderer.Triangle(vector3f, vector3f2, vector3f3)
			);
		}
	}
}
