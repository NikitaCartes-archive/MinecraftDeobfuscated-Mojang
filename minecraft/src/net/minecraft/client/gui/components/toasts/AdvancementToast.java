package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class AdvancementToast implements Toast {
	private final Advancement advancement;
	private boolean playedSound;

	public AdvancementToast(Advancement advancement) {
		this.advancement = advancement;
	}

	@Override
	public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		DisplayInfo displayInfo = this.advancement.getDisplay();
		toastComponent.blit(poseStack, 0, 0, 0, 0, this.width(), this.height());
		if (displayInfo != null) {
			List<FormattedCharSequence> list = toastComponent.getMinecraft().font.split(displayInfo.getTitle(), 125);
			int i = displayInfo.getFrame() == FrameType.CHALLENGE ? 16746751 : 16776960;
			if (list.size() == 1) {
				toastComponent.getMinecraft().font.draw(poseStack, displayInfo.getFrame().getDisplayName(), 30.0F, 7.0F, i | 0xFF000000);
				toastComponent.getMinecraft().font.draw(poseStack, (FormattedCharSequence)list.get(0), 30.0F, 18.0F, -1);
			} else {
				int j = 1500;
				float f = 300.0F;
				if (l < 1500L) {
					int k = Mth.floor(Mth.clamp((float)(1500L - l) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
					toastComponent.getMinecraft().font.draw(poseStack, displayInfo.getFrame().getDisplayName(), 30.0F, 11.0F, i | k);
				} else {
					int k = Mth.floor(Mth.clamp((float)(l - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
					int m = this.height() / 2 - list.size() * 9 / 2;

					for (FormattedCharSequence formattedCharSequence : list) {
						toastComponent.getMinecraft().font.draw(poseStack, formattedCharSequence, 30.0F, (float)m, 16777215 | k);
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

			toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(displayInfo.getIcon(), 8, 8);
			return l >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
		} else {
			return Toast.Visibility.HIDE;
		}
	}
}
