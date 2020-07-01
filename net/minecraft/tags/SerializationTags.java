/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagContainer;

public class SerializationTags {
    private static volatile TagContainer instance = TagContainer.of(BlockTags.getAllTags(), ItemTags.getAllTags(), FluidTags.getAllTags(), EntityTypeTags.getAllTags());

    public static TagContainer getInstance() {
        return instance;
    }

    public static void bind(TagContainer tagContainer) {
        instance = tagContainer;
    }
}

