/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CarvingMaskDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<CarvingMaskDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)GenerationStep.Carving.CODEC.fieldOf("step")).forGetter(carvingMaskDecoratorConfiguration -> carvingMaskDecoratorConfiguration.step), ((MapCodec)Codec.FLOAT.fieldOf("probability")).forGetter(carvingMaskDecoratorConfiguration -> Float.valueOf(carvingMaskDecoratorConfiguration.probability))).apply((Applicative<CarvingMaskDecoratorConfiguration, ?>)instance, CarvingMaskDecoratorConfiguration::new));
    protected final GenerationStep.Carving step;
    protected final float probability;

    public CarvingMaskDecoratorConfiguration(GenerationStep.Carving carving, float f) {
        this.step = carving;
        this.probability = f;
    }
}

