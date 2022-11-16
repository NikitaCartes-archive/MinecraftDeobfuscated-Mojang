/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ResourceLocationPattern;

public class ResourceFilterSection {
    private static final Codec<ResourceFilterSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.list(ResourceLocationPattern.CODEC).fieldOf("block")).forGetter(resourceFilterSection -> resourceFilterSection.blockList)).apply((Applicative<ResourceFilterSection, ?>)instance, ResourceFilterSection::new));
    public static final MetadataSectionType<ResourceFilterSection> TYPE = MetadataSectionType.fromCodec("filter", CODEC);
    private final List<ResourceLocationPattern> blockList;

    public ResourceFilterSection(List<ResourceLocationPattern> list) {
        this.blockList = List.copyOf(list);
    }

    public boolean isNamespaceFiltered(String string) {
        return this.blockList.stream().anyMatch(resourceLocationPattern -> resourceLocationPattern.namespacePredicate().test(string));
    }

    public boolean isPathFiltered(String string) {
        return this.blockList.stream().anyMatch(resourceLocationPattern -> resourceLocationPattern.pathPredicate().test(string));
    }
}

