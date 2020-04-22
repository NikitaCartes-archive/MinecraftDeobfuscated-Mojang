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
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.resources.AssetIndex;
import net.minecraft.client.resources.DefaultClientResourcePack;
import net.minecraft.client.resources.UnopenedResourcePack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FileResourcePack;
import net.minecraft.server.packs.FolderResourcePack;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.VanillaPack;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.UnopenedPack;
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
    private final VanillaPack vanillaPack;
    private final File serverPackDir;
    private final ReentrantLock downloadLock = new ReentrantLock();
    private final AssetIndex assetIndex;
    @Nullable
    private CompletableFuture<?> currentDownload;
    @Nullable
    private UnopenedResourcePack serverPack;

    public ClientPackSource(File file, AssetIndex assetIndex) {
        this.serverPackDir = file;
        this.assetIndex = assetIndex;
        this.vanillaPack = new DefaultClientResourcePack(assetIndex);
    }

    @Override
    public <T extends UnopenedPack> void loadPacks(Map<String, T> map, UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor) {
        T unopenedPack = UnopenedPack.create("vanilla", true, () -> this.vanillaPack, unopenedPackConstructor, UnopenedPack.Position.BOTTOM);
        if (unopenedPack != null) {
            map.put("vanilla", unopenedPack);
        }
        if (this.serverPack != null) {
            map.put("server", this.serverPack);
        }
        this.addProgrammerArtPack(map, unopenedPackConstructor);
    }

    public VanillaPack getVanillaPack() {
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
                return this.setServerPack(file);
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

    public CompletableFuture<Void> setServerPack(File file) {
        PackMetadataSection packMetadataSection = null;
        NativeImage nativeImage = null;
        String string = null;
        try (FileResourcePack fileResourcePack = new FileResourcePack(file);){
            packMetadataSection = fileResourcePack.getMetadataSection(PackMetadataSection.SERIALIZER);
            try (InputStream inputStream = fileResourcePack.getRootResource("pack.png");){
                nativeImage = NativeImage.read(inputStream);
            } catch (IOException | IllegalArgumentException exception) {
                LOGGER.info("Could not read pack.png: {}", (Object)exception.getMessage());
            }
        } catch (IOException iOException) {
            string = iOException.getMessage();
        }
        if (string != null) {
            return Util.failedFuture(new RuntimeException(String.format("Invalid resourcepack at %s: %s", file, string)));
        }
        LOGGER.info("Applying server pack {}", (Object)file);
        this.serverPack = new UnopenedResourcePack("server", true, () -> new FileResourcePack(file), new TranslatableComponent("resourcePack.server.name"), packMetadataSection.getDescription(), PackCompatibility.forFormat(packMetadataSection.getPackFormat()), UnopenedPack.Position.TOP, true, nativeImage);
        return Minecraft.getInstance().delayTextureReload();
    }

    private <T extends UnopenedPack> void addProgrammerArtPack(Map<String, T> map, UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor) {
        File file2;
        File file = this.assetIndex.getFile(new ResourceLocation("resourcepacks/programmer_art.zip"));
        if (file != null && file.isFile() && ClientPackSource.addProgrammerArtPack(map, unopenedPackConstructor, () -> ClientPackSource.createProgrammerArtZipPack(file))) {
            return;
        }
        if (SharedConstants.IS_RUNNING_IN_IDE && (file2 = this.assetIndex.getRootFile("../resourcepacks/programmer_art")) != null && file2.isDirectory()) {
            ClientPackSource.addProgrammerArtPack(map, unopenedPackConstructor, () -> ClientPackSource.createProgrammerArtDirPack(file2));
        }
    }

    private static <T extends UnopenedPack> boolean addProgrammerArtPack(Map<String, T> map, UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor, Supplier<Pack> supplier) {
        T unopenedPack = UnopenedPack.create("programer_art", false, supplier, unopenedPackConstructor, UnopenedPack.Position.TOP);
        if (unopenedPack != null) {
            map.put("programer_art", unopenedPack);
            return true;
        }
        return false;
    }

    private static FolderResourcePack createProgrammerArtDirPack(File file) {
        return new FolderResourcePack(file){

            @Override
            public String getName() {
                return "Programmer Art";
            }
        };
    }

    private static Pack createProgrammerArtZipPack(File file) {
        return new FileResourcePack(file){

            @Override
            public String getName() {
                return "Programmer Art";
            }
        };
    }
}

