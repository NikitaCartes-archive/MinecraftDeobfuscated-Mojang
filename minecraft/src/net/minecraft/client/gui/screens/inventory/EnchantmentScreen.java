package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

@Environment(EnvType.CLIENT)
public class EnchantmentScreen extends AbstractContainerScreen<EnchantmentMenu> {
	private static final ResourceLocation ENCHANTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/enchanting_table.png");
	private static final ResourceLocation ENCHANTING_BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
	private final Random random = new Random();
	private BookModel bookModel;
	public int time;
	public float flip;
	public float oFlip;
	public float flipT;
	public float flipA;
	public float open;
	public float oOpen;
	private ItemStack last = ItemStack.EMPTY;

	public EnchantmentScreen(EnchantmentMenu enchantmentMenu, Inventory inventory, Component component) {
		super(enchantmentMenu, inventory, component);
	}

	@Override
	protected void init() {
		super.init();
		this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
	}

	@Override
	public void containerTick() {
		super.containerTick();
		this.tickBook();
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		int j = (this.width - this.imageWidth) / 2;
		int k = (this.height - this.imageHeight) / 2;

		for (int l = 0; l < 3; l++) {
			double f = d - (double)(j + 60);
			double g = e - (double)(k + 14 + 19 * l);
			if (f >= 0.0 && g >= 0.0 && f < 108.0 && g < 19.0 && this.menu.clickMenuButton(this.minecraft.player, l)) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, l);
				return true;
			}
		}

		return super.mouseClicked(d, e, i);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		Lighting.setupForFlatItems();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, ENCHANTING_TABLE_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
		int m = (int)this.minecraft.getWindow().getGuiScale();
		RenderSystem.viewport((this.width - 320) / 2 * m, (this.height - 240) / 2 * m, 320 * m, 240 * m);
		Matrix4f matrix4f = Matrix4f.createTranslateMatrix(-0.34F, 0.23F, 0.0F);
		matrix4f.multiply(Matrix4f.perspective(90.0, 1.3333334F, 9.0F, 80.0F));
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrix4f);
		poseStack.pushPose();
		PoseStack.Pose pose = poseStack.last();
		pose.pose().setIdentity();
		pose.normal().setIdentity();
		poseStack.translate(0.0, 3.3F, 1984.0);
		float g = 5.0F;
		poseStack.scale(5.0F, 5.0F, 5.0F);
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(20.0F));
		float h = Mth.lerp(f, this.oOpen, this.open);
		poseStack.translate((double)((1.0F - h) * 0.2F), (double)((1.0F - h) * 0.1F), (double)((1.0F - h) * 0.25F));
		float n = -(1.0F - h) * 90.0F - 90.0F;
		poseStack.mulPose(Vector3f.YP.rotationDegrees(n));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
		float o = Mth.lerp(f, this.oFlip, this.flip) + 0.25F;
		float p = Mth.lerp(f, this.oFlip, this.flip) + 0.75F;
		o = (o - (float)Mth.fastFloor((double)o)) * 1.6F - 0.3F;
		p = (p - (float)Mth.fastFloor((double)p)) * 1.6F - 0.3F;
		if (o < 0.0F) {
			o = 0.0F;
		}

		if (p < 0.0F) {
			p = 0.0F;
		}

		if (o > 1.0F) {
			o = 1.0F;
		}

		if (p > 1.0F) {
			p = 1.0F;
		}

		this.bookModel.setupAnim(0.0F, o, p, h);
		MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		VertexConsumer vertexConsumer = bufferSource.getBuffer(this.bookModel.renderType(ENCHANTING_BOOK_LOCATION));
		this.bookModel.renderToBuffer(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		bufferSource.endBatch();
		poseStack.popPose();
		RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
		RenderSystem.restoreProjectionMatrix();
		Lighting.setupFor3DItems();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		EnchantmentNames.getInstance().initSeed((long)this.menu.getEnchantmentSeed());
		int q = this.menu.getGoldCount();

		for (int r = 0; r < 3; r++) {
			int s = k + 60;
			int t = s + 20;
			this.setBlitOffset(0);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, ENCHANTING_TABLE_LOCATION);
			int u = this.menu.costs[r];
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			if (u == 0) {
				this.blit(poseStack, s, l + 14 + 19 * r, 0, 185, 108, 19);
			} else {
				String string = u + "";
				int v = 86 - this.font.width(string);
				FormattedText formattedText = EnchantmentNames.getInstance().getRandomName(this.font, v);
				int w = 6839882;
				if ((q < r + 1 || this.minecraft.player.experienceLevel < u) && !this.minecraft.player.getAbilities().instabuild) {
					this.blit(poseStack, s, l + 14 + 19 * r, 0, 185, 108, 19);
					this.blit(poseStack, s + 1, l + 15 + 19 * r, 16 * r, 239, 16, 16);
					this.font.drawWordWrap(formattedText, t, l + 16 + 19 * r, v, (w & 16711422) >> 1);
					w = 4226832;
				} else {
					int x = i - (k + 60);
					int y = j - (l + 14 + 19 * r);
					if (x >= 0 && y >= 0 && x < 108 && y < 19) {
						this.blit(poseStack, s, l + 14 + 19 * r, 0, 204, 108, 19);
						w = 16777088;
					} else {
						this.blit(poseStack, s, l + 14 + 19 * r, 0, 166, 108, 19);
					}

					this.blit(poseStack, s + 1, l + 15 + 19 * r, 16 * r, 223, 16, 16);
					this.font.drawWordWrap(formattedText, t, l + 16 + 19 * r, v, w);
					w = 8453920;
				}

				this.font.drawShadow(poseStack, string, (float)(t + 86 - this.font.width(string)), (float)(l + 16 + 19 * r + 7), w);
			}
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		f = this.minecraft.getFrameTime();
		this.renderBackground(poseStack);
		super.render(poseStack, i, j, f);
		this.renderTooltip(poseStack, i, j);
		boolean bl = this.minecraft.player.getAbilities().instabuild;
		int k = this.menu.getGoldCount();

		for (int l = 0; l < 3; l++) {
			int m = this.menu.costs[l];
			Enchantment enchantment = Enchantment.byId(this.menu.enchantClue[l]);
			int n = this.menu.levelClue[l];
			int o = l + 1;
			if (this.isHovering(60, 14 + 19 * l, 108, 17, (double)i, (double)j) && m > 0 && n >= 0 && enchantment != null) {
				List<Component> list = Lists.<Component>newArrayList();
				list.add(new TranslatableComponent("container.enchant.clue", enchantment.getFullname(n)).withStyle(ChatFormatting.WHITE));
				if (!bl) {
					list.add(TextComponent.EMPTY);
					if (this.minecraft.player.experienceLevel < m) {
						list.add(new TranslatableComponent("container.enchant.level.requirement", this.menu.costs[l]).withStyle(ChatFormatting.RED));
					} else {
						MutableComponent mutableComponent;
						if (o == 1) {
							mutableComponent = new TranslatableComponent("container.enchant.lapis.one");
						} else {
							mutableComponent = new TranslatableComponent("container.enchant.lapis.many", o);
						}

						list.add(mutableComponent.withStyle(k >= o ? ChatFormatting.GRAY : ChatFormatting.RED));
						MutableComponent mutableComponent2;
						if (o == 1) {
							mutableComponent2 = new TranslatableComponent("container.enchant.level.one");
						} else {
							mutableComponent2 = new TranslatableComponent("container.enchant.level.many", o);
						}

						list.add(mutableComponent2.withStyle(ChatFormatting.GRAY));
					}
				}

				this.renderComponentTooltip(poseStack, list, i, j);
				break;
			}
		}
	}

	public void tickBook() {
		ItemStack itemStack = this.menu.getSlot(0).getItem();
		if (!ItemStack.matches(itemStack, this.last)) {
			this.last = itemStack;

			do {
				this.flipT = this.flipT + (float)(this.random.nextInt(4) - this.random.nextInt(4));
			} while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
		}

		this.time++;
		this.oFlip = this.flip;
		this.oOpen = this.open;
		boolean bl = false;

		for (int i = 0; i < 3; i++) {
			if (this.menu.costs[i] != 0) {
				bl = true;
			}
		}

		if (bl) {
			this.open += 0.2F;
		} else {
			this.open -= 0.2F;
		}

		this.open = Mth.clamp(this.open, 0.0F, 1.0F);
		float f = (this.flipT - this.flip) * 0.4F;
		float g = 0.2F;
		f = Mth.clamp(f, -0.2F, 0.2F);
		this.flipA = this.flipA + (f - this.flipA) * 0.9F;
		this.flip = this.flip + this.flipA;
	}
}
