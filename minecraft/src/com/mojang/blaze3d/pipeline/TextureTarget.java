package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TextureTarget extends RenderTarget {
	public TextureTarget(int i, int j, boolean bl) {
		super(bl);
		RenderSystem.assertOnRenderThreadOrInit();
		this.resize(i, j);
	}
}
