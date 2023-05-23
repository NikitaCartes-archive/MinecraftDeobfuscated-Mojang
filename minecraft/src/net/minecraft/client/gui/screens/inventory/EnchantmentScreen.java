package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

@Environment(EnvType.CLIENT)
public class EnchantmentScreen extends AbstractContainerScreen<EnchantmentMenu> {
	private static final ResourceLocation ENCHANTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/enchanting_table.png");
	private static final ResourceLocation ENCHANTING_BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
	private final RandomSource random = RandomSource.create();
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
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(ENCHANTING_TABLE_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
		this.renderBook(guiGraphics, k, l, f);
		EnchantmentNames.getInstance().initSeed((long)this.menu.getEnchantmentSeed());
		int m = this.menu.getGoldCount();

		for (int n = 0; n < 3; n++) {
			int o = k + 60;
			int p = o + 20;
			int q = this.menu.costs[n];
			if (q == 0) {
				guiGraphics.blit(ENCHANTING_TABLE_LOCATION, o, l + 14 + 19 * n, 0, 185, 108, 19);
			} else {
				String string = q + "";
				int r = 86 - this.font.width(string);
				FormattedText formattedText = EnchantmentNames.getInstance().getRandomName(this.font, r);
				int s = 6839882;
				if ((m < n + 1 || this.minecraft.player.experienceLevel < q) && !this.minecraft.player.getAbilities().instabuild) {
					guiGraphics.blit(ENCHANTING_TABLE_LOCATION, o, l + 14 + 19 * n, 0, 185, 108, 19);
					guiGraphics.blit(ENCHANTING_TABLE_LOCATION, o + 1, l + 15 + 19 * n, 16 * n, 239, 16, 16);
					guiGraphics.drawWordWrap(this.font, formattedText, p, l + 16 + 19 * n, r, (s & 16711422) >> 1);
					s = 4226832;
				} else {
					int t = i - (k + 60);
					int u = j - (l + 14 + 19 * n);
					if (t >= 0 && u >= 0 && t < 108 && u < 19) {
						guiGraphics.blit(ENCHANTING_TABLE_LOCATION, o, l + 14 + 19 * n, 0, 204, 108, 19);
						s = 16777088;
					} else {
						guiGraphics.blit(ENCHANTING_TABLE_LOCATION, o, l + 14 + 19 * n, 0, 166, 108, 19);
					}

					guiGraphics.blit(ENCHANTING_TABLE_LOCATION, o + 1, l + 15 + 19 * n, 16 * n, 223, 16, 16);
					guiGraphics.drawWordWrap(this.font, formattedText, p, l + 16 + 19 * n, r, s);
					s = 8453920;
				}

				guiGraphics.drawString(this.font, string, p + 86 - this.font.width(string), l + 16 + 19 * n + 7, s);
			}
		}
	}

	private void renderBook(GuiGraphics guiGraphics, int i, int j, float f) {
		float g = Mth.lerp(f, this.oOpen, this.open);
		float h = Mth.lerp(f, this.oFlip, this.flip);
		Lighting.setupForEntityInInventory();
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((float)i + 33.0F, (float)j + 31.0F, 100.0F);
		float k = 40.0F;
		guiGraphics.pose().scale(-40.0F, 40.0F, 40.0F);
		guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(25.0F));
		guiGraphics.pose().translate((1.0F - g) * 0.2F, (1.0F - g) * 0.1F, (1.0F - g) * 0.25F);
		float l = -(1.0F - g) * 90.0F - 90.0F;
		guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(l));
		guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
		float m = Mth.clamp(Mth.frac(h + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
		float n = Mth.clamp(Mth.frac(h + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
		this.bookModel.setupAnim(0.0F, m, n, g);
		VertexConsumer vertexConsumer = guiGraphics.bufferSource().getBuffer(this.bookModel.renderType(ENCHANTING_BOOK_LOCATION));
		this.bookModel.renderToBuffer(guiGraphics.pose(), vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		guiGraphics.flush();
		guiGraphics.pose().popPose();
		Lighting.setupFor3DItems();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		f = this.minecraft.getFrameTime();
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
		boolean bl = this.minecraft.player.getAbilities().instabuild;
		int k = this.menu.getGoldCount();

		for (int l = 0; l < 3; l++) {
			int m = this.menu.costs[l];
			Enchantment enchantment = Enchantment.byId(this.menu.enchantClue[l]);
			int n = this.menu.levelClue[l];
			int o = l + 1;
			if (this.isHovering(60, 14 + 19 * l, 108, 17, (double)i, (double)j) && m > 0 && n >= 0 && enchantment != null) {
				List<Component> list = Lists.<Component>newArrayList();
				list.add(Component.translatable("container.enchant.clue", enchantment.getFullname(n)).withStyle(ChatFormatting.WHITE));
				if (!bl) {
					list.add(CommonComponents.EMPTY);
					if (this.minecraft.player.experienceLevel < m) {
						list.add(Component.translatable("container.enchant.level.requirement", this.menu.costs[l]).withStyle(ChatFormatting.RED));
					} else {
						MutableComponent mutableComponent;
						if (o == 1) {
							mutableComponent = Component.translatable("container.enchant.lapis.one");
						} else {
							mutableComponent = Component.translatable("container.enchant.lapis.many", o);
						}

						list.add(mutableComponent.withStyle(k >= o ? ChatFormatting.GRAY : ChatFormatting.RED));
						MutableComponent mutableComponent2;
						if (o == 1) {
							mutableComponent2 = Component.translatable("container.enchant.level.one");
						} else {
							mutableComponent2 = Component.translatable("container.enchant.level.many", o);
						}

						list.add(mutableComponent2.withStyle(ChatFormatting.GRAY));
					}
				}

				guiGraphics.renderComponentTooltip(this.font, list, i, j);
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
