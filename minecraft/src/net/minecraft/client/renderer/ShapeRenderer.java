package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class ShapeRenderer {
	public static void renderShape(
		PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j
	) {
		PoseStack.Pose pose = poseStack.last();
		voxelShape.forAllEdges((k, l, m, n, o, p) -> {
			Vector3f vector3f = new Vector3f((float)(n - k), (float)(o - l), (float)(p - m)).normalize();
			vertexConsumer.addVertex(pose, (float)(k + d), (float)(l + e), (float)(m + f)).setColor(g, h, i, j).setNormal(pose, vector3f);
			vertexConsumer.addVertex(pose, (float)(n + d), (float)(o + e), (float)(p + f)).setColor(g, h, i, j).setNormal(pose, vector3f);
		});
	}

	public static void renderLineBox(PoseStack poseStack, VertexConsumer vertexConsumer, AABB aABB, float f, float g, float h, float i) {
		renderLineBox(poseStack, vertexConsumer, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i, f, g, h);
	}

	public static void renderLineBox(
		PoseStack poseStack, VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m
	) {
		renderLineBox(poseStack, vertexConsumer, d, e, f, g, h, i, j, k, l, m, j, k, l);
	}

	public static void renderLineBox(
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		float j,
		float k,
		float l,
		float m,
		float n,
		float o,
		float p
	) {
		PoseStack.Pose pose = poseStack.last();
		float q = (float)d;
		float r = (float)e;
		float s = (float)f;
		float t = (float)g;
		float u = (float)h;
		float v = (float)i;
		vertexConsumer.addVertex(pose, q, r, s).setColor(j, o, p, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, r, s).setColor(j, o, p, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, r, s).setColor(n, k, p, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, u, s).setColor(n, k, p, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, r, s).setColor(n, o, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, q, r, v).setColor(n, o, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, t, r, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, s).setColor(j, k, l, m).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, u, s).setColor(j, k, l, m).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, u, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, q, u, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, q, u, v).setColor(j, k, l, m).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, r, v).setColor(j, k, l, m).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, r, v).setColor(j, k, l, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, r, v).setColor(j, k, l, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, r, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexConsumer.addVertex(pose, t, r, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexConsumer.addVertex(pose, q, u, v).setColor(j, k, l, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, v).setColor(j, k, l, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, r, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, t, u, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	public static void addChainedFilledBoxVertices(
		PoseStack poseStack, VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m
	) {
		addChainedFilledBoxVertices(poseStack, vertexConsumer, (float)d, (float)e, (float)f, (float)g, (float)h, (float)i, j, k, l, m);
	}

	public static void addChainedFilledBoxVertices(
		PoseStack poseStack, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, float n, float o
	) {
		Matrix4f matrix4f = poseStack.last().pose();
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
	}

	public static void renderFace(
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		Direction direction,
		float f,
		float g,
		float h,
		float i,
		float j,
		float k,
		float l,
		float m,
		float n,
		float o
	) {
		Matrix4f matrix4f = poseStack.last().pose();
		switch (direction) {
			case DOWN:
				vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
				break;
			case UP:
				vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
				break;
			case NORTH:
				vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
				break;
			case SOUTH:
				vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
				break;
			case WEST:
				vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
				break;
			case EAST:
				vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		}
	}

	public static void renderVector(PoseStack poseStack, VertexConsumer vertexConsumer, Vector3f vector3f, Vec3 vec3, int i) {
		PoseStack.Pose pose = poseStack.last();
		vertexConsumer.addVertex(pose, vector3f).setColor(i).setNormal(pose, (float)vec3.x, (float)vec3.y, (float)vec3.z);
		vertexConsumer.addVertex(pose, (float)((double)vector3f.x() + vec3.x), (float)((double)vector3f.y() + vec3.y), (float)((double)vector3f.z() + vec3.z))
			.setColor(i)
			.setNormal(pose, (float)vec3.x, (float)vec3.y, (float)vec3.z);
	}
}
