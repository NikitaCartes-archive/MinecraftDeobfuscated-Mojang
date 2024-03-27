package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.Util;

public class AllOfCondition extends CompositeLootItemCondition {
	public static final MapCodec<AllOfCondition> CODEC = createCodec(AllOfCondition::new);
	public static final Codec<AllOfCondition> INLINE_CODEC = createInlineCodec(AllOfCondition::new);

	AllOfCondition(List<LootItemCondition> list) {
		super(list, Util.allOf(list));
	}

	public static AllOfCondition allOf(List<LootItemCondition> list) {
		return new AllOfCondition(List.copyOf(list));
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
		protected LootItemCondition create(List<LootItemCondition> list) {
			return new AllOfCondition(list);
		}
	}
}
