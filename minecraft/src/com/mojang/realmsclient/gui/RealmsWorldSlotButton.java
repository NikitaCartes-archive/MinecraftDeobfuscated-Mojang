package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class RealmsWorldSlotButton extends Button {
	private static final ResourceLocation SLOT_FRAME_SPRITE = new ResourceLocation("widget/slot_frame");
	private static final ResourceLocation CHECKMARK_SPRITE = new ResourceLocation("icon/checkmark");
	public static final ResourceLocation EMPTY_SLOT_LOCATION = new ResourceLocation("textures/gui/realms/empty_frame.png");
	public static final ResourceLocation DEFAULT_WORLD_SLOT_1 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png");
	public static final ResourceLocation DEFAULT_WORLD_SLOT_2 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_2.png");
	public static final ResourceLocation DEFAULT_WORLD_SLOT_3 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_3.png");
	private static final Component SLOT_ACTIVE_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.active");
	private static final Component SWITCH_TO_MINIGAME_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.minigame");
	private static final Component SWITCH_TO_WORLD_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip");
	static final Component MINIGAME = Component.translatable("mco.worldSlot.minigame");
	private final int slotIndex;
	@Nullable
	private RealmsWorldSlotButton.State state;

	public RealmsWorldSlotButton(int i, int j, int k, int l, int m, Button.OnPress onPress) {
		super(i, j, k, l, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
		this.slotIndex = m;
	}

	@Nullable
	public RealmsWorldSlotButton.State getState() {
		return this.state;
	}

	public void setServerData(RealmsServer realmsServer) {
		this.state = new RealmsWorldSlotButton.State(realmsServer, this.slotIndex);
		this.setTooltipAndNarration(this.state, realmsServer.minigameName);
	}

	private void setTooltipAndNarration(RealmsWorldSlotButton.State state, @Nullable String string) {
		Component component = switch (state.action) {
			case SWITCH_SLOT -> state.minigame ? SWITCH_TO_MINIGAME_SLOT_TOOLTIP : SWITCH_TO_WORLD_SLOT_TOOLTIP;
			case JOIN -> SLOT_ACTIVE_TOOLTIP;
			default -> null;
		};
		if (component != null) {
			this.setTooltip(Tooltip.create(component));
		}

		MutableComponent mutableComponent = Component.literal(state.slotName);
		if (state.minigame && string != null) {
			mutableComponent = mutableComponent.append(CommonComponents.SPACE).append(string);
		}

		this.setMessage(mutableComponent);
	}

	static RealmsWorldSlotButton.Action getAction(RealmsServer realmsServer, boolean bl, boolean bl2) {
		if (bl && !realmsServer.expired && realmsServer.state != RealmsServer.State.UNINITIALIZED) {
			return RealmsWorldSlotButton.Action.JOIN;
		} else {
			return bl || bl2 && realmsServer.expired ? RealmsWorldSlotButton.Action.NOTHING : RealmsWorldSlotButton.Action.SWITCH_SLOT;
		}
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.state != null) {
			int k = this.getX();
			int l = this.getY();
			boolean bl = this.isHoveredOrFocused();
			ResourceLocation resourceLocation;
			if (this.state.minigame) {
				resourceLocation = RealmsTextureManager.worldTemplate(String.valueOf(this.state.imageId), this.state.image);
			} else if (this.state.empty) {
				resourceLocation = EMPTY_SLOT_LOCATION;
			} else if (this.state.image != null && this.state.imageId != -1L) {
				resourceLocation = RealmsTextureManager.worldTemplate(String.valueOf(this.state.imageId), this.state.image);
			} else if (this.slotIndex == 1) {
				resourceLocation = DEFAULT_WORLD_SLOT_1;
			} else if (this.slotIndex == 2) {
				resourceLocation = DEFAULT_WORLD_SLOT_2;
			} else if (this.slotIndex == 3) {
				resourceLocation = DEFAULT_WORLD_SLOT_3;
			} else {
				resourceLocation = EMPTY_SLOT_LOCATION;
			}

			if (this.state.isCurrentlyActiveSlot) {
				guiGraphics.setColor(0.56F, 0.56F, 0.56F, 1.0F);
			}

			guiGraphics.blit(resourceLocation, k + 3, l + 3, 0.0F, 0.0F, 74, 74, 74, 74);
			boolean bl2 = bl && this.state.action != RealmsWorldSlotButton.Action.NOTHING;
			if (bl2) {
				guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			} else if (this.state.isCurrentlyActiveSlot) {
				guiGraphics.setColor(0.8F, 0.8F, 0.8F, 1.0F);
			} else {
				guiGraphics.setColor(0.56F, 0.56F, 0.56F, 1.0F);
			}

			guiGraphics.blitSprite(SLOT_FRAME_SPRITE, k, l, 80, 80);
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			if (this.state.isCurrentlyActiveSlot) {
				RenderSystem.enableBlend();
				guiGraphics.blitSprite(CHECKMARK_SPRITE, k + 67, l + 4, 9, 8);
				RenderSystem.disableBlend();
			}

			Font font = Minecraft.getInstance().font;
			guiGraphics.drawCenteredString(font, this.state.slotName, k + 40, l + 66, -1);
			guiGraphics.drawCenteredString(
				font, RealmsMainScreen.getVersionComponent(this.state.slotVersion, this.state.compatibility.isCompatible()), k + 40, l + 80 + 2, -1
			);
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Action {
		NOTHING,
		SWITCH_SLOT,
		JOIN;
	}

	@Environment(EnvType.CLIENT)
	public static class State {
		final boolean isCurrentlyActiveSlot;
		final String slotName;
		final String slotVersion;
		final RealmsServer.Compatibility compatibility;
		final long imageId;
		@Nullable
		final String image;
		public final boolean empty;
		public final boolean minigame;
		public final RealmsWorldSlotButton.Action action;

		public State(RealmsServer realmsServer, int i) {
			this.minigame = i == 4;
			if (this.minigame) {
				this.isCurrentlyActiveSlot = realmsServer.isMinigameActive();
				this.slotName = RealmsWorldSlotButton.MINIGAME.getString();
				this.imageId = (long)realmsServer.minigameId;
				this.image = realmsServer.minigameImage;
				this.empty = realmsServer.minigameId == -1;
				this.slotVersion = "";
				this.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
			} else {
				RealmsWorldOptions realmsWorldOptions = (RealmsWorldOptions)realmsServer.slots.get(i);
				this.isCurrentlyActiveSlot = realmsServer.activeSlot == i && !realmsServer.isMinigameActive();
				this.slotName = realmsWorldOptions.getSlotName(i);
				this.imageId = realmsWorldOptions.templateId;
				this.image = realmsWorldOptions.templateImage;
				this.empty = realmsWorldOptions.empty;
				this.slotVersion = realmsWorldOptions.version;
				this.compatibility = realmsWorldOptions.compatibility;
			}

			this.action = RealmsWorldSlotButton.getAction(realmsServer, this.isCurrentlyActiveSlot, this.minigame);
		}
	}
}
