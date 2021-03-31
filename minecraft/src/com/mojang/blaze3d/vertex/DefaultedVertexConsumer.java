package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class DefaultedVertexConsumer implements VertexConsumer {
	protected boolean defaultColorSet;
	protected int defaultR = 255;
	protected int defaultG = 255;
	protected int defaultB = 255;
	protected int defaultA = 255;

	@Override
	public void defaultColor(int i, int j, int k, int l) {
		this.defaultR = i;
		this.defaultG = j;
		this.defaultB = k;
		this.defaultA = l;
		this.defaultColorSet = true;
	}

	@Override
	public void unsetDefaultColor() {
		this.defaultColorSet = false;
	}
}
