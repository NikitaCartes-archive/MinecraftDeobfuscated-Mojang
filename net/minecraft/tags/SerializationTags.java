/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import java.util.stream.Collectors;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;

public class SerializationTags {
    private static volatile TagContainer instance = TagContainer.of(TagCollection.of(BlockTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, named -> named))), TagCollection.of(ItemTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, named -> named))), TagCollection.of(FluidTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, named -> named))), TagCollection.of(EntityTypeTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, named -> named))), TagCollection.of(GameEventTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, named -> named))));

    public static TagContainer getInstance() {
        return instance;
    }

    public static void bind(TagContainer tagContainer) {
        instance = tagContainer;
    }
}

