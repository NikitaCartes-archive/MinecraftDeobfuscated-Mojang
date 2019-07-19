package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface HttpTextureProcessor {
	NativeImage process(NativeImage nativeImage);

	void onTextureDownloaded();
}
