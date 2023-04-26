package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class EmptyGlyph extends BakedGlyph {
	public static final EmptyGlyph INSTANCE = new EmptyGlyph();

	public EmptyGlyph() {
		super(GlyphRenderTypes.createForColorTexture(new ResourceLocation("")), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
	}

	@Override
	public void render(boolean bl, float f, float g, Matrix4f matrix4f, VertexConsumer vertexConsumer, float h, float i, float j, float k, int l) {
	}
}
