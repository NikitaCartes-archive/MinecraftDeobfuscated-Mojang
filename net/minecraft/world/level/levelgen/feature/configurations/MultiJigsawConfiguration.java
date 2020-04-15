/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class MultiJigsawConfiguration
implements FeatureConfiguration {
    private final List<JigsawConfiguration> configurations;

    public MultiJigsawConfiguration(Map<String, Integer> map) {
        this.configurations = map.entrySet().stream().map(entry -> new JigsawConfiguration((String)entry.getKey(), (Integer)entry.getValue())).collect(Collectors.toList());
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<Object>(dynamicOps, dynamicOps.createList(this.configurations.stream().map(jigsawConfiguration -> jigsawConfiguration.serialize(dynamicOps).getValue())));
    }

    public static <T> MultiJigsawConfiguration deserialize(Dynamic<T> dynamic) {
        List<JigsawConfiguration> list = dynamic.asList(JigsawConfiguration::deserialize);
        return new MultiJigsawConfiguration(list.stream().collect(Collectors.toMap(JigsawConfiguration::getStartPool, JigsawConfiguration::getSize)));
    }

    public JigsawConfiguration getRandomPool(Random random) {
        return this.configurations.get(random.nextInt(this.configurations.size()));
    }
}

