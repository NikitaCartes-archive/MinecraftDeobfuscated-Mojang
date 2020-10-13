/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.network;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TextFilter {
    public void join();

    public void leave();

    public CompletableFuture<Optional<String>> processStreamMessage(String var1);

    public CompletableFuture<Optional<List<String>>> processMessageBundle(List<String> var1);
}

