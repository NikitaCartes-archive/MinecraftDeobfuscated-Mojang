package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsPlayerScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation OP_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/op_icon.png");
	private static final ResourceLocation USER_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/user_icon.png");
	private static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_player_icon.png");
	private static final ResourceLocation OPTIONS_BACKGROUND = new ResourceLocation("minecraft", "textures/gui/options_background.png");
	private String toolTip;
	private final RealmsConfigureWorldScreen lastScreen;
	private final RealmsServer serverData;
	private RealmsPlayerScreen.InvitedObjectSelectionList invitedObjectSelectionList;
	private int column1X;
	private int columnWidth;
	private int column2X;
	private Button removeButton;
	private Button opdeopButton;
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
	public void init() {
		this.column1X = this.width / 2 - 160;
		this.columnWidth = 150;
		this.column2X = this.width / 2 + 12;
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.invitedObjectSelectionList = new RealmsPlayerScreen.InvitedObjectSelectionList();
		this.invitedObjectSelectionList.setLeftPos(this.column1X);
		this.addWidget(this.invitedObjectSelectionList);

		for (PlayerInfo playerInfo : this.serverData.players) {
			this.invitedObjectSelectionList.addEntry(playerInfo);
		}

		this.addButton(
			new Button(
				this.column2X,
				row(1),
				this.columnWidth + 10,
				20,
				I18n.get("mco.configure.world.buttons.invite"),
				button -> this.minecraft.setScreen(new RealmsInviteScreen(this.lastScreen, this, this.serverData))
			)
		);
		this.removeButton = this.addButton(
			new Button(this.column2X, row(7), this.columnWidth + 10, 20, I18n.get("mco.configure.world.invites.remove.tooltip"), button -> this.uninvite(this.player))
		);
		this.opdeopButton = this.addButton(
			new Button(this.column2X, row(9), this.columnWidth + 10, 20, I18n.get("mco.configure.world.invites.ops.tooltip"), button -> {
				if (((PlayerInfo)this.serverData.players.get(this.player)).isOperator()) {
					this.deop(this.player);
				} else {
					this.op(this.player);
				}
			})
		);
		this.addButton(
			new Button(this.column2X + this.columnWidth / 2 + 2, row(12), this.columnWidth / 2 + 10 - 2, 20, I18n.get("gui.back"), button -> this.backButtonClicked())
		);
		this.titleLabel = this.addWidget(new RealmsLabel(I18n.get("mco.configure.world.players.title"), this.width / 2, 17, 16777215));
		this.narrateLabels();
		this.updateButtonStates();
	}

	private void updateButtonStates() {
		this.removeButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
		this.opdeopButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
	}

	private boolean shouldRemoveAndOpdeopButtonBeVisible(int i) {
		return i != -1;
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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

	private void op(int i) {
		this.updateButtonStates();
		RealmsClient realmsClient = RealmsClient.create();
		String string = ((PlayerInfo)this.serverData.players.get(i)).getUuid();

		try {
			this.updateOps(realmsClient.op(this.serverData.id, string));
		} catch (RealmsServiceException var5) {
			LOGGER.error("Couldn't op the user");
		}
	}

	private void deop(int i) {
		this.updateButtonStates();
		RealmsClient realmsClient = RealmsClient.create();
		String string = ((PlayerInfo)this.serverData.players.get(i)).getUuid();

		try {
			this.updateOps(realmsClient.deop(this.serverData.id, string));
		} catch (RealmsServiceException var5) {
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
			PlayerInfo playerInfo = (PlayerInfo)this.serverData.players.get(i);
			this.selectedInvited = playerInfo.getUuid();
			this.selectedInvitedIndex = i;
			RealmsConfirmScreen realmsConfirmScreen = new RealmsConfirmScreen(bl -> {
				if (bl) {
					RealmsClient realmsClient = RealmsClient.create();

					try {
						realmsClient.uninvite(this.serverData.id, this.selectedInvited);
					} catch (RealmsServiceException var4) {
						LOGGER.error("Couldn't uninvite user");
					}

					this.deleteFromInvitedList(this.selectedInvitedIndex);
					this.player = -1;
					this.updateButtonStates();
				}

				this.stateChanged = true;
				this.minecraft.setScreen(this);
			}, "Question", I18n.get("mco.configure.world.uninvite.question") + " '" + playerInfo.getName() + "' ?");
			this.minecraft.setScreen(realmsConfirmScreen);
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

		int k = row(12) + 20;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		this.minecraft.getTextureManager().bind(OPTIONS_BACKGROUND);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float g = 32.0F;
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(0.0, (double)this.height, 0.0).uv(0.0F, (float)(this.height - k) / 32.0F + 0.0F).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex((double)this.width, (double)this.height, 0.0)
			.uv((float)this.width / 32.0F, (float)(this.height - k) / 32.0F + 0.0F)
			.color(64, 64, 64, 255)
			.endVertex();
		bufferBuilder.vertex((double)this.width, (double)k, 0.0).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex(0.0, (double)k, 0.0).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
		tesselator.end();
		this.titleLabel.render(this);
		if (this.serverData != null && this.serverData.players != null) {
			this.font.draw(I18n.get("mco.configure.world.invited") + " (" + this.serverData.players.size() + ")", (float)this.column1X, (float)row(0), 10526880);
		} else {
			this.font.draw(I18n.get("mco.configure.world.invited"), (float)this.column1X, (float)row(0), 10526880);
		}

		super.render(i, j, f);
		if (this.serverData != null) {
			if (this.toolTip != null) {
				this.renderMousehoverTooltip(this.toolTip, i, j);
			}
		}
	}

	protected void renderMousehoverTooltip(String string, int i, int j) {
		if (string != null) {
			int k = i + 12;
			int l = j - 12;
			int m = this.font.width(string);
			this.fillGradient(k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
			this.font.drawShadow(string, (float)k, (float)l, 16777215);
		}
	}

	private void drawRemoveIcon(int i, int j, int k, int l) {
		boolean bl = k >= i && k <= i + 9 && l >= j && l <= j + 9 && l < row(12) + 20 && l > row(1);
		this.minecraft.getTextureManager().bind(CROSS_ICON_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = bl ? 7.0F : 0.0F;
		GuiComponent.blit(i, j, 0.0F, f, 8, 7, 8, 14);
		if (bl) {
			this.toolTip = I18n.get("mco.configure.world.invites.remove.tooltip");
		}
	}

	private void drawOpped(int i, int j, int k, int l) {
		boolean bl = k >= i && k <= i + 9 && l >= j && l <= j + 9 && l < row(12) + 20 && l > row(1);
		this.minecraft.getTextureManager().bind(OP_ICON_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = bl ? 8.0F : 0.0F;
		GuiComponent.blit(i, j, 0.0F, f, 8, 8, 8, 16);
		if (bl) {
			this.toolTip = I18n.get("mco.configure.world.invites.ops.tooltip");
		}
	}

	private void drawNormal(int i, int j, int k, int l) {
		boolean bl = k >= i && k <= i + 9 && l >= j && l <= j + 9 && l < row(12) + 20 && l > row(1);
		this.minecraft.getTextureManager().bind(USER_ICON_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = bl ? 8.0F : 0.0F;
		GuiComponent.blit(i, j, 0.0F, f, 8, 8, 8, 16);
		if (bl) {
			this.toolTip = I18n.get("mco.configure.world.invites.normal.tooltip");
		}
	}

	@Environment(EnvType.CLIENT)
	class Entry extends ObjectSelectionList.Entry<RealmsPlayerScreen.Entry> {
		private final PlayerInfo playerInfo;

		public Entry(PlayerInfo playerInfo) {
			this.playerInfo = playerInfo;
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderInvitedItem(this.playerInfo, k, j, n, o);
		}

		private void renderInvitedItem(PlayerInfo playerInfo, int i, int j, int k, int l) {
			int m;
			if (!playerInfo.getAccepted()) {
				m = 10526880;
			} else if (playerInfo.getOnline()) {
				m = 8388479;
			} else {
				m = 16777215;
			}

			RealmsPlayerScreen.this.font.draw(playerInfo.getName(), (float)(RealmsPlayerScreen.this.column1X + 3 + 12), (float)(j + 1), m);
			if (playerInfo.isOperator()) {
				RealmsPlayerScreen.this.drawOpped(RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, j + 1, k, l);
			} else {
				RealmsPlayerScreen.this.drawNormal(RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, j + 1, k, l);
			}

			RealmsPlayerScreen.this.drawRemoveIcon(RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 22, j + 2, k, l);
			RealmsPlayerScreen.this.font
				.draw(I18n.get("mco.configure.world.activityfeed.disabled"), (float)RealmsPlayerScreen.this.column2X, (float)RealmsPlayerScreen.row(5), 10526880);
			RealmsTextureManager.withBoundFace(playerInfo.getUuid(), () -> {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				GuiComponent.blit(RealmsPlayerScreen.this.column1X + 2 + 2, j + 1, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64);
				GuiComponent.blit(RealmsPlayerScreen.this.column1X + 2 + 2, j + 1, 8, 8, 40.0F, 8.0F, 8, 8, 64, 64);
			});
		}
	}

	@Environment(EnvType.CLIENT)
	class InvitedObjectSelectionList extends RealmsObjectSelectionList<RealmsPlayerScreen.Entry> {
		public InvitedObjectSelectionList() {
			super(RealmsPlayerScreen.this.columnWidth + 10, RealmsPlayerScreen.row(12) + 20, RealmsPlayerScreen.row(1), RealmsPlayerScreen.row(12) + 20, 13);
		}

		public void addEntry(PlayerInfo playerInfo) {
			this.addEntry(RealmsPlayerScreen.this.new Entry(playerInfo));
		}

		@Override
		public int getRowWidth() {
			return (int)((double)this.width * 1.0);
		}

		@Override
		public boolean isFocused() {
			return RealmsPlayerScreen.this.getFocused() == this;
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
					this.itemClicked(l, m, d, e, this.width);
				}

				return true;
			} else {
				return super.mouseClicked(d, e, i);
			}
		}

		@Override
		public void itemClicked(int i, int j, double d, double e, int k) {
			if (j >= 0 && j <= RealmsPlayerScreen.this.serverData.players.size() && RealmsPlayerScreen.this.toolTip != null) {
				if (!RealmsPlayerScreen.this.toolTip.equals(I18n.get("mco.configure.world.invites.ops.tooltip"))
					&& !RealmsPlayerScreen.this.toolTip.equals(I18n.get("mco.configure.world.invites.normal.tooltip"))) {
					if (RealmsPlayerScreen.this.toolTip.equals(I18n.get("mco.configure.world.invites.remove.tooltip"))) {
						RealmsPlayerScreen.this.uninvite(j);
					}
				} else if (((PlayerInfo)RealmsPlayerScreen.this.serverData.players.get(j)).isOperator()) {
					RealmsPlayerScreen.this.deop(j);
				} else {
					RealmsPlayerScreen.this.op(j);
				}
			}
		}

		@Override
		public void selectItem(int i) {
			this.setSelectedItem(i);
			if (i != -1) {
				NarrationHelper.now(I18n.get("narrator.select", ((PlayerInfo)RealmsPlayerScreen.this.serverData.players.get(i)).getName()));
			}

			this.selectInviteListItem(i);
		}

		public void selectInviteListItem(int i) {
			RealmsPlayerScreen.this.player = i;
			RealmsPlayerScreen.this.updateButtonStates();
		}

		public void setSelected(@Nullable RealmsPlayerScreen.Entry entry) {
			super.setSelected(entry);
			RealmsPlayerScreen.this.player = this.children().indexOf(entry);
			RealmsPlayerScreen.this.updateButtonStates();
		}

		@Override
		public void renderBackground() {
			RealmsPlayerScreen.this.renderBackground();
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
