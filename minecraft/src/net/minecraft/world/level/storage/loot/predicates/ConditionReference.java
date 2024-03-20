package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.slf4j.Logger;

public record ConditionReference(ResourceKey<LootItemCondition> name) implements LootItemCondition {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Codec<ConditionReference> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(ResourceKey.codec(Registries.PREDICATE).fieldOf("name").forGetter(ConditionReference::name))
				.apply(instance, ConditionReference::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.REFERENCE;
	}

	@Override
	public void validate(ValidationContext validationContext) {
		if (validationContext.hasVisitedElement(this.name)) {
			validationContext.reportProblem("Condition " + this.name.location() + " is recursively called");
		} else {
			LootItemCondition.super.validate(validationContext);
			validationContext.resolver()
				.get(Registries.PREDICATE, this.name)
				.ifPresentOrElse(
					reference -> ((LootItemCondition)reference.value()).validate(validationContext.enterElement(".{" + this.name.location() + "}", this.name)),
					() -> validationContext.reportProblem("Unknown condition table called " + this.name.location())
				);
		}
	}

	public boolean test(LootContext lootContext) {
		LootItemCondition lootItemCondition = (LootItemCondition)lootContext.getResolver()
			.get(Registries.PREDICATE, this.name)
			.map(Holder.Reference::value)
			.orElse(null);
		if (lootItemCondition == null) {
			LOGGER.warn("Tried using unknown condition table called {}", this.name.location());
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

	public static LootItemCondition.Builder conditionReference(ResourceKey<LootItemCondition> resourceKey) {
		return () -> new ConditionReference(resourceKey);
	}
}
