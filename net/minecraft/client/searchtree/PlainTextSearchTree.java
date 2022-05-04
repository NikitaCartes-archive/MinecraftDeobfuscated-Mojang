/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.searchtree.SuffixArray;

@Environment(value=EnvType.CLIENT)
public interface PlainTextSearchTree<T> {
    public static <T> PlainTextSearchTree<T> empty() {
        return string -> List.of();
    }

    public static <T> PlainTextSearchTree<T> create(List<T> list, Function<T, Stream<String>> function) {
        if (list.isEmpty()) {
            return PlainTextSearchTree.empty();
        }
        SuffixArray suffixArray = new SuffixArray();
        for (Object object : list) {
            function.apply(object).forEach(string -> suffixArray.add(object, string.toLowerCase(Locale.ROOT)));
        }
        suffixArray.generate();
        return suffixArray::search;
    }

    public List<T> search(String var1);
}

