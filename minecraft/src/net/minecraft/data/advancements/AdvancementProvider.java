package net.minecraft.data.advancements;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class AdvancementProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final List<AdvancementSubProvider> subProviders;
	private final CompletableFuture<HolderLookup.Provider> registries;

	public AdvancementProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, List<AdvancementSubProvider> list) {
		this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
		this.subProviders = list;
		this.registries = completableFuture;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		return this.registries.thenCompose(provider -> {
			Set<ResourceLocation> set = new HashSet();
			List<CompletableFuture<?>> list = new ArrayList();
			Consumer<Advancement> consumer = advancement -> {
				if (!set.add(advancement.getId())) {
					throw new IllegalStateException("Duplicate advancement " + advancement.getId());
				} else {
					Path path = this.pathProvider.json(advancement.getId());
					list.add(DataProvider.saveStable(cachedOutput, advancement.deconstruct().serializeToJson(), path));
				}
			};

			for (AdvancementSubProvider advancementSubProvider : this.subProviders) {
				advancementSubProvider.generate(provider, consumer);
			}

			return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
		});
	}

	@Override
	public final String getName() {
		return "Advancements";
	}
}
