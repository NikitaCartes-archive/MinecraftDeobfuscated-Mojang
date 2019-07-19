/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.datafixers.Dynamic;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Deserializer<T> {
    public static final Logger LOGGER = LogManager.getLogger();

    public T deserialize(Dynamic<?> var1);

    public static <T, V, U extends Deserializer<V>> V deserialize(Dynamic<T> dynamic, Registry<U> registry, String string, V object) {
        Object object2;
        Deserializer deserializer = (Deserializer)registry.get(new ResourceLocation(dynamic.get(string).asString("")));
        if (deserializer != null) {
            object2 = deserializer.deserialize(dynamic);
        } else {
            LOGGER.error("Unknown type {}, replacing with {}", (Object)dynamic.get(string).asString(""), (Object)object);
            object2 = object;
        }
        return (V)object2;
    }
}

