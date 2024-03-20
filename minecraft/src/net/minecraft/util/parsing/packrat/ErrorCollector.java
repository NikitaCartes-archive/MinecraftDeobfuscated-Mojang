package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;

public interface ErrorCollector<S> {
	void store(int i, SuggestionSupplier<S> suggestionSupplier, Object object);

	default void store(int i, Object object) {
		this.store(i, SuggestionSupplier.empty(), object);
	}

	void finish(int i);

	public static class LongestOnly<S> implements ErrorCollector<S> {
		private final List<ErrorEntry<S>> entries = new ArrayList();
		private int lastCursor = -1;

		private void discardErrorsFromShorterParse(int i) {
			if (i > this.lastCursor) {
				this.lastCursor = i;
				this.entries.clear();
			}
		}

		@Override
		public void finish(int i) {
			this.discardErrorsFromShorterParse(i);
		}

		@Override
		public void store(int i, SuggestionSupplier<S> suggestionSupplier, Object object) {
			this.discardErrorsFromShorterParse(i);
			if (i == this.lastCursor) {
				this.entries.add(new ErrorEntry<>(i, suggestionSupplier, object));
			}
		}

		public List<ErrorEntry<S>> entries() {
			return this.entries;
		}

		public int cursor() {
			return this.lastCursor;
		}
	}
}
