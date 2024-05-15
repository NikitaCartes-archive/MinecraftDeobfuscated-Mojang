package com.mojang.blaze3d.vertex;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Tesselator {
	private static final int MAX_BYTES = 786432;
	private final BufferBuilder builder;
	@Nullable
	private static Tesselator instance;

	public static void init() {
		if (instance != null) {
			throw new IllegalStateException("Tesselator has already been initialized");
		} else {
			instance = new Tesselator();
		}
	}

	public static Tesselator getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Tesselator has not been initialized");
		} else {
			return instance;
		}
	}

	public Tesselator(int i) {
		this.builder = new BufferBuilder(i);
	}

	public Tesselator() {
		this(786432);
	}

	public void end() {
		BufferUploader.drawWithShader(this.builder.end());
	}

	public BufferBuilder getBuilder() {
		return this.builder;
	}
}
