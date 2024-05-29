package net.minecraft.client.resources.model;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record ModelResourceLocation(ResourceLocation id, String variant) {
	public static final String INVENTORY_VARIANT = "inventory";

	public ModelResourceLocation(ResourceLocation id, String variant) {
		variant = lowercaseVariant(variant);
		this.id = id;
		this.variant = variant;
	}

	public static ModelResourceLocation vanilla(String string, String string2) {
		return new ModelResourceLocation(ResourceLocation.withDefaultNamespace(string), string2);
	}

	public static ModelResourceLocation inventory(ResourceLocation resourceLocation) {
		return new ModelResourceLocation(resourceLocation, "inventory");
	}

	private static String lowercaseVariant(String string) {
		return string.toLowerCase(Locale.ROOT);
	}

	public String getVariant() {
		return this.variant;
	}

	public String toString() {
		return this.id + "#" + this.variant;
	}
}
