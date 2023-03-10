/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
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
    private static final int SLOT_COUNT = 5;
    private static final int NO_SPACE = -1;
    final Minecraft minecraft;
    private final List<ToastInstance<?>> visible = new ArrayList();
    private final BitSet occupiedSlots = new BitSet(5);
    private final Deque<Toast> queued = Queues.newArrayDeque();

    public ToastComponent(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(PoseStack poseStack) {
        if (this.minecraft.options.hideGui) {
            return;
        }
        int i = this.minecraft.getWindow().getGuiScaledWidth();
        this.visible.removeIf(toastInstance -> {
            if (toastInstance != null && toastInstance.render(i, poseStack)) {
                this.occupiedSlots.clear(toastInstance.index, toastInstance.index + toastInstance.slotCount);
                return true;
            }
            return false;
        });
        if (!this.queued.isEmpty() && this.freeSlots() > 0) {
            this.queued.removeIf(toast -> {
                int i = toast.slotCount();
                int j = this.findFreeIndex(i);
                if (j != -1) {
                    this.visible.add(new ToastInstance(this, toast, j, i));
                    this.occupiedSlots.set(j, j + i);
                    return true;
                }
                return false;
            });
        }
    }

    private int findFreeIndex(int i) {
        if (this.freeSlots() >= i) {
            int j = 0;
            for (int k = 0; k < 5; ++k) {
                if (this.occupiedSlots.get(k)) {
                    j = 0;
                    continue;
                }
                if (++j != i) continue;
                return k + 1 - j;
            }
        }
        return -1;
    }

    private int freeSlots() {
        return 5 - this.occupiedSlots.cardinality();
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
        this.occupiedSlots.clear();
        this.visible.clear();
        this.queued.clear();
    }

    public void addToast(Toast toast) {
        this.queued.add(toast);
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public double getNotificationDisplayTimeMultiplier() {
        return this.minecraft.options.notificationDisplayTime().get();
    }

    @Environment(value=EnvType.CLIENT)
    class ToastInstance<T extends Toast> {
        private static final long ANIMATION_TIME = 600L;
        private final T toast;
        final int index;
        final int slotCount;
        private long animationTime = -1L;
        private long visibleTime = -1L;
        private Toast.Visibility visibility = Toast.Visibility.SHOW;
        final /* synthetic */ ToastComponent field_2245;

        /*
         * WARNING - Possible parameter corruption
         */
        ToastInstance(T toast, int i, int j) {
            this.field_2245 = (ToastComponent)toastComponent;
            this.toast = toast;
            this.index = i;
            this.slotCount = j;
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

        public boolean render(int i, PoseStack poseStack) {
            long l = Util.getMillis();
            if (this.animationTime == -1L) {
                this.animationTime = l;
                this.visibility.playSound(this.field_2245.minecraft.getSoundManager());
            }
            if (this.visibility == Toast.Visibility.SHOW && l - this.animationTime <= 600L) {
                this.visibleTime = l;
            }
            poseStack.pushPose();
            poseStack.translate((float)i - (float)this.toast.width() * this.getVisibility(l), this.index * 32, 800.0f);
            Toast.Visibility visibility = this.toast.render(poseStack, this.field_2245, l - this.visibleTime);
            poseStack.popPose();
            if (visibility != this.visibility) {
                this.animationTime = l - (long)((int)((1.0f - this.getVisibility(l)) * 600.0f));
                this.visibility = visibility;
                this.visibility.playSound(this.field_2245.minecraft.getSoundManager());
            }
            return this.visibility == Toast.Visibility.HIDE && l - this.animationTime > 600L;
        }
    }
}

