package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsPendingInvitesScreen extends RealmsScreen {
	static final Logger LOGGER = LogUtils.getLogger();
	static final ResourceLocation ACCEPT_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/accept_icon.png");
	static final ResourceLocation REJECT_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/reject_icon.png");
	private static final Component NO_PENDING_INVITES_TEXT = Component.translatable("mco.invites.nopending");
	static final Component ACCEPT_INVITE_TOOLTIP = Component.translatable("mco.invites.button.accept");
	static final Component REJECT_INVITE_TOOLTIP = Component.translatable("mco.invites.button.reject");
	private final Screen lastScreen;
	@Nullable
	Component toolTip;
	boolean loaded;
	RealmsPendingInvitesScreen.PendingInvitationSelectionList pendingInvitationSelectionList;
	int selectedInvite = -1;
	private Button acceptButton;
	private Button rejectButton;

	public RealmsPendingInvitesScreen(Screen screen) {
		super(Component.translatable("mco.invites.title"));
		this.lastScreen = screen;
	}

	@Override
	public void init() {
		this.pendingInvitationSelectionList = new RealmsPendingInvitesScreen.PendingInvitationSelectionList();
		(new Thread("Realms-pending-invitations-fetcher") {
				public void run() {
					RealmsClient realmsClient = RealmsClient.create();

					try {
						List<PendingInvite> list = realmsClient.pendingInvites().pendingInvites;
						List<RealmsPendingInvitesScreen.Entry> list2 = (List<RealmsPendingInvitesScreen.Entry>)list.stream()
							.map(pendingInvite -> RealmsPendingInvitesScreen.this.new Entry(pendingInvite))
							.collect(Collectors.toList());
						RealmsPendingInvitesScreen.this.minecraft.execute(() -> RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.replaceEntries(list2));
					} catch (RealmsServiceException var7) {
						RealmsPendingInvitesScreen.LOGGER.error("Couldn't list invites");
					} finally {
						RealmsPendingInvitesScreen.this.loaded = true;
					}
				}
			})
			.start();
		this.addWidget(this.pendingInvitationSelectionList);
		this.acceptButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.invites.button.accept"), button -> {
			this.accept(this.selectedInvite);
			this.selectedInvite = -1;
			this.updateButtonStates();
		}).bounds(this.width / 2 - 174, this.height - 32, 100, 20).build());
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(new RealmsMainScreen(this.lastScreen)))
				.bounds(this.width / 2 - 50, this.height - 32, 100, 20)
				.build()
		);
		this.rejectButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.invites.button.reject"), button -> {
			this.reject(this.selectedInvite);
			this.selectedInvite = -1;
			this.updateButtonStates();
		}).bounds(this.width / 2 + 74, this.height - 32, 100, 20).build());
		this.updateButtonStates();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.setScreen(new RealmsMainScreen(this.lastScreen));
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	void updateList(int i) {
		this.pendingInvitationSelectionList.removeAtIndex(i);
	}

	void reject(int i) {
		if (i < this.pendingInvitationSelectionList.getItemCount()) {
			(new Thread("Realms-reject-invitation") {
					public void run() {
						try {
							RealmsClient realmsClient = RealmsClient.create();
							realmsClient.rejectInvitation(
								((RealmsPendingInvitesScreen.Entry)RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(i)).pendingInvite.invitationId
							);
							RealmsPendingInvitesScreen.this.minecraft.execute(() -> RealmsPendingInvitesScreen.this.updateList(i));
						} catch (RealmsServiceException var2) {
							RealmsPendingInvitesScreen.LOGGER.error("Couldn't reject invite");
						}
					}
				})
				.start();
		}
	}

	void accept(int i) {
		if (i < this.pendingInvitationSelectionList.getItemCount()) {
			(new Thread("Realms-accept-invitation") {
					public void run() {
						try {
							RealmsClient realmsClient = RealmsClient.create();
							realmsClient.acceptInvitation(
								((RealmsPendingInvitesScreen.Entry)RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(i)).pendingInvite.invitationId
							);
							RealmsPendingInvitesScreen.this.minecraft.execute(() -> RealmsPendingInvitesScreen.this.updateList(i));
						} catch (RealmsServiceException var2) {
							RealmsPendingInvitesScreen.LOGGER.error("Couldn't accept invite");
						}
					}
				})
				.start();
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.toolTip = null;
		this.renderBackground(poseStack);
		this.pendingInvitationSelectionList.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 12, 16777215);
		if (this.toolTip != null) {
			this.renderMousehoverTooltip(poseStack, this.toolTip, i, j);
		}

		if (this.pendingInvitationSelectionList.getItemCount() == 0 && this.loaded) {
			drawCenteredString(poseStack, this.font, NO_PENDING_INVITES_TEXT, this.width / 2, this.height / 2 - 20, 16777215);
		}

		super.render(poseStack, i, j, f);
	}

	protected void renderMousehoverTooltip(PoseStack poseStack, @Nullable Component component, int i, int j) {
		if (component != null) {
			int k = i + 12;
			int l = j - 12;
			int m = this.font.width(component);
			this.fillGradient(poseStack, k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
			this.font.drawShadow(poseStack, component, (float)k, (float)l, 16777215);
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

		Entry(PendingInvite pendingInvite) {
			this.pendingInvite = pendingInvite;
			this.rowButtons = Arrays.asList(new RealmsPendingInvitesScreen.Entry.AcceptRowButton(), new RealmsPendingInvitesScreen.Entry.RejectRowButton());
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderPendingInvitationItem(poseStack, this.pendingInvite, k, j, n, o);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			RowButton.rowButtonMouseClicked(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, this, this.rowButtons, i, d, e);
			return true;
		}

		private void renderPendingInvitationItem(PoseStack poseStack, PendingInvite pendingInvite, int i, int j, int k, int l) {
			RealmsPendingInvitesScreen.this.font.draw(poseStack, pendingInvite.worldName, (float)(i + 38), (float)(j + 1), 16777215);
			RealmsPendingInvitesScreen.this.font.draw(poseStack, pendingInvite.worldOwnerName, (float)(i + 38), (float)(j + 12), 7105644);
			RealmsPendingInvitesScreen.this.font
				.draw(poseStack, RealmsUtil.convertToAgePresentationFromInstant(pendingInvite.date), (float)(i + 38), (float)(j + 24), 7105644);
			RowButton.drawButtonsInRow(poseStack, this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, i, j, k, l);
			RealmsTextureManager.withBoundFace(pendingInvite.worldOwnerUuid, () -> PlayerFaceRenderer.draw(poseStack, i, j, 32));
		}

		@Override
		public Component getNarration() {
			Component component = CommonComponents.joinLines(
				Component.literal(this.pendingInvite.worldName),
				Component.literal(this.pendingInvite.worldOwnerName),
				Component.literal(RealmsUtil.convertToAgePresentationFromInstant(this.pendingInvite.date))
			);
			return Component.translatable("narrator.select", component);
		}

		@Environment(EnvType.CLIENT)
		class AcceptRowButton extends RowButton {
			AcceptRowButton() {
				super(15, 15, 215, 5);
			}

			@Override
			protected void draw(PoseStack poseStack, int i, int j, boolean bl) {
				RenderSystem.setShaderTexture(0, RealmsPendingInvitesScreen.ACCEPT_ICON_LOCATION);
				float f = bl ? 19.0F : 0.0F;
				GuiComponent.blit(poseStack, i, j, f, 0.0F, 18, 18, 37, 18);
				if (bl) {
					RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.ACCEPT_INVITE_TOOLTIP;
				}
			}

			@Override
			public void onClick(int i) {
				RealmsPendingInvitesScreen.this.accept(i);
			}
		}

		@Environment(EnvType.CLIENT)
		class RejectRowButton extends RowButton {
			RejectRowButton() {
				super(15, 15, 235, 5);
			}

			@Override
			protected void draw(PoseStack poseStack, int i, int j, boolean bl) {
				RenderSystem.setShaderTexture(0, RealmsPendingInvitesScreen.REJECT_ICON_LOCATION);
				float f = bl ? 19.0F : 0.0F;
				GuiComponent.blit(poseStack, i, j, f, 0.0F, 18, 18, 37, 18);
				if (bl) {
					RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.REJECT_INVITE_TOOLTIP;
				}
			}

			@Override
			public void onClick(int i) {
				RealmsPendingInvitesScreen.this.reject(i);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class PendingInvitationSelectionList extends RealmsObjectSelectionList<RealmsPendingInvitesScreen.Entry> {
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
		public void renderBackground(PoseStack poseStack) {
			RealmsPendingInvitesScreen.this.renderBackground(poseStack);
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
