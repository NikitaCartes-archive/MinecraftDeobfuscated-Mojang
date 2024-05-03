package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface SearchTree<T> {
	static <T> SearchTree<T> empty() {
		return string -> List.of();
	}

	static <T> SearchTree<T> plainText(List<T> list, Function<T, Stream<String>> function) {
		if (list.isEmpty()) {
			return empty();
		} else {
			SuffixArray<T> suffixArray = new SuffixArray<>();

			for (T object : list) {
				((Stream)function.apply(object)).forEach(string -> suffixArray.add(object, string.toLowerCase(Locale.ROOT)));
			}

			suffixArray.generate();
			return suffixArray::search;
		}
	}

	List<T> search(String string);
}
