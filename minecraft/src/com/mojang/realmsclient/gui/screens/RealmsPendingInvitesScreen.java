package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RowButton;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsPendingInvitesScreen extends RealmsScreen {
	static final ResourceLocation ACCEPT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/accept_highlighted");
	static final ResourceLocation ACCEPT_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/accept");
	static final ResourceLocation REJECT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/reject_highlighted");
	static final ResourceLocation REJECT_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/reject");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component NO_PENDING_INVITES_TEXT = Component.translatable("mco.invites.nopending");
	static final Component ACCEPT_INVITE = Component.translatable("mco.invites.button.accept");
	static final Component REJECT_INVITE = Component.translatable("mco.invites.button.reject");
	private final Screen lastScreen;
	private final CompletableFuture<List<PendingInvite>> pendingInvites = CompletableFuture.supplyAsync(() -> {
		try {
			return RealmsClient.create().pendingInvites().pendingInvites;
		} catch (RealmsServiceException var1) {
			LOGGER.error("Couldn't list invites", (Throwable)var1);
			return List.of();
		}
	}, Util.ioPool());
	@Nullable
	Component toolTip;
	RealmsPendingInvitesScreen.PendingInvitationSelectionList pendingInvitationSelectionList;
	int selectedInvite = -1;
	private Button acceptButton;
	private Button rejectButton;

	public RealmsPendingInvitesScreen(Screen screen, Component component) {
		super(component);
		this.lastScreen = screen;
	}

	@Override
	public void init() {
		RealmsMainScreen.refreshPendingInvites();
		this.pendingInvitationSelectionList = new RealmsPendingInvitesScreen.PendingInvitationSelectionList();
		this.pendingInvites.thenAcceptAsync(list -> {
			List<RealmsPendingInvitesScreen.Entry> list2 = list.stream().map(pendingInvite -> new RealmsPendingInvitesScreen.Entry(pendingInvite)).toList();
			this.pendingInvitationSelectionList.replaceEntries(list2);
			if (list2.isEmpty()) {
				this.minecraft.getNarrator().say(NO_PENDING_INVITES_TEXT);
			}
		}, this.screenExecutor);
		this.addRenderableWidget(this.pendingInvitationSelectionList);
		this.acceptButton = this.addRenderableWidget(Button.builder(ACCEPT_INVITE, button -> {
			this.handleInvitation(this.selectedInvite, true);
			this.selectedInvite = -1;
			this.updateButtonStates();
		}).bounds(this.width / 2 - 174, this.height - 32, 100, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 50, this.height - 32, 100, 20).build());
		this.rejectButton = this.addRenderableWidget(Button.builder(REJECT_INVITE, button -> {
			this.handleInvitation(this.selectedInvite, false);
			this.selectedInvite = -1;
			this.updateButtonStates();
		}).bounds(this.width / 2 + 74, this.height - 32, 100, 20).build());
		this.updateButtonStates();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	void handleInvitation(int i, boolean bl) {
		if (i < this.pendingInvitationSelectionList.getItemCount()) {
			String string = ((RealmsPendingInvitesScreen.Entry)this.pendingInvitationSelectionList.children().get(i)).pendingInvite.invitationId;
			CompletableFuture.supplyAsync(() -> {
				try {
					RealmsClient realmsClient = RealmsClient.create();
					if (bl) {
						realmsClient.acceptInvitation(string);
					} else {
						realmsClient.rejectInvitation(string);
					}

					return true;
				} catch (RealmsServiceException var3x) {
					LOGGER.error("Couldn't handle invite", (Throwable)var3x);
					return false;
				}
			}, Util.ioPool()).thenAcceptAsync(boolean_ -> {
				if (boolean_) {
					this.pendingInvitationSelectionList.removeAtIndex(i);
					RealmsDataFetcher realmsDataFetcher = this.minecraft.realmsDataFetcher();
					if (bl) {
						realmsDataFetcher.serverListUpdateTask.reset();
					}

					realmsDataFetcher.pendingInvitesTask.reset();
				}
			}, this.screenExecutor);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.toolTip = null;
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, -1);
		if (this.toolTip != null) {
			guiGraphics.renderTooltip(this.font, this.toolTip, i, j);
		}

		if (this.pendingInvites.isDone() && this.pendingInvitationSelectionList.getItemCount() == 0) {
			guiGraphics.drawCenteredString(this.font, NO_PENDING_INVITES_TEXT, this.width / 2, this.height / 2 - 20, -1);
		}
	}

	void updateButtonStates() {
		this.acceptButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
		this.rejectButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
	}

	private boolean shouldAcceptAndRejectButtonBeVisible(int i) {
		return i != -1;
	}

	@Environment(EnvType.CLIENT)
	class Entry extends ObjectSelectionList.Entry<RealmsPendingInvitesScreen.Entry> {
		private static final int TEXT_LEFT = 38;
		final PendingInvite pendingInvite;
		private final List<RowButton> rowButtons;

		Entry(final PendingInvite pendingInvite) {
			this.pendingInvite = pendingInvite;
			this.rowButtons = Arrays.asList(new RealmsPendingInvitesScreen.Entry.AcceptRowButton(), new RealmsPendingInvitesScreen.Entry.RejectRowButton());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderPendingInvitationItem(guiGraphics, this.pendingInvite, k, j, n, o);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			RowButton.rowButtonMouseClicked(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, this, this.rowButtons, i, d, e);
			return super.mouseClicked(d, e, i);
		}

		private void renderPendingInvitationItem(GuiGraphics guiGraphics, PendingInvite pendingInvite, int i, int j, int k, int l) {
			guiGraphics.drawString(RealmsPendingInvitesScreen.this.font, pendingInvite.realmName, i + 38, j + 1, -1, false);
			guiGraphics.drawString(RealmsPendingInvitesScreen.this.font, pendingInvite.realmOwnerName, i + 38, j + 12, 7105644, false);
			guiGraphics.drawString(
				RealmsPendingInvitesScreen.this.font, RealmsUtil.convertToAgePresentationFromInstant(pendingInvite.date), i + 38, j + 24, 7105644, false
			);
			RowButton.drawButtonsInRow(guiGraphics, this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, i, j, k, l);
			RealmsUtil.renderPlayerFace(guiGraphics, i, j, 32, pendingInvite.realmOwnerUuid);
		}

		@Override
		public Component getNarration() {
			Component component = CommonComponents.joinLines(
				Component.literal(this.pendingInvite.realmName),
				Component.literal(this.pendingInvite.realmOwnerName),
				RealmsUtil.convertToAgePresentationFromInstant(this.pendingInvite.date)
			);
			return Component.translatable("narrator.select", component);
		}

		@Environment(EnvType.CLIENT)
		class AcceptRowButton extends RowButton {
			AcceptRowButton() {
				super(15, 15, 215, 5);
			}

			@Override
			protected void draw(GuiGraphics guiGraphics, int i, int j, boolean bl) {
				guiGraphics.blitSprite(
					RenderType::guiTextured, bl ? RealmsPendingInvitesScreen.ACCEPT_HIGHLIGHTED_SPRITE : RealmsPendingInvitesScreen.ACCEPT_SPRITE, i, j, 18, 18
				);
				if (bl) {
					RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.ACCEPT_INVITE;
				}
			}

			@Override
			public void onClick(int i) {
				RealmsPendingInvitesScreen.this.handleInvitation(i, true);
			}
		}

		@Environment(EnvType.CLIENT)
		class RejectRowButton extends RowButton {
			RejectRowButton() {
				super(15, 15, 235, 5);
			}

			@Override
			protected void draw(GuiGraphics guiGraphics, int i, int j, boolean bl) {
				guiGraphics.blitSprite(
					RenderType::guiTextured, bl ? RealmsPendingInvitesScreen.REJECT_HIGHLIGHTED_SPRITE : RealmsPendingInvitesScreen.REJECT_SPRITE, i, j, 18, 18
				);
				if (bl) {
					RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.REJECT_INVITE;
				}
			}

			@Override
			public void onClick(int i) {
				RealmsPendingInvitesScreen.this.handleInvitation(i, false);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class PendingInvitationSelectionList extends RealmsObjectSelectionList<RealmsPendingInvitesScreen.Entry> {
		public PendingInvitationSelectionList() {
			super(RealmsPendingInvitesScreen.this.width, RealmsPendingInvitesScreen.this.height - 72, 32, 36);
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
		public void selectItem(int i) {
			super.selectItem(i);
			this.selectInviteListItem(i);
		}

		public void selectInviteListItem(int i) {
			RealmsPendingInvitesScreen.this.selectedInvite = i;
			RealmsPendingInvitesScreen.this.updateButtonStates();
		}

		public void setSelected(@Nullable RealmsPendingInvitesScreen.Entry entry) {
			super.setSelected(entry);
			RealmsPendingInvitesScreen.this.selectedInvite = this.children().indexOf(entry);
			RealmsPendingInvitesScreen.this.updateButtonStates();
		}
	}
}
