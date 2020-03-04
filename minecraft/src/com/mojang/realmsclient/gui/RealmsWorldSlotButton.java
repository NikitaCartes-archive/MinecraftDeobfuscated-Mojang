package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class RealmsWorldSlotButton extends Button implements TickableWidget {
	public static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
	public static final ResourceLocation EMPTY_SLOT_LOCATION = new ResourceLocation("realms", "textures/gui/realms/empty_frame.png");
	public static final ResourceLocation DEFAULT_WORLD_SLOT_1 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png");
	public static final ResourceLocation DEFAULT_WORLD_SLOT_2 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_2.png");
	public static final ResourceLocation DEFAULT_WORLD_SLOT_3 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_3.png");
	private final Supplier<RealmsServer> serverDataProvider;
	private final Consumer<String> toolTipSetter;
	private final int slotIndex;
	private int animTick;
	@Nullable
	private RealmsWorldSlotButton.State state;

	public RealmsWorldSlotButton(int i, int j, int k, int l, Supplier<RealmsServer> supplier, Consumer<String> consumer, int m, Button.OnPress onPress) {
		super(i, j, k, l, "", onPress);
		this.serverDataProvider = supplier;
		this.slotIndex = m;
		this.toolTipSetter = consumer;
	}

	@Nullable
	public RealmsWorldSlotButton.State getState() {
		return this.state;
	}

	@Override
	public void tick() {
		this.animTick++;
		RealmsServer realmsServer = (RealmsServer)this.serverDataProvider.get();
		if (realmsServer != null) {
			RealmsWorldOptions realmsWorldOptions = (RealmsWorldOptions)realmsServer.slots.get(this.slotIndex);
			boolean bl = this.slotIndex == 4;
			boolean bl2;
			String string;
			long l;
			String string2;
			boolean bl3;
			if (bl) {
				bl2 = realmsServer.worldType == RealmsServer.WorldType.MINIGAME;
				string = "Minigame";
				l = (long)realmsServer.minigameId;
				string2 = realmsServer.minigameImage;
				bl3 = realmsServer.minigameId == -1;
			} else {
				bl2 = realmsServer.activeSlot == this.slotIndex && realmsServer.worldType != RealmsServer.WorldType.MINIGAME;
				string = realmsWorldOptions.getSlotName(this.slotIndex);
				l = realmsWorldOptions.templateId;
				string2 = realmsWorldOptions.templateImage;
				bl3 = realmsWorldOptions.empty;
			}

			RealmsWorldSlotButton.Action action = RealmsWorldSlotButton.Action.NOTHING;
			String string3 = null;
			if (bl2) {
				if (!realmsServer.expired && realmsServer.state != RealmsServer.State.UNINITIALIZED) {
					action = RealmsWorldSlotButton.Action.JOIN;
					string3 = I18n.get("mco.configure.world.slot.tooltip.active");
				}
			} else if (bl) {
				if (!realmsServer.expired) {
					action = RealmsWorldSlotButton.Action.SWITCH_SLOT;
					string3 = I18n.get("mco.configure.world.slot.tooltip.minigame");
				}
			} else {
				action = RealmsWorldSlotButton.Action.SWITCH_SLOT;
				string3 = I18n.get("mco.configure.world.slot.tooltip");
			}

			this.state = new RealmsWorldSlotButton.State(bl2, string, l, string2, bl3, bl, action, string3);
			this.handleNarration(realmsServer, this.state.slotName, this.state.empty, this.state.minigame, this.state.action, this.state.actionPrompt);
		}
	}

	public void handleNarration(RealmsServer realmsServer, String string, boolean bl, boolean bl2, RealmsWorldSlotButton.Action action, String string2) {
		String string3;
		if (action == RealmsWorldSlotButton.Action.NOTHING) {
			string3 = string;
		} else if (bl2) {
			if (bl) {
				string3 = string2;
			} else {
				string3 = string2 + " " + string + " " + realmsServer.minigameName;
			}
		} else {
			string3 = string2 + " " + string;
		}

		this.setMessage(string3);
	}

	@Override
	public void renderButton(int i, int j, float f) {
		if (this.state != null) {
			this.drawSlotFrame(
				this.x,
				this.y,
				i,
				j,
				this.state.isCurrentlyActiveSlot,
				this.state.slotName,
				this.slotIndex,
				this.state.imageId,
				this.state.image,
				this.state.empty,
				this.state.minigame,
				this.state.action,
				this.state.actionPrompt
			);
		}
	}

	private void drawSlotFrame(
		int i,
		int j,
		int k,
		int l,
		boolean bl,
		String string,
		int m,
		long n,
		@Nullable String string2,
		boolean bl2,
		boolean bl3,
		RealmsWorldSlotButton.Action action,
		@Nullable String string3
	) {
		boolean bl4 = this.isHovered();
		if (this.isMouseOver((double)k, (double)l) && string3 != null) {
			this.toolTipSetter.accept(string3);
		}

		Minecraft minecraft = Minecraft.getInstance();
		TextureManager textureManager = minecraft.getTextureManager();
		if (bl3) {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
		} else if (bl2) {
			textureManager.bind(EMPTY_SLOT_LOCATION);
		} else if (string2 != null && n != -1L) {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
		} else if (m == 1) {
			textureManager.bind(DEFAULT_WORLD_SLOT_1);
		} else if (m == 2) {
			textureManager.bind(DEFAULT_WORLD_SLOT_2);
		} else if (m == 3) {
			textureManager.bind(DEFAULT_WORLD_SLOT_3);
		}

		if (bl) {
			float f = 0.85F + 0.15F * Mth.cos((float)this.animTick * 0.2F);
			RenderSystem.color4f(f, f, f, 1.0F);
		} else {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		}

		blit(i + 3, j + 3, 0.0F, 0.0F, 74, 74, 74, 74);
		textureManager.bind(SLOT_FRAME_LOCATION);
		boolean bl5 = bl4 && action != RealmsWorldSlotButton.Action.NOTHING;
		if (bl5) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		} else if (bl) {
			RenderSystem.color4f(0.8F, 0.8F, 0.8F, 1.0F);
		} else {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		}

		blit(i, j, 0.0F, 0.0F, 80, 80, 80, 80);
		this.drawCenteredString(minecraft.font, string, i + 40, j + 66, 16777215);
	}

	@Environment(EnvType.CLIENT)
	public static enum Action {
		NOTHING,
		SWITCH_SLOT,
		JOIN;
	}

	@Environment(EnvType.CLIENT)
	public static class State {
		private final boolean isCurrentlyActiveSlot;
		private final String slotName;
		private final long imageId;
		private final String image;
		public final boolean empty;
		public final boolean minigame;
		public final RealmsWorldSlotButton.Action action;
		private final String actionPrompt;

		State(boolean bl, String string, long l, @Nullable String string2, boolean bl2, boolean bl3, RealmsWorldSlotButton.Action action, @Nullable String string3) {
			this.isCurrentlyActiveSlot = bl;
			this.slotName = string;
			this.imageId = l;
			this.image = string2;
			this.empty = bl2;
			this.minigame = bl3;
			this.action = action;
			this.actionPrompt = string3;
		}
	}
}
