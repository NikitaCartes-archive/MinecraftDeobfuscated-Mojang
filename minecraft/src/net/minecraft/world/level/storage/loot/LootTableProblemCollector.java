package net.minecraft.world.level.storage.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Supplier;

public class LootTableProblemCollector {
	private final Multimap<String, String> problems;
	private final Supplier<String> context;
	private String contextCache;

	public LootTableProblemCollector() {
		this(HashMultimap.create(), () -> "");
	}

	public LootTableProblemCollector(Multimap<String, String> multimap, Supplier<String> supplier) {
		this.problems = multimap;
		this.context = supplier;
	}

	private String getContext() {
		if (this.contextCache == null) {
			this.contextCache = (String)this.context.get();
		}

		return this.contextCache;
	}

	public void reportProblem(String string) {
		this.problems.put(this.getContext(), string);
	}

	public LootTableProblemCollector forChild(String string) {
		return new LootTableProblemCollector(this.problems, () -> this.getContext() + string);
	}

	public Multimap<String, String> getProblems() {
		return ImmutableMultimap.copyOf(this.problems);
	}
}
