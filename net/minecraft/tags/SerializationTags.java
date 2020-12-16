/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.tags.StaticTags;
import net.minecraft.tags.TagContainer;

public class SerializationTags {
    private static volatile TagContainer instance = StaticTags.createCollection();

    public static TagContainer getInstance() {
        return instance;
    }

    public static void bind(TagContainer tagContainer) {
        instance = tagContainer;
    }
}

