package net.minecraft.voting.rules.actual;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.CounterRule;
import net.minecraft.voting.rules.RuleChange;
import org.apache.commons.lang3.ArrayUtils;

public class OptimizationRule extends CounterRule.Simple {
	private static final List<String> NAMES = List.of(
		"Super", "Hyper", "Opti", "Extra", "Extreme", "Incredible", "Beyond", "Ultra", "Atomic", "Warp", "Performance", "Realistic", "Future", "Quantum", "Quad"
	);
	private static final Int2ObjectMap<String> LEVELS = new Int2ObjectArrayMap<>();

	public OptimizationRule() {
		super(0);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		int[] is = IntStream.range(-3, i + 5).filter(ix -> ix == 0).toArray();
		ArrayUtils.shuffle(is);
		return IntStream.of(is).limit((long)i).mapToObj(ix -> this.relative(ix));
	}

	@Override
	public Component setDescription(int i) {
		return Component.translatable("rule.optimize.set", get(i));
	}

	@Override
	public Component changeDescription(int i, int j) {
		return Component.translatable("rule.optimize.change", get(i), get(j));
	}

	private static String generate(int i) {
		if (i < 0) {
			return "Abysmal";
		} else if (i == Integer.MAX_VALUE) {
			return "L0L N00B";
		} else {
			RandomSource randomSource = RandomSource.create((long)i * 2L);
			List<String> list = new ArrayList(NAMES);
			StringBuilder stringBuilder = new StringBuilder();
			int j = Math.min(Math.max(Mth.log2(i) - 1, 1), list.size());

			for (int k = 0; k < j; k++) {
				int l = randomSource.nextInt(list.size());
				String string = (String)list.remove(l);
				stringBuilder.append(string);
			}

			return stringBuilder.append(" ").append(i + 1).append("000").toString();
		}
	}

	public static String get(int i) {
		return LEVELS.computeIfAbsent(i, OptimizationRule::generate);
	}
}
