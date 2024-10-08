package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public record LootItemEntityPropertyCondition(Optional<EntityPredicate> predicate, LootContext.EntityTarget entityTarget) implements LootItemCondition {
	public static final MapCodec<LootItemEntityPropertyCondition> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					EntityPredicate.CODEC.optionalFieldOf("predicate").forGetter(LootItemEntityPropertyCondition::predicate),
					LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(LootItemEntityPropertyCondition::entityTarget)
				)
				.apply(instance, LootItemEntityPropertyCondition::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.ENTITY_PROPERTIES;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return Set.of(LootContextParams.ORIGIN, this.entityTarget.getParam());
	}

	public boolean test(LootContext lootContext) {
		Entity entity = lootContext.getOptionalParameter(this.entityTarget.getParam());
		Vec3 vec3 = lootContext.getOptionalParameter(LootContextParams.ORIGIN);
		return this.predicate.isEmpty() || ((EntityPredicate)this.predicate.get()).matches(lootContext.getLevel(), vec3, entity);
	}

	public static LootItemCondition.Builder entityPresent(LootContext.EntityTarget entityTarget) {
		return hasProperties(entityTarget, EntityPredicate.Builder.entity());
	}

	public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget entityTarget, EntityPredicate.Builder builder) {
		return () -> new LootItemEntityPropertyCondition(Optional.of(builder.build()), entityTarget);
	}

	public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget entityTarget, EntityPredicate entityPredicate) {
		return () -> new LootItemEntityPropertyCondition(Optional.of(entityPredicate), entityTarget);
	}
}
