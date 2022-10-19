/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public record FeatureFlagsMetadataSection(FeatureFlagSet flags) {
    private static final Codec<FeatureFlagsMetadataSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)FeatureFlags.CODEC.fieldOf("enabled")).forGetter(FeatureFlagsMetadataSection::flags)).apply((Applicative<FeatureFlagsMetadataSection, ?>)instance, FeatureFlagsMetadataSection::new));
    public static final MetadataSectionType<FeatureFlagsMetadataSection> TYPE = MetadataSectionType.fromCodec("features", CODEC);
}

