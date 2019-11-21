package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

@Environment(EnvType.CLIENT)
public class EnchantTableRenderer extends BlockEntityRenderer<EnchantmentTableBlockEntity> {
	public static final Material BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/enchanting_table_book"));
	private final BookModel bookModel = new BookModel();

	public EnchantTableRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(EnchantmentTableBlockEntity enchantmentTableBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.75, 0.5);
		float g = (float)enchantmentTableBlockEntity.time + f;
		poseStack.translate(0.0, (double)(0.1F + Mth.sin(g * 0.1F) * 0.01F), 0.0);
		float h = enchantmentTableBlockEntity.rot - enchantmentTableBlockEntity.oRot;

		while (h >= (float) Math.PI) {
			h -= (float) (Math.PI * 2);
		}

		while (h < (float) -Math.PI) {
			h += (float) (Math.PI * 2);
		}

		float k = enchantmentTableBlockEntity.oRot + h * f;
		poseStack.mulPose(Vector3f.YP.rotation(-k));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
		float l = Mth.lerp(f, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip);
		float m = Mth.frac(l + 0.25F) * 1.6F - 0.3F;
		float n = Mth.frac(l + 0.75F) * 1.6F - 0.3F;
		float o = Mth.lerp(f, enchantmentTableBlockEntity.oOpen, enchantmentTableBlockEntity.open);
		this.bookModel.setupAnim(g, Mth.clamp(m, 0.0F, 1.0F), Mth.clamp(n, 0.0F, 1.0F), o);
		VertexConsumer vertexConsumer = BOOK_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
		this.bookModel.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();
	}
}
