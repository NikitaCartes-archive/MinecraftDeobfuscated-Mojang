package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TextFilter {
	TextFilter DUMMY = new TextFilter() {
		@Override
		public void join() {
		}

		@Override
		public void leave() {
		}

		@Override
		public CompletableFuture<TextFilter.FilteredText> processStreamMessage(String string) {
			return CompletableFuture.completedFuture(TextFilter.FilteredText.passThrough(string));
		}

		@Override
		public CompletableFuture<List<TextFilter.FilteredText>> processMessageBundle(List<String> list) {
			return CompletableFuture.completedFuture((List)list.stream().map(TextFilter.FilteredText::passThrough).collect(ImmutableList.toImmutableList()));
		}
	};

	void join();

	void leave();

	CompletableFuture<TextFilter.FilteredText> processStreamMessage(String string);

	CompletableFuture<List<TextFilter.FilteredText>> processMessageBundle(List<String> list);

	public static class FilteredText {
		public static final TextFilter.FilteredText EMPTY = new TextFilter.FilteredText("", "");
		private final String raw;
		private final String filtered;

		public FilteredText(String string, String string2) {
			this.raw = string;
			this.filtered = string2;
		}

		public String getRaw() {
			return this.raw;
		}

		public String getFiltered() {
			return this.filtered;
		}

		public static TextFilter.FilteredText passThrough(String string) {
			return new TextFilter.FilteredText(string, string);
		}

		public static TextFilter.FilteredText fullyFiltered(String string) {
			return new TextFilter.FilteredText(string, "");
		}

		public boolean isFiltered() {
			return !this.raw.equals(this.filtered);
		}
	}
}
