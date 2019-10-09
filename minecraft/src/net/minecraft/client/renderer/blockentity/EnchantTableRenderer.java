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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

@Environment(EnvType.CLIENT)
public class EnchantTableRenderer extends BlockEntityRenderer<EnchantmentTableBlockEntity> {
	public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("entity/enchanting_table_book");
	private final BookModel bookModel = new BookModel();

	public EnchantTableRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(
		EnchantmentTableBlockEntity enchantmentTableBlockEntity,
		double d,
		double e,
		double f,
		float g,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j
	) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.75, 0.5);
		float h = (float)enchantmentTableBlockEntity.time + g;
		poseStack.translate(0.0, (double)(0.1F + Mth.sin(h * 0.1F) * 0.01F), 0.0);
		float k = enchantmentTableBlockEntity.rot - enchantmentTableBlockEntity.oRot;

		while (k >= (float) Math.PI) {
			k -= (float) (Math.PI * 2);
		}

		while (k < (float) -Math.PI) {
			k += (float) (Math.PI * 2);
		}

		float l = enchantmentTableBlockEntity.oRot + k * g;
		poseStack.mulPose(Vector3f.YP.rotation(-l));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
		float m = Mth.lerp(g, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip);
		float n = Mth.frac(m + 0.25F) * 1.6F - 0.3F;
		float o = Mth.frac(m + 0.75F) * 1.6F - 0.3F;
		float p = Mth.lerp(g, enchantmentTableBlockEntity.oOpen, enchantmentTableBlockEntity.open);
		this.bookModel.setupAnim(h, Mth.clamp(n, 0.0F, 1.0F), Mth.clamp(o, 0.0F, 1.0F), p);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
		this.bookModel.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, this.getSprite(BOOK_LOCATION));
		poseStack.popPose();
	}
}
