/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TextFilter {
    public static final TextFilter DUMMY = new TextFilter(){

        @Override
        public void join() {
        }

        @Override
        public void leave() {
        }

        @Override
        public CompletableFuture<FilteredText> processStreamMessage(String string) {
            return CompletableFuture.completedFuture(FilteredText.passThrough(string));
        }

        @Override
        public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list) {
            return CompletableFuture.completedFuture((List)list.stream().map(FilteredText::passThrough).collect(ImmutableList.toImmutableList()));
        }
    };

    public void join();

    public void leave();

    public CompletableFuture<FilteredText> processStreamMessage(String var1);

    public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> var1);

    public static class FilteredText {
        public static final FilteredText EMPTY = new FilteredText("", "");
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

        public static FilteredText passThrough(String string) {
            return new FilteredText(string, string);
        }

        public static FilteredText fullyFiltered(String string) {
            return new FilteredText(string, "");
        }
    }
}

