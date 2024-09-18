package com.mojang.blaze3d.buffers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum BufferType {
	VERTICES(34962),
	INDICES(34963),
	PIXEL_PACK(35051),
	COPY_READ(36662),
	COPY_WRITE(36663),
	PIXEL_UNPACK(35052),
	UNIFORM(35345);

	final int id;

	private BufferType(final int j) {
		this.id = j;
	}
}
