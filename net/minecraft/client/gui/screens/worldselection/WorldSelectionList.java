/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.WorldStem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldSelectionList
extends ObjectSelectionList<Entry> {
    static final Logger LOGGER = LogUtils.getLogger();
    static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
    static final Component FROM_NEWER_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
    static final Component FROM_NEWER_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
    static final Component SNAPSHOT_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
    static final Component SNAPSHOT_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
    static final Component WORLD_LOCKED_TOOLTIP = Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
    static final Component WORLD_REQUIRES_CONVERSION = Component.translatable("selectWorld.conversion.tooltip").withStyle(ChatFormatting.RED);
    private static final Duration MAX_LOAD_BLOCK_TIME = Duration.ofMillis(100L);
    private final SelectWorldScreen screen;
    @Nullable
    private CompletableFuture<List<LevelSummary>> levelsFuture;
    private final LoadingHeader loadingHeader;

    public WorldSelectionList(SelectWorldScreen selectWorldScreen, Minecraft minecraft, int i, int j, int k, int l, int m, Supplier<String> supplier, @Nullable WorldSelectionList worldSelectionList) {
        super(minecraft, i, j, k, l, m);
        this.screen = selectWorldScreen;
        this.loadingHeader = new LoadingHeader(minecraft);
        if (worldSelectionList != null) {
            this.levelsFuture = worldSelectionList.levelsFuture;
            this.refreshList(supplier.get());
        } else {
            this.reloadLevels(supplier);
        }
    }

    public void reloadLevels(Supplier<String> supplier) {
        this.levelsFuture = this.loadLevels();
        List<LevelSummary> list2 = this.pollReadyLevels(this.levelsFuture, MAX_LOAD_BLOCK_TIME);
        if (list2 != null) {
            this.fillLevels(supplier.get(), list2);
        } else {
            this.fillLoadingLevels();
            this.levelsFuture.thenAcceptAsync(list -> this.fillLevels((String)supplier.get(), (List<LevelSummary>)list), (Executor)this.minecraft);
        }
    }

    public void refreshList(String string) {
        if (this.levelsFuture == null) {
            this.clearEntries();
            return;
        }
        List<LevelSummary> list = this.pollReadyLevels(this.levelsFuture, Duration.ZERO);
        if (list != null) {
            this.fillLevels(string, list);
        } else {
            this.fillLoadingLevels();
        }
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorageSource.LevelCandidates levelCandidates;
        try {
            levelCandidates = this.minecraft.getLevelSource().findLevelCandidates();
        } catch (LevelStorageException levelStorageException) {
            LOGGER.error("Couldn't load level list", levelStorageException);
            this.handleLevelLoadFailure(levelStorageException.getMessageComponent());
            return CompletableFuture.completedFuture(List.of());
        }
        if (levelCandidates.isEmpty()) {
            CreateWorldScreen.openFresh(this.minecraft, null);
            return CompletableFuture.completedFuture(List.of());
        }
        return this.minecraft.getLevelSource().loadLevelSummaries(levelCandidates).exceptionally(throwable -> {
            this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Couldn't load level list"));
            return List.of();
        });
    }

    @Nullable
    private List<LevelSummary> pollReadyLevels(CompletableFuture<List<LevelSummary>> completableFuture, Duration duration) {
        List<LevelSummary> list = null;
        try {
            list = completableFuture.get(duration.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException exception) {
            // empty catch block
        }
        return list;
    }

    private void fillLevels(String string, List<LevelSummary> list) {
        this.clearEntries();
        string = string.toLowerCase(Locale.ROOT);
        for (LevelSummary levelSummary : list) {
            if (!this.filterAccepts(string, levelSummary)) continue;
            this.addEntry(new WorldListEntry(this, levelSummary));
        }
        this.notifyListUpdated();
    }

    private boolean filterAccepts(String string, LevelSummary levelSummary) {
        return levelSummary.getLevelName().toLowerCase(Locale.ROOT).contains(string) || levelSummary.getLevelId().toLowerCase(Locale.ROOT).contains(string);
    }

    private void fillLoadingLevels() {
        this.clearEntries();
        this.addEntry(this.loadingHeader);
        this.notifyListUpdated();
    }

    private void notifyListUpdated() {
        this.screen.triggerImmediateNarration(true);
    }

    private void handleLevelLoadFailure(Component component) {
        this.minecraft.setScreen(new ErrorScreen(Component.translatable("selectWorld.unable_to_load"), component));
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    @Override
    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    @Override
    public void setSelected(@Nullable Entry entry) {
        super.setSelected(entry);
        this.screen.updateButtonStatus(entry != null && entry.isSelectable());
    }

    @Override
    protected void moveSelection(AbstractSelectionList.SelectionDirection selectionDirection) {
        this.moveSelection(selectionDirection, Entry::isSelectable);
    }

    public Optional<WorldListEntry> getSelectedOpt() {
        Entry entry = (Entry)this.getSelected();
        if (entry instanceof WorldListEntry) {
            WorldListEntry worldListEntry = (WorldListEntry)entry;
            return Optional.of(worldListEntry);
        }
        return Optional.empty();
    }

    public SelectWorldScreen getScreen() {
        return this.screen;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        if (this.children().contains(this.loadingHeader)) {
            this.loadingHeader.updateNarration(narrationElementOutput);
            return;
        }
        super.updateNarration(narrationElementOutput);
    }

    @Environment(value=EnvType.CLIENT)
    public static class LoadingHeader
    extends Entry {
        private static final Component LOADING_LABEL = Component.translatable("selectWorld.loading_list");
        private final Minecraft minecraft;

        public LoadingHeader(Minecraft minecraft) {
            this.minecraft = minecraft;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            int p = (this.minecraft.screen.width - this.minecraft.font.width(LOADING_LABEL)) / 2;
            int q = j + (m - this.minecraft.font.lineHeight) / 2;
            this.minecraft.font.draw(poseStack, LOADING_LABEL, (float)p, (float)q, 0xFFFFFF);
            String string = LoadingDotsText.get(Util.getMillis());
            int r = (this.minecraft.screen.width - this.minecraft.font.width(string)) / 2;
            int s = q + this.minecraft.font.lineHeight;
            this.minecraft.font.draw(poseStack, string, (float)r, (float)s, 0x808080);
        }

        @Override
        public Component getNarration() {
            return LOADING_LABEL;
        }

        @Override
        public boolean isSelectable() {
            return false;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public final class WorldListEntry
    extends Entry
    implements AutoCloseable {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private static final int ICON_OVERLAY_X_JOIN = 0;
        private static final int ICON_OVERLAY_X_JOIN_WITH_NOTIFY = 32;
        private static final int ICON_OVERLAY_X_WARNING = 64;
        private static final int ICON_OVERLAY_X_ERROR = 96;
        private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
        private static final int ICON_OVERLAY_Y_SELECTED = 32;
        private final Minecraft minecraft;
        private final SelectWorldScreen screen;
        private final LevelSummary summary;
        private final ResourceLocation iconLocation;
        @Nullable
        private Path iconFile;
        @Nullable
        private final DynamicTexture icon;
        private long lastClickTime;

        public WorldListEntry(WorldSelectionList worldSelectionList2, LevelSummary levelSummary) {
            this.minecraft = worldSelectionList2.minecraft;
            this.screen = worldSelectionList2.getScreen();
            this.summary = levelSummary;
            String string = levelSummary.getLevelId();
            this.iconLocation = new ResourceLocation("minecraft", "worlds/" + Util.sanitizeName(string, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon");
            this.iconFile = levelSummary.getIcon();
            if (!Files.isRegularFile(this.iconFile, new LinkOption[0])) {
                this.iconFile = null;
            }
            this.icon = this.loadServerIcon();
        }

        @Override
        public Component getNarration() {
            MutableComponent component = Component.translatable("narrator.select.world", this.summary.getLevelName(), new Date(this.summary.getLastPlayed()), this.summary.isHardcore() ? Component.translatable("gameMode.hardcore") : Component.translatable("gameMode." + this.summary.getGameMode().getName()), this.summary.hasCheats() ? Component.translatable("selectWorld.cheats") : CommonComponents.EMPTY, this.summary.getWorldVersionName());
            MutableComponent component2 = this.summary.isLocked() ? CommonComponents.joinForNarration(component, WORLD_LOCKED_TOOLTIP) : component;
            return Component.translatable("narrator.select", component2);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            Object string = this.summary.getLevelName();
            String string2 = this.summary.getLevelId() + " (" + DATE_FORMAT.format(new Date(this.summary.getLastPlayed())) + ")";
            if (StringUtils.isEmpty((CharSequence)string)) {
                string = I18n.get("selectWorld.world", new Object[0]) + " " + (i + 1);
            }
            Component component = this.summary.getInfo();
            this.minecraft.font.draw(poseStack, (String)string, (float)(k + 32 + 3), (float)(j + 1), 0xFFFFFF);
            this.minecraft.font.draw(poseStack, string2, (float)(k + 32 + 3), (float)(j + this.minecraft.font.lineHeight + 3), 0x808080);
            this.minecraft.font.draw(poseStack, component, (float)(k + 32 + 3), (float)(j + this.minecraft.font.lineHeight + this.minecraft.font.lineHeight + 3), 0x808080);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, this.icon != null ? this.iconLocation : ICON_MISSING);
            RenderSystem.enableBlend();
            GuiComponent.blit(poseStack, k, j, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.minecraft.options.touchscreen().get().booleanValue() || bl) {
                int q;
                RenderSystem.setShaderTexture(0, ICON_OVERLAY_LOCATION);
                GuiComponent.fill(poseStack, k, j, k + 32, j + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                int p = n - k;
                boolean bl2 = p < 32;
                int n2 = q = bl2 ? 32 : 0;
                if (this.summary.isLocked()) {
                    GuiComponent.blit(poseStack, k, j, 96.0f, q, 32, 32, 256, 256);
                    if (bl2) {
                        this.screen.setToolTip(this.minecraft.font.split(WORLD_LOCKED_TOOLTIP, 175));
                    }
                } else if (this.summary.requiresManualConversion()) {
                    GuiComponent.blit(poseStack, k, j, 96.0f, q, 32, 32, 256, 256);
                    if (bl2) {
                        this.screen.setToolTip(this.minecraft.font.split(WORLD_REQUIRES_CONVERSION, 175));
                    }
                } else if (this.summary.markVersionInList()) {
                    GuiComponent.blit(poseStack, k, j, 32.0f, q, 32, 32, 256, 256);
                    if (this.summary.askToOpenWorld()) {
                        GuiComponent.blit(poseStack, k, j, 96.0f, q, 32, 32, 256, 256);
                        if (bl2) {
                            this.screen.setToolTip(ImmutableList.of(FROM_NEWER_TOOLTIP_1.getVisualOrderText(), FROM_NEWER_TOOLTIP_2.getVisualOrderText()));
                        }
                    } else if (!SharedConstants.getCurrentVersion().isStable()) {
                        GuiComponent.blit(poseStack, k, j, 64.0f, q, 32, 32, 256, 256);
                        if (bl2) {
                            this.screen.setToolTip(ImmutableList.of(SNAPSHOT_TOOLTIP_1.getVisualOrderText(), SNAPSHOT_TOOLTIP_2.getVisualOrderText()));
                        }
                    }
                } else {
                    GuiComponent.blit(poseStack, k, j, 0.0f, q, 32, 32, 256, 256);
                }
            }
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (this.summary.isDisabled()) {
                return true;
            }
            WorldSelectionList.this.setSelected(this);
            this.screen.updateButtonStatus(WorldSelectionList.this.getSelectedOpt().isPresent());
            if (d - (double)WorldSelectionList.this.getRowLeft() <= 32.0) {
                this.joinWorld();
                return true;
            }
            if (Util.getMillis() - this.lastClickTime < 250L) {
                this.joinWorld();
                return true;
            }
            this.lastClickTime = Util.getMillis();
            return false;
        }

        public void joinWorld() {
            if (this.summary.isDisabled()) {
                return;
            }
            LevelSummary.BackupStatus backupStatus = this.summary.backupStatus();
            if (backupStatus.shouldBackup()) {
                String string = "selectWorld.backupQuestion." + backupStatus.getTranslationKey();
                String string2 = "selectWorld.backupWarning." + backupStatus.getTranslationKey();
                MutableComponent mutableComponent = Component.translatable(string);
                if (backupStatus.isSevere()) {
                    mutableComponent.withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
                }
                MutableComponent component = Component.translatable(string2, this.summary.getWorldVersionName(), SharedConstants.getCurrentVersion().getName());
                this.minecraft.setScreen(new BackupConfirmScreen(this.screen, (bl, bl2) -> {
                    if (bl) {
                        String string = this.summary.getLevelId();
                        try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(string);){
                            EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
                        } catch (IOException iOException) {
                            SystemToast.onWorldAccessFailure(this.minecraft, string);
                            LOGGER.error("Failed to backup level {}", (Object)string, (Object)iOException);
                        }
                    }
                    this.loadWorld();
                }, mutableComponent, component, false));
            } else if (this.summary.askToOpenWorld()) {
                this.minecraft.setScreen(new ConfirmScreen(bl -> {
                    if (bl) {
                        try {
                            this.loadWorld();
                        } catch (Exception exception) {
                            LOGGER.error("Failure to open 'future world'", exception);
                            this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this.screen), Component.translatable("selectWorld.futureworld.error.title"), Component.translatable("selectWorld.futureworld.error.text")));
                        }
                    } else {
                        this.minecraft.setScreen(this.screen);
                    }
                }, Component.translatable("selectWorld.versionQuestion"), Component.translatable("selectWorld.versionWarning", this.summary.getWorldVersionName()), Component.translatable("selectWorld.versionJoinButton"), CommonComponents.GUI_CANCEL));
            } else {
                this.loadWorld();
            }
        }

        public void deleteWorld() {
            this.minecraft.setScreen(new ConfirmScreen(bl -> {
                if (bl) {
                    this.minecraft.setScreen(new ProgressScreen(true));
                    this.doDeleteWorld();
                }
                this.minecraft.setScreen(this.screen);
            }, Component.translatable("selectWorld.deleteQuestion"), Component.translatable("selectWorld.deleteWarning", this.summary.getLevelName()), Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
        }

        public void doDeleteWorld() {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            String string = this.summary.getLevelId();
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);){
                levelStorageAccess.deleteLevel();
            } catch (IOException iOException) {
                SystemToast.onWorldDeleteFailure(this.minecraft, string);
                LOGGER.error("Failed to delete world {}", (Object)string, (Object)iOException);
            }
            WorldSelectionList.this.reloadLevels(this.screen.getFilterSupplier());
        }

        public void editWorld() {
            this.queueLoadScreen();
            String string = this.summary.getLevelId();
            try {
                LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(string);
                this.minecraft.setScreen(new EditWorldScreen(bl -> {
                    try {
                        levelStorageAccess.close();
                    } catch (IOException iOException) {
                        LOGGER.error("Failed to unlock level {}", (Object)string, (Object)iOException);
                    }
                    if (bl) {
                        WorldSelectionList.this.reloadLevels(this.screen.getFilterSupplier());
                    }
                    this.minecraft.setScreen(this.screen);
                }, levelStorageAccess));
            } catch (IOException iOException) {
                SystemToast.onWorldAccessFailure(this.minecraft, string);
                LOGGER.error("Failed to access level {}", (Object)string, (Object)iOException);
                WorldSelectionList.this.reloadLevels(this.screen.getFilterSupplier());
            }
        }

        public void recreateWorld() {
            this.queueLoadScreen();
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(this.summary.getLevelId());
                 WorldStem worldStem = this.minecraft.createWorldOpenFlows().loadWorldStem(levelStorageAccess, false);){
                WorldGenSettings worldGenSettings = worldStem.worldData().worldGenSettings();
                Path path = CreateWorldScreen.createTempDataPackDirFromExistingWorld(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR), this.minecraft);
                if (worldGenSettings.isOldCustomizedWorld()) {
                    this.minecraft.setScreen(new ConfirmScreen(bl -> this.minecraft.setScreen(bl ? CreateWorldScreen.createFromExisting(this.screen, worldStem, path) : this.screen), Component.translatable("selectWorld.recreate.customized.title"), Component.translatable("selectWorld.recreate.customized.text"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
                } else {
                    this.minecraft.setScreen(CreateWorldScreen.createFromExisting(this.screen, worldStem, path));
                }
            } catch (Exception exception) {
                LOGGER.error("Unable to recreate world", exception);
                this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this.screen), Component.translatable("selectWorld.recreate.error.title"), Component.translatable("selectWorld.recreate.error.text")));
            }
        }

        private void loadWorld() {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            if (this.minecraft.getLevelSource().levelExists(this.summary.getLevelId())) {
                this.queueLoadScreen();
                this.minecraft.createWorldOpenFlows().loadLevel(this.screen, this.summary.getLevelId());
            }
        }

        private void queueLoadScreen() {
            this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
        }

        @Nullable
        private DynamicTexture loadServerIcon() {
            boolean bl;
            boolean bl2 = bl = this.iconFile != null && Files.isRegularFile(this.iconFile, new LinkOption[0]);
            if (bl) {
                DynamicTexture dynamicTexture;
                block9: {
                    InputStream inputStream = Files.newInputStream(this.iconFile, new OpenOption[0]);
                    try {
                        NativeImage nativeImage = NativeImage.read(inputStream);
                        Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                        Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                        DynamicTexture dynamicTexture2 = new DynamicTexture(nativeImage);
                        this.minecraft.getTextureManager().register(this.iconLocation, (AbstractTexture)dynamicTexture2);
                        dynamicTexture = dynamicTexture2;
                        if (inputStream == null) break block9;
                    } catch (Throwable throwable) {
                        try {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        } catch (Throwable throwable3) {
                            LOGGER.error("Invalid icon for world {}", (Object)this.summary.getLevelId(), (Object)throwable3);
                            this.iconFile = null;
                            return null;
                        }
                    }
                    inputStream.close();
                }
                return dynamicTexture;
            }
            this.minecraft.getTextureManager().release(this.iconLocation);
            return null;
        }

        @Override
        public void close() {
            if (this.icon != null) {
                this.icon.close();
            }
        }

        public String getLevelName() {
            return this.summary.getLevelName();
        }

        @Override
        public boolean isSelectable() {
            return !this.summary.isDisabled();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
    extends ObjectSelectionList.Entry<Entry>
    implements AutoCloseable {
        public abstract boolean isSelectable();

        @Override
        public void close() {
        }
    }
}

