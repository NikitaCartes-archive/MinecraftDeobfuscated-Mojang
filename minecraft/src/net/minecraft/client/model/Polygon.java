package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class Polygon {
	public Vertex[] vertices;
	public final int vertexCount;
	private boolean flipNormal;

	public Polygon(Vertex[] vertexs) {
		this.vertices = vertexs;
		this.vertexCount = vertexs.length;
	}

	public Polygon(Vertex[] vertexs, int i, int j, int k, int l, float f, float g) {
		this(vertexs);
		float h = 0.0F / f;
		float m = 0.0F / g;
		vertexs[0] = vertexs[0].remap((float)k / f - h, (float)j / g + m);
		vertexs[1] = vertexs[1].remap((float)i / f + h, (float)j / g + m);
		vertexs[2] = vertexs[2].remap((float)i / f + h, (float)l / g - m);
		vertexs[3] = vertexs[3].remap((float)k / f - h, (float)l / g - m);
	}

	public void mirror() {
		Vertex[] vertexs = new Vertex[this.vertices.length];

		for (int i = 0; i < this.vertices.length; i++) {
			vertexs[i] = this.vertices[this.vertices.length - i - 1];
		}

		this.vertices = vertexs;
	}

	public void render(BufferBuilder bufferBuilder, float f) {
		Vec3 vec3 = this.vertices[1].pos.vectorTo(this.vertices[0].pos);
		Vec3 vec32 = this.vertices[1].pos.vectorTo(this.vertices[2].pos);
		Vec3 vec33 = vec32.cross(vec3).normalize();
		float g = (float)vec33.x;
		float h = (float)vec33.y;
		float i = (float)vec33.z;
		if (this.flipNormal) {
			g = -g;
			h = -h;
			i = -i;
		}

		bufferBuilder.begin(7, DefaultVertexFormat.ENTITY);

		for (int j = 0; j < 4; j++) {
			Vertex vertex = this.vertices[j];
			bufferBuilder.vertex(vertex.pos.x * (double)f, vertex.pos.y * (double)f, vertex.pos.z * (double)f)
				.uv((double)vertex.u, (double)vertex.v)
				.normal(g, h, i)
				.endVertex();
		}

		Tesselator.getInstance().end();
	}
}
