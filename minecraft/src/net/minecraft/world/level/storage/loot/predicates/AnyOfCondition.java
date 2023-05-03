package net.minecraft.world.level.storage.loot.predicates;

public class AnyOfCondition extends CompositeLootItemCondition {
	AnyOfCondition(LootItemCondition[] lootItemConditions) {
		super(lootItemConditions, LootItemConditions.orConditions(lootItemConditions));
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.ANY_OF;
	}

	public static AnyOfCondition.Builder anyOf(LootItemCondition.Builder... builders) {
		return new AnyOfCondition.Builder(builders);
	}

	public static class Builder extends CompositeLootItemCondition.Builder {
		public Builder(LootItemCondition.Builder... builders) {
			super(builders);
		}

		@Override
		public AnyOfCondition.Builder or(LootItemCondition.Builder builder) {
			this.addTerm(builder);
			return this;
		}

		@Override
		protected LootItemCondition create(LootItemCondition[] lootItemConditions) {
			return new AnyOfCondition(lootItemConditions);
		}
	}

	public static class Serializer extends CompositeLootItemCondition.Serializer<AnyOfCondition> {
		protected AnyOfCondition create(LootItemCondition[] lootItemConditions) {
			return new AnyOfCondition(lootItemConditions);
		}
	}
}
