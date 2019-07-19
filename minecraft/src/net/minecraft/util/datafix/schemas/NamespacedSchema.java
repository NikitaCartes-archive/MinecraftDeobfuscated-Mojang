package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.resources.ResourceLocation;

public class NamespacedSchema extends Schema {
	public NamespacedSchema(int i, Schema schema) {
		super(i, schema);
	}

	public static String ensureNamespaced(String string) {
		ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
		return resourceLocation != null ? resourceLocation.toString() : string;
	}

	@Override
	public Type<?> getChoiceType(TypeReference typeReference, String string) {
		return super.getChoiceType(typeReference, ensureNamespaced(string));
	}
}
