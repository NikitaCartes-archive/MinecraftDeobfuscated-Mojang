/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Codecs;

public class StructureFeatureConfiguration {
    public static final Codec<StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.intRange(0, 4096).fieldOf("spacing")).forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.spacing), ((MapCodec)Codecs.intRange(0, 4096).fieldOf("separation")).forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.separation), ((MapCodec)Codecs.intRange(0, Integer.MAX_VALUE).fieldOf("salt")).forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.salt)).apply((Applicative<StructureFeatureConfiguration, ?>)instance, StructureFeatureConfiguration::new)).comapFlatMap(structureFeatureConfiguration -> {
        if (structureFeatureConfiguration.spacing <= structureFeatureConfiguration.separation) {
            return DataResult.error("Spacing has to be smaller than separation");
        }
        return DataResult.success(structureFeatureConfiguration);
    }, Function.identity());
    private final int spacing;
    private final int separation;
    private final int salt;

    public StructureFeatureConfiguration(int i, int j, int k) {
        this.spacing = i;
        this.separation = j;
        this.salt = k;
    }

    public int spacing() {
        return this.spacing;
    }

    public int separation() {
        return this.separation;
    }

    public int salt() {
        return this.salt;
    }
}

