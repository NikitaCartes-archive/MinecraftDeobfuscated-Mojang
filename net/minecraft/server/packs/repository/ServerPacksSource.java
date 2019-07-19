/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import java.util.Map;
import net.minecraft.server.packs.VanillaPack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.UnopenedPack;

public class ServerPacksSource
implements RepositorySource {
    private final VanillaPack vanillaPack = new VanillaPack("minecraft");

    @Override
    public <T extends UnopenedPack> void loadPacks(Map<String, T> map, UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor) {
        T unopenedPack = UnopenedPack.create("vanilla", false, () -> this.vanillaPack, unopenedPackConstructor, UnopenedPack.Position.BOTTOM);
        if (unopenedPack != null) {
            map.put("vanilla", unopenedPack);
        }
    }
}

