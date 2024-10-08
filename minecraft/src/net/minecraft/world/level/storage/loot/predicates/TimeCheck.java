package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;

public record TimeCheck(Optional<Long> period, IntRange value) implements LootItemCondition {
	public static final MapCodec<TimeCheck> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codec.LONG.optionalFieldOf("period").forGetter(TimeCheck::period), IntRange.CODEC.fieldOf("value").forGetter(TimeCheck::value))
				.apply(instance, TimeCheck::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.TIME_CHECK;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return this.value.getReferencedContextParams();
	}

	public boolean test(LootContext lootContext) {
		ServerLevel serverLevel = lootContext.getLevel();
		long l = serverLevel.getDayTime();
		if (this.period.isPresent()) {
			l %= this.period.get();
		}

		return this.value.test(lootContext, (int)l);
	}

	public static TimeCheck.Builder time(IntRange intRange) {
		return new TimeCheck.Builder(intRange);
	}

	public static class Builder implements LootItemCondition.Builder {
		private Optional<Long> period = Optional.empty();
		private final IntRange value;

		public Builder(IntRange intRange) {
			this.value = intRange;
		}

		public TimeCheck.Builder setPeriod(long l) {
			this.period = Optional.of(l);
			return this;
		}

		public TimeCheck build() {
			return new TimeCheck(this.period, this.value);
		}
	}
}
