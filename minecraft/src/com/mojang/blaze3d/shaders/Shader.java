package com.mojang.blaze3d.shaders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Shader {
	int getId();

	void markDirty();

	Program getVertexProgram();

	Program getFragmentProgram();

	void attachToProgram();
}
