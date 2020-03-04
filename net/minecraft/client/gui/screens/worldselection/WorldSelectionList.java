/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldSelectionList
extends ObjectSelectionList<WorldListEntry> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
    private final SelectWorldScreen screen;
    @Nullable
    private List<LevelSummary> cachedList;

    public WorldSelectionList(SelectWorldScreen selectWorldScreen, Minecraft minecraft, int i, int j, int k, int l, int m, Supplier<String> supplier, @Nullable WorldSelectionList worldSelectionList) {
        super(minecraft, i, j, k, l, m);
        this.screen = selectWorldScreen;
        if (worldSelectionList != null) {
            this.cachedList = worldSelectionList.cachedList;
        }
        this.refreshList(supplier, false);
    }

    public void refreshList(Supplier<String> supplier, boolean bl) {
        this.clearEntries();
        LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
        if (this.cachedList == null || bl) {
            try {
                this.cachedList = levelStorageSource.getLevelList();
            } catch (LevelStorageException levelStorageException) {
                LOGGER.error("Couldn't load level list", (Throwable)levelStorageException);
                this.minecraft.setScreen(new ErrorScreen(new TranslatableComponent("selectWorld.unable_to_load", new Object[0]), levelStorageException.getMessage()));
                return;
            }
            Collections.sort(this.cachedList);
        }
        String string = supplier.get().toLowerCase(Locale.ROOT);
        for (LevelSummary levelSummary : this.cachedList) {
            if (!levelSummary.getLevelName().toLowerCase(Locale.ROOT).contains(string) && !levelSummary.getLevelId().toLowerCase(Locale.ROOT).contains(string)) continue;
            this.addEntry(new WorldListEntry(this, levelSummary, this.minecraft.getLevelSource()));
        }
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
    public void setSelected(@Nullable WorldListEntry worldListEntry) {
        super.setSelected(worldListEntry);
        if (worldListEntry != null) {
            LevelSummary levelSummary = worldListEntry.summary;
            NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", new TranslatableComponent("narrator.select.world", levelSummary.getLevelName(), new Date(levelSummary.getLastPlayed()), levelSummary.isHardcore() ? I18n.get("gameMode.hardcore", new Object[0]) : I18n.get("gameMode." + levelSummary.getGameMode().getName(), new Object[0]), levelSummary.hasCheats() ? I18n.get("selectWorld.cheats", new Object[0]) : "", levelSummary.getWorldVersionName())).getString());
        }
    }

    @Override
    protected void moveSelection(int i) {
        super.moveSelection(i);
        this.screen.updateButtonStatus(true);
    }

    public Optional<WorldListEntry> getSelectedOpt() {
        return Optional.ofNullable(this.getSelected());
    }

    public SelectWorldScreen getScreen() {
        return this.screen;
    }

    @Environment(value=EnvType.CLIENT)
    public final class WorldListEntry
    extends ObjectSelectionList.Entry<WorldListEntry>
    implements AutoCloseable {
        private final Minecraft minecraft;
        private final SelectWorldScreen screen;
        private final LevelSummary summary;
        private final ResourceLocation iconLocation;
        private File iconFile;
        @Nullable
        private final DynamicTexture icon;
        private long lastClickTime;

        public WorldListEntry(WorldSelectionList worldSelectionList2, LevelSummary levelSummary, LevelStorageSource levelStorageSource) {
            this.screen = worldSelectionList2.getScreen();
            this.summary = levelSummary;
            this.minecraft = Minecraft.getInstance();
            this.iconLocation = new ResourceLocation("worlds/" + Hashing.sha1().hashUnencodedChars(levelSummary.getLevelId()) + "/icon");
            this.iconFile = levelStorageSource.getFile(levelSummary.getLevelId(), "icon.png");
            if (!this.iconFile.isFile()) {
                this.iconFile = null;
            }
            this.icon = this.loadServerIcon();
        }

        @Override
        public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            String string = this.summary.getLevelName();
            String string2 = this.summary.getLevelId() + " (" + DATE_FORMAT.format(new Date(this.summary.getLastPlayed())) + ")";
            if (StringUtils.isEmpty(string)) {
                string = I18n.get("selectWorld.world", new Object[0]) + " " + (i + 1);
            }
            String string3 = "";
            if (this.summary.isRequiresConversion()) {
                string3 = I18n.get("selectWorld.conversion", new Object[0]) + " " + string3;
            } else {
                string3 = I18n.get("gameMode." + this.summary.getGameMode().getName(), new Object[0]);
                if (this.summary.isHardcore()) {
                    string3 = (Object)((Object)ChatFormatting.DARK_RED) + I18n.get("gameMode.hardcore", new Object[0]) + (Object)((Object)ChatFormatting.RESET);
                }
                if (this.summary.hasCheats()) {
                    string3 = string3 + ", " + I18n.get("selectWorld.cheats", new Object[0]);
                }
                String string4 = this.summary.getWorldVersionName().getColoredString();
                string3 = this.summary.markVersionInList() ? (this.summary.askToOpenWorld() ? string3 + ", " + I18n.get("selectWorld.version", new Object[0]) + " " + (Object)((Object)ChatFormatting.RED) + string4 + (Object)((Object)ChatFormatting.RESET) : string3 + ", " + I18n.get("selectWorld.version", new Object[0]) + " " + (Object)((Object)ChatFormatting.ITALIC) + string4 + (Object)((Object)ChatFormatting.RESET)) : string3 + ", " + I18n.get("selectWorld.version", new Object[0]) + " " + string4;
            }
            this.minecraft.font.draw(string, k + 32 + 3, j + 1, 0xFFFFFF);
            this.minecraft.font.draw(string2, k + 32 + 3, j + this.minecraft.font.lineHeight + 3, 0x808080);
            this.minecraft.font.draw(string3, k + 32 + 3, j + this.minecraft.font.lineHeight + this.minecraft.font.lineHeight + 3, 0x808080);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.minecraft.getTextureManager().bind(this.icon != null ? this.iconLocation : ICON_MISSING);
            RenderSystem.enableBlend();
            GuiComponent.blit(k, j, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.minecraft.options.touchscreen || bl) {
                int q;
                this.minecraft.getTextureManager().bind(ICON_OVERLAY_LOCATION);
                GuiComponent.fill(k, j, k + 32, j + 32, -1601138544);
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                int p = n - k;
                int n2 = q = p < 32 ? 32 : 0;
                if (this.summary.markVersionInList()) {
                    GuiComponent.blit(k, j, 32.0f, q, 32, 32, 256, 256);
                    if (this.summary.isOldCustomizedWorld()) {
                        GuiComponent.blit(k, j, 96.0f, q, 32, 32, 256, 256);
                        if (p < 32) {
                            Component component = new TranslatableComponent("selectWorld.tooltip.unsupported", this.summary.getWorldVersionName()).withStyle(ChatFormatting.RED);
                            this.screen.setToolTip(this.minecraft.font.insertLineBreaks(component.getColoredString(), 175));
                        }
                    } else if (this.summary.askToOpenWorld()) {
                        GuiComponent.blit(k, j, 96.0f, q, 32, 32, 256, 256);
                        if (p < 32) {
                            this.screen.setToolTip((Object)((Object)ChatFormatting.RED) + I18n.get("selectWorld.tooltip.fromNewerVersion1", new Object[0]) + "\n" + (Object)((Object)ChatFormatting.RED) + I18n.get("selectWorld.tooltip.fromNewerVersion2", new Object[0]));
                        }
                    } else if (!SharedConstants.getCurrentVersion().isStable()) {
                        GuiComponent.blit(k, j, 64.0f, q, 32, 32, 256, 256);
                        if (p < 32) {
                            this.screen.setToolTip((Object)((Object)ChatFormatting.GOLD) + I18n.get("selectWorld.tooltip.snapshot1", new Object[0]) + "\n" + (Object)((Object)ChatFormatting.GOLD) + I18n.get("selectWorld.tooltip.snapshot2", new Object[0]));
                        }
                    }
                } else {
                    GuiComponent.blit(k, j, 0.0f, q, 32, 32, 256, 256);
                }
            }
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
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
            if (this.summary.shouldBackup() || this.summary.isOldCustomizedWorld()) {
                TranslatableComponent component = new TranslatableComponent("selectWorld.backupQuestion", new Object[0]);
                TranslatableComponent component2 = new TranslatableComponent("selectWorld.backupWarning", this.summary.getWorldVersionName().getColoredString(), SharedConstants.getCurrentVersion().getName());
                if (this.summary.isOldCustomizedWorld()) {
                    component = new TranslatableComponent("selectWorld.backupQuestion.customized", new Object[0]);
                    component2 = new TranslatableComponent("selectWorld.backupWarning.customized", new Object[0]);
                }
                this.minecraft.setScreen(new BackupConfirmScreen(this.screen, (bl, bl2) -> {
                    if (bl) {
                        String string = this.summary.getLevelId();
                        EditWorldScreen.makeBackupAndShowToast(this.minecraft.getLevelSource(), string);
                    }
                    this.loadWorld();
                }, component, component2, false));
            } else if (this.summary.askToOpenWorld()) {
                this.minecraft.setScreen(new ConfirmScreen(bl -> {
                    if (bl) {
                        try {
                            this.loadWorld();
                        } catch (Exception exception) {
                            LOGGER.error("Failure to open 'future world'", (Throwable)exception);
                            this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this.screen), new TranslatableComponent("selectWorld.futureworld.error.title", new Object[0]), new TranslatableComponent("selectWorld.futureworld.error.text", new Object[0])));
                        }
                    } else {
                        this.minecraft.setScreen(this.screen);
                    }
                }, new TranslatableComponent("selectWorld.versionQuestion", new Object[0]), new TranslatableComponent("selectWorld.versionWarning", this.summary.getWorldVersionName().getColoredString()), I18n.get("selectWorld.versionJoinButton", new Object[0]), I18n.get("gui.cancel", new Object[0])));
            } else {
                this.loadWorld();
            }
        }

        public void deleteWorld() {
            this.minecraft.setScreen(new ConfirmScreen(bl -> {
                if (bl) {
                    this.minecraft.setScreen(new ProgressScreen());
                    LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
                    levelStorageSource.deleteLevel(this.summary.getLevelId());
                    WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
                }
                this.minecraft.setScreen(this.screen);
            }, new TranslatableComponent("selectWorld.deleteQuestion", new Object[0]), new TranslatableComponent("selectWorld.deleteWarning", this.summary.getLevelName()), I18n.get("selectWorld.deleteButton", new Object[0]), I18n.get("gui.cancel", new Object[0])));
        }

        public void editWorld() {
            this.minecraft.setScreen(new EditWorldScreen(bl -> {
                if (bl) {
                    WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
                }
                this.minecraft.setScreen(this.screen);
            }, this.summary.getLevelId()));
        }

        public void recreateWorld() {
            try {
                this.minecraft.setScreen(new ProgressScreen());
                CreateWorldScreen createWorldScreen = new CreateWorldScreen(this.screen);
                LevelStorage levelStorage = this.minecraft.getLevelSource().selectLevel(this.summary.getLevelId(), null);
                LevelData levelData = levelStorage.prepareLevel();
                if (levelData != null) {
                    createWorldScreen.copyFromWorld(levelData);
                    if (this.summary.isOldCustomizedWorld()) {
                        this.minecraft.setScreen(new ConfirmScreen(bl -> this.minecraft.setScreen(bl ? createWorldScreen : this.screen), new TranslatableComponent("selectWorld.recreate.customized.title", new Object[0]), new TranslatableComponent("selectWorld.recreate.customized.text", new Object[0]), I18n.get("gui.proceed", new Object[0]), I18n.get("gui.cancel", new Object[0])));
                    } else {
                        this.minecraft.setScreen(createWorldScreen);
                    }
                }
            } catch (Exception exception) {
                LOGGER.error("Unable to recreate world", (Throwable)exception);
                this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this.screen), new TranslatableComponent("selectWorld.recreate.error.title", new Object[0]), new TranslatableComponent("selectWorld.recreate.error.text", new Object[0])));
            }
        }

        private void loadWorld() {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            if (this.minecraft.getLevelSource().levelExists(this.summary.getLevelId())) {
                this.minecraft.selectLevel(this.summary.getLevelId(), this.summary.getLevelName(), null);
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        @Nullable
        private DynamicTexture loadServerIcon() {
            boolean bl;
            boolean bl2 = bl = this.iconFile != null && this.iconFile.isFile();
            if (!bl) {
                this.minecraft.getTextureManager().release(this.iconLocation);
                return null;
            }
            try (FileInputStream inputStream = new FileInputStream(this.iconFile);){
                NativeImage nativeImage = NativeImage.read(inputStream);
                Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                DynamicTexture dynamicTexture2 = new DynamicTexture(nativeImage);
                this.minecraft.getTextureManager().register(this.iconLocation, (AbstractTexture)dynamicTexture2);
                DynamicTexture dynamicTexture = dynamicTexture2;
                return dynamicTexture;
            } catch (Throwable throwable6) {
                LOGGER.error("Invalid icon for world {}", (Object)this.summary.getLevelId(), (Object)throwable6);
                this.iconFile = null;
                return null;
            }
        }

        @Override
        public void close() {
            if (this.icon != null) {
                this.icon.close();
            }
        }
    }
}

