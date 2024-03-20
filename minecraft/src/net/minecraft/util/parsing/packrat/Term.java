package net.minecraft.util.parsing.packrat;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.mutable.MutableBoolean;

public interface Term<S> {
	boolean parse(ParseState<S> parseState, Scope scope, Control control);

	static <S> Term<S> named(Atom<?> atom) {
		return new Term.Reference<>(atom);
	}

	static <S, T> Term<S> marker(Atom<T> atom, T object) {
		return new Term.Marker<>(atom, object);
	}

	@SafeVarargs
	static <S> Term<S> sequence(Term<S>... terms) {
		return new Term.Sequence<>(List.of(terms));
	}

	@SafeVarargs
	static <S> Term<S> alternative(Term<S>... terms) {
		return new Term.Alternative<>(List.of(terms));
	}

	static <S> Term<S> optional(Term<S> term) {
		return new Term.Maybe<>(term);
	}

	static <S> Term<S> cut() {
		return new Term<S>() {
			@Override
			public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
				control.cut();
				return true;
			}

			public String toString() {
				return "↑";
			}
		};
	}

	static <S> Term<S> empty() {
		return new Term<S>() {
			@Override
			public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
				return true;
			}

			public String toString() {
				return "ε";
			}
		};
	}

	public static record Alternative<S>(List<Term<S>> elements) implements Term<S> {
		@Override
		public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
			MutableBoolean mutableBoolean = new MutableBoolean();
			Control control2 = mutableBoolean::setTrue;
			int i = parseState.mark();

			for (Term<S> term : this.elements) {
				if (mutableBoolean.isTrue()) {
					break;
				}

				Scope scope2 = new Scope();
				if (term.parse(parseState, scope2, control2)) {
					scope.putAll(scope2);
					return true;
				}

				parseState.restore(i);
			}

			return false;
		}
	}

	public static record Marker<S, T>(Atom<T> name, T value) implements Term<S> {
		@Override
		public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
			scope.put(this.name, this.value);
			return true;
		}
	}

	public static record Maybe<S>(Term<S> term) implements Term<S> {
		@Override
		public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
			int i = parseState.mark();
			if (!this.term.parse(parseState, scope, control)) {
				parseState.restore(i);
			}

			return true;
		}
	}

	public static record Reference<S, T>(Atom<T> name) implements Term<S> {
		@Override
		public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
			Optional<T> optional = parseState.parse(this.name);
			if (optional.isEmpty()) {
				return false;
			} else {
				scope.put(this.name, (T)optional.get());
				return true;
			}
		}
	}

	public static record Sequence<S>(List<Term<S>> elements) implements Term<S> {
		@Override
		public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
			int i = parseState.mark();

			for (Term<S> term : this.elements) {
				if (!term.parse(parseState, scope, control)) {
					parseState.restore(i);
					return false;
				}
			}

			return true;
		}
	}
}
