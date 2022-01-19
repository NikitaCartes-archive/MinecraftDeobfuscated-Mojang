/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.AssetIndex;
import net.minecraft.client.resources.DefaultClientPackResources;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.HttpUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientPackSource
implements RepositorySource {
    private static final PackMetadataSection BUILT_IN = new PackMetadataSection(new TranslatableComponent("resourcePack.vanilla.description"), PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion()));
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    private static final int MAX_PACK_SIZE_BYTES = 0xFA00000;
    private static final int MAX_KEPT_PACKS = 10;
    private static final String VANILLA_ID = "vanilla";
    private static final String SERVER_ID = "server";
    private static final String PROGRAMMER_ART_ID = "programer_art";
    private static final String PROGRAMMER_ART_NAME = "Programmer Art";
    private static final Component APPLYING_PACK_TEXT = new TranslatableComponent("multiplayer.applyingPack");
    private final VanillaPackResources vanillaPack;
    private final File serverPackDir;
    private final ReentrantLock downloadLock = new ReentrantLock();
    private final AssetIndex assetIndex;
    @Nullable
    private CompletableFuture<?> currentDownload;
    @Nullable
    private Pack serverPack;

    public ClientPackSource(File file, AssetIndex assetIndex) {
        this.serverPackDir = file;
        this.assetIndex = assetIndex;
        this.vanillaPack = new DefaultClientPackResources(BUILT_IN, assetIndex);
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
        Pack pack2;
        Pack pack = Pack.create(VANILLA_ID, true, () -> this.vanillaPack, packConstructor, Pack.Position.BOTTOM, PackSource.BUILT_IN);
        if (pack != null) {
            consumer.accept(pack);
        }
        if (this.serverPack != null) {
            consumer.accept(this.serverPack);
        }
        if ((pack2 = this.createProgrammerArtPack(packConstructor)) != null) {
            consumer.accept(pack2);
        }
    }

    public VanillaPackResources getVanillaPack() {
        return this.vanillaPack;
    }

    private static Map<String, String> getDownloadHeaders() {
        HashMap<String, String> map = Maps.newHashMap();
        map.put("X-Minecraft-Username", Minecraft.getInstance().getUser().getName());
        map.put("X-Minecraft-UUID", Minecraft.getInstance().getUser().getUuid());
        map.put("X-Minecraft-Version", SharedConstants.getCurrentVersion().getName());
        map.put("X-Minecraft-Version-ID", SharedConstants.getCurrentVersion().getId());
        map.put("X-Minecraft-Pack-Format", String.valueOf(PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())));
        map.put("User-Agent", "Minecraft Java/" + SharedConstants.getCurrentVersion().getName());
        return map;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CompletableFuture<?> downloadAndSelectResourcePack(String string, String string2, boolean bl) {
        String string3 = Hashing.sha1().hashString(string, StandardCharsets.UTF_8).toString();
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
                ProgressScreen progressScreen = new ProgressScreen(bl);
                Map<String, String> map = ClientPackSource.getDownloadHeaders();
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.executeBlocking(() -> minecraft.setScreen(progressScreen));
                completableFuture = HttpUtil.downloadTo(file, string, map, 0xFA00000, progressScreen, minecraft.getProxy());
            }
            CompletableFuture<?> completableFuture2 = this.currentDownload = ((CompletableFuture)completableFuture.thenCompose(object -> {
                if (!this.checkHash(string4, file)) {
                    return Util.failedFuture(new RuntimeException("Hash check failure for file " + file + ", see log"));
                }
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.execute(() -> {
                    if (!bl) {
                        minecraft.setScreen(new GenericDirtMessageScreen(APPLYING_PACK_TEXT));
                    }
                });
                return this.setServerPack(file, PackSource.SERVER);
            })).whenComplete((void_, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Pack application failed: {}, deleting file {}", (Object)throwable.getMessage(), (Object)file);
                    ClientPackSource.deleteQuietly(file);
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.execute(() -> minecraft.setScreen(new ConfirmScreen(bl -> {
                        if (bl) {
                            minecraft.setScreen(null);
                        } else {
                            ClientPacketListener clientPacketListener = minecraft.getConnection();
                            if (clientPacketListener != null) {
                                clientPacketListener.getConnection().disconnect(new TranslatableComponent("connect.aborted"));
                            }
                        }
                    }, new TranslatableComponent("multiplayer.texturePrompt.failure.line1"), new TranslatableComponent("multiplayer.texturePrompt.failure.line2"), CommonComponents.GUI_PROCEED, new TranslatableComponent("menu.disconnect"))));
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
            ArrayList<File> list = Lists.newArrayList(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, null));
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
        PackMetadataSection packMetadataSection;
        try (FilePackResources filePackResources = new FilePackResources(file);){
            packMetadataSection = filePackResources.getMetadataSection(PackMetadataSection.SERIALIZER);
        } catch (IOException iOException) {
            return Util.failedFuture(new IOException(String.format("Invalid resourcepack at %s", file), iOException));
        }
        LOGGER.info("Applying server pack {}", (Object)file);
        this.serverPack = new Pack(SERVER_ID, true, () -> new FilePackResources(file), new TranslatableComponent("resourcePack.server.name"), packMetadataSection.getDescription(), PackCompatibility.forMetadata(packMetadataSection, PackType.CLIENT_RESOURCES), Pack.Position.TOP, true, packSource);
        return Minecraft.getInstance().delayTextureReload();
    }

    @Nullable
    private Pack createProgrammerArtPack(Pack.PackConstructor packConstructor) {
        File file2;
        Pack pack = null;
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
    private static Pack createProgrammerArtPack(Pack.PackConstructor packConstructor, Supplier<PackResources> supplier) {
        return Pack.create(PROGRAMMER_ART_ID, false, supplier, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN);
    }

    private static FolderPackResources createProgrammerArtDirPack(File file) {
        return new FolderPackResources(file){

            @Override
            public String getName() {
                return ClientPackSource.PROGRAMMER_ART_NAME;
            }
        };
    }

    private static PackResources createProgrammerArtZipPack(File file) {
        return new FilePackResources(file){

            @Override
            public String getName() {
                return ClientPackSource.PROGRAMMER_ART_NAME;
            }
        };
    }
}

