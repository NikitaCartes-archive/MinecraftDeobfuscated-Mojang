package net.minecraft.data.advancements;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AdvancementProvider implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final String name;
	private final PackOutput.PathProvider pathProvider;
	private final List<AdvancementSubProvider> subProviders;

	public AdvancementProvider(String string, PackOutput packOutput, List<AdvancementSubProvider> list) {
		this.name = string;
		this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
		this.subProviders = list;
	}

	@Override
	public void run(CachedOutput cachedOutput) {
		Set<ResourceLocation> set = new HashSet();
		Consumer<Advancement> consumer = advancement -> {
			if (!set.add(advancement.getId())) {
				throw new IllegalStateException("Duplicate advancement " + advancement.getId());
			} else {
				Path path = this.pathProvider.json(advancement.getId());

				try {
					DataProvider.saveStable(cachedOutput, advancement.deconstruct().serializeToJson(), path);
				} catch (IOException var6) {
					LOGGER.error("Couldn't save advancement {}", path, var6);
				}
			}
		};

		for (AdvancementSubProvider advancementSubProvider : this.subProviders) {
			advancementSubProvider.generate(consumer);
		}
	}

	@Override
	public String getName() {
		return this.name;
	}
}
