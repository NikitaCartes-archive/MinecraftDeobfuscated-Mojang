package com.mojang.blaze3d.pipeline;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface RenderCall {
	void execute();
}
