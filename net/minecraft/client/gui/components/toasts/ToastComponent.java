/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ToastComponent
extends GuiComponent {
    private static final int VISIBLE_TOASTS = 5;
    private final Minecraft minecraft;
    private final ToastInstance<?>[] visible = new ToastInstance[5];
    private final Deque<Toast> queued = Queues.newArrayDeque();

    public ToastComponent(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(PoseStack poseStack) {
        if (this.minecraft.options.hideGui) {
            return;
        }
        for (int i = 0; i < this.visible.length; ++i) {
            ToastInstance<?> toastInstance = this.visible[i];
            if (toastInstance != null && toastInstance.render(this.minecraft.getWindow().getGuiScaledWidth(), i, poseStack)) {
                this.visible[i] = null;
            }
            if (this.visible[i] != null || this.queued.isEmpty()) continue;
            this.visible[i] = new ToastInstance(this, this.queued.removeFirst());
        }
    }

    @Nullable
    public <T extends Toast> T getToast(Class<? extends T> class_, Object object) {
        for (ToastInstance<?> toastInstance : this.visible) {
            if (toastInstance == null || !class_.isAssignableFrom(toastInstance.getToast().getClass()) || !toastInstance.getToast().getToken().equals(object)) continue;
            return (T)toastInstance.getToast();
        }
        for (Toast toast : this.queued) {
            if (!class_.isAssignableFrom(toast.getClass()) || !toast.getToken().equals(object)) continue;
            return (T)toast;
        }
        return null;
    }

    public void clear() {
        Arrays.fill(this.visible, null);
        this.queued.clear();
    }

    public void addToast(Toast toast) {
        this.queued.add(toast);
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    @Environment(value=EnvType.CLIENT)
    static class ToastInstance<T extends Toast> {
        private static final long ANIMATION_TIME = 600L;
        private final T toast;
        private long animationTime = -1L;
        private long visibleTime = -1L;
        private Toast.Visibility visibility = Toast.Visibility.SHOW;
        final /* synthetic */ ToastComponent field_2245;

        private ToastInstance(T toast) {
            this.field_2245 = toastComponent;
            this.toast = toast;
        }

        public T getToast() {
            return this.toast;
        }

        private float getVisibility(long l) {
            float f = Mth.clamp((float)(l - this.animationTime) / 600.0f, 0.0f, 1.0f);
            f *= f;
            if (this.visibility == Toast.Visibility.HIDE) {
                return 1.0f - f;
            }
            return f;
        }

        public boolean render(int i, int j, PoseStack poseStack) {
            long l = Util.getMillis();
            if (this.animationTime == -1L) {
                this.animationTime = l;
                this.visibility.playSound(this.field_2245.minecraft.getSoundManager());
            }
            if (this.visibility == Toast.Visibility.SHOW && l - this.animationTime <= 600L) {
                this.visibleTime = l;
            }
            PoseStack poseStack2 = RenderSystem.getModelViewStack();
            poseStack2.pushPose();
            poseStack2.translate((float)i - (float)this.toast.width() * this.getVisibility(l), j * this.toast.height(), 800 + j);
            RenderSystem.applyModelViewMatrix();
            Toast.Visibility visibility = this.toast.render(poseStack, this.field_2245, l - this.visibleTime);
            poseStack2.popPose();
            RenderSystem.applyModelViewMatrix();
            if (visibility != this.visibility) {
                this.animationTime = l - (long)((int)((1.0f - this.getVisibility(l)) * 600.0f));
                this.visibility = visibility;
                this.visibility.playSound(this.field_2245.minecraft.getSoundManager());
            }
            return this.visibility == Toast.Visibility.HIDE && l - this.animationTime > 600L;
        }
    }
}

