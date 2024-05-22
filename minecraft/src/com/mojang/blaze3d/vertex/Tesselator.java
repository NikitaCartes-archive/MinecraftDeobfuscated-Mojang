package com.mojang.blaze3d.vertex;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Tesselator {
	private static final int MAX_BYTES = 786432;
	private final ByteBufferBuilder buffer;
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
		this.buffer = new ByteBufferBuilder(i);
	}

	public Tesselator() {
		this(786432);
	}

	public BufferBuilder begin(VertexFormat.Mode mode, VertexFormat vertexFormat) {
		return new BufferBuilder(this.buffer, mode, vertexFormat);
	}

	public void clear() {
		this.buffer.clear();
	}
}
