package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.MapRule;
import net.minecraft.voting.rules.RuleChange;

public class CodepointReplaceRule extends MapRule<Integer, Integer> {
	private final Int2IntMap entries = new Int2IntOpenHashMap();

	public CodepointReplaceRule() {
		super(Codec.INT, Codec.INT);
	}

	public int getChange(int i) {
		return this.entries.getOrDefault(i, i);
	}

	protected Component description(Integer integer, Integer integer2) {
		return Component.translatable("rule.codepoint_replace", Character.toString(integer), Character.toString(integer2));
	}

	protected void set(Integer integer, Integer integer2) {
		this.entries.put(integer.intValue(), integer2.intValue());
	}

	protected void remove(Integer integer) {
		this.entries.remove(integer.intValue());
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.entries.entrySet().stream().map(entry -> new MapRule.MapRuleChange((Integer)entry.getKey(), (Integer)entry.getValue()));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return Stream.generate(() -> {
			if ((double)randomSource.nextFloat() < 0.7) {
				int ix;
				int j;
				if (randomSource.nextBoolean()) {
					ix = randomSource.nextIntBetweenInclusive(65, 90);
					j = Character.toLowerCase(ix);
				} else {
					ix = randomSource.nextIntBetweenInclusive(97, 122);
					j = Character.toUpperCase(ix);
				}

				return this.entries.containsKey(ix) ? Optional.empty() : Optional.of(new MapRule.MapRuleChange(ix, j));
			} else {
				int ixx = randomSource.nextIntBetweenInclusive(32, 126);
				if (this.entries.containsKey(ixx)) {
					return Optional.empty();
				} else {
					int j = randomSource.nextIntBetweenInclusive(32, 126);
					return ixx == j ? Optional.empty() : Optional.of(new MapRule.MapRuleChange(ixx, j));
				}
			}
		}).flatMap(Optional::stream).limit((long)i).map(ruleChange -> ruleChange);
	}
}
