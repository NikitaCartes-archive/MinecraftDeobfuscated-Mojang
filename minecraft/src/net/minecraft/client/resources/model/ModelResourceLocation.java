package net.minecraft.client.resources.model;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ModelResourceLocation extends ResourceLocation {
	private final String variant;

	protected ModelResourceLocation(String[] strings) {
		super(strings);
		this.variant = strings[2].toLowerCase(Locale.ROOT);
	}

	public ModelResourceLocation(String string) {
		this(decompose(string));
	}

	public ModelResourceLocation(ResourceLocation resourceLocation, String string) {
		this(resourceLocation.toString(), string);
	}

	public ModelResourceLocation(String string, String string2) {
		this(decompose(string + '#' + string2));
	}

	protected static String[] decompose(String string) {
		String[] strings = new String[]{null, string, ""};
		int i = string.indexOf(35);
		String string2 = string;
		if (i >= 0) {
			strings[2] = string.substring(i + 1, string.length());
			if (i > 1) {
				string2 = string.substring(0, i);
			}
		}

		System.arraycopy(ResourceLocation.decompose(string2, ':'), 0, strings, 0, 2);
		return strings;
	}

	public String getVariant() {
		return this.variant;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object instanceof ModelResourceLocation && super.equals(object)) {
			ModelResourceLocation modelResourceLocation = (ModelResourceLocation)object;
			return this.variant.equals(modelResourceLocation.variant);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return 31 * super.hashCode() + this.variant.hashCode();
	}

	@Override
	public String toString() {
		return super.toString() + '#' + this.variant;
	}
}
