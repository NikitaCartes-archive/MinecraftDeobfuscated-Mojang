package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class Vertex {
	public final Vec3 pos;
	public final float u;
	public final float v;

	public Vertex(float f, float g, float h, float i, float j) {
		this(new Vec3((double)f, (double)g, (double)h), i, j);
	}

	public Vertex remap(float f, float g) {
		return new Vertex(this, f, g);
	}

	public Vertex(Vertex vertex, float f, float g) {
		this.pos = vertex.pos;
		this.u = f;
		this.v = g;
	}

	public Vertex(Vec3 vec3, float f, float g) {
		this.pos = vec3;
		this.u = f;
		this.v = g;
	}
}
