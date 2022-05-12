package net.minecraft.data.advancements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AdvancementProvider implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final DataGenerator.PathProvider pathProvider;
	private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(
		new TheEndAdvancements(), new HusbandryAdvancements(), new AdventureAdvancements(), new NetherAdvancements(), new StoryAdvancements()
	);

	public AdvancementProvider(DataGenerator dataGenerator) {
		this.pathProvider = dataGenerator.createPathProvider(DataGenerator.Target.DATA_PACK, "advancements");
	}

	@Override
	public void run(CachedOutput cachedOutput) {
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();
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

		for (Consumer<Consumer<Advancement>> consumer2 : this.tabs) {
			consumer2.accept(consumer);
		}
	}

	@Override
	public String getName() {
		return "Advancements";
	}
}
