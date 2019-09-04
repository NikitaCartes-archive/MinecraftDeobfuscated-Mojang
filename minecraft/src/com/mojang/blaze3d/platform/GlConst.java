package com.mojang.blaze3d.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GlConst {
	public static int GL_FRAMEBUFFER;
	public static int GL_RENDERBUFFER;
	public static int GL_COLOR_ATTACHMENT0;
	public static int GL_DEPTH_ATTACHMENT;
	public static int GL_FRAMEBUFFER_COMPLETE;
	public static int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
	public static int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
	public static int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
	public static int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;
}
