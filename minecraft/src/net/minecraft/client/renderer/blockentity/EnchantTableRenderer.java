package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
		int i
	) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.75, 0.5);
		float h = (float)enchantmentTableBlockEntity.time + g;
		poseStack.translate(0.0, (double)(0.1F + Mth.sin(h * 0.1F) * 0.01F), 0.0);
		float j = enchantmentTableBlockEntity.rot - enchantmentTableBlockEntity.oRot;

		while (j >= (float) Math.PI) {
			j -= (float) (Math.PI * 2);
		}

		while (j < (float) -Math.PI) {
			j += (float) (Math.PI * 2);
		}

		float k = enchantmentTableBlockEntity.oRot + j * g;
		poseStack.mulPose(Vector3f.YP.rotation(-k, false));
		poseStack.mulPose(Vector3f.ZP.rotation(80.0F, true));
		float l = Mth.lerp(g, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip);
		float m = Mth.frac(l + 0.25F) * 1.6F - 0.3F;
		float n = Mth.frac(l + 0.75F) * 1.6F - 0.3F;
		float o = Mth.lerp(g, enchantmentTableBlockEntity.oOpen, enchantmentTableBlockEntity.open);
		this.bookModel.setupAnim(h, Mth.clamp(m, 0.0F, 1.0F), Mth.clamp(n, 0.0F, 1.0F), o);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.SOLID);
		this.bookModel.render(poseStack, vertexConsumer, 0.0625F, i, this.getSprite(BOOK_LOCATION));
		poseStack.popPose();
	}
}
