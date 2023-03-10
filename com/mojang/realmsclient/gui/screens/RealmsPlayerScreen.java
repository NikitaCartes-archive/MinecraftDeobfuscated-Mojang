/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfirmScreen;
import com.mojang.realmsclient.gui.screens.RealmsInviteScreen;
import com.mojang.realmsclient.util.RealmsUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsPlayerScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation OP_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/op_icon.png");
    private static final ResourceLocation USER_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/user_icon.png");
    private static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_player_icon.png");
    private static final ResourceLocation OPTIONS_BACKGROUND = new ResourceLocation("minecraft", "textures/gui/options_background.png");
    private static final Component NORMAL_USER_TOOLTIP = Component.translatable("mco.configure.world.invites.normal.tooltip");
    private static final Component OP_TOOLTIP = Component.translatable("mco.configure.world.invites.ops.tooltip");
    private static final Component REMOVE_ENTRY_TOOLTIP = Component.translatable("mco.configure.world.invites.remove.tooltip");
    private static final Component INVITED_LABEL = Component.translatable("mco.configure.world.invited");
    @Nullable
    private Component toolTip;
    private final RealmsConfigureWorldScreen lastScreen;
    final RealmsServer serverData;
    private InvitedObjectSelectionList invitedObjectSelectionList;
    int column1X;
    int columnWidth;
    private int column2X;
    private Button removeButton;
    private Button opdeopButton;
    private int selectedInvitedIndex = -1;
    private String selectedInvited;
    int player = -1;
    private boolean stateChanged;
    UserAction hoveredUserAction = UserAction.NONE;

    public RealmsPlayerScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsServer realmsServer) {
        super(Component.translatable("mco.configure.world.players.title"));
        this.lastScreen = realmsConfigureWorldScreen;
        this.serverData = realmsServer;
    }

    @Override
    public void init() {
        this.column1X = this.width / 2 - 160;
        this.columnWidth = 150;
        this.column2X = this.width / 2 + 12;
        this.invitedObjectSelectionList = new InvitedObjectSelectionList();
        this.invitedObjectSelectionList.setLeftPos(this.column1X);
        this.addWidget(this.invitedObjectSelectionList);
        for (PlayerInfo playerInfo : this.serverData.players) {
            this.invitedObjectSelectionList.addEntry(playerInfo);
        }
        this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.invite"), button -> this.minecraft.setScreen(new RealmsInviteScreen(this.lastScreen, this, this.serverData))).bounds(this.column2X, RealmsPlayerScreen.row(1), this.columnWidth + 10, 20).build());
        this.removeButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.invites.remove.tooltip"), button -> this.uninvite(this.player)).bounds(this.column2X, RealmsPlayerScreen.row(7), this.columnWidth + 10, 20).build());
        this.opdeopButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.invites.ops.tooltip"), button -> {
            if (this.serverData.players.get(this.player).isOperator()) {
                this.deop(this.player);
            } else {
                this.op(this.player);
            }
        }).bounds(this.column2X, RealmsPlayerScreen.row(9), this.columnWidth + 10, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.backButtonClicked()).bounds(this.column2X + this.columnWidth / 2 + 2, RealmsPlayerScreen.row(12), this.columnWidth / 2 + 10 - 2, 20).build());
        this.updateButtonStates();
    }

    void updateButtonStates() {
        this.removeButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
        this.opdeopButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
    }

    private boolean shouldRemoveAndOpdeopButtonBeVisible(int i) {
        return i != -1;
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
            this.minecraft.setScreen(this.lastScreen.getNewScreen());
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    void op(int i) {
        this.updateButtonStates();
        RealmsClient realmsClient = RealmsClient.create();
        String string = this.serverData.players.get(i).getUuid();
        try {
            this.updateOps(realmsClient.op(this.serverData.id, string));
        } catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't op the user");
        }
    }

    void deop(int i) {
        this.updateButtonStates();
        RealmsClient realmsClient = RealmsClient.create();
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

    void uninvite(int i) {
        this.updateButtonStates();
        if (i >= 0 && i < this.serverData.players.size()) {
            PlayerInfo playerInfo = this.serverData.players.get(i);
            this.selectedInvited = playerInfo.getUuid();
            this.selectedInvitedIndex = i;
            RealmsConfirmScreen realmsConfirmScreen = new RealmsConfirmScreen(bl -> {
                if (bl) {
                    RealmsClient realmsClient = RealmsClient.create();
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
                this.minecraft.setScreen(this);
            }, Component.literal("Question"), Component.translatable("mco.configure.world.uninvite.question").append(" '").append(playerInfo.getName()).append("' ?"));
            this.minecraft.setScreen(realmsConfirmScreen);
        }
    }

    private void deleteFromInvitedList(int i) {
        this.serverData.players.remove(i);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.toolTip = null;
        this.hoveredUserAction = UserAction.NONE;
        this.renderBackground(poseStack);
        if (this.invitedObjectSelectionList != null) {
            this.invitedObjectSelectionList.render(poseStack, i, j, f);
        }
        RealmsPlayerScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 0xFFFFFF);
        int k = RealmsPlayerScreen.row(12) + 20;
        RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND);
        RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);
        RealmsPlayerScreen.blit(poseStack, 0, k, 0.0f, 0.0f, this.width, this.height - k, 32, 32);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.serverData != null && this.serverData.players != null) {
            this.font.draw(poseStack, Component.empty().append(INVITED_LABEL).append(" (").append(Integer.toString(this.serverData.players.size())).append(")"), (float)this.column1X, (float)RealmsPlayerScreen.row(0), 0xA0A0A0);
        } else {
            this.font.draw(poseStack, INVITED_LABEL, (float)this.column1X, (float)RealmsPlayerScreen.row(0), 0xA0A0A0);
        }
        super.render(poseStack, i, j, f);
        if (this.serverData == null) {
            return;
        }
        this.renderMousehoverTooltip(poseStack, this.toolTip, i, j);
    }

    protected void renderMousehoverTooltip(PoseStack poseStack, @Nullable Component component, int i, int j) {
        if (component == null) {
            return;
        }
        int k = i + 12;
        int l = j - 12;
        int m = this.font.width(component);
        RealmsPlayerScreen.fillGradient(poseStack, k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
        this.font.drawShadow(poseStack, component, (float)k, (float)l, 0xFFFFFF);
    }

    void drawRemoveIcon(PoseStack poseStack, int i, int j, int k, int l) {
        boolean bl = k >= i && k <= i + 9 && l >= j && l <= j + 9 && l < RealmsPlayerScreen.row(12) + 20 && l > RealmsPlayerScreen.row(1);
        RenderSystem.setShaderTexture(0, CROSS_ICON_LOCATION);
        float f = bl ? 7.0f : 0.0f;
        GuiComponent.blit(poseStack, i, j, 0.0f, f, 8, 7, 8, 14);
        if (bl) {
            this.toolTip = REMOVE_ENTRY_TOOLTIP;
            this.hoveredUserAction = UserAction.REMOVE;
        }
    }

    void drawOpped(PoseStack poseStack, int i, int j, int k, int l) {
        boolean bl = k >= i && k <= i + 9 && l >= j && l <= j + 9 && l < RealmsPlayerScreen.row(12) + 20 && l > RealmsPlayerScreen.row(1);
        RenderSystem.setShaderTexture(0, OP_ICON_LOCATION);
        float f = bl ? 8.0f : 0.0f;
        GuiComponent.blit(poseStack, i, j, 0.0f, f, 8, 8, 8, 16);
        if (bl) {
            this.toolTip = OP_TOOLTIP;
            this.hoveredUserAction = UserAction.TOGGLE_OP;
        }
    }

    void drawNormal(PoseStack poseStack, int i, int j, int k, int l) {
        boolean bl = k >= i && k <= i + 9 && l >= j && l <= j + 9 && l < RealmsPlayerScreen.row(12) + 20 && l > RealmsPlayerScreen.row(1);
        RenderSystem.setShaderTexture(0, USER_ICON_LOCATION);
        float f = bl ? 8.0f : 0.0f;
        GuiComponent.blit(poseStack, i, j, 0.0f, f, 8, 8, 8, 16);
        if (bl) {
            this.toolTip = NORMAL_USER_TOOLTIP;
            this.hoveredUserAction = UserAction.TOGGLE_OP;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum UserAction {
        TOGGLE_OP,
        REMOVE,
        NONE;

    }

    @Environment(value=EnvType.CLIENT)
    class InvitedObjectSelectionList
    extends RealmsObjectSelectionList<Entry> {
        public InvitedObjectSelectionList() {
            super(RealmsPlayerScreen.this.columnWidth + 10, RealmsPlayerScreen.row(12) + 20, RealmsPlayerScreen.row(1), RealmsPlayerScreen.row(12) + 20, 13);
        }

        public void addEntry(PlayerInfo playerInfo) {
            this.addEntry(new Entry(playerInfo));
        }

        @Override
        public int getRowWidth() {
            return (int)((double)this.width * 1.0);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (i == 0 && d < (double)this.getScrollbarPosition() && e >= (double)this.y0 && e <= (double)this.y1) {
                int j = RealmsPlayerScreen.this.column1X;
                int k = RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth;
                int l = (int)Math.floor(e - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
                int m = l / this.itemHeight;
                if (d >= (double)j && d <= (double)k && m >= 0 && l >= 0 && m < this.getItemCount()) {
                    this.selectItem(m);
                    this.itemClicked(l, m, d, e, this.width, i);
                }
                return true;
            }
            return super.mouseClicked(d, e, i);
        }

        @Override
        public void itemClicked(int i, int j, double d, double e, int k, int l) {
            if (j < 0 || j > RealmsPlayerScreen.this.serverData.players.size() || RealmsPlayerScreen.this.hoveredUserAction == UserAction.NONE) {
                return;
            }
            if (RealmsPlayerScreen.this.hoveredUserAction == UserAction.TOGGLE_OP) {
                if (RealmsPlayerScreen.this.serverData.players.get(j).isOperator()) {
                    RealmsPlayerScreen.this.deop(j);
                } else {
                    RealmsPlayerScreen.this.op(j);
                }
            } else if (RealmsPlayerScreen.this.hoveredUserAction == UserAction.REMOVE) {
                RealmsPlayerScreen.this.uninvite(j);
            }
        }

        @Override
        public void selectItem(int i) {
            super.selectItem(i);
            this.selectInviteListItem(i);
        }

        public void selectInviteListItem(int i) {
            RealmsPlayerScreen.this.player = i;
            RealmsPlayerScreen.this.updateButtonStates();
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            RealmsPlayerScreen.this.player = this.children().indexOf(entry);
            RealmsPlayerScreen.this.updateButtonStates();
        }

        @Override
        public void renderBackground(PoseStack poseStack) {
            RealmsPlayerScreen.this.renderBackground(poseStack);
        }

        @Override
        public int getScrollbarPosition() {
            return RealmsPlayerScreen.this.column1X + this.width - 5;
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 13;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class Entry
    extends ObjectSelectionList.Entry<Entry> {
        private final PlayerInfo playerInfo;

        public Entry(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.renderInvitedItem(poseStack, this.playerInfo, k, j, n, o);
        }

        private void renderInvitedItem(PoseStack poseStack, PlayerInfo playerInfo, int i, int j, int k, int l) {
            int m = !playerInfo.getAccepted() ? 0xA0A0A0 : (playerInfo.getOnline() ? 0x7FFF7F : 0xFFFFFF);
            RealmsPlayerScreen.this.font.draw(poseStack, playerInfo.getName(), (float)(RealmsPlayerScreen.this.column1X + 3 + 12), (float)(j + 1), m);
            if (playerInfo.isOperator()) {
                RealmsPlayerScreen.this.drawOpped(poseStack, RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, j + 1, k, l);
            } else {
                RealmsPlayerScreen.this.drawNormal(poseStack, RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, j + 1, k, l);
            }
            RealmsPlayerScreen.this.drawRemoveIcon(poseStack, RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 22, j + 2, k, l);
            RealmsUtil.renderPlayerFace(poseStack, RealmsPlayerScreen.this.column1X + 2 + 2, j + 1, 8, playerInfo.getUuid());
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.playerInfo.getName());
        }
    }
}

