package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.Util;

public class AnyOfCondition extends CompositeLootItemCondition {
	public static final MapCodec<AnyOfCondition> CODEC = createCodec(AnyOfCondition::new);

	AnyOfCondition(List<LootItemCondition> list) {
		super(list, Util.anyOf(list));
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
		protected LootItemCondition create(List<LootItemCondition> list) {
			return new AnyOfCondition(list);
		}
	}
}
