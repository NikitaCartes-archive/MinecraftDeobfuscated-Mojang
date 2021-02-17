/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CarvingMaskDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<CarvingMaskDecoratorConfiguration> CODEC = ((MapCodec)GenerationStep.Carving.CODEC.fieldOf("step")).xmap(CarvingMaskDecoratorConfiguration::new, carvingMaskDecoratorConfiguration -> carvingMaskDecoratorConfiguration.step).codec();
    protected final GenerationStep.Carving step;

    public CarvingMaskDecoratorConfiguration(GenerationStep.Carving carving) {
        this.step = carving;
    }
}

