package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.slf4j.Logger;

public record ConditionReference(ResourceLocation name) implements LootItemCondition {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Codec<ConditionReference> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(ResourceLocation.CODEC.fieldOf("name").forGetter(ConditionReference::name)).apply(instance, ConditionReference::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.REFERENCE;
	}

	@Override
	public void validate(ValidationContext validationContext) {
		LootDataId<LootItemCondition> lootDataId = new LootDataId<>(LootDataType.PREDICATE, this.name);
		if (validationContext.hasVisitedElement(lootDataId)) {
			validationContext.reportProblem("Condition " + this.name + " is recursively called");
		} else {
			LootItemCondition.super.validate(validationContext);
			validationContext.resolver()
				.getElementOptional(lootDataId)
				.ifPresentOrElse(
					lootItemCondition -> lootItemCondition.validate(validationContext.enterElement(".{" + this.name + "}", lootDataId)),
					() -> validationContext.reportProblem("Unknown condition table called " + this.name)
				);
		}
	}

	public boolean test(LootContext lootContext) {
		LootItemCondition lootItemCondition = lootContext.getResolver().getElement(LootDataType.PREDICATE, this.name);
		if (lootItemCondition == null) {
			LOGGER.warn("Tried using unknown condition table called {}", this.name);
			return false;
		} else {
			LootContext.VisitedEntry<?> visitedEntry = LootContext.createVisitedEntry(lootItemCondition);
			if (lootContext.pushVisitedElement(visitedEntry)) {
				boolean var4;
				try {
					var4 = lootItemCondition.test(lootContext);
				} finally {
					lootContext.popVisitedElement(visitedEntry);
				}

				return var4;
			} else {
				LOGGER.warn("Detected infinite loop in loot tables");
				return false;
			}
		}
	}

	public static LootItemCondition.Builder conditionReference(ResourceLocation resourceLocation) {
		return () -> new ConditionReference(resourceLocation);
	}
}
