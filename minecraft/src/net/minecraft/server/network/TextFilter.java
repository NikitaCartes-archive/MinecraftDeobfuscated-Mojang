package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

public interface TextFilter {
	TextFilter DUMMY = new TextFilter() {
		@Override
		public void join() {
		}

		@Override
		public void leave() {
		}

		@Override
		public CompletableFuture<FilteredText<String>> processStreamMessage(String string) {
			return CompletableFuture.completedFuture(FilteredText.passThrough(string));
		}

		@Override
		public CompletableFuture<List<FilteredText<String>>> processMessageBundle(List<String> list) {
			return CompletableFuture.completedFuture((List)list.stream().map(FilteredText::passThrough).collect(ImmutableList.toImmutableList()));
		}
	};

	void join();

	void leave();

	CompletableFuture<FilteredText<String>> processStreamMessage(String string);

	CompletableFuture<List<FilteredText<String>>> processMessageBundle(List<String> list);

	default CompletableFuture<FilteredText<Component>> processStreamComponent(Component component) {
		return this.processStreamMessage(component.getString()).thenApply(filteredText -> {
			Component component2 = Util.mapNullable((String)filteredText.filtered(), Component::literal);
			return new FilteredText<>(component, component2);
		});
	}
}
