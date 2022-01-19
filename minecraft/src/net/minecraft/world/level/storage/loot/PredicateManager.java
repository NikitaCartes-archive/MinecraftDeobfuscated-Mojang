package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class PredicateManager extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = Deserializers.createConditionSerializer().create();
	private Map<ResourceLocation, LootItemCondition> conditions = ImmutableMap.of();

	public PredicateManager() {
		super(GSON, "predicates");
	}

	@Nullable
	public LootItemCondition get(ResourceLocation resourceLocation) {
		return (LootItemCondition)this.conditions.get(resourceLocation);
	}

	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Builder<ResourceLocation, LootItemCondition> builder = ImmutableMap.builder();
		map.forEach((resourceLocation, jsonElement) -> {
			try {
				if (jsonElement.isJsonArray()) {
					LootItemCondition[] lootItemConditions = GSON.fromJson(jsonElement, LootItemCondition[].class);
					builder.put(resourceLocation, new PredicateManager.CompositePredicate(lootItemConditions));
				} else {
					LootItemCondition lootItemCondition = GSON.fromJson(jsonElement, LootItemCondition.class);
					builder.put(resourceLocation, lootItemCondition);
				}
			} catch (Exception var4x) {
				LOGGER.error("Couldn't parse loot table {}", resourceLocation, var4x);
			}
		});
		Map<ResourceLocation, LootItemCondition> map2 = builder.build();
		ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, map2::get, resourceLocation -> null);
		map2.forEach(
			(resourceLocation, lootItemCondition) -> lootItemCondition.validate(validationContext.enterCondition("{" + resourceLocation + "}", resourceLocation))
		);
		validationContext.getProblems().forEach((string, string2) -> LOGGER.warn("Found validation problem in {}: {}", string, string2));
		this.conditions = map2;
	}

	public Set<ResourceLocation> getKeys() {
		return Collections.unmodifiableSet(this.conditions.keySet());
	}

	static class CompositePredicate implements LootItemCondition {
		private final LootItemCondition[] terms;
		private final Predicate<LootContext> composedPredicate;

		CompositePredicate(LootItemCondition[] lootItemConditions) {
			this.terms = lootItemConditions;
			this.composedPredicate = LootItemConditions.andConditions(lootItemConditions);
		}

		public final boolean test(LootContext lootContext) {
			return this.composedPredicate.test(lootContext);
		}

		@Override
		public void validate(ValidationContext validationContext) {
			LootItemCondition.super.validate(validationContext);

			for (int i = 0; i < this.terms.length; i++) {
				this.terms[i].validate(validationContext.forChild(".term[" + i + "]"));
			}
		}

		@Override
		public LootItemConditionType getType() {
			throw new UnsupportedOperationException();
		}
	}
}
