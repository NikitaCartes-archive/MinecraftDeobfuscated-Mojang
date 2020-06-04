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
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;

@Environment(EnvType.CLIENT)
public class ResourcePack extends Pack {
	@Nullable
	private NativeImage icon;
	@Nullable
	private ResourceLocation iconLocation;

	public ResourcePack(
		String string,
		boolean bl,
		Supplier<PackResources> supplier,
		PackResources packResources,
		PackMetadataSection packMetadataSection,
		Pack.Position position,
		PackSource packSource
	) {
		super(string, bl, supplier, packResources, packMetadataSection, position, packSource);
		this.icon = readIcon(packResources);
	}

	public ResourcePack(
		String string,
		boolean bl,
		Supplier<PackResources> supplier,
		Component component,
		Component component2,
		PackCompatibility packCompatibility,
		Pack.Position position,
		boolean bl2,
		PackSource packSource,
		@Nullable NativeImage nativeImage
	) {
		super(string, bl, supplier, component, component2, packCompatibility, position, bl2, packSource);
		this.icon = nativeImage;
	}

	@Nullable
	public static NativeImage readIcon(PackResources packResources) {
		try {
			InputStream inputStream = packResources.getRootResource("pack.png");
			Throwable var2 = null;

			NativeImage var3;
			try {
				var3 = NativeImage.read(inputStream);
			} catch (Throwable var13) {
				var2 = var13;
				throw var13;
			} finally {
				if (inputStream != null) {
					if (var2 != null) {
						try {
							inputStream.close();
						} catch (Throwable var12) {
							var2.addSuppressed(var12);
						}
					} else {
						inputStream.close();
					}
				}
			}

			return var3;
		} catch (IllegalArgumentException | IOException var15) {
			return null;
		}
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
