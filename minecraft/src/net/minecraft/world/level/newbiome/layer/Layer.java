package net.minecraft.world.level.newbiome.layer;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Layer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final LazyArea area;

	public Layer(AreaFactory<LazyArea> areaFactory) {
		this.area = areaFactory.make();
	}

	public Biome get(Registry<Biome> registry, int i, int j) {
		int k = this.area.get(i, j);
		ResourceKey<Biome> resourceKey = Biomes.byId(k);
		if (resourceKey == null) {
			throw new IllegalStateException("Unknown biome id emitted by layers: " + k);
		} else {
			Biome biome = registry.get(resourceKey);
			if (biome == null) {
				if (SharedConstants.IS_RUNNING_IN_IDE) {
					throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Unknown biome id: " + k));
				} else {
					LOGGER.warn("Unknown biome id: {}", k);
					return registry.get(Biomes.byId(0));
				}
			} else {
				return biome;
			}
		}
	}
}
