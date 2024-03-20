package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class ValidationContext {
	private final ProblemReporter reporter;
	private final LootContextParamSet params;
	private final HolderGetter.Provider resolver;
	private final Set<ResourceKey<?>> visitedElements;

	public ValidationContext(ProblemReporter problemReporter, LootContextParamSet lootContextParamSet, HolderGetter.Provider provider) {
		this(problemReporter, lootContextParamSet, provider, Set.of());
	}

	private ValidationContext(ProblemReporter problemReporter, LootContextParamSet lootContextParamSet, HolderGetter.Provider provider, Set<ResourceKey<?>> set) {
		this.reporter = problemReporter;
		this.params = lootContextParamSet;
		this.resolver = provider;
		this.visitedElements = set;
	}

	public ValidationContext forChild(String string) {
		return new ValidationContext(this.reporter.forChild(string), this.params, this.resolver, this.visitedElements);
	}

	public ValidationContext enterElement(String string, ResourceKey<?> resourceKey) {
		Set<ResourceKey<?>> set = ImmutableSet.<ResourceKey<?>>builder().addAll(this.visitedElements).add(resourceKey).build();
		return new ValidationContext(this.reporter.forChild(string), this.params, this.resolver, set);
	}

	public boolean hasVisitedElement(ResourceKey<?> resourceKey) {
		return this.visitedElements.contains(resourceKey);
	}

	public void reportProblem(String string) {
		this.reporter.report(string);
	}

	public void validateUser(LootContextUser lootContextUser) {
		this.params.validateUser(this, lootContextUser);
	}

	public HolderGetter.Provider resolver() {
		return this.resolver;
	}

	public ValidationContext setParams(LootContextParamSet lootContextParamSet) {
		return new ValidationContext(this.reporter, lootContextParamSet, this.resolver, this.visitedElements);
	}
}
