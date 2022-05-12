/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.tags.TagEntry;

public record TagFile(List<TagEntry> entries, boolean replace) {
    public static final Codec<TagFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)TagEntry.CODEC.listOf().fieldOf("values")).forGetter(TagFile::entries), Codec.BOOL.optionalFieldOf("replace", false).forGetter(TagFile::replace)).apply((Applicative<TagFile, ?>)instance, TagFile::new));
}

