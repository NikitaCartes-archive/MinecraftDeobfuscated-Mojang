package net.minecraft.client.gui.components.toasts;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class AdvancementToast implements Toast {
	private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
	public static final int DISPLAY_TIME = 5000;
	private final AdvancementHolder advancement;
	private boolean playedSound;
	private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;

	public AdvancementToast(AdvancementHolder advancementHolder) {
		this.advancement = advancementHolder;
	}

	@Override
	public Toast.Visibility getWantedVisibility() {
		return this.wantedVisibility;
	}

	@Override
	public void update(ToastManager toastManager, long l) {
		DisplayInfo displayInfo = (DisplayInfo)this.advancement.value().display().orElse(null);
		if (displayInfo == null) {
			this.wantedVisibility = Toast.Visibility.HIDE;
		} else {
			if (!this.playedSound && l > 0L) {
				this.playedSound = true;
				if (displayInfo.getType() == AdvancementType.CHALLENGE) {
					toastManager.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
				}
			}

			this.wantedVisibility = (double)l >= 5000.0 * toastManager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, Font font, long l) {
		DisplayInfo displayInfo = (DisplayInfo)this.advancement.value().display().orElse(null);
		guiGraphics.blitSprite(RenderType::guiTextured, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
		if (displayInfo != null) {
			List<FormattedCharSequence> list = font.split(displayInfo.getTitle(), 125);
			int i = displayInfo.getType() == AdvancementType.CHALLENGE ? -30465 : -256;
			if (list.size() == 1) {
				guiGraphics.drawString(font, displayInfo.getType().getDisplayName(), 30, 7, i, false);
				guiGraphics.drawString(font, (FormattedCharSequence)list.get(0), 30, 18, -1, false);
			} else {
				int j = 1500;
				float f = 300.0F;
				if (l < 1500L) {
					int k = Mth.floor(Mth.clamp((float)(1500L - l) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
					guiGraphics.drawString(font, displayInfo.getType().getDisplayName(), 30, 11, i | k, false);
				} else {
					int k = Mth.floor(Mth.clamp((float)(l - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
					int m = this.height() / 2 - list.size() * 9 / 2;

					for (FormattedCharSequence formattedCharSequence : list) {
						guiGraphics.drawString(font, formattedCharSequence, 30, m, 16777215 | k, false);
						m += 9;
					}
				}
			}

			guiGraphics.renderFakeItem(displayInfo.getIcon(), 8, 8);
		}
	}
}
