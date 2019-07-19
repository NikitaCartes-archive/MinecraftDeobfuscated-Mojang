package net.minecraft.realms;

import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Tezzelator {
	public static final Tesselator t = Tesselator.getInstance();
	public static final Tezzelator instance = new Tezzelator();

	public void end() {
		t.end();
	}

	public Tezzelator vertex(double d, double e, double f) {
		t.getBuilder().vertex(d, e, f);
		return this;
	}

	public void color(float f, float g, float h, float i) {
		t.getBuilder().color(f, g, h, i);
	}

	public void tex2(short s, short t) {
		Tezzelator.t.getBuilder().uv2(s, t);
	}

	public void normal(float f, float g, float h) {
		t.getBuilder().normal(f, g, h);
	}

	public void begin(int i, RealmsVertexFormat realmsVertexFormat) {
		t.getBuilder().begin(i, realmsVertexFormat.getVertexFormat());
	}

	public void endVertex() {
		t.getBuilder().endVertex();
	}

	public void offset(double d, double e, double f) {
		t.getBuilder().offset(d, e, f);
	}

	public RealmsBufferBuilder color(int i, int j, int k, int l) {
		return new RealmsBufferBuilder(t.getBuilder().color(i, j, k, l));
	}

	public Tezzelator tex(double d, double e) {
		t.getBuilder().uv(d, e);
		return this;
	}
}
