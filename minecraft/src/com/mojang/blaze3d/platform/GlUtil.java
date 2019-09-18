package com.mojang.blaze3d.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GlUtil {
	public static String getVendor() {
		return "GLU.getVendor";
	}

	public static String getCpuInfo() {
		return GLX._getCpuInfo();
	}

	public static String getRenderer() {
		return "GLU.getRenderer";
	}

	public static String getOpenGLVersion() {
		return "GLU.getOpenGLVersion";
	}
}
