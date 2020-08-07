package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const.PrimitiveType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.resources.ResourceLocation;

public class NamespacedSchema extends Schema {
	public static final PrimitiveCodec<String> NAMESPACED_STRING_CODEC = new PrimitiveCodec<String>() {
		@Override
		public <T> DataResult<String> read(DynamicOps<T> dynamicOps, T object) {
			return dynamicOps.getStringValue(object).map(NamespacedSchema::ensureNamespaced);
		}

		public <T> T write(DynamicOps<T> dynamicOps, String string) {
			return dynamicOps.createString(string);
		}

		public String toString() {
			return "NamespacedString";
		}
	};
	private static final Type<String> NAMESPACED_STRING = new PrimitiveType<>(NAMESPACED_STRING_CODEC);

	public NamespacedSchema(int i, Schema schema) {
		super(i, schema);
	}

	public static String ensureNamespaced(String string) {
		ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
		return resourceLocation != null ? resourceLocation.toString() : string;
	}

	public static Type<String> namespacedString() {
		return NAMESPACED_STRING;
	}

	@Override
	public Type<?> getChoiceType(TypeReference typeReference, String string) {
		return super.getChoiceType(typeReference, ensureNamespaced(string));
	}
}
