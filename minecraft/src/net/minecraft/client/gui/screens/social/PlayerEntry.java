package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

@Environment(EnvType.CLIENT)
public class PlayerEntry extends ContainerObjectSelectionList.Entry<PlayerEntry> {
	private static final ResourceLocation REPORT_BUTTON_LOCATION = new ResourceLocation("textures/gui/report_button.png");
	private static final int TOOLTIP_DELAY = 10;
	private final Minecraft minecraft;
	private final List<AbstractWidget> children;
	private final UUID id;
	private final String playerName;
	private final Supplier<ResourceLocation> skinGetter;
	private boolean isRemoved;
	private boolean hasRecentMessages;
	private final boolean reportingEnabled;
	private final boolean playerReportable;
	private final boolean hasDraftReport;
	@Nullable
	private Button hideButton;
	@Nullable
	private Button showButton;
	@Nullable
	private Button reportButton;
	private float tooltipHoverTime;
	private static final Component HIDDEN = Component.translatable("gui.socialInteractions.status_hidden").withStyle(ChatFormatting.ITALIC);
	private static final Component BLOCKED = Component.translatable("gui.socialInteractions.status_blocked").withStyle(ChatFormatting.ITALIC);
	private static final Component OFFLINE = Component.translatable("gui.socialInteractions.status_offline").withStyle(ChatFormatting.ITALIC);
	private static final Component HIDDEN_OFFLINE = Component.translatable("gui.socialInteractions.status_hidden_offline").withStyle(ChatFormatting.ITALIC);
	private static final Component BLOCKED_OFFLINE = Component.translatable("gui.socialInteractions.status_blocked_offline").withStyle(ChatFormatting.ITALIC);
	private static final Component REPORT_DISABLED_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.disabled");
	private static final Component NOT_REPORTABLE_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.not_reportable");
	private static final Component HIDE_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.hide");
	private static final Component SHOW_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.show");
	private static final Component REPORT_PLAYER_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report");
	private static final int SKIN_SIZE = 24;
	private static final int PADDING = 4;
	private static final int CHAT_TOGGLE_ICON_SIZE = 20;
	private static final int CHAT_TOGGLE_ICON_X = 0;
	private static final int CHAT_TOGGLE_ICON_Y = 38;
	public static final int SKIN_SHADE = FastColor.ARGB32.color(190, 0, 0, 0);
	public static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
	public static final int BG_FILL_REMOVED = FastColor.ARGB32.color(255, 48, 48, 48);
	public static final int PLAYERNAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);
	public static final int PLAYER_STATUS_COLOR = FastColor.ARGB32.color(140, 255, 255, 255);

	public PlayerEntry(
		Minecraft minecraft, SocialInteractionsScreen socialInteractionsScreen, UUID uUID, String string, Supplier<ResourceLocation> supplier, boolean bl
	) {
		this.minecraft = minecraft;
		this.id = uUID;
		this.playerName = string;
		this.skinGetter = supplier;
		ReportingContext reportingContext = minecraft.getReportingContext();
		this.reportingEnabled = reportingContext.sender().isEnabled();
		this.playerReportable = bl;
		this.hasDraftReport = reportingContext.hasDraftReportFor(uUID);
		Component component = Component.translatable("gui.socialInteractions.narration.hide", string);
		Component component2 = Component.translatable("gui.socialInteractions.narration.show", string);
		PlayerSocialManager playerSocialManager = minecraft.getPlayerSocialManager();
		boolean bl2 = minecraft.getChatStatus().isChatAllowed(minecraft.isLocalServer());
		boolean bl3 = !minecraft.player.getUUID().equals(uUID);
		if (bl3 && bl2 && !playerSocialManager.isBlocked(uUID)) {
			this.reportButton = new ImageButton(0, 0, 20, 20, 0, 0, 20, REPORT_BUTTON_LOCATION, 64, 64, button -> {
				if (reportingContext.draftReportHandled(minecraft, socialInteractionsScreen, false)) {
					minecraft.setScreen(new ChatReportScreen(socialInteractionsScreen, reportingContext, uUID));
				}
			}, Component.translatable("gui.socialInteractions.report")) {
				@Override
				protected MutableComponent createNarrationMessage() {
					return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
				}
			};
			this.reportButton.setTooltip(Tooltip.create(this.getReportButtonText(false), this.getReportButtonText(true)));
			this.reportButton.setTooltipDelay(10);
			this.hideButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, button -> {
				playerSocialManager.hidePlayer(uUID);
				this.onHiddenOrShown(true, Component.translatable("gui.socialInteractions.hidden_in_chat", string));
			}, Component.translatable("gui.socialInteractions.hide")) {
				@Override
				protected MutableComponent createNarrationMessage() {
					return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
				}
			};
			this.hideButton.setTooltip(Tooltip.create(HIDE_TEXT_TOOLTIP, component));
			this.hideButton.setTooltipDelay(10);
			this.showButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, button -> {
				playerSocialManager.showPlayer(uUID);
				this.onHiddenOrShown(false, Component.translatable("gui.socialInteractions.shown_in_chat", string));
			}, Component.translatable("gui.socialInteractions.show")) {
				@Override
				protected MutableComponent createNarrationMessage() {
					return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
				}
			};
			this.showButton.setTooltip(Tooltip.create(SHOW_TEXT_TOOLTIP, component2));
			this.showButton.setTooltipDelay(10);
			this.showButton.visible = playerSocialManager.isHidden(uUID);
			this.hideButton.visible = !this.showButton.visible;
			this.reportButton.active = false;
			this.children = ImmutableList.of(this.hideButton, this.showButton, this.reportButton);
		} else {
			this.children = ImmutableList.of();
		}
	}

	private Component getReportButtonText(boolean bl) {
		if (!this.playerReportable) {
			return NOT_REPORTABLE_TOOLTIP;
		} else if (!this.reportingEnabled) {
			return REPORT_DISABLED_TOOLTIP;
		} else if (!this.hasRecentMessages) {
			return Component.translatable("gui.socialInteractions.tooltip.report.no_messages", this.playerName);
		} else {
			return (Component)(bl ? Component.translatable("gui.socialInteractions.narration.report", this.playerName) : REPORT_PLAYER_TOOLTIP);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
		int p = k + 4;
		int q = j + (m - 24) / 2;
		int r = p + 24 + 4;
		Component component = this.getStatusComponent();
		int s;
		if (component == CommonComponents.EMPTY) {
			GuiComponent.fill(poseStack, k, j, k + l, j + m, BG_FILL);
			s = j + (m - 9) / 2;
		} else {
			GuiComponent.fill(poseStack, k, j, k + l, j + m, BG_FILL_REMOVED);
			s = j + (m - (9 + 9)) / 2;
			this.minecraft.font.draw(poseStack, component, (float)r, (float)(s + 12), PLAYER_STATUS_COLOR);
		}

		RenderSystem.setShaderTexture(0, (ResourceLocation)this.skinGetter.get());
		PlayerFaceRenderer.draw(poseStack, p, q, 24);
		this.minecraft.font.draw(poseStack, this.playerName, (float)r, (float)s, PLAYERNAME_COLOR);
		if (this.isRemoved) {
			GuiComponent.fill(poseStack, p, q, p + 24, q + 24, SKIN_SHADE);
		}

		if (this.hideButton != null && this.showButton != null && this.reportButton != null) {
			float g = this.tooltipHoverTime;
			this.hideButton.setX(k + (l - this.hideButton.getWidth() - 4) - 20 - 4);
			this.hideButton.setY(j + (m - this.hideButton.getHeight()) / 2);
			this.hideButton.render(poseStack, n, o, f);
			this.showButton.setX(k + (l - this.showButton.getWidth() - 4) - 20 - 4);
			this.showButton.setY(j + (m - this.showButton.getHeight()) / 2);
			this.showButton.render(poseStack, n, o, f);
			this.reportButton.setX(k + (l - this.showButton.getWidth() - 4));
			this.reportButton.setY(j + (m - this.showButton.getHeight()) / 2);
			this.reportButton.render(poseStack, n, o, f);
			if (g == this.tooltipHoverTime) {
				this.tooltipHoverTime = 0.0F;
			}
		}

		if (this.hasDraftReport && this.reportButton != null) {
			RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(poseStack, this.reportButton.getX() + 5, this.reportButton.getY() + 1, 182.0F, 24.0F, 15, 15, 256, 256);
		}
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return this.children;
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return this.children;
	}

	public String getPlayerName() {
		return this.playerName;
	}

	public UUID getPlayerId() {
		return this.id;
	}

	public void setRemoved(boolean bl) {
		this.isRemoved = bl;
	}

	public boolean isRemoved() {
		return this.isRemoved;
	}

	public void setHasRecentMessages(boolean bl) {
		this.hasRecentMessages = bl;
		if (this.reportButton != null) {
			this.reportButton.active = this.reportingEnabled && this.playerReportable && bl;
		}
	}

	public boolean hasRecentMessages() {
		return this.hasRecentMessages;
	}

	private void onHiddenOrShown(boolean bl, Component component) {
		this.showButton.visible = bl;
		this.hideButton.visible = !bl;
		this.minecraft.gui.getChat().addMessage(component);
		this.minecraft.getNarrator().sayNow(component);
	}

	MutableComponent getEntryNarationMessage(MutableComponent mutableComponent) {
		Component component = this.getStatusComponent();
		return component == CommonComponents.EMPTY
			? Component.literal(this.playerName).append(", ").append(mutableComponent)
			: Component.literal(this.playerName).append(", ").append(component).append(", ").append(mutableComponent);
	}

	private Component getStatusComponent() {
		boolean bl = this.minecraft.getPlayerSocialManager().isHidden(this.id);
		boolean bl2 = this.minecraft.getPlayerSocialManager().isBlocked(this.id);
		if (bl2 && this.isRemoved) {
			return BLOCKED_OFFLINE;
		} else if (bl && this.isRemoved) {
			return HIDDEN_OFFLINE;
		} else if (bl2) {
			return BLOCKED;
		} else if (bl) {
			return HIDDEN;
		} else {
			return this.isRemoved ? OFFLINE : CommonComponents.EMPTY;
		}
	}
}
