package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;

public class LootContextParamSet {
	private final Set<LootContextParam<?>> required;
	private final Set<LootContextParam<?>> all;

	private LootContextParamSet(Set<LootContextParam<?>> set, Set<LootContextParam<?>> set2) {
		this.required = ImmutableSet.copyOf(set);
		this.all = ImmutableSet.copyOf(Sets.union(set, set2));
	}

	public Set<LootContextParam<?>> getRequired() {
		return this.required;
	}

	public Set<LootContextParam<?>> getAllowed() {
		return this.all;
	}

	public String toString() {
		return "["
			+ Joiner.on(", ")
				.join(this.all.stream().map(lootContextParam -> (this.required.contains(lootContextParam) ? "!" : "") + lootContextParam.getName()).iterator())
			+ "]";
	}

	public void validateUser(LootTableProblemCollector lootTableProblemCollector, LootContextUser lootContextUser) {
		Set<LootContextParam<?>> set = lootContextUser.getReferencedContextParams();
		Set<LootContextParam<?>> set2 = Sets.<LootContextParam<?>>difference(set, this.all);
		if (!set2.isEmpty()) {
			lootTableProblemCollector.reportProblem("Parameters " + set2 + " are not provided in this context");
		}
	}

	public static class Builder {
		private final Set<LootContextParam<?>> required = Sets.newIdentityHashSet();
		private final Set<LootContextParam<?>> optional = Sets.newIdentityHashSet();

		public LootContextParamSet.Builder required(LootContextParam<?> lootContextParam) {
			if (this.optional.contains(lootContextParam)) {
				throw new IllegalArgumentException("Parameter " + lootContextParam.getName() + " is already optional");
			} else {
				this.required.add(lootContextParam);
				return this;
			}
		}

		public LootContextParamSet.Builder optional(LootContextParam<?> lootContextParam) {
			if (this.required.contains(lootContextParam)) {
				throw new IllegalArgumentException("Parameter " + lootContextParam.getName() + " is already required");
			} else {
				this.optional.add(lootContextParam);
				return this;
			}
		}

		public LootContextParamSet build() {
			return new LootContextParamSet(this.required, this.optional);
		}
	}
}
