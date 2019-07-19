/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;

public interface ReloadableResourceManager
extends ResourceManager {
    public CompletableFuture<Unit> reload(Executor var1, Executor var2, List<Pack> var3, CompletableFuture<Unit> var4);

    @Environment(value=EnvType.CLIENT)
    public ReloadInstance createQueuedReload(Executor var1, Executor var2, CompletableFuture<Unit> var3);

    @Environment(value=EnvType.CLIENT)
    public ReloadInstance createFullReload(Executor var1, Executor var2, CompletableFuture<Unit> var3, List<Pack> var4);

    public void registerReloadListener(PreparableReloadListener var1);
}

