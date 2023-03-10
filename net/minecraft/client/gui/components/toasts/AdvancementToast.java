/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class AdvancementToast
implements Toast {
    public static final int DISPLAY_TIME = 5000;
    private final Advancement advancement;
    private boolean playedSound;

    public AdvancementToast(Advancement advancement) {
        this.advancement = advancement;
    }

    @Override
    public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        DisplayInfo displayInfo = this.advancement.getDisplay();
        GuiComponent.blit(poseStack, 0, 0, 0, 0, this.width(), this.height());
        if (displayInfo != null) {
            int i;
            List<FormattedCharSequence> list = toastComponent.getMinecraft().font.split(displayInfo.getTitle(), 125);
            int n = i = displayInfo.getFrame() == FrameType.CHALLENGE ? 0xFF88FF : 0xFFFF00;
            if (list.size() == 1) {
                toastComponent.getMinecraft().font.draw(poseStack, displayInfo.getFrame().getDisplayName(), 30.0f, 7.0f, i | 0xFF000000);
                toastComponent.getMinecraft().font.draw(poseStack, list.get(0), 30.0f, 18.0f, -1);
            } else {
                int j = 1500;
                float f = 300.0f;
                if (l < 1500L) {
                    int k = Mth.floor(Mth.clamp((float)(1500L - l) / 300.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
                    toastComponent.getMinecraft().font.draw(poseStack, displayInfo.getFrame().getDisplayName(), 30.0f, 11.0f, i | k);
                } else {
                    int k = Mth.floor(Mth.clamp((float)(l - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f) << 24 | 0x4000000;
                    int m = this.height() / 2 - list.size() * toastComponent.getMinecraft().font.lineHeight / 2;
                    for (FormattedCharSequence formattedCharSequence : list) {
                        toastComponent.getMinecraft().font.draw(poseStack, formattedCharSequence, 30.0f, (float)m, 0xFFFFFF | k);
                        m += toastComponent.getMinecraft().font.lineHeight;
                    }
                }
            }
            if (!this.playedSound && l > 0L) {
                this.playedSound = true;
                if (displayInfo.getFrame() == FrameType.CHALLENGE) {
                    toastComponent.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f));
                }
            }
            toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(poseStack, displayInfo.getIcon(), 8, 8);
            return (double)l >= 5000.0 * toastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }
        return Toast.Visibility.HIDE;
    }
}

