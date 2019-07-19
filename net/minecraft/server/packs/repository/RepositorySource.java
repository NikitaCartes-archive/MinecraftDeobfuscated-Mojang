/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import java.util.Map;
import net.minecraft.server.packs.repository.UnopenedPack;

public interface RepositorySource {
    public <T extends UnopenedPack> void loadPacks(Map<String, T> var1, UnopenedPack.UnopenedPackConstructor<T> var2);
}

