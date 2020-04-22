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
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class AdvancementToast
implements Toast {
    private final Advancement advancement;
    private boolean playedSound;

    public AdvancementToast(Advancement advancement) {
        this.advancement = advancement;
    }

    @Override
    public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        toastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0f, 1.0f, 1.0f);
        DisplayInfo displayInfo = this.advancement.getDisplay();
        toastComponent.blit(poseStack, 0, 0, 0, 0, 160, 32);
        if (displayInfo != null) {
            int i;
            List<Component> list = toastComponent.getMinecraft().font.split(displayInfo.getTitle(), 125);
            int n = i = displayInfo.getFrame() == FrameType.CHALLENGE ? 0xFF88FF : 0xFFFF00;
            if (list.size() == 1) {
                toastComponent.getMinecraft().font.draw(poseStack, I18n.get("advancements.toast." + displayInfo.getFrame().getName(), new Object[0]), 30.0f, 7.0f, i | 0xFF000000);
                toastComponent.getMinecraft().font.draw(poseStack, list.get(0), 30.0f, 18.0f, -1);
            } else {
                int j = 1500;
                float f = 300.0f;
                if (l < 1500L) {
                    int k = Mth.floor(Mth.clamp((float)(1500L - l) / 300.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
                    toastComponent.getMinecraft().font.draw(poseStack, I18n.get("advancements.toast." + displayInfo.getFrame().getName(), new Object[0]), 30.0f, 11.0f, i | k);
                } else {
                    int k = Mth.floor(Mth.clamp((float)(l - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f) << 24 | 0x4000000;
                    int m = 16 - list.size() * toastComponent.getMinecraft().font.lineHeight / 2;
                    for (Component component : list) {
                        toastComponent.getMinecraft().font.draw(poseStack, component, 30.0f, (float)m, 0xFFFFFF | k);
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
            toastComponent.getMinecraft().getItemRenderer().renderAndDecorateItem(null, displayInfo.getIcon(), 8, 8);
            return l >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }
        return Toast.Visibility.HIDE;
    }
}

