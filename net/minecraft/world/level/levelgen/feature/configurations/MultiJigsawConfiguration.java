/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class MultiJigsawConfiguration
implements FeatureConfiguration {
    public static final Codec<MultiJigsawConfiguration> CODEC = JigsawConfiguration.CODEC.listOf().xmap(MultiJigsawConfiguration::new, multiJigsawConfiguration -> multiJigsawConfiguration.configurations);
    private final List<JigsawConfiguration> configurations;

    public MultiJigsawConfiguration(Map<String, Integer> map) {
        this(map.entrySet().stream().map(entry -> new JigsawConfiguration(new ResourceLocation((String)entry.getKey()), (Integer)entry.getValue())).collect(Collectors.toList()));
    }

    private MultiJigsawConfiguration(List<JigsawConfiguration> list) {
        this.configurations = list;
    }

    public JigsawConfiguration getRandomPool(Random random) {
        return this.configurations.get(random.nextInt(this.configurations.size()));
    }
}

