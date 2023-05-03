package net.minecraft.world.level.storage.loot.predicates;

public class AllOfCondition extends CompositeLootItemCondition {
	AllOfCondition(LootItemCondition[] lootItemConditions) {
		super(lootItemConditions, LootItemConditions.andConditions(lootItemConditions));
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.ALL_OF;
	}

	public static AllOfCondition.Builder allOf(LootItemCondition.Builder... builders) {
		return new AllOfCondition.Builder(builders);
	}

	public static class Builder extends CompositeLootItemCondition.Builder {
		public Builder(LootItemCondition.Builder... builders) {
			super(builders);
		}

		@Override
		public AllOfCondition.Builder and(LootItemCondition.Builder builder) {
			this.addTerm(builder);
			return this;
		}

		@Override
		protected LootItemCondition create(LootItemCondition[] lootItemConditions) {
			return new AllOfCondition(lootItemConditions);
		}
	}

	public static class Serializer extends CompositeLootItemCondition.Serializer<AllOfCondition> {
		protected AllOfCondition create(LootItemCondition[] lootItemConditions) {
			return new AllOfCondition(lootItemConditions);
		}
	}
}
