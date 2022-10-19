/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record GuiMessageTag(int indicatorColor, @Nullable Icon icon, @Nullable Component text, @Nullable String logTag) {
    private static final Component SYSTEM_TEXT = Component.translatable("chat.tag.system");
    private static final Component CHAT_NOT_SECURE_TEXT = Component.translatable("chat.tag.not_secure");
    private static final Component CHAT_MODIFIED_TEXT = Component.translatable("chat.tag.modified");
    private static final int CHAT_NOT_SECURE_INDICATOR_COLOR = 0xD0D0D0;
    private static final int CHAT_MODIFIED_INDICATOR_COLOR = 0x606060;
    private static final GuiMessageTag SYSTEM = new GuiMessageTag(0xD0D0D0, null, SYSTEM_TEXT, "System");
    private static final GuiMessageTag CHAT_NOT_SECURE = new GuiMessageTag(0xD0D0D0, null, CHAT_NOT_SECURE_TEXT, "Not Secure");
    static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/chat_tags.png");

    public static GuiMessageTag system() {
        return SYSTEM;
    }

    public static GuiMessageTag chatNotSecure() {
        return CHAT_NOT_SECURE;
    }

    public static GuiMessageTag chatModified(String string) {
        MutableComponent component = Component.literal(string).withStyle(ChatFormatting.GRAY);
        MutableComponent component2 = Component.empty().append(CHAT_MODIFIED_TEXT).append(CommonComponents.NEW_LINE).append(component);
        return new GuiMessageTag(0x606060, Icon.CHAT_MODIFIED, component2, "Modified");
    }

    @Nullable
    public Icon icon() {
        return this.icon;
    }

    @Nullable
    public Component text() {
        return this.text;
    }

    @Nullable
    public String logTag() {
        return this.logTag;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Icon {
        CHAT_MODIFIED(0, 0, 9, 9);

        public final int u;
        public final int v;
        public final int width;
        public final int height;

        private Icon(int j, int k, int l, int m) {
            this.u = j;
            this.v = k;
            this.width = l;
            this.height = m;
        }

        public void draw(PoseStack poseStack, int i, int j) {
            RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
            GuiComponent.blit(poseStack, i, j, this.u, this.v, this.width, this.height, 32, 32);
        }
    }
}

