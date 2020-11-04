/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;

public class GeodeCrackSettings {
    public static final Codec<GeodeCrackSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)GeodeConfiguration.CHANCE_RANGE.fieldOf("generate_crack_chance")).orElse(1.0).forGetter(geodeCrackSettings -> geodeCrackSettings.generateCrackChance), ((MapCodec)Codec.doubleRange(0.0, 5.0).fieldOf("base_crack_size")).orElse(2.0).forGetter(geodeCrackSettings -> geodeCrackSettings.baseCrackSize), ((MapCodec)Codec.intRange(0, 10).fieldOf("crack_point_offset")).orElse(2).forGetter(geodeCrackSettings -> geodeCrackSettings.crackPointOffset)).apply((Applicative<GeodeCrackSettings, ?>)instance, GeodeCrackSettings::new));
    public final double generateCrackChance;
    public final double baseCrackSize;
    public final int crackPointOffset;

    public GeodeCrackSettings(double d, double e, int i) {
        this.generateCrackChance = d;
        this.baseCrackSize = e;
        this.crackPointOffset = i;
    }
}

