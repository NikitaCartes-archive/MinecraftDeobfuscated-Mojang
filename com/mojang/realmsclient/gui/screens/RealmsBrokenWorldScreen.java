/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsBrokenWorldScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private final RealmsMainScreen mainScreen;
    private RealmsServer serverData;
    private final long serverId;
    private String title = RealmsBrokenWorldScreen.getLocalizedString("mco.brokenworld.title");
    private final String message = RealmsBrokenWorldScreen.getLocalizedString("mco.brokenworld.message.line1") + "\\n" + RealmsBrokenWorldScreen.getLocalizedString("mco.brokenworld.message.line2");
    private int left_x;
    private int right_x;
    private final int default_button_width = 80;
    private final int default_button_offset = 5;
    private static final List<Integer> playButtonIds = Arrays.asList(1, 2, 3);
    private static final List<Integer> resetButtonIds = Arrays.asList(4, 5, 6);
    private static final List<Integer> downloadButtonIds = Arrays.asList(7, 8, 9);
    private static final List<Integer> downloadConfirmationIds = Arrays.asList(10, 11, 12);
    private final List<Integer> slotsThatHasBeenDownloaded = new ArrayList<Integer>();
    private int animTick;

    public RealmsBrokenWorldScreen(RealmsScreen realmsScreen, RealmsMainScreen realmsMainScreen, long l) {
        this.lastScreen = realmsScreen;
        this.mainScreen = realmsMainScreen;
        this.serverId = l;
    }

    public void setTitle(String string) {
        this.title = string;
    }

    @Override
    public void init() {
        this.left_x = this.width() / 2 - 150;
        this.right_x = this.width() / 2 + 190;
        this.buttonsAdd(new RealmsButton(0, this.right_x - 80 + 8, RealmsConstants.row(13) - 5, 70, 20, RealmsBrokenWorldScreen.getLocalizedString("gui.back")){

            @Override
            public void onPress() {
                RealmsBrokenWorldScreen.this.backButtonClicked();
            }
        });
        if (this.serverData == null) {
            this.fetchServerData(this.serverId);
        } else {
            this.addButtons();
        }
        this.setKeyboardHandlerSendRepeatsToGui(true);
    }

    public void addButtons() {
        for (Map.Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
            RealmsWorldOptions realmsWorldOptions = entry.getValue();
            boolean bl = entry.getKey() != this.serverData.activeSlot || this.serverData.worldType.equals((Object)RealmsServer.WorldType.MINIGAME);
            RealmsButton realmsButton = bl ? new PlayButton((int)playButtonIds.get(entry.getKey() - 1), this.getFramePositionX(entry.getKey()), RealmsBrokenWorldScreen.getLocalizedString("mco.brokenworld.play")) : new DownloadButton((int)downloadButtonIds.get(entry.getKey() - 1), this.getFramePositionX(entry.getKey()), RealmsBrokenWorldScreen.getLocalizedString("mco.brokenworld.download"));
            if (this.slotsThatHasBeenDownloaded.contains(entry.getKey())) {
                realmsButton.active(false);
                realmsButton.setMessage(RealmsBrokenWorldScreen.getLocalizedString("mco.brokenworld.downloaded"));
            }
            this.buttonsAdd(realmsButton);
            this.buttonsAdd(new RealmsButton(resetButtonIds.get(entry.getKey() - 1), this.getFramePositionX(entry.getKey()), RealmsConstants.row(10), 80, 20, RealmsBrokenWorldScreen.getLocalizedString("mco.brokenworld.reset")){

                @Override
                public void onPress() {
                    int i = resetButtonIds.indexOf(this.id()) + 1;
                    RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(RealmsBrokenWorldScreen.this, RealmsBrokenWorldScreen.this.serverData, RealmsBrokenWorldScreen.this);
                    if (i != ((RealmsBrokenWorldScreen)RealmsBrokenWorldScreen.this).serverData.activeSlot || ((RealmsBrokenWorldScreen)RealmsBrokenWorldScreen.this).serverData.worldType.equals((Object)RealmsServer.WorldType.MINIGAME)) {
                        realmsResetWorldScreen.setSlot(i);
                    }
                    realmsResetWorldScreen.setConfirmationId(14);
                    Realms.setScreen(realmsResetWorldScreen);
                }
            });
        }
    }

    @Override
    public void tick() {
        ++this.animTick;
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        super.render(i, j, f);
        this.drawCenteredString(this.title, this.width() / 2, 17, 0xFFFFFF);
        String[] strings = this.message.split("\\\\n");
        for (int k = 0; k < strings.length; ++k) {
            this.drawCenteredString(strings[k], this.width() / 2, RealmsConstants.row(-1) + 3 + k * 12, 0xA0A0A0);
        }
        if (this.serverData == null) {
            return;
        }
        for (Map.Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
            if (entry.getValue().templateImage != null && entry.getValue().templateId != -1L) {
                this.drawSlotFrame(this.getFramePositionX(entry.getKey()), RealmsConstants.row(1) + 5, i, j, this.serverData.activeSlot == entry.getKey() && !this.isMinigame(), entry.getValue().getSlotName(entry.getKey()), entry.getKey(), entry.getValue().templateId, entry.getValue().templateImage, entry.getValue().empty);
                continue;
            }
            this.drawSlotFrame(this.getFramePositionX(entry.getKey()), RealmsConstants.row(1) + 5, i, j, this.serverData.activeSlot == entry.getKey() && !this.isMinigame(), entry.getValue().getSlotName(entry.getKey()), entry.getKey(), -1L, null, entry.getValue().empty);
        }
    }

    private int getFramePositionX(int i) {
        return this.left_x + (i - 1) * 110;
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.backButtonClicked();
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    private void backButtonClicked() {
        Realms.setScreen(this.lastScreen);
    }

    private void fetchServerData(final long l) {
        new Thread(){

            @Override
            public void run() {
                RealmsClient realmsClient = RealmsClient.createRealmsClient();
                try {
                    RealmsBrokenWorldScreen.this.serverData = realmsClient.getOwnWorld(l);
                    RealmsBrokenWorldScreen.this.addButtons();
                } catch (RealmsServiceException realmsServiceException) {
                    LOGGER.error("Couldn't get own world");
                    Realms.setScreen(new RealmsGenericErrorScreen(realmsServiceException.getMessage(), RealmsBrokenWorldScreen.this.lastScreen));
                } catch (IOException iOException) {
                    LOGGER.error("Couldn't parse response getting own world");
                }
            }
        }.start();
    }

    @Override
    public void confirmResult(boolean bl, int i) {
        if (!bl) {
            Realms.setScreen(this);
            return;
        }
        if (i == 13 || i == 14) {
            new Thread(){

                @Override
                public void run() {
                    RealmsClient realmsClient = RealmsClient.createRealmsClient();
                    if (((RealmsBrokenWorldScreen)RealmsBrokenWorldScreen.this).serverData.state.equals((Object)RealmsServer.State.CLOSED)) {
                        RealmsTasks.OpenServerTask openServerTask = new RealmsTasks.OpenServerTask(RealmsBrokenWorldScreen.this.serverData, RealmsBrokenWorldScreen.this, RealmsBrokenWorldScreen.this.lastScreen, true);
                        RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(RealmsBrokenWorldScreen.this, openServerTask);
                        realmsLongRunningMcoTaskScreen.start();
                        Realms.setScreen(realmsLongRunningMcoTaskScreen);
                    } else {
                        try {
                            RealmsBrokenWorldScreen.this.mainScreen.newScreen().play(realmsClient.getOwnWorld(RealmsBrokenWorldScreen.this.serverId), RealmsBrokenWorldScreen.this);
                        } catch (RealmsServiceException realmsServiceException) {
                            LOGGER.error("Couldn't get own world");
                            Realms.setScreen(RealmsBrokenWorldScreen.this.lastScreen);
                        } catch (IOException iOException) {
                            LOGGER.error("Couldn't parse response getting own world");
                            Realms.setScreen(RealmsBrokenWorldScreen.this.lastScreen);
                        }
                    }
                }
            }.start();
        } else if (downloadButtonIds.contains(i)) {
            this.downloadWorld(downloadButtonIds.indexOf(i) + 1);
        } else if (downloadConfirmationIds.contains(i)) {
            this.slotsThatHasBeenDownloaded.add(downloadConfirmationIds.indexOf(i) + 1);
            this.childrenClear();
            this.addButtons();
        }
    }

    private void downloadWorld(int i) {
        RealmsClient realmsClient = RealmsClient.createRealmsClient();
        try {
            WorldDownload worldDownload = realmsClient.download(this.serverData.id, i);
            RealmsDownloadLatestWorldScreen realmsDownloadLatestWorldScreen = new RealmsDownloadLatestWorldScreen(this, worldDownload, this.serverData.name + " (" + this.serverData.slots.get(i).getSlotName(i) + ")");
            realmsDownloadLatestWorldScreen.setConfirmationId(downloadConfirmationIds.get(i - 1));
            Realms.setScreen(realmsDownloadLatestWorldScreen);
        } catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't download world data");
            Realms.setScreen(new RealmsGenericErrorScreen(realmsServiceException, (RealmsScreen)this));
        }
    }

    private boolean isMinigame() {
        return this.serverData != null && this.serverData.worldType.equals((Object)RealmsServer.WorldType.MINIGAME);
    }

    private void drawSlotFrame(int i, int j, int k, int l, boolean bl, String string, int m, long n, String string2, boolean bl2) {
        if (bl2) {
            RealmsBrokenWorldScreen.bind("realms:textures/gui/realms/empty_frame.png");
        } else if (string2 != null && n != -1L) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
        } else if (m == 1) {
            RealmsBrokenWorldScreen.bind("textures/gui/title/background/panorama_0.png");
        } else if (m == 2) {
            RealmsBrokenWorldScreen.bind("textures/gui/title/background/panorama_2.png");
        } else if (m == 3) {
            RealmsBrokenWorldScreen.bind("textures/gui/title/background/panorama_3.png");
        } else {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
        }
        if (!bl) {
            GlStateManager.color4f(0.56f, 0.56f, 0.56f, 1.0f);
        } else if (bl) {
            float f = 0.9f + 0.1f * RealmsMth.cos((float)this.animTick * 0.2f);
            GlStateManager.color4f(f, f, f, 1.0f);
        }
        RealmsScreen.blit(i + 3, j + 3, 0.0f, 0.0f, 74, 74, 74, 74);
        RealmsBrokenWorldScreen.bind("realms:textures/gui/realms/slot_frame.png");
        if (bl) {
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            GlStateManager.color4f(0.56f, 0.56f, 0.56f, 1.0f);
        }
        RealmsScreen.blit(i, j, 0.0f, 0.0f, 80, 80, 80, 80);
        this.drawCenteredString(string, i + 40, j + 66, 0xFFFFFF);
    }

    private void switchSlot(int i) {
        RealmsTasks.SwitchSlotTask switchSlotTask = new RealmsTasks.SwitchSlotTask(this.serverData.id, i, this, 13);
        RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, switchSlotTask);
        realmsLongRunningMcoTaskScreen.start();
        Realms.setScreen(realmsLongRunningMcoTaskScreen);
    }

    @Environment(value=EnvType.CLIENT)
    class DownloadButton
    extends RealmsButton {
        public DownloadButton(int i, int j, String string) {
            super(i, j, RealmsConstants.row(8), 80, 20, string);
        }

        @Override
        public void onPress() {
            String string = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line1");
            String string2 = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line2");
            Realms.setScreen(new RealmsLongConfirmationScreen(RealmsBrokenWorldScreen.this, RealmsLongConfirmationScreen.Type.Info, string, string2, true, this.id()));
        }
    }

    @Environment(value=EnvType.CLIENT)
    class PlayButton
    extends RealmsButton {
        public PlayButton(int i, int j, String string) {
            super(i, j, RealmsConstants.row(8), 80, 20, string);
        }

        @Override
        public void onPress() {
            int i = playButtonIds.indexOf(this.id()) + 1;
            if (((RealmsBrokenWorldScreen)RealmsBrokenWorldScreen.this).serverData.slots.get((Object)Integer.valueOf((int)i)).empty) {
                RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(RealmsBrokenWorldScreen.this, RealmsBrokenWorldScreen.this.serverData, RealmsBrokenWorldScreen.this, RealmsScreen.getLocalizedString("mco.configure.world.switch.slot"), RealmsScreen.getLocalizedString("mco.configure.world.switch.slot.subtitle"), 0xA0A0A0, RealmsScreen.getLocalizedString("gui.cancel"));
                realmsResetWorldScreen.setSlot(i);
                realmsResetWorldScreen.setResetTitle(RealmsScreen.getLocalizedString("mco.create.world.reset.title"));
                realmsResetWorldScreen.setConfirmationId(14);
                Realms.setScreen(realmsResetWorldScreen);
            } else {
                RealmsBrokenWorldScreen.this.switchSlot(i);
            }
        }
    }
}

