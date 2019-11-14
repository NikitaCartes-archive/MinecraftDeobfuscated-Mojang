/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.screens.RealmsBackupInfoScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsUtil;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsBackupScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static int lastScrollPosition = -1;
    private final RealmsConfigureWorldScreen lastScreen;
    private List<Backup> backups = Collections.emptyList();
    private String toolTip;
    private BackupObjectSelectionList backupObjectSelectionList;
    private int selectedBackup = -1;
    private final int slotId;
    private RealmsButton downloadButton;
    private RealmsButton restoreButton;
    private RealmsButton changesButton;
    private Boolean noBackups = false;
    private final RealmsServer serverData;
    private RealmsLabel titleLabel;

    public RealmsBackupScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsServer realmsServer, int i) {
        this.lastScreen = realmsConfigureWorldScreen;
        this.serverData = realmsServer;
        this.slotId = i;
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.backupObjectSelectionList = new BackupObjectSelectionList();
        if (lastScrollPosition != -1) {
            this.backupObjectSelectionList.scroll(lastScrollPosition);
        }
        new Thread("Realms-fetch-backups"){

            @Override
            public void run() {
                RealmsClient realmsClient = RealmsClient.createRealmsClient();
                try {
                    List<Backup> list = realmsClient.backupsFor((long)((RealmsBackupScreen)RealmsBackupScreen.this).serverData.id).backups;
                    Realms.execute(() -> {
                        RealmsBackupScreen.this.backups = list;
                        RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.isEmpty();
                        RealmsBackupScreen.this.backupObjectSelectionList.clear();
                        for (Backup backup : RealmsBackupScreen.this.backups) {
                            RealmsBackupScreen.this.backupObjectSelectionList.addEntry(backup);
                        }
                        RealmsBackupScreen.this.generateChangeList();
                    });
                } catch (RealmsServiceException realmsServiceException) {
                    LOGGER.error("Couldn't request backups", (Throwable)realmsServiceException);
                }
            }
        }.start();
        this.postInit();
    }

    private void generateChangeList() {
        if (this.backups.size() <= 1) {
            return;
        }
        for (int i = 0; i < this.backups.size() - 1; ++i) {
            Backup backup = this.backups.get(i);
            Backup backup2 = this.backups.get(i + 1);
            if (backup.metadata.isEmpty() || backup2.metadata.isEmpty()) continue;
            for (String string : backup.metadata.keySet()) {
                if (!string.contains("Uploaded") && backup2.metadata.containsKey(string)) {
                    if (backup.metadata.get(string).equals(backup2.metadata.get(string))) continue;
                    this.addToChangeList(backup, string);
                    continue;
                }
                this.addToChangeList(backup, string);
            }
        }
    }

    private void addToChangeList(Backup backup, String string) {
        if (string.contains("Uploaded")) {
            String string2 = DateFormat.getDateTimeInstance(3, 3).format(backup.lastModifiedDate);
            backup.changeList.put(string, string2);
            backup.setUploadedVersion(true);
        } else {
            backup.changeList.put(string, backup.metadata.get(string));
        }
    }

    private void postInit() {
        this.downloadButton = new RealmsButton(2, this.width() - 135, RealmsConstants.row(1), 120, 20, RealmsBackupScreen.getLocalizedString("mco.backup.button.download")){

            @Override
            public void onPress() {
                RealmsBackupScreen.this.downloadClicked();
            }
        };
        this.buttonsAdd(this.downloadButton);
        this.restoreButton = new RealmsButton(3, this.width() - 135, RealmsConstants.row(3), 120, 20, RealmsBackupScreen.getLocalizedString("mco.backup.button.restore")){

            @Override
            public void onPress() {
                RealmsBackupScreen.this.restoreClicked(RealmsBackupScreen.this.selectedBackup);
            }
        };
        this.buttonsAdd(this.restoreButton);
        this.changesButton = new RealmsButton(4, this.width() - 135, RealmsConstants.row(5), 120, 20, RealmsBackupScreen.getLocalizedString("mco.backup.changes.tooltip")){

            @Override
            public void onPress() {
                Realms.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, (Backup)RealmsBackupScreen.this.backups.get(RealmsBackupScreen.this.selectedBackup)));
                RealmsBackupScreen.this.selectedBackup = -1;
            }
        };
        this.buttonsAdd(this.changesButton);
        this.buttonsAdd(new RealmsButton(0, this.width() - 100, this.height() - 35, 85, 20, RealmsBackupScreen.getLocalizedString("gui.back")){

            @Override
            public void onPress() {
                Realms.setScreen(RealmsBackupScreen.this.lastScreen);
            }
        });
        this.addWidget(this.backupObjectSelectionList);
        this.titleLabel = new RealmsLabel(RealmsBackupScreen.getLocalizedString("mco.configure.world.backup"), this.width() / 2, 12, 0xFFFFFF);
        this.addWidget(this.titleLabel);
        this.focusOn(this.backupObjectSelectionList);
        this.updateButtonStates();
        this.narrateLabels();
    }

    private void updateButtonStates() {
        this.restoreButton.setVisible(this.shouldRestoreButtonBeVisible());
        this.changesButton.setVisible(this.shouldChangesButtonBeVisible());
    }

    private boolean shouldChangesButtonBeVisible() {
        if (this.selectedBackup == -1) {
            return false;
        }
        return !this.backups.get((int)this.selectedBackup).changeList.isEmpty();
    }

    private boolean shouldRestoreButtonBeVisible() {
        if (this.selectedBackup == -1) {
            return false;
        }
        return !this.serverData.expired;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            Realms.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    private void restoreClicked(int i) {
        if (i >= 0 && i < this.backups.size() && !this.serverData.expired) {
            this.selectedBackup = i;
            Date date = this.backups.get((int)i).lastModifiedDate;
            String string = DateFormat.getDateTimeInstance(3, 3).format(date);
            String string2 = RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - date.getTime());
            String string3 = RealmsBackupScreen.getLocalizedString("mco.configure.world.restore.question.line1", string, string2);
            String string4 = RealmsBackupScreen.getLocalizedString("mco.configure.world.restore.question.line2");
            Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Warning, string3, string4, true, 1));
        }
    }

    private void downloadClicked() {
        String string = RealmsBackupScreen.getLocalizedString("mco.configure.world.restore.download.question.line1");
        String string2 = RealmsBackupScreen.getLocalizedString("mco.configure.world.restore.download.question.line2");
        Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, string, string2, true, 2));
    }

    private void downloadWorldData() {
        RealmsTasks.DownloadTask downloadTask = new RealmsTasks.DownloadTask(this.serverData.id, this.slotId, this.serverData.name + " (" + this.serverData.slots.get(this.serverData.activeSlot).getSlotName(this.serverData.activeSlot) + ")", this);
        RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), downloadTask);
        realmsLongRunningMcoTaskScreen.start();
        Realms.setScreen(realmsLongRunningMcoTaskScreen);
    }

    @Override
    public void confirmResult(boolean bl, int i) {
        if (bl && i == 1) {
            this.restore();
        } else if (i == 1) {
            this.selectedBackup = -1;
            Realms.setScreen(this);
        } else if (bl && i == 2) {
            this.downloadWorldData();
        } else {
            Realms.setScreen(this);
        }
    }

    private void restore() {
        Backup backup = this.backups.get(this.selectedBackup);
        this.selectedBackup = -1;
        RealmsTasks.RestoreTask restoreTask = new RealmsTasks.RestoreTask(backup, this.serverData.id, this.lastScreen);
        RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), restoreTask);
        realmsLongRunningMcoTaskScreen.start();
        Realms.setScreen(realmsLongRunningMcoTaskScreen);
    }

    @Override
    public void render(int i, int j, float f) {
        this.toolTip = null;
        this.renderBackground();
        this.backupObjectSelectionList.render(i, j, f);
        this.titleLabel.render(this);
        this.drawString(RealmsBackupScreen.getLocalizedString("mco.configure.world.backup"), (this.width() - 150) / 2 - 90, 20, 0xA0A0A0);
        if (this.noBackups.booleanValue()) {
            this.drawString(RealmsBackupScreen.getLocalizedString("mco.backup.nobackups"), 20, this.height() / 2 - 10, 0xFFFFFF);
        }
        this.downloadButton.active(this.noBackups == false);
        super.render(i, j, f);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, i, j);
        }
    }

    protected void renderMousehoverTooltip(String string, int i, int j) {
        if (string == null) {
            return;
        }
        int k = i + 12;
        int l = j - 12;
        int m = this.fontWidth(string);
        this.fillGradient(k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
        this.fontDrawShadow(string, k, l, 0xFFFFFF);
    }

    @Environment(value=EnvType.CLIENT)
    class BackupObjectSelectionListEntry
    extends RealmListEntry {
        final Backup mBackup;

        public BackupObjectSelectionListEntry(Backup backup) {
            this.mBackup = backup;
        }

        @Override
        public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.renderBackupItem(this.mBackup, k - 40, j, n, o);
        }

        private void renderBackupItem(Backup backup, int i, int j, int k, int l) {
            int m = backup.isUploadedVersion() ? -8388737 : 0xFFFFFF;
            RealmsBackupScreen.this.drawString("Backup (" + RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - backup.lastModifiedDate.getTime()) + ")", i + 40, j + 1, m);
            RealmsBackupScreen.this.drawString(this.getMediumDatePresentation(backup.lastModifiedDate), i + 40, j + 12, 0x808080);
            int n = RealmsBackupScreen.this.width() - 175;
            int o = -3;
            int p = n - 10;
            boolean q = false;
            if (!((RealmsBackupScreen)RealmsBackupScreen.this).serverData.expired) {
                this.drawRestore(n, j + -3, k, l);
            }
            if (!backup.changeList.isEmpty()) {
                this.drawInfo(p, j + 0, k, l);
            }
        }

        private String getMediumDatePresentation(Date date) {
            return DateFormat.getDateTimeInstance(3, 3).format(date);
        }

        private void drawRestore(int i, int j, int k, int l) {
            boolean bl = k >= i && k <= i + 12 && l >= j && l <= j + 14 && l < RealmsBackupScreen.this.height() - 15 && l > 32;
            RealmsScreen.bind("realms:textures/gui/realms/restore_icon.png");
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5f, 0.5f, 0.5f);
            RealmsScreen.blit(i * 2, j * 2, 0.0f, bl ? 28.0f : 0.0f, 23, 28, 23, 56);
            RenderSystem.popMatrix();
            if (bl) {
                RealmsBackupScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.backup.button.restore");
            }
        }

        private void drawInfo(int i, int j, int k, int l) {
            boolean bl = k >= i && k <= i + 8 && l >= j && l <= j + 8 && l < RealmsBackupScreen.this.height() - 15 && l > 32;
            RealmsScreen.bind("realms:textures/gui/realms/plus_icon.png");
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5f, 0.5f, 0.5f);
            RealmsScreen.blit(i * 2, j * 2, 0.0f, bl ? 15.0f : 0.0f, 15, 15, 15, 30);
            RenderSystem.popMatrix();
            if (bl) {
                RealmsBackupScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.backup.changes.tooltip");
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BackupObjectSelectionList
    extends RealmsObjectSelectionList {
        public BackupObjectSelectionList() {
            super(RealmsBackupScreen.this.width() - 150, RealmsBackupScreen.this.height(), 32, RealmsBackupScreen.this.height() - 15, 36);
        }

        public void addEntry(Backup backup) {
            this.addEntry(new BackupObjectSelectionListEntry(backup));
        }

        @Override
        public int getRowWidth() {
            return (int)((double)this.width() * 0.93);
        }

        @Override
        public boolean isFocused() {
            return RealmsBackupScreen.this.isFocused(this);
        }

        @Override
        public int getItemCount() {
            return RealmsBackupScreen.this.backups.size();
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public void renderBackground() {
            RealmsBackupScreen.this.renderBackground();
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (i != 0) {
                return false;
            }
            if (d < (double)this.getScrollbarPosition() && e >= (double)this.y0() && e <= (double)this.y1()) {
                int j = this.width() / 2 - 92;
                int k = this.width();
                int l = (int)Math.floor(e - (double)this.y0()) - this.headerHeight() + this.getScroll();
                int m = l / this.itemHeight();
                if (d >= (double)j && d <= (double)k && m >= 0 && l >= 0 && m < this.getItemCount()) {
                    this.selectItem(m);
                    this.itemClicked(l, m, d, e, this.width());
                }
                return true;
            }
            return false;
        }

        @Override
        public int getScrollbarPosition() {
            return this.width() - 5;
        }

        @Override
        public void itemClicked(int i, int j, double d, double e, int k) {
            int l = this.width() - 35;
            int m = j * this.itemHeight() + 36 - this.getScroll();
            int n = l + 10;
            int o = m - 3;
            if (d >= (double)l && d <= (double)(l + 9) && e >= (double)m && e <= (double)(m + 9)) {
                if (!((Backup)((RealmsBackupScreen)RealmsBackupScreen.this).backups.get((int)j)).changeList.isEmpty()) {
                    RealmsBackupScreen.this.selectedBackup = -1;
                    lastScrollPosition = this.getScroll();
                    Realms.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, (Backup)RealmsBackupScreen.this.backups.get(j)));
                }
            } else if (d >= (double)n && d < (double)(n + 13) && e >= (double)o && e < (double)(o + 15)) {
                lastScrollPosition = this.getScroll();
                RealmsBackupScreen.this.restoreClicked(j);
            }
        }

        @Override
        public void selectItem(int i) {
            this.setSelected(i);
            if (i != -1) {
                Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", ((Backup)((RealmsBackupScreen)RealmsBackupScreen.this).backups.get((int)i)).lastModifiedDate.toString()));
            }
            this.selectInviteListItem(i);
        }

        public void selectInviteListItem(int i) {
            RealmsBackupScreen.this.selectedBackup = i;
            RealmsBackupScreen.this.updateButtonStates();
        }
    }
}

