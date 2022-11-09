package net.minecraft.client.resources;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DefaultPlayerSkin {
	private static final DefaultPlayerSkin.SkinType[] DEFAULT_SKINS = new DefaultPlayerSkin.SkinType[]{
		new DefaultPlayerSkin.SkinType("textures/entity/player/slim/alex.png", DefaultPlayerSkin.ModelType.SLIM),
		new DefaultPlayerSkin.SkinType("textures/entity/player/slim/ari.png", DefaultPlayerSkin.ModelType.SLIM),
		new DefaultPlayerSkin.SkinType("textures/entity/player/slim/efe.png", DefaultPlayerSkin.ModelType.SLIM),
		new DefaultPlayerSkin.SkinType("textures/entity/player/slim/kai.png", DefaultPlayerSkin.ModelType.SLIM),
		new DefaultPlayerSkin.SkinType("textures/entity/player/slim/makena.png", DefaultPlayerSkin.ModelType.SLIM),
		new DefaultPlayerSkin.SkinType("textures/entity/player/slim/noor.png", DefaultPlayerSkin.ModelType.SLIM),
		new DefaultPlayerSkin.SkinType("textures/entity/player/slim/steve.png", DefaultPlayerSkin.ModelType.SLIM),
		new DefaultPlayerSkin.SkinType("textures/entity/player/slim/sunny.png", DefaultPlayerSkin.ModelType.SLIM),
		new DefaultPlayerSkin.SkinType("textures/entity/player/slim/zuri.png", DefaultPlayerSkin.ModelType.SLIM),
		new DefaultPlayerSkin.SkinType("textures/entity/player/wide/alex.png", DefaultPlayerSkin.ModelType.WIDE),
		new DefaultPlayerSkin.SkinType("textures/entity/player/wide/ari.png", DefaultPlayerSkin.ModelType.WIDE),
		new DefaultPlayerSkin.SkinType("textures/entity/player/wide/efe.png", DefaultPlayerSkin.ModelType.WIDE),
		new DefaultPlayerSkin.SkinType("textures/entity/player/wide/kai.png", DefaultPlayerSkin.ModelType.WIDE),
		new DefaultPlayerSkin.SkinType("textures/entity/player/wide/makena.png", DefaultPlayerSkin.ModelType.WIDE),
		new DefaultPlayerSkin.SkinType("textures/entity/player/wide/noor.png", DefaultPlayerSkin.ModelType.WIDE),
		new DefaultPlayerSkin.SkinType("textures/entity/player/wide/steve.png", DefaultPlayerSkin.ModelType.WIDE),
		new DefaultPlayerSkin.SkinType("textures/entity/player/wide/sunny.png", DefaultPlayerSkin.ModelType.WIDE),
		new DefaultPlayerSkin.SkinType("textures/entity/player/wide/zuri.png", DefaultPlayerSkin.ModelType.WIDE)
	};

	public static ResourceLocation getDefaultSkin() {
		return DEFAULT_SKINS[6].texture();
	}

	public static ResourceLocation getDefaultSkin(UUID uUID) {
		return getSkinType(uUID).texture;
	}

	public static String getSkinModelName(UUID uUID) {
		return getSkinType(uUID).model.id;
	}

	private static DefaultPlayerSkin.SkinType getSkinType(UUID uUID) {
		return DEFAULT_SKINS[Math.floorMod(uUID.hashCode(), DEFAULT_SKINS.length)];
	}

	@Environment(EnvType.CLIENT)
	static enum ModelType {
		SLIM("slim"),
		WIDE("default");

		final String id;

		private ModelType(String string2) {
			this.id = string2;
		}
	}

	@Environment(EnvType.CLIENT)
	static record SkinType(ResourceLocation texture, DefaultPlayerSkin.ModelType model) {

		public SkinType(String string, DefaultPlayerSkin.ModelType modelType) {
			this(new ResourceLocation(string), modelType);
		}
	}
}
