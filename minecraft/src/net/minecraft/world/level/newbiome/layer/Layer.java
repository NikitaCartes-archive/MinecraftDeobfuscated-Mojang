package net.minecraft.world.level.newbiome.layer;

import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
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

	public Biome[] getArea(int i, int j, int k, int l) {
		Biome[] biomes = new Biome[k * l];

		for (int m = 0; m < l; m++) {
			for (int n = 0; n < k; n++) {
				int o = this.area.get(i + n, j + m);
				Biome biome = this.getBiome(o);
				biomes[n + m * k] = biome;
			}
		}

		return biomes;
	}

	private Biome getBiome(int i) {
		Biome biome = Registry.BIOME.byId(i);
		if (biome == null) {
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				throw new IllegalStateException("Unknown biome id: " + i);
			} else {
				LOGGER.warn("Unknown biome id: ", i);
				return Biomes.DEFAULT;
			}
		} else {
			return biome;
		}
	}

	public Biome get(int i, int j) {
		return this.getBiome(this.area.get(i, j));
	}
}
