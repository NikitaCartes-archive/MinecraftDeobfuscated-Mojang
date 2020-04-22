/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SystemToast
implements Toast {
    private final SystemToastIds id;
    private String title;
    private String message;
    private long lastChanged;
    private boolean changed;

    public SystemToast(SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
        this.id = systemToastIds;
        this.title = component.getString();
        this.message = component2 == null ? null : component2.getString();
    }

    @Override
    public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }
        toastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0f, 1.0f, 1.0f);
        toastComponent.blit(poseStack, 0, 0, 0, 64, 160, 32);
        if (this.message == null) {
            toastComponent.getMinecraft().font.draw(poseStack, this.title, 18.0f, 12.0f, -256);
        } else {
            toastComponent.getMinecraft().font.draw(poseStack, this.title, 18.0f, 7.0f, -256);
            toastComponent.getMinecraft().font.draw(poseStack, this.message, 18.0f, 18.0f, -1);
        }
        return l - this.lastChanged < 5000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    public void reset(Component component, @Nullable Component component2) {
        this.title = component.getString();
        this.message = component2 == null ? null : component2.getString();
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
        SystemToast.add(minecraft.getToasts(), SystemToastIds.WORLD_ACCESS_FAILURE, new TranslatableComponent("selectWorld.access_failure"), new TextComponent(string));
    }

    public static void onWorldDeleteFailure(Minecraft minecraft, String string) {
        SystemToast.add(minecraft.getToasts(), SystemToastIds.WORLD_ACCESS_FAILURE, new TranslatableComponent("selectWorld.delete_failure"), new TextComponent(string));
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
        WORLD_ACCESS_FAILURE;

    }
}

