package net.minecraft.util.parsing.packrat;

import java.util.Optional;

public interface Rule<S, T> {
	Optional<T> parse(ParseState<S> parseState);

	static <S, T> Rule<S, T> fromTerm(Term<S> term, Rule.RuleAction<S, T> ruleAction) {
		return new Rule.WrappedTerm<>(ruleAction, term);
	}

	static <S, T> Rule<S, T> fromTerm(Term<S> term, Rule.SimpleRuleAction<T> simpleRuleAction) {
		return new Rule.WrappedTerm<>((parseState, scope) -> Optional.of(simpleRuleAction.run(scope)), term);
	}

	@FunctionalInterface
	public interface RuleAction<S, T> {
		Optional<T> run(ParseState<S> parseState, Scope scope);
	}

	@FunctionalInterface
	public interface SimpleRuleAction<T> {
		T run(Scope scope);
	}

	public static record WrappedTerm<S, T>(Rule.RuleAction<S, T> action, Term<S> child) implements Rule<S, T> {
		@Override
		public Optional<T> parse(ParseState<S> parseState) {
			Scope scope = new Scope();
			return this.child.parse(parseState, scope, Control.UNBOUND) ? this.action.run(parseState, scope) : Optional.empty();
		}
	}
}
