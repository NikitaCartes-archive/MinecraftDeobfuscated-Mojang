/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PackSelectionScreen
extends Screen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int LIST_WIDTH = 200;
    private static final Component DRAG_AND_DROP = Component.translatable("pack.dropInfo").withStyle(ChatFormatting.GRAY);
    private static final Component DIRECTORY_BUTTON_TOOLTIP = Component.translatable("pack.folderInfo");
    private static final int RELOAD_COOLDOWN = 20;
    private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
    private final PackSelectionModel model;
    private final Screen lastScreen;
    @Nullable
    private Watcher watcher;
    private long ticksToReload;
    private TransferableSelectionList availablePackList;
    private TransferableSelectionList selectedPackList;
    private final Path packDir;
    private Button doneButton;
    private final Map<String, ResourceLocation> packIcons = Maps.newHashMap();

    public PackSelectionScreen(Screen screen, PackRepository packRepository, Consumer<PackRepository> consumer, Path path, Component component) {
        super(component);
        this.lastScreen = screen;
        this.model = new PackSelectionModel(this::populateLists, this::getPackIcon, packRepository, consumer);
        this.packDir = path;
        this.watcher = Watcher.create(path);
    }

    @Override
    public void onClose() {
        this.model.commit();
        this.minecraft.setScreen(this.lastScreen);
        this.closeWatcher();
    }

    private void closeWatcher() {
        if (this.watcher != null) {
            try {
                this.watcher.close();
                this.watcher = null;
            } catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    protected void init() {
        this.availablePackList = new TransferableSelectionList(this.minecraft, this, 200, this.height, Component.translatable("pack.available.title"));
        this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
        this.addWidget(this.availablePackList);
        this.selectedPackList = new TransferableSelectionList(this.minecraft, this, 200, this.height, Component.translatable("pack.selected.title"));
        this.selectedPackList.setLeftPos(this.width / 2 + 4);
        this.addWidget(this.selectedPackList);
        this.addRenderableWidget(Button.builder(Component.translatable("pack.openFolder"), button -> Util.getPlatform().openUri(this.packDir.toUri())).bounds(this.width / 2 - 154, this.height - 48, 150, 20).tooltip(Tooltip.create(DIRECTORY_BUTTON_TOOLTIP)).build());
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 + 4, this.height - 48, 150, 20).build());
        this.reload();
    }

    @Override
    public void tick() {
        if (this.watcher != null) {
            try {
                if (this.watcher.pollForChanges()) {
                    this.ticksToReload = 20L;
                }
            } catch (IOException iOException) {
                LOGGER.warn("Failed to poll for directory {} changes, stopping", (Object)this.packDir);
                this.closeWatcher();
            }
        }
        if (this.ticksToReload > 0L && --this.ticksToReload == 0L) {
            this.reload();
        }
    }

    private void populateLists() {
        this.updateList(this.selectedPackList, this.model.getSelected());
        this.updateList(this.availablePackList, this.model.getUnselected());
        this.doneButton.active = !this.selectedPackList.children().isEmpty();
    }

    private void updateList(TransferableSelectionList transferableSelectionList, Stream<PackSelectionModel.Entry> stream) {
        transferableSelectionList.children().clear();
        TransferableSelectionList.PackEntry packEntry = (TransferableSelectionList.PackEntry)transferableSelectionList.getSelected();
        String string = packEntry == null ? "" : packEntry.getPackId();
        transferableSelectionList.setSelected(null);
        stream.forEach(entry -> {
            TransferableSelectionList.PackEntry packEntry = new TransferableSelectionList.PackEntry(this.minecraft, transferableSelectionList, (PackSelectionModel.Entry)entry);
            transferableSelectionList.children().add(packEntry);
            if (entry.getId().equals(string)) {
                transferableSelectionList.setSelected(packEntry);
            }
        });
    }

    public void updateFocus(TransferableSelectionList transferableSelectionList) {
        TransferableSelectionList transferableSelectionList2 = this.selectedPackList == transferableSelectionList ? this.availablePackList : this.selectedPackList;
        this.changeFocus(ComponentPath.path(transferableSelectionList2.getFirstElement(), new ContainerEventHandler[]{transferableSelectionList2, this}));
    }

    public void clearSelected() {
        this.selectedPackList.setSelected(null);
        this.availablePackList.setSelected(null);
    }

    private void reload() {
        this.model.findNewPacks();
        this.populateLists();
        this.ticksToReload = 0L;
        this.packIcons.clear();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(poseStack);
        this.availablePackList.render(poseStack, i, j, f);
        this.selectedPackList.render(poseStack, i, j, f);
        PackSelectionScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        PackSelectionScreen.drawCenteredString(poseStack, this.font, DRAG_AND_DROP, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    protected static void copyPacks(Minecraft minecraft, List<Path> list, Path path) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        list.forEach(path2 -> {
            try (Stream<Path> stream = Files.walk(path2, new FileVisitOption[0]);){
                stream.forEach(path3 -> {
                    try {
                        Util.copyBetweenDirs(path2.getParent(), path, path3);
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to copy datapack file  from {} to {}", path3, path, iOException);
                        mutableBoolean.setTrue();
                    }
                });
            } catch (IOException iOException) {
                LOGGER.warn("Failed to copy datapack file from {} to {}", path2, (Object)path);
                mutableBoolean.setTrue();
            }
        });
        if (mutableBoolean.isTrue()) {
            SystemToast.onPackCopyFailure(minecraft, path.toString());
        }
    }

    @Override
    public void onFilesDrop(List<Path> list) {
        String string = list.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
        this.minecraft.setScreen(new ConfirmScreen(bl -> {
            if (bl) {
                PackSelectionScreen.copyPacks(this.minecraft, list, this.packDir);
                this.reload();
            }
            this.minecraft.setScreen(this);
        }, Component.translatable("pack.dropConfirm"), Component.literal(string)));
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private ResourceLocation loadPackIcon(TextureManager textureManager, Pack pack) {
        try (PackResources packResources = pack.open();){
            ResourceLocation resourceLocation;
            block16: {
                IoSupplier<InputStream> ioSupplier = packResources.getRootResource("pack.png");
                if (ioSupplier == null) {
                    ResourceLocation resourceLocation2 = DEFAULT_ICON;
                    return resourceLocation2;
                }
                String string = pack.getId();
                ResourceLocation resourceLocation3 = new ResourceLocation("minecraft", "pack/" + Util.sanitizeName(string, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon");
                InputStream inputStream = ioSupplier.get();
                try {
                    NativeImage nativeImage = NativeImage.read(inputStream);
                    textureManager.register(resourceLocation3, (AbstractTexture)new DynamicTexture(nativeImage));
                    resourceLocation = resourceLocation3;
                    if (inputStream == null) break block16;
                } catch (Throwable throwable) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                inputStream.close();
            }
            return resourceLocation;
        } catch (Exception exception) {
            LOGGER.warn("Failed to load icon from pack {}", (Object)pack.getId(), (Object)exception);
            return DEFAULT_ICON;
        }
    }

    private ResourceLocation getPackIcon(Pack pack) {
        return this.packIcons.computeIfAbsent(pack.getId(), string -> this.loadPackIcon(this.minecraft.getTextureManager(), pack));
    }

    @Environment(value=EnvType.CLIENT)
    static class Watcher
    implements AutoCloseable {
        private final WatchService watcher;
        private final Path packPath;

        public Watcher(Path path) throws IOException {
            this.packPath = path;
            this.watcher = path.getFileSystem().newWatchService();
            try {
                this.watchDir(path);
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);){
                    for (Path path2 : directoryStream) {
                        if (!Files.isDirectory(path2, LinkOption.NOFOLLOW_LINKS)) continue;
                        this.watchDir(path2);
                    }
                }
            } catch (Exception exception) {
                this.watcher.close();
                throw exception;
            }
        }

        @Nullable
        public static Watcher create(Path path) {
            try {
                return new Watcher(path);
            } catch (IOException iOException) {
                LOGGER.warn("Failed to initialize pack directory {} monitoring", (Object)path, (Object)iOException);
                return null;
            }
        }

        private void watchDir(Path path) throws IOException {
            path.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        public boolean pollForChanges() throws IOException {
            WatchKey watchKey;
            boolean bl = false;
            while ((watchKey = this.watcher.poll()) != null) {
                List<WatchEvent<?>> list = watchKey.pollEvents();
                for (WatchEvent<?> watchEvent : list) {
                    Path path;
                    bl = true;
                    if (watchKey.watchable() != this.packPath || watchEvent.kind() != StandardWatchEventKinds.ENTRY_CREATE || !Files.isDirectory(path = this.packPath.resolve((Path)watchEvent.context()), LinkOption.NOFOLLOW_LINKS)) continue;
                    this.watchDir(path);
                }
                watchKey.reset();
            }
            return bl;
        }

        @Override
        public void close() throws IOException {
            this.watcher.close();
        }
    }
}

