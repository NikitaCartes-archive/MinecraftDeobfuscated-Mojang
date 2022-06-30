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
    private static final Component CHAT_NOT_SECURE_TEXT = Component.translatable("chat.tag.not_secure").withStyle(ChatFormatting.UNDERLINE);
    private static final Component CHAT_MODIFIED_TEXT = Component.translatable("chat.tag.modified").withStyle(ChatFormatting.UNDERLINE);
    private static final int CHAT_NOT_SECURE_INDICATOR_COLOR = 15224664;
    private static final int CHAT_MODIFIED_INDICATOR_COLOR = 15386724;
    private static final GuiMessageTag CHAT_NOT_SECURE = new GuiMessageTag(15224664, Icon.CHAT_NOT_SECURE, CHAT_NOT_SECURE_TEXT, "Not Secure");
    static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/chat_tags.png");

    public static GuiMessageTag chatNotSecure() {
        return CHAT_NOT_SECURE;
    }

    public static GuiMessageTag chatModified(Component component) {
        MutableComponent component2 = Component.translatable("chat.tag.modified.original", component);
        MutableComponent component3 = Component.empty().append(CHAT_MODIFIED_TEXT).append(CommonComponents.NEW_LINE).append(component2);
        return new GuiMessageTag(15386724, Icon.CHAT_MODIFIED, component3, "Modified");
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
        CHAT_NOT_SECURE(0, 0, 9, 9),
        CHAT_MODIFIED(9, 0, 9, 9);

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

