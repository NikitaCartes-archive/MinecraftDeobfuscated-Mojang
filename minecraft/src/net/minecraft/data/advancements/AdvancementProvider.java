package net.minecraft.data.advancements;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class AdvancementProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final List<AdvancementSubProvider> subProviders;
	private final CompletableFuture<HolderLookup.Provider> registries;

	public AdvancementProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, List<AdvancementSubProvider> list) {
		this.pathProvider = packOutput.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
		this.subProviders = list;
		this.registries = completableFuture;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		return this.registries.thenCompose(provider -> {
			Set<ResourceLocation> set = new HashSet();
			List<CompletableFuture<?>> list = new ArrayList();
			Consumer<AdvancementHolder> consumer = advancementHolder -> {
				if (!set.add(advancementHolder.id())) {
					throw new IllegalStateException("Duplicate advancement " + advancementHolder.id());
				} else {
					Path path = this.pathProvider.json(advancementHolder.id());
					list.add(DataProvider.saveStable(cachedOutput, provider, Advancement.CODEC, advancementHolder.value(), path));
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
