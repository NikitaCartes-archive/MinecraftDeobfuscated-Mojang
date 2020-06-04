/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import java.util.function.Consumer;
import net.minecraft.server.packs.repository.Pack;

public interface RepositorySource {
    public <T extends Pack> void loadPacks(Consumer<T> var1, Pack.PackConstructor<T> var2);
}

