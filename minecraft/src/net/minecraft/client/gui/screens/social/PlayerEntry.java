package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class PlayerEntry extends ContainerObjectSelectionList.Entry<PlayerEntry> {
	private final Minecraft minecraft;
	private final List<GuiEventListener> children;
	private final UUID id;
	private final String playerName;
	private final ResourceLocation skin;
	private boolean isRemoved;
	@Nullable
	private Button hideButton;
	@Nullable
	private Button showButton;
	private final List<FormattedCharSequence> hideTooltip;
	private final List<FormattedCharSequence> showTooltip;
	private float tooltipHoverTime;
	public static final int SKIN_SHADE = FastColor.ARGB32.color(190, 0, 0, 0);
	public static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
	public static final int BG_FILL_REMOVED = FastColor.ARGB32.color(255, 48, 48, 48);
	public static final int PLAYERNAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);

	public PlayerEntry(
		Minecraft minecraft,
		SocialInteractionsScreen socialInteractionsScreen,
		UUID uUID,
		String string,
		ResourceLocation resourceLocation,
		SocialInteractionsScreen.Page page
	) {
		this.minecraft = minecraft;
		this.id = uUID;
		this.playerName = string;
		this.skin = resourceLocation;
		this.hideTooltip = minecraft.font.split(new TranslatableComponent("gui.socialInteractions.tooltip.hide", string), 150);
		this.showTooltip = minecraft.font.split(new TranslatableComponent("gui.socialInteractions.tooltip.show", string), 150);
		if (!minecraft.player.getGameProfile().getId().equals(uUID)) {
			this.hideButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, button -> {
				minecraft.getPlayerSocialManager().hidePlayer(uUID);
				this.onHiddenOrShown(true, new TranslatableComponent("gui.socialInteractions.hidden_in_chat", string));
				socialInteractionsScreen.onHiddenOrShown();
			}, (button, poseStack, i, j) -> {
				this.tooltipHoverTime = this.tooltipHoverTime + minecraft.getDeltaFrameTime();
				if (this.tooltipHoverTime >= 20.0F) {
					socialInteractionsScreen.setPostRenderRunnable(() -> postRenderTooltip(socialInteractionsScreen, poseStack, this.hideTooltip, i, j));
				}
			}, new TranslatableComponent("gui.socialInteractions.hide", string));
			this.showButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, button -> {
				minecraft.getPlayerSocialManager().showPlayer(uUID);
				this.onHiddenOrShown(false, new TranslatableComponent("gui.socialInteractions.shown_in_chat", string));
				socialInteractionsScreen.onHiddenOrShown();
			}, (button, poseStack, i, j) -> {
				this.tooltipHoverTime = this.tooltipHoverTime + minecraft.getDeltaFrameTime();
				if (this.tooltipHoverTime >= 20.0F) {
					socialInteractionsScreen.setPostRenderRunnable(() -> postRenderTooltip(socialInteractionsScreen, poseStack, this.showTooltip, i, j));
				}
			}, new TranslatableComponent("gui.socialInteractions.show", string));
			this.showButton.visible = minecraft.getPlayerSocialManager().isHidden(uUID);
			this.hideButton.visible = !this.showButton.visible;
			this.children = ImmutableList.of(this.hideButton, this.showButton);
		} else {
			this.children = ImmutableList.of();
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
		GuiComponent.fill(poseStack, k, j, k + l, j + m, this.isRemoved ? BG_FILL_REMOVED : BG_FILL);
		int p = k + 4;
		int q = j + (m - 24) / 2;
		this.minecraft.getTextureManager().bind(this.skin);
		GuiComponent.blit(poseStack, p, q, 24, 24, 8.0F, 8.0F, 8, 8, 64, 64);
		RenderSystem.enableBlend();
		GuiComponent.blit(poseStack, p, q, 24, 24, 40.0F, 8.0F, 8, 8, 64, 64);
		RenderSystem.disableBlend();
		boolean bl2 = this.minecraft.getPlayerSocialManager().isHidden(this.id);
		if (this.isRemoved || bl2) {
			GuiComponent.fill(poseStack, p, q, p + 24, q + 24, SKIN_SHADE);
			if (bl2) {
				this.minecraft.getTextureManager().bind(SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION);
				RenderSystem.enableBlend();
				GuiComponent.blit(poseStack, p + 5, q + 8, 14, 14, 241.0F, 37.0F, 14, 14, 256, 256);
				RenderSystem.disableBlend();
			}
		}

		int r = p + 24 + 4;
		int s = j + (m - (9 + 9)) / 2;
		this.minecraft.font.draw(poseStack, this.playerName, (float)r, (float)s, PLAYERNAME_COLOR);
		if (this.hideButton != null && this.showButton != null) {
			float g = this.tooltipHoverTime;
			this.hideButton.x = k + (l - this.hideButton.getWidth() - 4);
			this.hideButton.y = j + (m - this.hideButton.getHeight()) / 2;
			this.hideButton.render(poseStack, n, o, f);
			this.showButton.x = k + (l - this.showButton.getWidth() - 4);
			this.showButton.y = j + (m - this.showButton.getHeight()) / 2;
			this.showButton.render(poseStack, n, o, f);
			if (g == this.tooltipHoverTime) {
				this.tooltipHoverTime = 0.0F;
			}
		}
	}

	@Override
	public List<? extends GuiEventListener> children() {
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

	private void onHiddenOrShown(boolean bl, Component component) {
		this.showButton.visible = bl;
		this.hideButton.visible = !bl;
		this.minecraft.gui.getChat().addMessage(component);
		NarratorChatListener.INSTANCE.sayNow(component.getString());
	}

	private static void postRenderTooltip(SocialInteractionsScreen socialInteractionsScreen, PoseStack poseStack, List<FormattedCharSequence> list, int i, int j) {
		socialInteractionsScreen.renderTooltip(poseStack, list, i, j);
		socialInteractionsScreen.setPostRenderRunnable(null);
	}
}
