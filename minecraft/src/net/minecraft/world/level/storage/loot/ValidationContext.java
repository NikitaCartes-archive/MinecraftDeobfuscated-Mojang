package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;

public class ValidationContext {
	private final ProblemReporter reporter;
	private final ContextKeySet contextKeySet;
	private final Optional<HolderGetter.Provider> resolver;
	private final Set<ResourceKey<?>> visitedElements;

	public ValidationContext(ProblemReporter problemReporter, ContextKeySet contextKeySet, HolderGetter.Provider provider) {
		this(problemReporter, contextKeySet, Optional.of(provider), Set.of());
	}

	public ValidationContext(ProblemReporter problemReporter, ContextKeySet contextKeySet) {
		this(problemReporter, contextKeySet, Optional.empty(), Set.of());
	}

	private ValidationContext(ProblemReporter problemReporter, ContextKeySet contextKeySet, Optional<HolderGetter.Provider> optional, Set<ResourceKey<?>> set) {
		this.reporter = problemReporter;
		this.contextKeySet = contextKeySet;
		this.resolver = optional;
		this.visitedElements = set;
	}

	public ValidationContext forChild(String string) {
		return new ValidationContext(this.reporter.forChild(string), this.contextKeySet, this.resolver, this.visitedElements);
	}

	public ValidationContext enterElement(String string, ResourceKey<?> resourceKey) {
		Set<ResourceKey<?>> set = ImmutableSet.<ResourceKey<?>>builder().addAll(this.visitedElements).add(resourceKey).build();
		return new ValidationContext(this.reporter.forChild(string), this.contextKeySet, this.resolver, set);
	}

	public boolean hasVisitedElement(ResourceKey<?> resourceKey) {
		return this.visitedElements.contains(resourceKey);
	}

	public void reportProblem(String string) {
		this.reporter.report(string);
	}

	public void validateContextUsage(LootContextUser lootContextUser) {
		Set<ContextKey<?>> set = lootContextUser.getReferencedContextParams();
		Set<ContextKey<?>> set2 = Sets.<ContextKey<?>>difference(set, this.contextKeySet.allowed());
		if (!set2.isEmpty()) {
			this.reporter.report("Parameters " + set2 + " are not provided in this context");
		}
	}

	public HolderGetter.Provider resolver() {
		return (HolderGetter.Provider)this.resolver.orElseThrow(() -> new UnsupportedOperationException("References not allowed"));
	}

	public boolean allowsReferences() {
		return this.resolver.isPresent();
	}

	public ValidationContext setContextKeySet(ContextKeySet contextKeySet) {
		return new ValidationContext(this.reporter, contextKeySet, this.resolver, this.visitedElements);
	}

	public ProblemReporter reporter() {
		return this.reporter;
	}
}
