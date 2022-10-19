package net.minecraft.client.renderer.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface SpriteTicker extends AutoCloseable {
	void tickAndUpload(int i, int j);

	void close();
}
