/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class HangingSignEditScreen
extends AbstractSignEditScreen {
    public static final float MAGIC_BACKGROUND_SCALE = 4.0f;
    private static final Vector3f TEXT_SCALE = new Vector3f(1.0f, 1.0f, 1.0f);
    private static final int TEXTURE_WIDTH = 16;
    private static final int TEXTURE_HEIGHT = 16;
    private final ResourceLocation texture;

    public HangingSignEditScreen(SignBlockEntity signBlockEntity, boolean bl) {
        super(signBlockEntity, bl, Component.translatable("hanging_sign.edit"));
        this.texture = new ResourceLocation("textures/gui/hanging_signs/" + this.woodType.name() + ".png");
    }

    @Override
    protected void offsetSign(PoseStack poseStack, BlockState blockState) {
        poseStack.translate((float)this.width / 2.0f, 125.0f, 50.0f);
    }

    @Override
    protected void renderSignBackground(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockState blockState) {
        poseStack.translate(0.0f, -13.0f, 0.0f);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.scale(4.0f, 4.0f, 1.0f);
        HangingSignEditScreen.blit(poseStack, -8, -8, 0.0f, 0.0f, 16, 16, 16, 16);
    }

    @Override
    protected Vector3f getSignTextScale() {
        return TEXT_SCALE;
    }
}

