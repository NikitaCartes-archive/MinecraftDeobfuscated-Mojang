/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfirmScreen;
import com.mojang.realmsclient.gui.screens.RealmsInviteScreen;
import com.mojang.realmsclient.util.RealmsTextureManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsPlayerScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private String toolTip;
    private final RealmsConfigureWorldScreen lastScreen;
    private final RealmsServer serverData;
    private InvitedObjectSelectionList invitedObjectSelectionList;
    private int column1_x;
    private int column_width;
    private int column2_x;
    private RealmsButton removeButton;
    private RealmsButton opdeopButton;
    private int selectedInvitedIndex = -1;
    private String selectedInvited;
    private int player = -1;
    private boolean stateChanged;
    private RealmsLabel titleLabel;

    public RealmsPlayerScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsServer realmsServer) {
        this.lastScreen = realmsConfigureWorldScreen;
        this.serverData = realmsServer;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void init() {
        this.column1_x = this.width() / 2 - 160;
        this.column_width = 150;
        this.column2_x = this.width() / 2 + 12;
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(new RealmsButton(1, this.column2_x, RealmsConstants.row(1), this.column_width + 10, 20, RealmsPlayerScreen.getLocalizedString("mco.configure.world.buttons.invite")){

            @Override
            public void onPress() {
                Realms.setScreen(new RealmsInviteScreen(RealmsPlayerScreen.this.lastScreen, RealmsPlayerScreen.this, RealmsPlayerScreen.this.serverData));
            }
        });
        this.removeButton = new RealmsButton(4, this.column2_x, RealmsConstants.row(7), this.column_width + 10, 20, RealmsPlayerScreen.getLocalizedString("mco.configure.world.invites.remove.tooltip")){

            @Override
            public void onPress() {
                RealmsPlayerScreen.this.uninvite(RealmsPlayerScreen.this.player);
            }
        };
        this.buttonsAdd(this.removeButton);
        this.opdeopButton = new RealmsButton(5, this.column2_x, RealmsConstants.row(9), this.column_width + 10, 20, RealmsPlayerScreen.getLocalizedString("mco.configure.world.invites.ops.tooltip")){

            @Override
            public void onPress() {
                if (((RealmsPlayerScreen)RealmsPlayerScreen.this).serverData.players.get(RealmsPlayerScreen.this.player).isOperator()) {
                    RealmsPlayerScreen.this.deop(RealmsPlayerScreen.this.player);
                } else {
                    RealmsPlayerScreen.this.op(RealmsPlayerScreen.this.player);
                }
            }
        };
        this.buttonsAdd(this.opdeopButton);
        this.buttonsAdd(new RealmsButton(0, this.column2_x + this.column_width / 2 + 2, RealmsConstants.row(12), this.column_width / 2 + 10 - 2, 20, RealmsPlayerScreen.getLocalizedString("gui.back")){

            @Override
            public void onPress() {
                RealmsPlayerScreen.this.backButtonClicked();
            }
        });
        this.invitedObjectSelectionList = new InvitedObjectSelectionList();
        this.invitedObjectSelectionList.setLeftPos(this.column1_x);
        this.addWidget(this.invitedObjectSelectionList);
        for (PlayerInfo playerInfo : this.serverData.players) {
            this.invitedObjectSelectionList.addEntry(playerInfo);
        }
        this.titleLabel = new RealmsLabel(RealmsPlayerScreen.getLocalizedString("mco.configure.world.players.title"), this.width() / 2, 17, 0xFFFFFF);
        this.addWidget(this.titleLabel);
        this.narrateLabels();
        this.updateButtonStates();
    }

    private void updateButtonStates() {
        this.removeButton.setVisible(this.shouldRemoveAndOpdeopButtonBeVisible(this.player));
        this.opdeopButton.setVisible(this.shouldRemoveAndOpdeopButtonBeVisible(this.player));
    }

    private boolean shouldRemoveAndOpdeopButtonBeVisible(int i) {
        return i != -1;
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
        if (this.stateChanged) {
            Realms.setScreen(this.lastScreen.getNewScreen());
        } else {
            Realms.setScreen(this.lastScreen);
        }
    }

    private void op(int i) {
        this.updateButtonStates();
        RealmsClient realmsClient = RealmsClient.createRealmsClient();
        String string = this.serverData.players.get(i).getUuid();
        try {
            this.updateOps(realmsClient.op(this.serverData.id, string));
        } catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't op the user");
        }
    }

    private void deop(int i) {
        this.updateButtonStates();
        RealmsClient realmsClient = RealmsClient.createRealmsClient();
        String string = this.serverData.players.get(i).getUuid();
        try {
            this.updateOps(realmsClient.deop(this.serverData.id, string));
        } catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't deop the user");
        }
    }

    private void updateOps(Ops ops) {
        for (PlayerInfo playerInfo : this.serverData.players) {
            playerInfo.setOperator(ops.ops.contains(playerInfo.getName()));
        }
    }

    private void uninvite(int i) {
        this.updateButtonStates();
        if (i >= 0 && i < this.serverData.players.size()) {
            PlayerInfo playerInfo = this.serverData.players.get(i);
            this.selectedInvited = playerInfo.getUuid();
            this.selectedInvitedIndex = i;
            RealmsConfirmScreen realmsConfirmScreen = new RealmsConfirmScreen(this, "Question", RealmsPlayerScreen.getLocalizedString("mco.configure.world.uninvite.question") + " '" + playerInfo.getName() + "' ?", 2);
            Realms.setScreen(realmsConfirmScreen);
        }
    }

    @Override
    public void confirmResult(boolean bl, int i) {
        if (i == 2) {
            if (bl) {
                RealmsClient realmsClient = RealmsClient.createRealmsClient();
                try {
                    realmsClient.uninvite(this.serverData.id, this.selectedInvited);
                } catch (RealmsServiceException realmsServiceException) {
                    LOGGER.error("Couldn't uninvite user");
                }
                this.deleteFromInvitedList(this.selectedInvitedIndex);
                this.player = -1;
                this.updateButtonStates();
            }
            this.stateChanged = true;
            Realms.setScreen(this);
        }
    }

    private void deleteFromInvitedList(int i) {
        this.serverData.players.remove(i);
    }

    @Override
    public void render(int i, int j, float f) {
        this.toolTip = null;
        this.renderBackground();
        if (this.invitedObjectSelectionList != null) {
            this.invitedObjectSelectionList.render(i, j, f);
        }
        int k = RealmsConstants.row(12) + 20;
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tezzelator tezzelator = Tezzelator.instance;
        RealmsPlayerScreen.bind("textures/gui/options_background.png");
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        float g = 32.0f;
        tezzelator.begin(7, RealmsDefaultVertexFormat.POSITION_TEX_COLOR);
        tezzelator.vertex(0.0, this.height(), 0.0).tex(0.0, (float)(this.height() - k) / 32.0f + 0.0f).color(64, 64, 64, 255).endVertex();
        tezzelator.vertex(this.width(), this.height(), 0.0).tex((float)this.width() / 32.0f, (float)(this.height() - k) / 32.0f + 0.0f).color(64, 64, 64, 255).endVertex();
        tezzelator.vertex(this.width(), k, 0.0).tex((float)this.width() / 32.0f, 0.0).color(64, 64, 64, 255).endVertex();
        tezzelator.vertex(0.0, k, 0.0).tex(0.0, 0.0).color(64, 64, 64, 255).endVertex();
        tezzelator.end();
        this.titleLabel.render(this);
        if (this.serverData != null && this.serverData.players != null) {
            this.drawString(RealmsPlayerScreen.getLocalizedString("mco.configure.world.invited") + " (" + this.serverData.players.size() + ")", this.column1_x, RealmsConstants.row(0), 0xA0A0A0);
        } else {
            this.drawString(RealmsPlayerScreen.getLocalizedString("mco.configure.world.invited"), this.column1_x, RealmsConstants.row(0), 0xA0A0A0);
        }
        super.render(i, j, f);
        if (this.serverData == null) {
            return;
        }
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

    private void drawRemoveIcon(int i, int j, int k, int l) {
        boolean bl = k >= i && k <= i + 9 && l >= j && l <= j + 9 && l < RealmsConstants.row(12) + 20 && l > RealmsConstants.row(1);
        RealmsPlayerScreen.bind("realms:textures/gui/realms/cross_player_icon.png");
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
        RealmsScreen.blit(i, j, 0.0f, bl ? 7.0f : 0.0f, 8, 7, 8, 14);
        GlStateManager.popMatrix();
        if (bl) {
            this.toolTip = RealmsPlayerScreen.getLocalizedString("mco.configure.world.invites.remove.tooltip");
        }
    }

    private void drawOpped(int i, int j, int k, int l) {
        boolean bl = k >= i && k <= i + 9 && l >= j && l <= j + 9 && l < RealmsConstants.row(12) + 20 && l > RealmsConstants.row(1);
        RealmsPlayerScreen.bind("realms:textures/gui/realms/op_icon.png");
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
        RealmsScreen.blit(i, j, 0.0f, bl ? 8.0f : 0.0f, 8, 8, 8, 16);
        GlStateManager.popMatrix();
        if (bl) {
            this.toolTip = RealmsPlayerScreen.getLocalizedString("mco.configure.world.invites.ops.tooltip");
        }
    }

    private void drawNormal(int i, int j, int k, int l) {
        boolean bl = k >= i && k <= i + 9 && l >= j && l <= j + 9 && l < RealmsConstants.row(12) + 20 && l > RealmsConstants.row(1);
        RealmsPlayerScreen.bind("realms:textures/gui/realms/user_icon.png");
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
        RealmsScreen.blit(i, j, 0.0f, bl ? 8.0f : 0.0f, 8, 8, 8, 16);
        GlStateManager.popMatrix();
        if (bl) {
            this.toolTip = RealmsPlayerScreen.getLocalizedString("mco.configure.world.invites.normal.tooltip");
        }
    }

    @Environment(value=EnvType.CLIENT)
    class InvitedObjectSelectionListEntry
    extends RealmListEntry {
        final PlayerInfo mPlayerInfo;

        public InvitedObjectSelectionListEntry(PlayerInfo playerInfo) {
            this.mPlayerInfo = playerInfo;
        }

        @Override
        public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.renderInvitedItem(this.mPlayerInfo, k, j, n, o);
        }

        private void renderInvitedItem(PlayerInfo playerInfo, int i, int j, int k, int l) {
            int m = !playerInfo.getAccepted() ? 0xA0A0A0 : (playerInfo.getOnline() ? 0x7FFF7F : 0xFFFFFF);
            RealmsPlayerScreen.this.drawString(playerInfo.getName(), RealmsPlayerScreen.this.column1_x + 3 + 12, j + 1, m);
            if (playerInfo.isOperator()) {
                RealmsPlayerScreen.this.drawOpped(RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width - 10, j + 1, k, l);
            } else {
                RealmsPlayerScreen.this.drawNormal(RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width - 10, j + 1, k, l);
            }
            RealmsPlayerScreen.this.drawRemoveIcon(RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width - 22, j + 2, k, l);
            RealmsPlayerScreen.this.drawString(RealmsScreen.getLocalizedString("mco.configure.world.activityfeed.disabled"), RealmsPlayerScreen.this.column2_x, RealmsConstants.row(5), 0xA0A0A0);
            RealmsTextureManager.withBoundFace(playerInfo.getUuid(), () -> {
                GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                RealmsScreen.blit(RealmsPlayerScreen.this.column1_x + 2 + 2, j + 1, 8.0f, 8.0f, 8, 8, 8, 8, 64, 64);
                RealmsScreen.blit(RealmsPlayerScreen.this.column1_x + 2 + 2, j + 1, 40.0f, 8.0f, 8, 8, 8, 8, 64, 64);
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    class InvitedObjectSelectionList
    extends RealmsObjectSelectionList {
        public InvitedObjectSelectionList() {
            super(RealmsPlayerScreen.this.column_width + 10, RealmsConstants.row(12) + 20, RealmsConstants.row(1), RealmsConstants.row(12) + 20, 13);
        }

        public void addEntry(PlayerInfo playerInfo) {
            this.addEntry(new InvitedObjectSelectionListEntry(playerInfo));
        }

        @Override
        public int getRowWidth() {
            return (int)((double)this.width() * 1.0);
        }

        @Override
        public boolean isFocused() {
            return RealmsPlayerScreen.this.isFocused(this);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (i == 0 && d < (double)this.getScrollbarPosition() && e >= (double)this.y0() && e <= (double)this.y1()) {
                int j = RealmsPlayerScreen.this.column1_x;
                int k = RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width;
                int l = (int)Math.floor(e - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
                int m = l / this.itemHeight();
                if (d >= (double)j && d <= (double)k && m >= 0 && l >= 0 && m < this.getItemCount()) {
                    this.selectItem(m);
                    this.itemClicked(l, m, d, e, this.width());
                }
                return true;
            }
            return super.mouseClicked(d, e, i);
        }

        @Override
        public void itemClicked(int i, int j, double d, double e, int k) {
            if (j < 0 || j > ((RealmsPlayerScreen)RealmsPlayerScreen.this).serverData.players.size() || RealmsPlayerScreen.this.toolTip == null) {
                return;
            }
            if (RealmsPlayerScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.ops.tooltip")) || RealmsPlayerScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.normal.tooltip"))) {
                if (((RealmsPlayerScreen)RealmsPlayerScreen.this).serverData.players.get(j).isOperator()) {
                    RealmsPlayerScreen.this.deop(j);
                } else {
                    RealmsPlayerScreen.this.op(j);
                }
            } else if (RealmsPlayerScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.remove.tooltip"))) {
                RealmsPlayerScreen.this.uninvite(j);
            }
        }

        @Override
        public void selectItem(int i) {
            this.setSelected(i);
            if (i != -1) {
                Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", ((RealmsPlayerScreen)RealmsPlayerScreen.this).serverData.players.get(i).getName()));
            }
            this.selectInviteListItem(i);
        }

        public void selectInviteListItem(int i) {
            RealmsPlayerScreen.this.player = i;
            RealmsPlayerScreen.this.updateButtonStates();
        }

        @Override
        public void renderBackground() {
            RealmsPlayerScreen.this.renderBackground();
        }

        @Override
        public int getScrollbarPosition() {
            return RealmsPlayerScreen.this.column1_x + this.width() - 5;
        }

        @Override
        public int getItemCount() {
            return RealmsPlayerScreen.this.serverData == null ? 1 : ((RealmsPlayerScreen)RealmsPlayerScreen.this).serverData.players.size();
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 13;
        }
    }
}

