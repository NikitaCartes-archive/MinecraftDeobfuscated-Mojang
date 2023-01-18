/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class DownloadedPackSource
implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    private static final int MAX_PACK_SIZE_BYTES = 0xFA00000;
    private static final int MAX_KEPT_PACKS = 10;
    private static final String SERVER_ID = "server";
    private static final Component SERVER_NAME = Component.translatable("resourcePack.server.name");
    private static final Component APPLYING_PACK_TEXT = Component.translatable("multiplayer.applyingPack");
    private final File serverPackDir;
    private final ReentrantLock downloadLock = new ReentrantLock();
    @Nullable
    private CompletableFuture<?> currentDownload;
    @Nullable
    private Pack serverPack;

    public DownloadedPackSource(File file) {
        this.serverPackDir = file;
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        if (this.serverPack != null) {
            consumer.accept(this.serverPack);
        }
    }

    private static Map<String, String> getDownloadHeaders() {
        return Map.of("X-Minecraft-Username", Minecraft.getInstance().getUser().getName(), "X-Minecraft-UUID", Minecraft.getInstance().getUser().getUuid(), "X-Minecraft-Version", SharedConstants.getCurrentVersion().getName(), "X-Minecraft-Version-ID", SharedConstants.getCurrentVersion().getId(), "X-Minecraft-Pack-Format", String.valueOf(SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES)), "User-Agent", "Minecraft Java/" + SharedConstants.getCurrentVersion().getName());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CompletableFuture<?> downloadAndSelectResourcePack(URL uRL, String string, boolean bl) {
        String string2 = Hashing.sha1().hashString(uRL.toString(), StandardCharsets.UTF_8).toString();
        String string3 = SHA1.matcher(string).matches() ? string : "";
        this.downloadLock.lock();
        try {
            CompletableFuture<String> completableFuture;
            Minecraft minecraft = Minecraft.getInstance();
            File file = new File(this.serverPackDir, string2);
            if (file.exists()) {
                completableFuture = CompletableFuture.completedFuture("");
            } else {
                ProgressScreen progressScreen = new ProgressScreen(bl);
                Map<String, String> map = DownloadedPackSource.getDownloadHeaders();
                minecraft.executeBlocking(() -> minecraft.setScreen(progressScreen));
                completableFuture = HttpUtil.downloadTo(file, uRL, map, 0xFA00000, progressScreen, minecraft.getProxy());
            }
            CompletableFuture<?> completableFuture2 = this.currentDownload = ((CompletableFuture)((CompletableFuture)completableFuture.thenCompose(object -> {
                if (!this.checkHash(string3, file)) {
                    return CompletableFuture.failedFuture(new RuntimeException("Hash check failure for file " + file + ", see log"));
                }
                minecraft.execute(() -> {
                    if (!bl) {
                        minecraft.setScreen(new GenericDirtMessageScreen(APPLYING_PACK_TEXT));
                    }
                });
                return this.setServerPack(file, PackSource.SERVER);
            })).exceptionallyCompose(throwable -> ((CompletableFuture)this.clearServerPack().thenAcceptAsync(void_ -> {
                LOGGER.warn("Pack application failed: {}, deleting file {}", (Object)throwable.getMessage(), (Object)file);
                DownloadedPackSource.deleteQuietly(file);
            }, (Executor)Util.ioPool())).thenAcceptAsync(void_ -> minecraft.setScreen(new ConfirmScreen(bl -> {
                if (bl) {
                    minecraft.setScreen(null);
                } else {
                    ClientPacketListener clientPacketListener = minecraft.getConnection();
                    if (clientPacketListener != null) {
                        clientPacketListener.getConnection().disconnect(Component.translatable("connect.aborted"));
                    }
                }
            }, Component.translatable("multiplayer.texturePrompt.failure.line1"), Component.translatable("multiplayer.texturePrompt.failure.line2"), CommonComponents.GUI_PROCEED, Component.translatable("menu.disconnect"))), (Executor)minecraft))).thenAcceptAsync(void_ -> this.clearOldDownloads(), (Executor)Util.ioPool());
            return completableFuture2;
        } finally {
            this.downloadLock.unlock();
        }
    }

    private static void deleteQuietly(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException iOException) {
            LOGGER.warn("Failed to delete file {}: {}", (Object)file, (Object)iOException.getMessage());
        }
    }

    public CompletableFuture<Void> clearServerPack() {
        this.downloadLock.lock();
        try {
            if (this.currentDownload != null) {
                this.currentDownload.cancel(true);
            }
            this.currentDownload = null;
            if (this.serverPack != null) {
                this.serverPack = null;
                CompletableFuture<Void> completableFuture = Minecraft.getInstance().delayTextureReload();
                return completableFuture;
            }
        } finally {
            this.downloadLock.unlock();
        }
        return CompletableFuture.completedFuture(null);
    }

    private boolean checkHash(String string, File file) {
        try {
            String string2 = com.google.common.io.Files.asByteSource(file).hash(Hashing.sha1()).toString();
            if (string.isEmpty()) {
                LOGGER.info("Found file {} without verification hash", (Object)file);
                return true;
            }
            if (string2.toLowerCase(Locale.ROOT).equals(string.toLowerCase(Locale.ROOT))) {
                LOGGER.info("Found file {} matching requested hash {}", (Object)file, (Object)string);
                return true;
            }
            LOGGER.warn("File {} had wrong hash (expected {}, found {}).", file, string, string2);
        } catch (IOException iOException) {
            LOGGER.warn("File {} couldn't be hashed.", (Object)file, (Object)iOException);
        }
        return false;
    }

    private void clearOldDownloads() {
        if (!this.serverPackDir.isDirectory()) {
            return;
        }
        try {
            ArrayList<File> list = new ArrayList<File>(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, null));
            list.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            int i = 0;
            for (File file : list) {
                if (i++ < 10) continue;
                LOGGER.info("Deleting old server resource pack {}", (Object)file.getName());
                FileUtils.deleteQuietly(file);
            }
        } catch (Exception exception) {
            LOGGER.error("Error while deleting old server resource pack : {}", (Object)exception.getMessage());
        }
    }

    public CompletableFuture<Void> setServerPack(File file, PackSource packSource) {
        Pack.ResourcesSupplier resourcesSupplier = string -> new FilePackResources(string, file, false);
        Pack.Info info = Pack.readPackInfo(SERVER_ID, resourcesSupplier);
        if (info == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid pack metadata at " + file));
        }
        LOGGER.info("Applying server pack {}", (Object)file);
        this.serverPack = Pack.create(SERVER_ID, SERVER_NAME, true, resourcesSupplier, info, PackType.CLIENT_RESOURCES, Pack.Position.TOP, true, packSource);
        return Minecraft.getInstance().delayTextureReload();
    }

    public CompletableFuture<Void> loadBundledResourcePack(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        Path path = levelStorageAccess.getLevelPath(LevelResource.MAP_RESOURCE_FILE);
        if (Files.exists(path, new LinkOption[0]) && !Files.isDirectory(path, new LinkOption[0])) {
            return this.setServerPack(path.toFile(), PackSource.WORLD);
        }
        return CompletableFuture.completedFuture(null);
    }
}

