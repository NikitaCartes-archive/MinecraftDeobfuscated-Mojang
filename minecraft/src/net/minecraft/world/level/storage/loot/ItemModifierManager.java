package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class ItemModifierManager extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = Deserializers.createFunctionSerializer().create();
	private final PredicateManager predicateManager;
	private final LootTables lootTables;
	private Map<ResourceLocation, LootItemFunction> functions = ImmutableMap.of();

	public ItemModifierManager(PredicateManager predicateManager, LootTables lootTables) {
		super(GSON, "item_modifiers");
		this.predicateManager = predicateManager;
		this.lootTables = lootTables;
	}

	@Nullable
	public LootItemFunction get(ResourceLocation resourceLocation) {
		return (LootItemFunction)this.functions.get(resourceLocation);
	}

	public LootItemFunction get(ResourceLocation resourceLocation, LootItemFunction lootItemFunction) {
		return (LootItemFunction)this.functions.getOrDefault(resourceLocation, lootItemFunction);
	}

	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Builder<ResourceLocation, LootItemFunction> builder = ImmutableMap.builder();
		map.forEach((resourceLocation, jsonElement) -> {
			try {
				if (jsonElement.isJsonArray()) {
					LootItemFunction[] lootItemFunctions = GSON.fromJson(jsonElement, LootItemFunction[].class);
					builder.put(resourceLocation, new ItemModifierManager.FunctionSequence(lootItemFunctions));
				} else {
					LootItemFunction lootItemFunction = GSON.fromJson(jsonElement, LootItemFunction.class);
					builder.put(resourceLocation, lootItemFunction);
				}
			} catch (Exception var4x) {
				LOGGER.error("Couldn't parse item modifier {}", resourceLocation, var4x);
			}
		});
		Map<ResourceLocation, LootItemFunction> map2 = builder.build();
		ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, this.lootTables::get);
		map2.forEach((resourceLocation, lootItemFunction) -> lootItemFunction.validate(validationContext));
		validationContext.getProblems().forEach((string, string2) -> LOGGER.warn("Found item modifier validation problem in {}: {}", string, string2));
		this.functions = map2;
	}

	public Set<ResourceLocation> getKeys() {
		return Collections.unmodifiableSet(this.functions.keySet());
	}

	static class FunctionSequence implements LootItemFunction {
		protected final LootItemFunction[] functions;
		private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

		public FunctionSequence(LootItemFunction[] lootItemFunctions) {
			this.functions = lootItemFunctions;
			this.compositeFunction = LootItemFunctions.compose(lootItemFunctions);
		}

		public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
			return (ItemStack)this.compositeFunction.apply(itemStack, lootContext);
		}

		@Override
		public LootItemFunctionType getType() {
			throw new UnsupportedOperationException();
		}
	}
}
