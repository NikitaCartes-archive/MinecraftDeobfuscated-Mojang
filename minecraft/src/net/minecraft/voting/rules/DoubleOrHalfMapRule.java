package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

public abstract class DoubleOrHalfMapRule<T> extends MapRule<T, Integer> {
	private final int max;
	private final int min;
	private final Object2IntMap<T> entries = new Object2IntOpenHashMap<>();

	protected DoubleOrHalfMapRule(Codec<T> codec, int i, int j) {
		super(codec, Codec.INT);
		this.min = i;
		this.max = j;
	}

	protected void set(T object, Integer integer) {
		this.entries.put(object, integer.intValue());
	}

	@Override
	protected void remove(T object) {
		this.entries.removeInt(object);
	}

	public int getInt(T object) {
		return this.entries.getInt(object);
	}

	public float getFloat(T object) {
		int i = this.entries.getInt(object);
		return (float)Math.pow(2.0, (double)i);
	}

	protected static String powerOfTwoText(int i) {
		return i < 0 ? "1/" + (1 << -i) : String.valueOf(1 << i);
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.entries.object2IntEntrySet().stream().map(entry -> new MapRule.MapRuleChange(entry.getKey(), entry.getIntValue()));
	}

	protected abstract Stream<T> randomDomainValues(MinecraftServer minecraftServer, RandomSource randomSource);

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return this.randomDomainValues(minecraftServer, randomSource).limit((long)i * 3L).mapMulti((object, consumer) -> {
			int ix = this.entries.getInt(object);
			if (ix < this.max) {
				consumer.accept(new MapRule.MapRuleChange(object, ix + 1));
			}

			if (this.min < ix) {
				consumer.accept(new MapRule.MapRuleChange(object, ix - 1));
			}
		}).limit((long)i);
	}
}
