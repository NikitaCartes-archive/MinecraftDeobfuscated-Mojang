package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Tesselator {
	private final BufferBuilder builder;
	private static final Tesselator INSTANCE = new Tesselator();

	public static Tesselator getInstance() {
		RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
		return INSTANCE;
	}

	public Tesselator(int i) {
		this.builder = new BufferBuilder(i);
	}

	public Tesselator() {
		this(2097152);
	}

	public void end() {
		this.builder.end();
		BufferUploader.end(this.builder);
	}

	public BufferBuilder getBuilder() {
		return this.builder;
	}
}
