package net.minecraft.world.level.storage.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class ValidationContext {
	private final Multimap<String, String> problems;
	private final Supplier<String> context;
	private final LootContextParamSet params;
	private final LootDataResolver resolver;
	private final Set<LootDataId<?>> visitedElements;
	@Nullable
	private String contextCache;

	public ValidationContext(LootContextParamSet lootContextParamSet, LootDataResolver lootDataResolver) {
		this(HashMultimap.create(), () -> "", lootContextParamSet, lootDataResolver, ImmutableSet.of());
	}

	public ValidationContext(
		Multimap<String, String> multimap,
		Supplier<String> supplier,
		LootContextParamSet lootContextParamSet,
		LootDataResolver lootDataResolver,
		Set<LootDataId<?>> set
	) {
		this.problems = multimap;
		this.context = supplier;
		this.params = lootContextParamSet;
		this.resolver = lootDataResolver;
		this.visitedElements = set;
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

	public ValidationContext forChild(String string) {
		return new ValidationContext(this.problems, () -> this.getContext() + string, this.params, this.resolver, this.visitedElements);
	}

	public ValidationContext enterElement(String string, LootDataId<?> lootDataId) {
		ImmutableSet<LootDataId<?>> immutableSet = ImmutableSet.<LootDataId<?>>builder().addAll(this.visitedElements).add(lootDataId).build();
		return new ValidationContext(this.problems, () -> this.getContext() + string, this.params, this.resolver, immutableSet);
	}

	public boolean hasVisitedElement(LootDataId<?> lootDataId) {
		return this.visitedElements.contains(lootDataId);
	}

	public Multimap<String, String> getProblems() {
		return ImmutableMultimap.copyOf(this.problems);
	}

	public void validateUser(LootContextUser lootContextUser) {
		this.params.validateUser(this, lootContextUser);
	}

	public LootDataResolver resolver() {
		return this.resolver;
	}

	public ValidationContext setParams(LootContextParamSet lootContextParamSet) {
		return new ValidationContext(this.problems, this.context, lootContextParamSet, this.resolver, this.visitedElements);
	}
}
