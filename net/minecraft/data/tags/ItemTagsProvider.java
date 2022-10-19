/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ItemTagsProvider
extends TagsProvider<Item> {
    private final Function<TagKey<Block>, TagBuilder> blockTags = tagsProvider::getOrCreateRawBuilder;

    public ItemTagsProvider(PackOutput packOutput, TagsProvider<Block> tagsProvider) {
        super(packOutput, Registry.ITEM);
    }

    protected void copy(TagKey<Block> tagKey, TagKey<Item> tagKey2) {
        TagBuilder tagBuilder = this.getOrCreateRawBuilder(tagKey2);
        TagBuilder tagBuilder2 = this.blockTags.apply(tagKey);
        tagBuilder2.build().forEach(tagBuilder::add);
    }
}

