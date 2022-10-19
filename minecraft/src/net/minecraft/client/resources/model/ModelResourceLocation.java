package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ModelResourceLocation extends ResourceLocation {
	@VisibleForTesting
	static final char VARIANT_SEPARATOR = '#';
	private final String variant;

	private ModelResourceLocation(String string, String string2, String string3, @Nullable ResourceLocation.Dummy dummy) {
		super(string, string2, dummy);
		this.variant = string3;
	}

	public ModelResourceLocation(String string, String string2, String string3) {
		super(string, string2);
		this.variant = lowercaseVariant(string3);
	}

	public ModelResourceLocation(ResourceLocation resourceLocation, String string) {
		this(resourceLocation.getNamespace(), resourceLocation.getPath(), lowercaseVariant(string), null);
	}

	public static ModelResourceLocation vanilla(String string, String string2) {
		return new ModelResourceLocation("minecraft", string, string2);
	}

	private static String lowercaseVariant(String string) {
		return string.toLowerCase(Locale.ROOT);
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
		return super.toString() + "#" + this.variant;
	}
}
