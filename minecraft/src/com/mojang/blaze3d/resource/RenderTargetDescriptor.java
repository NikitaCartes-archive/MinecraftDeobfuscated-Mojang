package com.mojang.blaze3d.resource;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record RenderTargetDescriptor(int width, int height, boolean useDepth) implements ResourceDescriptor<RenderTarget> {
	public RenderTarget allocate() {
		return new TextureTarget(this.width, this.height, this.useDepth);
	}

	public void free(RenderTarget renderTarget) {
		renderTarget.destroyBuffers();
	}
}
