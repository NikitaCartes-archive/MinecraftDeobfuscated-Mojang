/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TutorialToast
implements Toast {
    private final Icons icon;
    private final String title;
    private final String message;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;
    private long lastProgressTime;
    private float lastProgress;
    private float progress;
    private final boolean progressable;

    public TutorialToast(Icons icons, Component component, @Nullable Component component2, boolean bl) {
        this.icon = icons;
        this.title = component.getColoredString();
        this.message = component2 == null ? null : component2.getColoredString();
        this.progressable = bl;
    }

    @Override
    public Toast.Visibility render(ToastComponent toastComponent, long l) {
        toastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0f, 1.0f, 1.0f);
        toastComponent.blit(0, 0, 0, 96, 160, 32);
        this.icon.render(toastComponent, 6, 6);
        if (this.message == null) {
            toastComponent.getMinecraft().font.draw(this.title, 30.0f, 12.0f, -11534256);
        } else {
            toastComponent.getMinecraft().font.draw(this.title, 30.0f, 7.0f, -11534256);
            toastComponent.getMinecraft().font.draw(this.message, 30.0f, 18.0f, -16777216);
        }
        if (this.progressable) {
            GuiComponent.fill(3, 28, 157, 29, -1);
            float f = (float)Mth.clampedLerp(this.lastProgress, this.progress, (float)(l - this.lastProgressTime) / 100.0f);
            int i = this.progress >= this.lastProgress ? -16755456 : -11206656;
            GuiComponent.fill(3, 28, (int)(3.0f + 154.0f * f), 29, i);
            this.lastProgress = f;
            this.lastProgressTime = l;
        }
        return this.visibility;
    }

    public void hide() {
        this.visibility = Toast.Visibility.HIDE;
    }

    public void updateProgress(float f) {
        this.progress = f;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Icons {
        MOVEMENT_KEYS(0, 0),
        MOUSE(1, 0),
        TREE(2, 0),
        RECIPE_BOOK(0, 1),
        WOODEN_PLANKS(1, 1);

        private final int x;
        private final int y;

        private Icons(int j, int k) {
            this.x = j;
            this.y = k;
        }

        public void render(GuiComponent guiComponent, int i, int j) {
            RenderSystem.enableBlend();
            guiComponent.blit(i, j, 176 + this.x * 20, this.y * 20, 20, 20);
            RenderSystem.enableBlend();
        }
    }
}

