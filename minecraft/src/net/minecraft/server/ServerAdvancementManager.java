package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerAdvancementManager extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder()
		.registerTypeHierarchyAdapter(Advancement.Builder.class, (JsonDeserializer<Advancement.Builder>)(jsonElement, type, jsonDeserializationContext) -> {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "advancement");
			return Advancement.Builder.fromJson(jsonObject, jsonDeserializationContext);
		})
		.registerTypeAdapter(AdvancementRewards.class, new AdvancementRewards.Deserializer())
		.registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
		.registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
		.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
		.create();
	private AdvancementList advancements = new AdvancementList();

	public ServerAdvancementManager() {
		super(GSON, "advancements");
	}

	protected void apply(Map<ResourceLocation, JsonObject> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Map<ResourceLocation, Advancement.Builder> map2 = Maps.<ResourceLocation, Advancement.Builder>newHashMap();
		map.forEach((resourceLocation, jsonObject) -> {
			try {
				Advancement.Builder builder = GSON.fromJson(jsonObject, Advancement.Builder.class);
				map2.put(resourceLocation, builder);
			} catch (IllegalArgumentException | JsonParseException var4x) {
				LOGGER.error("Parsing error loading custom advancement {}: {}", resourceLocation, var4x.getMessage());
			}
		});
		AdvancementList advancementList = new AdvancementList();
		advancementList.add(map2);

		for (Advancement advancement : advancementList.getRoots()) {
			if (advancement.getDisplay() != null) {
				TreeNodePosition.run(advancement);
			}
		}

		this.advancements = advancementList;
	}

	@Nullable
	public Advancement getAdvancement(ResourceLocation resourceLocation) {
		return this.advancements.get(resourceLocation);
	}

	public Collection<Advancement> getAllAdvancements() {
		return this.advancements.getAllAdvancements();
	}
}
