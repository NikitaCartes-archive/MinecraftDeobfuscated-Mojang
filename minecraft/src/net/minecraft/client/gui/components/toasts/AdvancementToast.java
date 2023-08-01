package net.minecraft.client.gui.components.toasts;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class AdvancementToast implements Toast {
	private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("toast/advancement");
	public static final int DISPLAY_TIME = 5000;
	private final Advancement advancement;
	private boolean playedSound;

	public AdvancementToast(Advancement advancement) {
		this.advancement = advancement;
	}

	@Override
	public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l) {
		DisplayInfo displayInfo = this.advancement.getDisplay();
		guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
		if (displayInfo != null) {
			List<FormattedCharSequence> list = toastComponent.getMinecraft().font.split(displayInfo.getTitle(), 125);
			int i = displayInfo.getFrame() == FrameType.CHALLENGE ? 16746751 : 16776960;
			if (list.size() == 1) {
				guiGraphics.drawString(toastComponent.getMinecraft().font, displayInfo.getFrame().getDisplayName(), 30, 7, i | 0xFF000000, false);
				guiGraphics.drawString(toastComponent.getMinecraft().font, (FormattedCharSequence)list.get(0), 30, 18, -1, false);
			} else {
				int j = 1500;
				float f = 300.0F;
				if (l < 1500L) {
					int k = Mth.floor(Mth.clamp((float)(1500L - l) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
					guiGraphics.drawString(toastComponent.getMinecraft().font, displayInfo.getFrame().getDisplayName(), 30, 11, i | k, false);
				} else {
					int k = Mth.floor(Mth.clamp((float)(l - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
					int m = this.height() / 2 - list.size() * 9 / 2;

					for (FormattedCharSequence formattedCharSequence : list) {
						guiGraphics.drawString(toastComponent.getMinecraft().font, formattedCharSequence, 30, m, 16777215 | k, false);
						m += 9;
					}
				}
			}

			if (!this.playedSound && l > 0L) {
				this.playedSound = true;
				if (displayInfo.getFrame() == FrameType.CHALLENGE) {
					toastComponent.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
				}
			}

			guiGraphics.renderFakeItem(displayInfo.getIcon(), 8, 8);
			return (double)l >= 5000.0 * toastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
		} else {
			return Toast.Visibility.HIDE;
		}
	}
}
