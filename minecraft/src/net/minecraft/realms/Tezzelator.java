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

	public void begin(int i, RealmsVertexFormat realmsVertexFormat) {
		t.getBuilder().begin(i, realmsVertexFormat.getVertexFormat());
	}

	public void endVertex() {
		t.getBuilder().endVertex();
	}

	public Tezzelator color(int i, int j, int k, int l) {
		t.getBuilder().color(i, j, k, l);
		return this;
	}

	public Tezzelator tex(float f, float g) {
		t.getBuilder().uv(f, g);
		return this;
	}
}
