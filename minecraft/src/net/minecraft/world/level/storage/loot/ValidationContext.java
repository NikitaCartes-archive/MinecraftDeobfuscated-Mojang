package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class ValidationContext {
	private final ProblemReporter reporter;
	private final LootContextParamSet params;
	private final LootDataResolver resolver;
	private final Set<LootDataId<?>> visitedElements;

	public ValidationContext(ProblemReporter problemReporter, LootContextParamSet lootContextParamSet, LootDataResolver lootDataResolver) {
		this(problemReporter, lootContextParamSet, lootDataResolver, Set.of());
	}

	private ValidationContext(ProblemReporter problemReporter, LootContextParamSet lootContextParamSet, LootDataResolver lootDataResolver, Set<LootDataId<?>> set) {
		this.reporter = problemReporter;
		this.params = lootContextParamSet;
		this.resolver = lootDataResolver;
		this.visitedElements = set;
	}

	public ValidationContext forChild(String string) {
		return new ValidationContext(this.reporter.forChild(string), this.params, this.resolver, this.visitedElements);
	}

	public ValidationContext enterElement(String string, LootDataId<?> lootDataId) {
		ImmutableSet<LootDataId<?>> immutableSet = ImmutableSet.<LootDataId<?>>builder().addAll(this.visitedElements).add(lootDataId).build();
		return new ValidationContext(this.reporter.forChild(string), this.params, this.resolver, immutableSet);
	}

	public boolean hasVisitedElement(LootDataId<?> lootDataId) {
		return this.visitedElements.contains(lootDataId);
	}

	public void reportProblem(String string) {
		this.reporter.report(string);
	}

	public void validateUser(LootContextUser lootContextUser) {
		this.params.validateUser(this, lootContextUser);
	}

	public LootDataResolver resolver() {
		return this.resolver;
	}

	public ValidationContext setParams(LootContextParamSet lootContextParamSet) {
		return new ValidationContext(this.reporter, lootContextParamSet, this.resolver, this.visitedElements);
	}
}
