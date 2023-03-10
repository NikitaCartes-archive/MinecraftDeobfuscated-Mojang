/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SystemToast
implements Toast {
    private static final int MAX_LINE_SIZE = 200;
    private static final int LINE_SPACING = 12;
    private static final int MARGIN = 10;
    private final SystemToastIds id;
    private Component title;
    private List<FormattedCharSequence> messageLines;
    private long lastChanged;
    private boolean changed;
    private final int width;

    public SystemToast(SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
        this(systemToastIds, component, SystemToast.nullToEmpty(component2), Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(component), component2 == null ? 0 : Minecraft.getInstance().font.width(component2))));
    }

    public static SystemToast multiline(Minecraft minecraft, SystemToastIds systemToastIds, Component component, Component component2) {
        Font font = minecraft.font;
        List<FormattedCharSequence> list = font.split(component2, 200);
        int i = Math.max(200, list.stream().mapToInt(font::width).max().orElse(200));
        return new SystemToast(systemToastIds, component, list, i + 30);
    }

    private SystemToast(SystemToastIds systemToastIds, Component component, List<FormattedCharSequence> list, int i) {
        this.id = systemToastIds;
        this.title = component;
        this.messageLines = list;
        this.width = i;
    }

    private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component component) {
        return component == null ? ImmutableList.of() : ImmutableList.of(component.getVisualOrderText());
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return 20 + Math.max(this.messageLines.size(), 1) * 12;
    }

    @Override
    public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        int j;
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.width();
        if (i == 160 && this.messageLines.size() <= 1) {
            GuiComponent.blit(poseStack, 0, 0, 0, 64, i, this.height());
        } else {
            j = this.height();
            int k = 28;
            int m = Math.min(4, j - 28);
            this.renderBackgroundRow(poseStack, toastComponent, i, 0, 0, 28);
            for (int n = 28; n < j - m; n += 10) {
                this.renderBackgroundRow(poseStack, toastComponent, i, 16, n, Math.min(16, j - n - m));
            }
            this.renderBackgroundRow(poseStack, toastComponent, i, 32 - m, j - m, m);
        }
        if (this.messageLines == null) {
            toastComponent.getMinecraft().font.draw(poseStack, this.title, 18.0f, 12.0f, -256);
        } else {
            toastComponent.getMinecraft().font.draw(poseStack, this.title, 18.0f, 7.0f, -256);
            for (j = 0; j < this.messageLines.size(); ++j) {
                toastComponent.getMinecraft().font.draw(poseStack, this.messageLines.get(j), 18.0f, (float)(18 + j * 12), -1);
            }
        }
        return (double)(l - this.lastChanged) < (double)this.id.displayTime * toastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    private void renderBackgroundRow(PoseStack poseStack, ToastComponent toastComponent, int i, int j, int k, int l) {
        int m = j == 0 ? 20 : 5;
        int n = Math.min(60, i - m);
        GuiComponent.blit(poseStack, 0, k, 0, 64 + j, m, l);
        for (int o = m; o < i - n; o += 64) {
            GuiComponent.blit(poseStack, o, k, 32, 64 + j, Math.min(64, i - o - n), l);
        }
        GuiComponent.blit(poseStack, i - n, k, 160 - n, 64 + j, n, l);
    }

    public void reset(Component component, @Nullable Component component2) {
        this.title = component;
        this.messageLines = SystemToast.nullToEmpty(component2);
        this.changed = true;
    }

    public SystemToastIds getToken() {
        return this.id;
    }

    public static void add(ToastComponent toastComponent, SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
        toastComponent.addToast(new SystemToast(systemToastIds, component, component2));
    }

    public static void addOrUpdate(ToastComponent toastComponent, SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
        SystemToast systemToast = toastComponent.getToast(SystemToast.class, (Object)systemToastIds);
        if (systemToast == null) {
            SystemToast.add(toastComponent, systemToastIds, component, component2);
        } else {
            systemToast.reset(component, component2);
        }
    }

    public static void onWorldAccessFailure(Minecraft minecraft, String string) {
        SystemToast.add(minecraft.getToasts(), SystemToastIds.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.access_failure"), Component.literal(string));
    }

    public static void onWorldDeleteFailure(Minecraft minecraft, String string) {
        SystemToast.add(minecraft.getToasts(), SystemToastIds.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.delete_failure"), Component.literal(string));
    }

    public static void onPackCopyFailure(Minecraft minecraft, String string) {
        SystemToast.add(minecraft.getToasts(), SystemToastIds.PACK_COPY_FAILURE, Component.translatable("pack.copyFailure"), Component.literal(string));
    }

    @Override
    public /* synthetic */ Object getToken() {
        return this.getToken();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SystemToastIds {
        TUTORIAL_HINT,
        NARRATOR_TOGGLE,
        WORLD_BACKUP,
        PACK_LOAD_FAILURE,
        WORLD_ACCESS_FAILURE,
        PACK_COPY_FAILURE,
        PERIODIC_NOTIFICATION,
        UNSECURE_SERVER_WARNING(10000L);

        final long displayTime;

        private SystemToastIds(long l) {
            this.displayTime = l;
        }

        private SystemToastIds() {
            this(5000L);
        }
    }
}

