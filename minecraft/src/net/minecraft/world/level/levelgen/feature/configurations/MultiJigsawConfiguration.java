package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class MultiJigsawConfiguration implements FeatureConfiguration {
	private final List<JigsawConfiguration> configurations;

	public MultiJigsawConfiguration(Map<String, Integer> map) {
		this.configurations = (List<JigsawConfiguration>)map.entrySet()
			.stream()
			.map(entry -> new JigsawConfiguration((String)entry.getKey(), (Integer)entry.getValue()))
			.collect(Collectors.toList());
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps, dynamicOps.createList(this.configurations.stream().map(jigsawConfiguration -> jigsawConfiguration.serialize(dynamicOps).getValue()))
		);
	}

	public static <T> MultiJigsawConfiguration deserialize(Dynamic<T> dynamic) {
		List<JigsawConfiguration> list = dynamic.asList(JigsawConfiguration::deserialize);
		return new MultiJigsawConfiguration(
			(Map<String, Integer>)list.stream().collect(Collectors.toMap(JigsawConfiguration::getStartPool, JigsawConfiguration::getSize))
		);
	}

	public JigsawConfiguration getRandomPool(Random random) {
		return (JigsawConfiguration)this.configurations.get(random.nextInt(this.configurations.size()));
	}
}
