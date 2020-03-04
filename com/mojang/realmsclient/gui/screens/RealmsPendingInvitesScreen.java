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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsPendingInvitesScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation ACCEPT_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/accept_icon.png");
    private static final ResourceLocation REJECT_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/reject_icon.png");
    private final Screen lastScreen;
    private String toolTip;
    private boolean loaded;
    private PendingInvitationSelectionList pendingInvitationSelectionList;
    private RealmsLabel titleLabel;
    private int selectedInvite = -1;
    private Button acceptButton;
    private Button rejectButton;

    public RealmsPendingInvitesScreen(Screen screen) {
        this.lastScreen = screen;
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.pendingInvitationSelectionList = new PendingInvitationSelectionList();
        new Thread("Realms-pending-invitations-fetcher"){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                RealmsClient realmsClient = RealmsClient.create();
                try {
                    List<PendingInvite> list = realmsClient.pendingInvites().pendingInvites;
                    List list2 = list.stream().map(pendingInvite -> new Entry((PendingInvite)pendingInvite)).collect(Collectors.toList());
                    RealmsPendingInvitesScreen.this.minecraft.execute(() -> RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.replaceEntries(list2));
                } catch (RealmsServiceException realmsServiceException) {
                    LOGGER.error("Couldn't list invites");
                } finally {
                    RealmsPendingInvitesScreen.this.loaded = true;
                }
            }
        }.start();
        this.addWidget(this.pendingInvitationSelectionList);
        this.acceptButton = this.addButton(new Button(this.width / 2 - 174, this.height - 32, 100, 20, I18n.get("mco.invites.button.accept", new Object[0]), button -> {
            this.accept(this.selectedInvite);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }));
        this.addButton(new Button(this.width / 2 - 50, this.height - 32, 100, 20, I18n.get("gui.done", new Object[0]), button -> this.minecraft.setScreen(new RealmsMainScreen(this.lastScreen))));
        this.rejectButton = this.addButton(new Button(this.width / 2 + 74, this.height - 32, 100, 20, I18n.get("mco.invites.button.reject", new Object[0]), button -> {
            this.reject(this.selectedInvite);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }));
        this.titleLabel = new RealmsLabel(I18n.get("mco.invites.title", new Object[0]), this.width / 2, 12, 0xFFFFFF);
        this.addWidget(this.titleLabel);
        this.narrateLabels();
        this.updateButtonStates();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.minecraft.setScreen(new RealmsMainScreen(this.lastScreen));
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
                        RealmsClient realmsClient = RealmsClient.create();
                        realmsClient.rejectInvitation(((Entry)((Entry)((RealmsPendingInvitesScreen)RealmsPendingInvitesScreen.this).pendingInvitationSelectionList.children().get((int)i))).pendingInvite.invitationId);
                        RealmsPendingInvitesScreen.this.minecraft.execute(() -> RealmsPendingInvitesScreen.this.updateList(i));
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
                        RealmsClient realmsClient = RealmsClient.create();
                        realmsClient.acceptInvitation(((Entry)((Entry)((RealmsPendingInvitesScreen)RealmsPendingInvitesScreen.this).pendingInvitationSelectionList.children().get((int)i))).pendingInvite.invitationId);
                        RealmsPendingInvitesScreen.this.minecraft.execute(() -> RealmsPendingInvitesScreen.this.updateList(i));
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
            this.drawCenteredString(this.font, I18n.get("mco.invites.nopending", new Object[0]), this.width / 2, this.height / 2 - 20, 0xFFFFFF);
        }
        super.render(i, j, f);
    }

    protected void renderMousehoverTooltip(String string, int i, int j) {
        if (string == null) {
            return;
        }
        int k = i + 12;
        int l = j - 12;
        int m = this.font.width(string);
        this.fillGradient(k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
        this.font.drawShadow(string, k, l, 0xFFFFFF);
    }

    private void updateButtonStates() {
        this.acceptButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
        this.rejectButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
    }

    private boolean shouldAcceptAndRejectButtonBeVisible(int i) {
        return i != -1;
    }

    @Environment(value=EnvType.CLIENT)
    class Entry
    extends ObjectSelectionList.Entry<Entry> {
        private final PendingInvite pendingInvite;
        private final List<RowButton> rowButtons;

        Entry(PendingInvite pendingInvite) {
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
            RealmsPendingInvitesScreen.this.font.draw(pendingInvite.worldName, i + 38, j + 1, 0xFFFFFF);
            RealmsPendingInvitesScreen.this.font.draw(pendingInvite.worldOwnerName, i + 38, j + 12, 0x6C6C6C);
            RealmsPendingInvitesScreen.this.font.draw(RealmsUtil.convertToAgePresentationFromInstant(pendingInvite.date), i + 38, j + 24, 0x6C6C6C);
            RowButton.drawButtonsInRow(this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, i, j, k, l);
            RealmsTextureManager.withBoundFace(pendingInvite.worldOwnerUuid, () -> {
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                GuiComponent.blit(i, j, 32, 32, 8.0f, 8.0f, 8, 8, 64, 64);
                GuiComponent.blit(i, j, 32, 32, 40.0f, 8.0f, 8, 8, 64, 64);
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
                RealmsPendingInvitesScreen.this.minecraft.getTextureManager().bind(REJECT_ICON_LOCATION);
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                float f = bl ? 19.0f : 0.0f;
                GuiComponent.blit(i, j, f, 0.0f, 18, 18, 37, 18);
                if (bl) {
                    RealmsPendingInvitesScreen.this.toolTip = I18n.get("mco.invites.button.reject", new Object[0]);
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
                RealmsPendingInvitesScreen.this.minecraft.getTextureManager().bind(ACCEPT_ICON_LOCATION);
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                float f = bl ? 19.0f : 0.0f;
                GuiComponent.blit(i, j, f, 0.0f, 18, 18, 37, 18);
                if (bl) {
                    RealmsPendingInvitesScreen.this.toolTip = I18n.get("mco.invites.button.accept", new Object[0]);
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
    extends RealmsObjectSelectionList<Entry> {
        public PendingInvitationSelectionList() {
            super(RealmsPendingInvitesScreen.this.width, RealmsPendingInvitesScreen.this.height, 32, RealmsPendingInvitesScreen.this.height - 40, 36);
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
            return RealmsPendingInvitesScreen.this.getFocused() == this;
        }

        @Override
        public void renderBackground() {
            RealmsPendingInvitesScreen.this.renderBackground();
        }

        @Override
        public void selectItem(int i) {
            this.setSelectedItem(i);
            if (i != -1) {
                List list = RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children();
                PendingInvite pendingInvite = ((Entry)list.get(i)).pendingInvite;
                String string = I18n.get("narrator.select.list.position", i + 1, list.size());
                String string2 = NarrationHelper.join(Arrays.asList(pendingInvite.worldName, pendingInvite.worldOwnerName, RealmsUtil.convertToAgePresentationFromInstant(pendingInvite.date), string));
                NarrationHelper.now(I18n.get("narrator.select", string2));
            }
            this.selectInviteListItem(i);
        }

        public void selectInviteListItem(int i) {
            RealmsPendingInvitesScreen.this.selectedInvite = i;
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            RealmsPendingInvitesScreen.this.selectedInvite = this.children().indexOf(entry);
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }
    }
}

