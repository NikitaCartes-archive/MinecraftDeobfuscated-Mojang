/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import java.util.function.Consumer;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

public class ServerPacksSource
implements RepositorySource {
    private final VanillaPackResources vanillaPack = new VanillaPackResources("minecraft");

    @Override
    public <T extends Pack> void loadPacks(Consumer<T> consumer, Pack.PackConstructor<T> packConstructor) {
        T pack = Pack.create("vanilla", false, () -> this.vanillaPack, packConstructor, Pack.Position.BOTTOM, PackSource.BUILT_IN);
        if (pack != null) {
            consumer.accept(pack);
        }
    }
}

