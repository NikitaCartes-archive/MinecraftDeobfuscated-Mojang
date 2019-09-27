package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class BreakingTextureGenerator extends DefaultedVertexConsumer {
	private final VertexConsumer delegate;
	private final double camX;
	private final double camY;
	private final double camZ;
	private double x;
	private double y;
	private double z;
	private int r;
	private int g;
	private int b;
	private int a;
	private int overlayU;
	private int overlayV;
	private int lightCoords;
	private float nx;
	private float ny;
	private float nz;

	public BreakingTextureGenerator(VertexConsumer vertexConsumer, double d, double e, double f) {
		this.delegate = vertexConsumer;
		this.camX = d;
		this.camY = e;
		this.camZ = f;
		this.resetState();
	}

	private void resetState() {
		this.x = 0.0;
		this.y = 0.0;
		this.z = 0.0;
		this.r = this.defaultR;
		this.g = this.defaultG;
		this.b = this.defaultB;
		this.a = this.defaultA;
		this.overlayU = this.defaultOverlayU;
		this.overlayV = this.defaultOverlayV;
		this.lightCoords = 15728880;
		this.nx = 0.0F;
		this.ny = 1.0F;
		this.nz = 0.0F;
	}

	@Override
	public void endVertex() {
		Direction direction = Direction.getNearest(this.nx, this.ny, this.nz);
		double d = this.x + this.camX;
		double e = this.y + this.camY;
		double f = this.z + this.camZ;
		double g;
		double h;
		switch (direction.getAxis()) {
			case X:
				g = f;
				h = e;
				break;
			case Y:
				g = d;
				h = f;
				break;
			case Z:
			default:
				g = d;
				h = e;
		}

		float i = (float)(Mth.frac(g / 256.0) * 256.0);
		float j = (float)(Mth.frac(h / 256.0) * 256.0);
		this.delegate
			.vertex(this.x, this.y, this.z)
			.color(this.r, this.g, this.b, this.a)
			.uv(i, j)
			.overlayCoords(this.overlayU, this.overlayV)
			.uv2(this.lightCoords)
			.normal(this.nx, this.ny, this.nz)
			.endVertex();
		this.resetState();
	}

	@Override
	public VertexConsumer vertex(double d, double e, double f) {
		this.x = d;
		this.y = e;
		this.z = f;
		return this;
	}

	@Override
	public VertexConsumer color(int i, int j, int k, int l) {
		if (this.defaultColorSet) {
			throw new IllegalStateException();
		} else {
			this.r = i;
			this.g = j;
			this.b = k;
			this.a = l;
			return this;
		}
	}

	@Override
	public VertexConsumer uv(float f, float g) {
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int i, int j) {
		if (this.defaultOverlayCoordsSet) {
			throw new IllegalStateException();
		} else {
			this.overlayU = i;
			this.overlayV = j;
			return this;
		}
	}

	@Override
	public VertexConsumer uv2(int i, int j) {
		this.lightCoords = i | j << 16;
		return this;
	}

	@Override
	public VertexConsumer normal(float f, float g, float h) {
		this.nx = f;
		this.ny = g;
		this.nz = h;
		return this;
	}
}
