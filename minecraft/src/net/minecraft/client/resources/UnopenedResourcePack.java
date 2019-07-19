package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.UnopenedPack;

@Environment(EnvType.CLIENT)
public class UnopenedResourcePack extends UnopenedPack {
	@Nullable
	private NativeImage icon;
	@Nullable
	private ResourceLocation iconLocation;

	public UnopenedResourcePack(
		String string, boolean bl, Supplier<Pack> supplier, Pack pack, PackMetadataSection packMetadataSection, UnopenedPack.Position position
	) {
		super(string, bl, supplier, pack, packMetadataSection, position);
		NativeImage nativeImage = null;

		try {
			InputStream inputStream = pack.getRootResource("pack.png");
			Throwable var9 = null;

			try {
				nativeImage = NativeImage.read(inputStream);
			} catch (Throwable var19) {
				var9 = var19;
				throw var19;
			} finally {
				if (inputStream != null) {
					if (var9 != null) {
						try {
							inputStream.close();
						} catch (Throwable var18) {
							var9.addSuppressed(var18);
						}
					} else {
						inputStream.close();
					}
				}
			}
		} catch (IllegalArgumentException | IOException var21) {
		}

		this.icon = nativeImage;
	}

	public UnopenedResourcePack(
		String string,
		boolean bl,
		Supplier<Pack> supplier,
		Component component,
		Component component2,
		PackCompatibility packCompatibility,
		UnopenedPack.Position position,
		boolean bl2,
		@Nullable NativeImage nativeImage
	) {
		super(string, bl, supplier, component, component2, packCompatibility, position, bl2);
		this.icon = nativeImage;
	}

	public void bindIcon(TextureManager textureManager) {
		if (this.iconLocation == null) {
			if (this.icon == null) {
				this.iconLocation = new ResourceLocation("textures/misc/unknown_pack.png");
			} else {
				this.iconLocation = textureManager.register("texturepackicon", new DynamicTexture(this.icon));
			}
		}

		textureManager.bind(this.iconLocation);
	}

	@Override
	public void close() {
		super.close();
		if (this.icon != null) {
			this.icon.close();
			this.icon = null;
		}
	}
}
