package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Tesselator {
	private final BufferBuilder builder;
	private final BufferUploader uploader = new BufferUploader();
	private static final Tesselator INSTANCE = new Tesselator(2097152);

	public static Tesselator getInstance() {
		return INSTANCE;
	}

	public Tesselator(int i) {
		this.builder = new BufferBuilder(i);
	}

	public void end() {
		this.builder.end();
		this.uploader.end(this.builder);
	}

	public BufferBuilder getBuilder() {
		return this.builder;
	}
}
