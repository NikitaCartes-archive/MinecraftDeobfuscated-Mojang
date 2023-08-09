package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class DeserializationContext {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ResourceLocation id;
	private final LootDataManager lootData;

	public DeserializationContext(ResourceLocation resourceLocation, LootDataManager lootDataManager) {
		this.id = resourceLocation;
		this.lootData = lootDataManager;
	}

	public final List<LootItemCondition> deserializeConditions(JsonArray jsonArray, String string, LootContextParamSet lootContextParamSet) {
		List<LootItemCondition> list = Util.getOrThrow(LootItemConditions.CODEC.listOf().parse(JsonOps.INSTANCE, jsonArray), JsonParseException::new);
		ValidationContext validationContext = new ValidationContext(lootContextParamSet, this.lootData);

		for (LootItemCondition lootItemCondition : list) {
			lootItemCondition.validate(validationContext);
			validationContext.getProblems()
				.forEach((string2, string3) -> LOGGER.warn("Found validation problem in advancement trigger {}/{}: {}", string, string2, string3));
		}

		return list;
	}

	public ResourceLocation getAdvancementId() {
		return this.id;
	}
}
