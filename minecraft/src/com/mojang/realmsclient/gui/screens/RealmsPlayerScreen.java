package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsPlayerScreen extends RealmsScreen {
	private static final Logger LOGGER = LogUtils.getLogger();
	static final ResourceLocation OP_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/op_icon.png");
	static final ResourceLocation USER_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/user_icon.png");
	static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_player_icon.png");
	private static final ResourceLocation OPTIONS_BACKGROUND = new ResourceLocation("minecraft", "textures/gui/options_background.png");
	static final Component NORMAL_USER_TOOLTIP = Component.translatable("mco.configure.world.invites.normal.tooltip");
	static final Component OP_TOOLTIP = Component.translatable("mco.configure.world.invites.ops.tooltip");
	static final Component REMOVE_ENTRY_TOOLTIP = Component.translatable("mco.configure.world.invites.remove.tooltip");
	private static final Component INVITED_LABEL = Component.translatable("mco.configure.world.invited");
	private static final int NO_ENTRY_SELECTED = -1;
	private final RealmsConfigureWorldScreen lastScreen;
	final RealmsServer serverData;
	RealmsPlayerScreen.InvitedObjectSelectionList invitedObjectSelectionList;
	int column1X;
	int columnWidth;
	private Button removeButton;
	private Button opdeopButton;
	int playerIndex = -1;
	private boolean stateChanged;

	public RealmsPlayerScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsServer realmsServer) {
		super(Component.translatable("mco.configure.world.players.title"));
		this.lastScreen = realmsConfigureWorldScreen;
		this.serverData = realmsServer;
	}

	@Override
	public void init() {
		this.column1X = this.width / 2 - 160;
		this.columnWidth = 150;
		int i = this.width / 2 + 12;
		this.invitedObjectSelectionList = new RealmsPlayerScreen.InvitedObjectSelectionList();
		this.invitedObjectSelectionList.setLeftPos(this.column1X);
		this.addWidget(this.invitedObjectSelectionList);

		for (PlayerInfo playerInfo : this.serverData.players) {
			this.invitedObjectSelectionList.addEntry(playerInfo);
		}

		this.playerIndex = -1;
		this.addRenderableWidget(
			Button.builder(
					Component.translatable("mco.configure.world.buttons.invite"),
					button -> this.minecraft.setScreen(new RealmsInviteScreen(this.lastScreen, this, this.serverData))
				)
				.bounds(i, row(1), this.columnWidth + 10, 20)
				.build()
		);
		this.removeButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.configure.world.invites.remove.tooltip"), button -> this.uninvite(this.playerIndex))
				.bounds(i, row(7), this.columnWidth + 10, 20)
				.build()
		);
		this.opdeopButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.invites.ops.tooltip"), button -> {
			if (((PlayerInfo)this.serverData.players.get(this.playerIndex)).isOperator()) {
				this.deop(this.playerIndex);
			} else {
				this.op(this.playerIndex);
			}
		}).bounds(i, row(9), this.columnWidth + 10, 20).build());
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_BACK, button -> this.backButtonClicked())
				.bounds(i + this.columnWidth / 2 + 2, row(12), this.columnWidth / 2 + 10 - 2, 20)
				.build()
		);
		this.updateButtonStates();
	}

	void updateButtonStates() {
		this.removeButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.playerIndex);
		this.opdeopButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.playerIndex);
		this.invitedObjectSelectionList.updateButtons();
	}

	private boolean shouldRemoveAndOpdeopButtonBeVisible(int i) {
		return i != -1;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.backButtonClicked();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	private void backButtonClicked() {
		if (this.stateChanged) {
			this.minecraft.setScreen(this.lastScreen.getNewScreen());
		} else {
			this.minecraft.setScreen(this.lastScreen);
		}
	}

	void op(int i) {
		RealmsClient realmsClient = RealmsClient.create();
		String string = ((PlayerInfo)this.serverData.players.get(i)).getUuid();

		try {
			this.updateOps(realmsClient.op(this.serverData.id, string));
		} catch (RealmsServiceException var5) {
			LOGGER.error("Couldn't op the user");
		}

		this.updateButtonStates();
	}

	void deop(int i) {
		RealmsClient realmsClient = RealmsClient.create();
		String string = ((PlayerInfo)this.serverData.players.get(i)).getUuid();

		try {
			this.updateOps(realmsClient.deop(this.serverData.id, string));
		} catch (RealmsServiceException var5) {
			LOGGER.error("Couldn't deop the user");
		}

		this.updateButtonStates();
	}

	private void updateOps(Ops ops) {
		for (PlayerInfo playerInfo : this.serverData.players) {
			playerInfo.setOperator(ops.ops.contains(playerInfo.getName()));
		}
	}

	void uninvite(int i) {
		this.updateButtonStates();
		if (i >= 0 && i < this.serverData.players.size()) {
			PlayerInfo playerInfo = (PlayerInfo)this.serverData.players.get(i);
			RealmsConfirmScreen realmsConfirmScreen = new RealmsConfirmScreen(bl -> {
				if (bl) {
					RealmsClient realmsClient = RealmsClient.create();

					try {
						realmsClient.uninvite(this.serverData.id, playerInfo.getUuid());
					} catch (RealmsServiceException var5) {
						LOGGER.error("Couldn't uninvite user");
					}

					this.serverData.players.remove(this.playerIndex);
					this.playerIndex = -1;
					this.updateButtonStates();
				}

				this.stateChanged = true;
				this.minecraft.setScreen(this);
			}, Component.literal("Question"), Component.translatable("mco.configure.world.uninvite.question").append(" '").append(playerInfo.getName()).append("' ?"));
			this.minecraft.setScreen(realmsConfirmScreen);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderBackground(guiGraphics);
		this.invitedObjectSelectionList.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);
		int k = row(12) + 20;
		guiGraphics.setColor(0.25F, 0.25F, 0.25F, 1.0F);
		guiGraphics.blit(OPTIONS_BACKGROUND, 0, k, 0.0F, 0.0F, this.width, this.height - k, 32, 32);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.serverData.players != null) {
			guiGraphics.drawString(
				this.font,
				Component.empty().append(INVITED_LABEL).append(" (").append(Integer.toString(this.serverData.players.size())).append(")"),
				this.column1X,
				row(0),
				10526880,
				false
			);
		} else {
			guiGraphics.drawString(this.font, INVITED_LABEL, this.column1X, row(0), 10526880, false);
		}

		super.render(guiGraphics, i, j, f);
	}

	@Environment(EnvType.CLIENT)
	class Entry extends ObjectSelectionList.Entry<RealmsPlayerScreen.Entry> {
		private static final int X_OFFSET = 3;
		private static final int Y_PADDING = 1;
		private static final int BUTTON_WIDTH = 8;
		private static final int BUTTON_HEIGHT = 7;
		private final PlayerInfo playerInfo;
		private final List<AbstractWidget> children = new ArrayList();
		private final ImageButton removeButton;
		private final ImageButton makeOpButton;
		private final ImageButton removeOpButton;

		public Entry(PlayerInfo playerInfo) {
			this.playerInfo = playerInfo;
			int i = RealmsPlayerScreen.this.serverData.players.indexOf(this.playerInfo);
			int j = RealmsPlayerScreen.this.invitedObjectSelectionList.getRowRight() - 16 - 9;
			int k = RealmsPlayerScreen.this.invitedObjectSelectionList.getRowTop(i) + 1;
			this.removeButton = new ImageButton(j, k, 8, 7, 0, 0, 7, RealmsPlayerScreen.CROSS_ICON_LOCATION, 8, 14, button -> RealmsPlayerScreen.this.uninvite(i));
			this.removeButton.setTooltip(Tooltip.create(RealmsPlayerScreen.REMOVE_ENTRY_TOOLTIP));
			this.children.add(this.removeButton);
			j += 11;
			this.makeOpButton = new ImageButton(j, k, 8, 7, 0, 0, 7, RealmsPlayerScreen.USER_ICON_LOCATION, 8, 14, button -> RealmsPlayerScreen.this.op(i));
			this.makeOpButton.setTooltip(Tooltip.create(RealmsPlayerScreen.NORMAL_USER_TOOLTIP));
			this.children.add(this.makeOpButton);
			this.removeOpButton = new ImageButton(j, k, 8, 7, 0, 0, 7, RealmsPlayerScreen.OP_ICON_LOCATION, 8, 14, button -> RealmsPlayerScreen.this.deop(i));
			this.removeOpButton.setTooltip(Tooltip.create(RealmsPlayerScreen.OP_TOOLTIP));
			this.children.add(this.removeOpButton);
			this.updateButtons();
		}

		public void updateButtons() {
			this.makeOpButton.visible = !this.playerInfo.isOperator();
			this.removeOpButton.visible = !this.makeOpButton.visible;
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (!this.makeOpButton.mouseClicked(d, e, i)) {
				this.removeOpButton.mouseClicked(d, e, i);
			}

			this.removeButton.mouseClicked(d, e, i);
			return true;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			int p;
			if (!this.playerInfo.getAccepted()) {
				p = 10526880;
			} else if (this.playerInfo.getOnline()) {
				p = 8388479;
			} else {
				p = 16777215;
			}

			RealmsUtil.renderPlayerFace(guiGraphics, RealmsPlayerScreen.this.column1X + 2 + 2, j + 1, 8, this.playerInfo.getUuid());
			guiGraphics.drawString(RealmsPlayerScreen.this.font, this.playerInfo.getName(), RealmsPlayerScreen.this.column1X + 3 + 12, j + 1, p, false);
			this.children.forEach(abstractWidget -> {
				abstractWidget.setY(j + 1);
				abstractWidget.render(guiGraphics, n, o, f);
			});
		}

		@Override
		public Component getNarration() {
			return Component.translatable("narrator.select", this.playerInfo.getName());
		}
	}

	@Environment(EnvType.CLIENT)
	class InvitedObjectSelectionList extends RealmsObjectSelectionList<RealmsPlayerScreen.Entry> {
		public InvitedObjectSelectionList() {
			super(RealmsPlayerScreen.this.columnWidth + 10, RealmsPlayerScreen.row(12) + 20, RealmsPlayerScreen.row(1), RealmsPlayerScreen.row(12) + 20, 13);
		}

		public void updateButtons() {
			if (RealmsPlayerScreen.this.playerIndex != -1) {
				this.getEntry(RealmsPlayerScreen.this.playerIndex).updateButtons();
			}
		}

		public void addEntry(PlayerInfo playerInfo) {
			this.addEntry(RealmsPlayerScreen.this.new Entry(playerInfo));
		}

		@Override
		public int getRowWidth() {
			return (int)((double)this.width * 1.0);
		}

		@Override
		public void selectItem(int i) {
			super.selectItem(i);
			this.selectInviteListItem(i);
		}

		public void selectInviteListItem(int i) {
			RealmsPlayerScreen.this.playerIndex = i;
			RealmsPlayerScreen.this.updateButtonStates();
		}

		public void setSelected(@Nullable RealmsPlayerScreen.Entry entry) {
			super.setSelected(entry);
			RealmsPlayerScreen.this.playerIndex = this.children().indexOf(entry);
			RealmsPlayerScreen.this.updateButtonStates();
		}

		@Override
		public void renderBackground(GuiGraphics guiGraphics) {
			RealmsPlayerScreen.this.renderBackground(guiGraphics);
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
}
