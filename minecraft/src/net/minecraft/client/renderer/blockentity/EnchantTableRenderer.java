package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

@Environment(EnvType.CLIENT)
public class EnchantTableRenderer extends BlockEntityRenderer<EnchantmentTableBlockEntity> {
	private static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
	private final BookModel bookModel = new BookModel();

	public void render(EnchantmentTableBlockEntity enchantmentTableBlockEntity, double d, double e, double f, float g, int i) {
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float)d + 0.5F, (float)e + 0.75F, (float)f + 0.5F);
		float h = (float)enchantmentTableBlockEntity.time + g;
		GlStateManager.translatef(0.0F, 0.1F + Mth.sin(h * 0.1F) * 0.01F, 0.0F);
		float j = enchantmentTableBlockEntity.rot - enchantmentTableBlockEntity.oRot;

		while (j >= (float) Math.PI) {
			j -= (float) (Math.PI * 2);
		}

		while (j < (float) -Math.PI) {
			j += (float) (Math.PI * 2);
		}

		float k = enchantmentTableBlockEntity.oRot + j * g;
		GlStateManager.rotatef(-k * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(80.0F, 0.0F, 0.0F, 1.0F);
		this.bindTexture(BOOK_LOCATION);
		float l = Mth.lerp(g, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip) + 0.25F;
		float m = Mth.lerp(g, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip) + 0.75F;
		l = (l - (float)Mth.fastFloor((double)l)) * 1.6F - 0.3F;
		m = (m - (float)Mth.fastFloor((double)m)) * 1.6F - 0.3F;
		if (l < 0.0F) {
			l = 0.0F;
		}

		if (m < 0.0F) {
			m = 0.0F;
		}

		if (l > 1.0F) {
			l = 1.0F;
		}

		if (m > 1.0F) {
			m = 1.0F;
		}

		float n = Mth.lerp(g, enchantmentTableBlockEntity.oOpen, enchantmentTableBlockEntity.open);
		GlStateManager.enableCull();
		this.bookModel.render(h, l, m, n, 0.0F, 0.0625F);
		GlStateManager.popMatrix();
	}
}
