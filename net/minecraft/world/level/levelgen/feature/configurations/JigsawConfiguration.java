/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class JigsawConfiguration
implements FeatureConfiguration {
    public static final Codec<JigsawConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)StructureTemplatePool.CODEC.fieldOf("start_pool")).forGetter(JigsawConfiguration::startPool), ((MapCodec)Codec.intRange(0, 7).fieldOf("size")).forGetter(JigsawConfiguration::maxDepth)).apply((Applicative<JigsawConfiguration, ?>)instance, JigsawConfiguration::new));
    private final Supplier<StructureTemplatePool> startPool;
    private final int maxDepth;

    public JigsawConfiguration(Supplier<StructureTemplatePool> supplier, int i) {
        this.startPool = supplier;
        this.maxDepth = i;
    }

    public int maxDepth() {
        return this.maxDepth;
    }

    public Supplier<StructureTemplatePool> startPool() {
        return this.startPool;
    }
}

