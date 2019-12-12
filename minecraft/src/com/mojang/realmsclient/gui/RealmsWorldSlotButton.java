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
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsButtonProxy;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsWorldSlotButton extends RealmsButton {
	private final Supplier<RealmsServer> serverDataProvider;
	private final Consumer<String> toolTipSetter;
	private final RealmsWorldSlotButton.Listener listener;
	private final int slotIndex;
	private int animTick;
	@Nullable
	private RealmsWorldSlotButton.State state;

	public RealmsWorldSlotButton(
		int i, int j, int k, int l, Supplier<RealmsServer> supplier, Consumer<String> consumer, int m, int n, RealmsWorldSlotButton.Listener listener
	) {
		super(m, i, j, k, l, "");
		this.serverDataProvider = supplier;
		this.slotIndex = n;
		this.toolTipSetter = consumer;
		this.listener = listener;
	}

	@Override
	public void render(int i, int j, float f) {
		super.render(i, j, f);
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
				bl2 = realmsServer.worldType.equals(RealmsServer.WorldType.MINIGAME);
				string = "Minigame";
				l = (long)realmsServer.minigameId;
				string2 = realmsServer.minigameImage;
				bl3 = realmsServer.minigameId == -1;
			} else {
				bl2 = realmsServer.activeSlot == this.slotIndex && !realmsServer.worldType.equals(RealmsServer.WorldType.MINIGAME);
				string = realmsWorldOptions.getSlotName(this.slotIndex);
				l = realmsWorldOptions.templateId;
				string2 = realmsWorldOptions.templateImage;
				bl3 = realmsWorldOptions.empty;
			}

			String string3 = null;
			RealmsWorldSlotButton.Action action;
			if (bl2) {
				boolean bl4 = realmsServer.state == RealmsServer.State.OPEN || realmsServer.state == RealmsServer.State.CLOSED;
				if (!realmsServer.expired && bl4) {
					action = RealmsWorldSlotButton.Action.JOIN;
					string3 = Realms.getLocalizedString("mco.configure.world.slot.tooltip.active");
				} else {
					action = RealmsWorldSlotButton.Action.NOTHING;
				}
			} else if (bl) {
				if (realmsServer.expired) {
					action = RealmsWorldSlotButton.Action.NOTHING;
				} else {
					action = RealmsWorldSlotButton.Action.SWITCH_SLOT;
					string3 = Realms.getLocalizedString("mco.configure.world.slot.tooltip.minigame");
				}
			} else {
				action = RealmsWorldSlotButton.Action.SWITCH_SLOT;
				string3 = Realms.getLocalizedString("mco.configure.world.slot.tooltip");
			}

			this.state = new RealmsWorldSlotButton.State(bl2, string, l, string2, bl3, bl, action, string3);
			String string4;
			if (action == RealmsWorldSlotButton.Action.NOTHING) {
				string4 = string;
			} else if (bl) {
				if (bl3) {
					string4 = string3;
				} else {
					string4 = string3 + " " + string + " " + realmsServer.minigameName;
				}
			} else {
				string4 = string3 + " " + string;
			}

			this.setMessage(string4);
		}
	}

	@Override
	public void renderButton(int i, int j, float f) {
		if (this.state != null) {
			RealmsButtonProxy realmsButtonProxy = this.getProxy();
			this.drawSlotFrame(
				realmsButtonProxy.x,
				realmsButtonProxy.y,
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
		boolean bl4 = this.getProxy().isHovered();
		if (this.getProxy().isMouseOver((double)k, (double)l) && string3 != null) {
			this.toolTipSetter.accept(string3);
		}

		if (bl3) {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
		} else if (bl2) {
			Realms.bind("realms:textures/gui/realms/empty_frame.png");
		} else if (string2 != null && n != -1L) {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
		} else if (m == 1) {
			Realms.bind("textures/gui/title/background/panorama_0.png");
		} else if (m == 2) {
			Realms.bind("textures/gui/title/background/panorama_2.png");
		} else if (m == 3) {
			Realms.bind("textures/gui/title/background/panorama_3.png");
		}

		if (bl) {
			float f = 0.85F + 0.15F * RealmsMth.cos((float)this.animTick * 0.2F);
			RenderSystem.color4f(f, f, f, 1.0F);
		} else {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		}

		RealmsScreen.blit(i + 3, j + 3, 0.0F, 0.0F, 74, 74, 74, 74);
		Realms.bind("realms:textures/gui/realms/slot_frame.png");
		boolean bl5 = bl4 && action != RealmsWorldSlotButton.Action.NOTHING;
		if (bl5) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		} else if (bl) {
			RenderSystem.color4f(0.8F, 0.8F, 0.8F, 1.0F);
		} else {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		}

		RealmsScreen.blit(i, j, 0.0F, 0.0F, 80, 80, 80, 80);
		this.drawCenteredString(string, i + 40, j + 66, 16777215);
	}

	@Override
	public void onPress() {
		if (this.state != null) {
			this.listener.onSlotClick(this.slotIndex, this.state.action, this.state.minigame, this.state.empty);
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Action {
		NOTHING,
		SWITCH_SLOT,
		JOIN;
	}

	@Environment(EnvType.CLIENT)
	public interface Listener {
		void onSlotClick(int i, RealmsWorldSlotButton.Action action, boolean bl, boolean bl2);
	}

	@Environment(EnvType.CLIENT)
	public static class State {
		final boolean isCurrentlyActiveSlot;
		final String slotName;
		final long imageId;
		public final String image;
		public final boolean empty;
		final boolean minigame;
		public final RealmsWorldSlotButton.Action action;
		final String actionPrompt;

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
