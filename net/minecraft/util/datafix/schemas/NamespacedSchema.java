/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.resources.ResourceLocation;

public class NamespacedSchema
extends Schema {
    public NamespacedSchema(int i, Schema schema) {
        super(i, schema);
    }

    public static String ensureNamespaced(String string) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
        if (resourceLocation != null) {
            return resourceLocation.toString();
        }
        return string;
    }

    @Override
    public Type<?> getChoiceType(DSL.TypeReference typeReference, String string) {
        return super.getChoiceType(typeReference, NamespacedSchema.ensureNamespaced(string));
    }
}

