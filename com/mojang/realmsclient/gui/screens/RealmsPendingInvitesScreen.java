/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RowButton;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
public class RealmsPendingInvitesScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private String toolTip;
    private boolean loaded;
    private PendingInvitationSelectionList pendingInvitationSelectionList;
    private RealmsLabel titleLabel;
    private int selectedInvite = -1;
    private RealmsButton acceptButton;
    private RealmsButton rejectButton;

    public RealmsPendingInvitesScreen(RealmsScreen realmsScreen) {
        this.lastScreen = realmsScreen;
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.pendingInvitationSelectionList = new PendingInvitationSelectionList();
        new Thread("Realms-pending-invitations-fetcher"){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                RealmsClient realmsClient = RealmsClient.createRealmsClient();
                try {
                    List<PendingInvite> list = realmsClient.pendingInvites().pendingInvites;
                    List list2 = list.stream().map(pendingInvite -> new PendingInvitationSelectionListEntry((PendingInvite)pendingInvite)).collect(Collectors.toList());
                    Realms.execute(() -> RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.replaceEntries(list2));
                } catch (RealmsServiceException realmsServiceException) {
                    LOGGER.error("Couldn't list invites");
                } finally {
                    RealmsPendingInvitesScreen.this.loaded = true;
                }
            }
        }.start();
        this.acceptButton = new RealmsButton(1, this.width() / 2 - 174, this.height() - 32, 100, 20, RealmsPendingInvitesScreen.getLocalizedString("mco.invites.button.accept")){

            @Override
            public void onPress() {
                RealmsPendingInvitesScreen.this.accept(RealmsPendingInvitesScreen.this.selectedInvite);
                RealmsPendingInvitesScreen.this.selectedInvite = -1;
                RealmsPendingInvitesScreen.this.updateButtonStates();
            }
        };
        this.buttonsAdd(this.acceptButton);
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 50, this.height() - 32, 100, 20, RealmsPendingInvitesScreen.getLocalizedString("gui.done")){

            @Override
            public void onPress() {
                Realms.setScreen(new RealmsMainScreen(RealmsPendingInvitesScreen.this.lastScreen));
            }
        });
        this.rejectButton = new RealmsButton(2, this.width() / 2 + 74, this.height() - 32, 100, 20, RealmsPendingInvitesScreen.getLocalizedString("mco.invites.button.reject")){

            @Override
            public void onPress() {
                RealmsPendingInvitesScreen.this.reject(RealmsPendingInvitesScreen.this.selectedInvite);
                RealmsPendingInvitesScreen.this.selectedInvite = -1;
                RealmsPendingInvitesScreen.this.updateButtonStates();
            }
        };
        this.buttonsAdd(this.rejectButton);
        this.titleLabel = new RealmsLabel(RealmsPendingInvitesScreen.getLocalizedString("mco.invites.title"), this.width() / 2, 12, 0xFFFFFF);
        this.addWidget(this.titleLabel);
        this.addWidget(this.pendingInvitationSelectionList);
        this.narrateLabels();
        this.updateButtonStates();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            Realms.setScreen(new RealmsMainScreen(this.lastScreen));
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    private void updateList(int i) {
        this.pendingInvitationSelectionList.removeAtIndex(i);
    }

    private void reject(final int i) {
        if (i < this.pendingInvitationSelectionList.getItemCount()) {
            new Thread("Realms-reject-invitation"){

                @Override
                public void run() {
                    try {
                        RealmsClient realmsClient = RealmsClient.createRealmsClient();
                        realmsClient.rejectInvitation(((PendingInvitationSelectionListEntry)((RealmsPendingInvitesScreen)RealmsPendingInvitesScreen.this).pendingInvitationSelectionList.children().get((int)i)).pendingInvite.invitationId);
                        Realms.execute(() -> RealmsPendingInvitesScreen.this.updateList(i));
                    } catch (RealmsServiceException realmsServiceException) {
                        LOGGER.error("Couldn't reject invite");
                    }
                }
            }.start();
        }
    }

    private void accept(final int i) {
        if (i < this.pendingInvitationSelectionList.getItemCount()) {
            new Thread("Realms-accept-invitation"){

                @Override
                public void run() {
                    try {
                        RealmsClient realmsClient = RealmsClient.createRealmsClient();
                        realmsClient.acceptInvitation(((PendingInvitationSelectionListEntry)((RealmsPendingInvitesScreen)RealmsPendingInvitesScreen.this).pendingInvitationSelectionList.children().get((int)i)).pendingInvite.invitationId);
                        Realms.execute(() -> RealmsPendingInvitesScreen.this.updateList(i));
                    } catch (RealmsServiceException realmsServiceException) {
                        LOGGER.error("Couldn't accept invite");
                    }
                }
            }.start();
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.toolTip = null;
        this.renderBackground();
        this.pendingInvitationSelectionList.render(i, j, f);
        this.titleLabel.render(this);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, i, j);
        }
        if (this.pendingInvitationSelectionList.getItemCount() == 0 && this.loaded) {
            this.drawCenteredString(RealmsPendingInvitesScreen.getLocalizedString("mco.invites.nopending"), this.width() / 2, this.height() / 2 - 20, 0xFFFFFF);
        }
        super.render(i, j, f);
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

    private void updateButtonStates() {
        this.acceptButton.setVisible(this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite));
        this.rejectButton.setVisible(this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite));
    }

    private boolean shouldAcceptAndRejectButtonBeVisible(int i) {
        return i != -1;
    }

    public static String getAge(PendingInvite pendingInvite) {
        return RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - pendingInvite.date.getTime());
    }

    @Environment(value=EnvType.CLIENT)
    class PendingInvitationSelectionListEntry
    extends RealmListEntry {
        final PendingInvite pendingInvite;
        private final List<RowButton> rowButtons;

        PendingInvitationSelectionListEntry(PendingInvite pendingInvite) {
            this.pendingInvite = pendingInvite;
            this.rowButtons = Arrays.asList(new AcceptRowButton(), new RejectRowButton());
        }

        @Override
        public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.renderPendingInvitationItem(this.pendingInvite, k, j, n, o);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            RowButton.rowButtonMouseClicked(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, this, this.rowButtons, i, d, e);
            return true;
        }

        private void renderPendingInvitationItem(PendingInvite pendingInvite, int i, int j, int k, int l) {
            RealmsPendingInvitesScreen.this.drawString(pendingInvite.worldName, i + 38, j + 1, 0xFFFFFF);
            RealmsPendingInvitesScreen.this.drawString(pendingInvite.worldOwnerName, i + 38, j + 12, 0x6C6C6C);
            RealmsPendingInvitesScreen.this.drawString(RealmsPendingInvitesScreen.getAge(pendingInvite), i + 38, j + 24, 0x6C6C6C);
            RowButton.drawButtonsInRow(this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, i, j, k, l);
            RealmsTextureManager.withBoundFace(pendingInvite.worldOwnerUuid, () -> {
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                RealmsScreen.blit(i, j, 8.0f, 8.0f, 8, 8, 32, 32, 64, 64);
                RealmsScreen.blit(i, j, 40.0f, 8.0f, 8, 8, 32, 32, 64, 64);
            });
        }

        @Environment(value=EnvType.CLIENT)
        class RejectRowButton
        extends RowButton {
            RejectRowButton() {
                super(15, 15, 235, 5);
            }

            @Override
            protected void draw(int i, int j, boolean bl) {
                RealmsScreen.bind("realms:textures/gui/realms/reject_icon.png");
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                RenderSystem.pushMatrix();
                RealmsScreen.blit(i, j, bl ? 19.0f : 0.0f, 0.0f, 18, 18, 37, 18);
                RenderSystem.popMatrix();
                if (bl) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.invites.button.reject");
                }
            }

            @Override
            public void onClick(int i) {
                RealmsPendingInvitesScreen.this.reject(i);
            }
        }

        @Environment(value=EnvType.CLIENT)
        class AcceptRowButton
        extends RowButton {
            AcceptRowButton() {
                super(15, 15, 215, 5);
            }

            @Override
            protected void draw(int i, int j, boolean bl) {
                RealmsScreen.bind("realms:textures/gui/realms/accept_icon.png");
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                RenderSystem.pushMatrix();
                RealmsScreen.blit(i, j, bl ? 19.0f : 0.0f, 0.0f, 18, 18, 37, 18);
                RenderSystem.popMatrix();
                if (bl) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.invites.button.accept");
                }
            }

            @Override
            public void onClick(int i) {
                RealmsPendingInvitesScreen.this.accept(i);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class PendingInvitationSelectionList
    extends RealmsObjectSelectionList<PendingInvitationSelectionListEntry> {
        public PendingInvitationSelectionList() {
            super(RealmsPendingInvitesScreen.this.width(), RealmsPendingInvitesScreen.this.height(), 32, RealmsPendingInvitesScreen.this.height() - 40, 36);
        }

        public void removeAtIndex(int i) {
            this.remove(i);
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        public boolean isFocused() {
            return RealmsPendingInvitesScreen.this.isFocused(this);
        }

        @Override
        public void renderBackground() {
            RealmsPendingInvitesScreen.this.renderBackground();
        }

        @Override
        public void selectItem(int i) {
            this.setSelected(i);
            if (i != -1) {
                List list = RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children();
                PendingInvite pendingInvite = ((PendingInvitationSelectionListEntry)list.get((int)i)).pendingInvite;
                String string = RealmsScreen.getLocalizedString("narrator.select.list.position", i + 1, list.size());
                String string2 = Realms.joinNarrations(Arrays.asList(pendingInvite.worldName, pendingInvite.worldOwnerName, RealmsPendingInvitesScreen.getAge(pendingInvite), string));
                Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", string2));
            }
            this.selectInviteListItem(i);
        }

        public void selectInviteListItem(int i) {
            RealmsPendingInvitesScreen.this.selectedInvite = i;
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }
    }
}

