package net.minecraft.client.resources;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DefaultPlayerSkin {
	private static final ResourceLocation STEVE_SKIN_LOCATION = new ResourceLocation("textures/entity/steve.png");
	private static final ResourceLocation ALEX_SKIN_LOCATION = new ResourceLocation("textures/entity/alex.png");
	private static final String STEVE_MODEL = "default";
	private static final String ALEX_MODEL = "slim";

	public static ResourceLocation getDefaultSkin() {
		return STEVE_SKIN_LOCATION;
	}

	public static ResourceLocation getDefaultSkin(UUID uUID) {
		return isAlexDefault(uUID) ? ALEX_SKIN_LOCATION : STEVE_SKIN_LOCATION;
	}

	public static String getSkinModelName(UUID uUID) {
		return isAlexDefault(uUID) ? "slim" : "default";
	}

	private static boolean isAlexDefault(UUID uUID) {
		return (uUID.hashCode() & 1) == 1;
	}
}
