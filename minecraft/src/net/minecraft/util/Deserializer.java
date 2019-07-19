package net.minecraft.util;

import com.mojang.datafixers.Dynamic;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Deserializer<T> {
	Logger LOGGER = LogManager.getLogger();

	T deserialize(Dynamic<?> dynamic);

	static <T, V, U extends Deserializer<V>> V deserialize(Dynamic<T> dynamic, Registry<U> registry, String string, V object) {
		U deserializer = (U)registry.get(new ResourceLocation(dynamic.get(string).asString("")));
		V object2;
		if (deserializer != null) {
			object2 = deserializer.deserialize(dynamic);
		} else {
			LOGGER.error("Unknown type {}, replacing with {}", dynamic.get(string).asString(""), object);
			object2 = object;
		}

		return object2;
	}
}
