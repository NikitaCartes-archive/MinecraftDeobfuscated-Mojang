package net.minecraft.data.models.blockstates;

import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

public class VariantProperties {
	public static final VariantProperty<VariantProperties.Rotation> X_ROT = new VariantProperty<>("x", rotation -> new JsonPrimitive(rotation.value));
	public static final VariantProperty<VariantProperties.Rotation> Y_ROT = new VariantProperty<>("y", rotation -> new JsonPrimitive(rotation.value));
	public static final VariantProperty<ResourceLocation> MODEL = new VariantProperty<>(
		"model", resourceLocation -> new JsonPrimitive(resourceLocation.toString())
	);
	public static final VariantProperty<Boolean> UV_LOCK = new VariantProperty<>("uvlock", JsonPrimitive::new);
	public static final VariantProperty<Integer> WEIGHT = new VariantProperty<>("weight", JsonPrimitive::new);

	public static enum Rotation {
		R0(0),
		R90(90),
		R180(180),
		R270(270);

		final int value;

		private Rotation(int j) {
			this.value = j;
		}
	}
}
