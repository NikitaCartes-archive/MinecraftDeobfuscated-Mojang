/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CatRenderer
extends MobRenderer<Cat, CatModel<Cat>> {
    public CatRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new CatModel(0.0f), 0.4f);
        this.addLayer(new CatCollarLayer(this));
    }

    @Override
    @Nullable
    protected ResourceLocation getTextureLocation(Cat cat) {
        return cat.getResourceLocation();
    }

    @Override
    protected void scale(Cat cat, float f) {
        super.scale(cat, f);
        RenderSystem.scalef(0.8f, 0.8f, 0.8f);
    }

    @Override
    protected void setupRotations(Cat cat, float f, float g, float h) {
        super.setupRotations(cat, f, g, h);
        float i = cat.getLieDownAmount(h);
        if (i > 0.0f) {
            RenderSystem.translatef(0.4f * i, 0.15f * i, 0.1f * i);
            RenderSystem.rotatef(Mth.rotLerp(i, 0.0f, 90.0f), 0.0f, 0.0f, 1.0f);
            BlockPos blockPos = new BlockPos(cat);
            List<Player> list = cat.level.getEntitiesOfClass(Player.class, new AABB(blockPos).inflate(2.0, 2.0, 2.0));
            for (Player player : list) {
                if (!player.isSleeping()) continue;
                RenderSystem.translatef(0.15f * i, 0.0f, 0.0f);
                break;
            }
        }
    }
}

