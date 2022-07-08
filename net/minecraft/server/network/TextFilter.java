/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.network.FilteredText;

public interface TextFilter {
    public static final TextFilter DUMMY = new TextFilter(){

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

    public void join();

    public void leave();

    public CompletableFuture<FilteredText<String>> processStreamMessage(String var1);

    public CompletableFuture<List<FilteredText<String>>> processMessageBundle(List<String> var1);
}

