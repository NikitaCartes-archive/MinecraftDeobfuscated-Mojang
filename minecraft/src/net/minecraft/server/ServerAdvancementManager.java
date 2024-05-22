package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ServerAdvancementManager extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().create();
	private Map<ResourceLocation, AdvancementHolder> advancements = Map.of();
	private AdvancementTree tree = new AdvancementTree();
	private final HolderLookup.Provider registries;

	public ServerAdvancementManager(HolderLookup.Provider provider) {
		super(GSON, Registries.elementsDirPath(Registries.ADVANCEMENT));
		this.registries = provider;
	}

	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		RegistryOps<JsonElement> registryOps = this.registries.createSerializationContext(JsonOps.INSTANCE);
		Builder<ResourceLocation, AdvancementHolder> builder = ImmutableMap.builder();
		map.forEach((resourceLocation, jsonElement) -> {
			try {
				Advancement advancement = Advancement.CODEC.parse(registryOps, jsonElement).getOrThrow(JsonParseException::new);
				this.validate(resourceLocation, advancement);
				builder.put(resourceLocation, new AdvancementHolder(resourceLocation, advancement));
			} catch (Exception var6x) {
				LOGGER.error("Parsing error loading custom advancement {}: {}", resourceLocation, var6x.getMessage());
			}
		});
		this.advancements = builder.buildOrThrow();
		AdvancementTree advancementTree = new AdvancementTree();
		advancementTree.addAll(this.advancements.values());

		for (AdvancementNode advancementNode : advancementTree.roots()) {
			if (advancementNode.holder().value().display().isPresent()) {
				TreeNodePosition.run(advancementNode);
			}
		}

		this.tree = advancementTree;
	}

	private void validate(ResourceLocation resourceLocation, Advancement advancement) {
		ProblemReporter.Collector collector = new ProblemReporter.Collector();
		advancement.validate(collector, this.registries.asGetterLookup());
		collector.getReport().ifPresent(string -> LOGGER.warn("Found validation problems in advancement {}: \n{}", resourceLocation, string));
	}

	@Nullable
	public AdvancementHolder get(ResourceLocation resourceLocation) {
		return (AdvancementHolder)this.advancements.get(resourceLocation);
	}

	public AdvancementTree tree() {
		return this.tree;
	}

	public Collection<AdvancementHolder> getAllAdvancements() {
		return this.advancements.values();
	}
}
