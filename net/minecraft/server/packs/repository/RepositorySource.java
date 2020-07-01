/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import java.util.function.Consumer;
import net.minecraft.server.packs.repository.Pack;

public interface RepositorySource {
    public void loadPacks(Consumer<Pack> var1, Pack.PackConstructor var2);
}

