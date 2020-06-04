/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.resources.AssetIndex;
import net.minecraft.client.resources.DefaultClientPackResources;
import net.minecraft.client.resources.ResourcePack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.HttpUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientPackSource
implements RepositorySource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    private final VanillaPackResources vanillaPack;
    private final File serverPackDir;
    private final ReentrantLock downloadLock = new ReentrantLock();
    private final AssetIndex assetIndex;
    @Nullable
    private CompletableFuture<?> currentDownload;
    @Nullable
    private ResourcePack serverPack;

    public ClientPackSource(File file, AssetIndex assetIndex) {
        this.serverPackDir = file;
        this.assetIndex = assetIndex;
        this.vanillaPack = new DefaultClientPackResources(assetIndex);
    }

    @Override
    public <T extends Pack> void loadPacks(Consumer<T> consumer, Pack.PackConstructor<T> packConstructor) {
        T pack2;
        T pack = Pack.create("vanilla", true, () -> this.vanillaPack, packConstructor, Pack.Position.BOTTOM, PackSource.BUILT_IN);
        if (pack != null) {
            consumer.accept((ResourcePack)pack);
        }
        if (this.serverPack != null) {
            consumer.accept(this.serverPack);
        }
        if ((pack2 = this.createProgrammerArtPack(packConstructor)) != null) {
            consumer.accept((ResourcePack)pack2);
        }
    }

    public VanillaPackResources getVanillaPack() {
        return this.vanillaPack;
    }

    public static Map<String, String> getDownloadHeaders() {
        HashMap<String, String> map = Maps.newHashMap();
        map.put("X-Minecraft-Username", Minecraft.getInstance().getUser().getName());
        map.put("X-Minecraft-UUID", Minecraft.getInstance().getUser().getUuid());
        map.put("X-Minecraft-Version", SharedConstants.getCurrentVersion().getName());
        map.put("X-Minecraft-Version-ID", SharedConstants.getCurrentVersion().getId());
        map.put("X-Minecraft-Pack-Format", String.valueOf(SharedConstants.getCurrentVersion().getPackVersion()));
        map.put("User-Agent", "Minecraft Java/" + SharedConstants.getCurrentVersion().getName());
        return map;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CompletableFuture<?> downloadAndSelectResourcePack(String string, String string2) {
        String string3 = DigestUtils.sha1Hex(string);
        String string4 = SHA1.matcher(string2).matches() ? string2 : "";
        this.downloadLock.lock();
        try {
            CompletableFuture<String> completableFuture;
            this.clearServerPack();
            this.clearOldDownloads();
            File file = new File(this.serverPackDir, string3);
            if (file.exists()) {
                completableFuture = CompletableFuture.completedFuture("");
            } else {
                ProgressScreen progressScreen = new ProgressScreen();
                Map<String, String> map = ClientPackSource.getDownloadHeaders();
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.executeBlocking(() -> minecraft.setScreen(progressScreen));
                completableFuture = HttpUtil.downloadTo(file, string, map, 0x6400000, progressScreen, minecraft.getProxy());
            }
            CompletableFuture<?> completableFuture2 = this.currentDownload = ((CompletableFuture)completableFuture.thenCompose(object -> {
                if (!this.checkHash(string4, file)) {
                    return Util.failedFuture(new RuntimeException("Hash check failure for file " + file + ", see log"));
                }
                return this.setServerPack(file, PackSource.SERVER);
            })).whenComplete((void_, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Pack application failed: {}, deleting file {}", (Object)throwable.getMessage(), (Object)file);
                    ClientPackSource.deleteQuietly(file);
                }
            });
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

    public void clearServerPack() {
        this.downloadLock.lock();
        try {
            if (this.currentDownload != null) {
                this.currentDownload.cancel(true);
            }
            this.currentDownload = null;
            if (this.serverPack != null) {
                this.serverPack = null;
                Minecraft.getInstance().delayTextureReload();
            }
        } finally {
            this.downloadLock.unlock();
        }
    }

    private boolean checkHash(String string, File file) {
        try {
            String string2;
            try (FileInputStream fileInputStream = new FileInputStream(file);){
                string2 = DigestUtils.sha1Hex(fileInputStream);
            }
            if (string.isEmpty()) {
                LOGGER.info("Found file {} without verification hash", (Object)file);
                return true;
            }
            if (string2.toLowerCase(Locale.ROOT).equals(string.toLowerCase(Locale.ROOT))) {
                LOGGER.info("Found file {} matching requested hash {}", (Object)file, (Object)string);
                return true;
            }
            LOGGER.warn("File {} had wrong hash (expected {}, found {}).", (Object)file, (Object)string, (Object)string2);
        } catch (IOException iOException) {
            LOGGER.warn("File {} couldn't be hashed.", (Object)file, (Object)iOException);
        }
        return false;
    }

    private void clearOldDownloads() {
        try {
            ArrayList<File> list = Lists.newArrayList(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, null));
            list.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            int i = 0;
            for (File file : list) {
                if (i++ < 10) continue;
                LOGGER.info("Deleting old server resource pack {}", (Object)file.getName());
                FileUtils.deleteQuietly(file);
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.error("Error while deleting old server resource pack : {}", (Object)illegalArgumentException.getMessage());
        }
    }

    public CompletableFuture<Void> setServerPack(File file, PackSource packSource) {
        NativeImage nativeImage;
        PackMetadataSection packMetadataSection;
        try (FilePackResources filePackResources = new FilePackResources(file);){
            packMetadataSection = filePackResources.getMetadataSection(PackMetadataSection.SERIALIZER);
            nativeImage = ResourcePack.readIcon(filePackResources);
        } catch (IOException iOException) {
            return Util.failedFuture(new IOException(String.format("Invalid resourcepack at %s", file), iOException));
        }
        LOGGER.info("Applying server pack {}", (Object)file);
        this.serverPack = new ResourcePack("server", true, () -> new FilePackResources(file), new TranslatableComponent("resourcePack.server.name"), packMetadataSection.getDescription(), PackCompatibility.forFormat(packMetadataSection.getPackFormat()), Pack.Position.TOP, true, packSource, nativeImage);
        return Minecraft.getInstance().delayTextureReload();
    }

    @Nullable
    private <T extends Pack> T createProgrammerArtPack(Pack.PackConstructor<T> packConstructor) {
        File file2;
        T pack = null;
        File file = this.assetIndex.getFile(new ResourceLocation("resourcepacks/programmer_art.zip"));
        if (file != null && file.isFile()) {
            pack = ClientPackSource.createProgrammerArtPack(packConstructor, () -> ClientPackSource.createProgrammerArtZipPack(file));
        }
        if (pack == null && SharedConstants.IS_RUNNING_IN_IDE && (file2 = this.assetIndex.getRootFile("../resourcepacks/programmer_art")) != null && file2.isDirectory()) {
            pack = ClientPackSource.createProgrammerArtPack(packConstructor, () -> ClientPackSource.createProgrammerArtDirPack(file2));
        }
        return pack;
    }

    @Nullable
    private static <T extends Pack> T createProgrammerArtPack(Pack.PackConstructor<T> packConstructor, Supplier<PackResources> supplier) {
        return Pack.create("programer_art", false, supplier, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN);
    }

    private static FolderPackResources createProgrammerArtDirPack(File file) {
        return new FolderPackResources(file){

            @Override
            public String getName() {
                return "Programmer Art";
            }
        };
    }

    private static PackResources createProgrammerArtZipPack(File file) {
        return new FilePackResources(file){

            @Override
            public String getName() {
                return "Programmer Art";
            }
        };
    }
}

