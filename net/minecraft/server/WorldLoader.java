/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;

public class WorldLoader {
    public static <D, R> CompletableFuture<R> load(InitConfig initConfig, WorldDataSupplier<D> worldDataSupplier, ResultFactory<D, R> resultFactory, Executor executor, Executor executor2) {
        try {
            Pair<DataPackConfig, CloseableResourceManager> pair = initConfig.packConfig.createResourceManager();
            CloseableResourceManager closeableResourceManager = pair.getSecond();
            Pair<D, RegistryAccess.Frozen> pair2 = worldDataSupplier.get(closeableResourceManager, pair.getFirst());
            Object object = pair2.getFirst();
            RegistryAccess.Frozen frozen = pair2.getSecond();
            return ((CompletableFuture)ReloadableServerResources.loadResources(closeableResourceManager, frozen, initConfig.commandSelection(), initConfig.functionCompilationLevel(), executor, executor2).whenComplete((reloadableServerResources, throwable) -> {
                if (throwable != null) {
                    closeableResourceManager.close();
                }
            })).thenApplyAsync(reloadableServerResources -> {
                reloadableServerResources.updateRegistryTags(frozen);
                return resultFactory.create(closeableResourceManager, (ReloadableServerResources)reloadableServerResources, frozen, object);
            }, executor2);
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    public record InitConfig(PackConfig packConfig, Commands.CommandSelection commandSelection, int functionCompilationLevel) {
    }

    public record PackConfig(PackRepository packRepository, DataPackConfig initialDataPacks, boolean safeMode) {
        public Pair<DataPackConfig, CloseableResourceManager> createResourceManager() {
            DataPackConfig dataPackConfig = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataPacks, this.safeMode);
            List<PackResources> list = this.packRepository.openAllSelected();
            MultiPackResourceManager closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, list);
            return Pair.of(dataPackConfig, closeableResourceManager);
        }
    }

    @FunctionalInterface
    public static interface WorldDataSupplier<D> {
        public Pair<D, RegistryAccess.Frozen> get(ResourceManager var1, DataPackConfig var2);
    }

    @FunctionalInterface
    public static interface ResultFactory<D, R> {
        public R create(CloseableResourceManager var1, ReloadableServerResources var2, RegistryAccess.Frozen var3, D var4);
    }
}

