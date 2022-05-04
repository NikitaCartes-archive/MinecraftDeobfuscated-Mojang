package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Environment(EnvType.CLIENT)
public class LoomScreen extends AbstractContainerScreen<LoomMenu> {
	private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/loom.png");
	private static final int PATTERN_COLUMNS = 4;
	private static final int PATTERN_ROWS = 4;
	private static final int SCROLLER_WIDTH = 12;
	private static final int SCROLLER_HEIGHT = 15;
	private static final int PATTERN_IMAGE_SIZE = 14;
	private static final int SCROLLER_FULL_HEIGHT = 56;
	private static final int PATTERNS_X = 60;
	private static final int PATTERNS_Y = 13;
	private ModelPart flag;
	@Nullable
	private List<Pair<Holder<BannerPattern>, DyeColor>> resultBannerPatterns;
	private ItemStack bannerStack = ItemStack.EMPTY;
	private ItemStack dyeStack = ItemStack.EMPTY;
	private ItemStack patternStack = ItemStack.EMPTY;
	private boolean displayPatterns;
	private boolean hasMaxPatterns;
	private float scrollOffs;
	private boolean scrolling;
	private int startRow;

	public LoomScreen(LoomMenu loomMenu, Inventory inventory, Component component) {
		super(loomMenu, inventory, component);
		loomMenu.registerUpdateListener(this::containerChanged);
		this.titleLabelY -= 2;
	}

	@Override
	protected void init() {
		super.init();
		this.flag = this.minecraft.getEntityModels().bakeLayer(ModelLayers.BANNER).getChild("flag");
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		super.render(poseStack, i, j, f);
		this.renderTooltip(poseStack, i, j);
	}

	private int totalRowCount() {
		return Mth.positiveCeilDiv(this.menu.getSelectablePatterns().size(), 4);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		this.renderBackground(poseStack);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, BG_LOCATION);
		int k = this.leftPos;
		int l = this.topPos;
		this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
		Slot slot = this.menu.getBannerSlot();
		Slot slot2 = this.menu.getDyeSlot();
		Slot slot3 = this.menu.getPatternSlot();
		Slot slot4 = this.menu.getResultSlot();
		if (!slot.hasItem()) {
			this.blit(poseStack, k + slot.x, l + slot.y, this.imageWidth, 0, 16, 16);
		}

		if (!slot2.hasItem()) {
			this.blit(poseStack, k + slot2.x, l + slot2.y, this.imageWidth + 16, 0, 16, 16);
		}

		if (!slot3.hasItem()) {
			this.blit(poseStack, k + slot3.x, l + slot3.y, this.imageWidth + 32, 0, 16, 16);
		}

		int m = (int)(41.0F * this.scrollOffs);
		this.blit(poseStack, k + 119, l + 13 + m, 232 + (this.displayPatterns ? 0 : 12), 0, 12, 15);
		Lighting.setupForFlatItems();
		if (this.resultBannerPatterns != null && !this.hasMaxPatterns) {
			MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
			poseStack.pushPose();
			poseStack.translate((double)(k + 139), (double)(l + 52), 0.0);
			poseStack.scale(24.0F, -24.0F, 1.0F);
			poseStack.translate(0.5, 0.5, 0.5);
			float g = 0.6666667F;
			poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
			this.flag.xRot = 0.0F;
			this.flag.y = -32.0F;
			BannerRenderer.renderPatterns(
				poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, this.resultBannerPatterns
			);
			poseStack.popPose();
			bufferSource.endBatch();
		} else if (this.hasMaxPatterns) {
			this.blit(poseStack, k + slot4.x - 2, l + slot4.y - 2, this.imageWidth, 17, 17, 16);
		}

		if (this.displayPatterns) {
			int n = k + 60;
			int o = l + 13;
			List<Holder<BannerPattern>> list = this.menu.getSelectablePatterns();

			label64:
			for (int p = 0; p < 4; p++) {
				for (int q = 0; q < 4; q++) {
					int r = p + this.startRow;
					int s = r * 4 + q;
					if (s >= list.size()) {
						break label64;
					}

					RenderSystem.setShaderTexture(0, BG_LOCATION);
					int t = n + q * 14;
					int u = o + p * 14;
					boolean bl = i >= t && j >= u && i < t + 14 && j < u + 14;
					int v;
					if (s == this.menu.getSelectedBannerPatternIndex()) {
						v = this.imageHeight + 14;
					} else if (bl) {
						v = this.imageHeight + 28;
					} else {
						v = this.imageHeight;
					}

					this.blit(poseStack, t, u, 0, v, 14, 14);
					this.renderPattern((Holder<BannerPattern>)list.get(s), t, u);
				}
			}
		}

		Lighting.setupFor3DItems();
	}

	private void renderPattern(Holder<BannerPattern> holder, int i, int j) {
		CompoundTag compoundTag = new CompoundTag();
		ListTag listTag = new BannerPattern.Builder().addPattern(BannerPatterns.BASE, DyeColor.GRAY).addPattern(holder, DyeColor.WHITE).toListTag();
		compoundTag.put("Patterns", listTag);
		ItemStack itemStack = new ItemStack(Items.GRAY_BANNER);
		BlockItem.setBlockEntityData(itemStack, BlockEntityType.BANNER, compoundTag);
		PoseStack poseStack = new PoseStack();
		poseStack.pushPose();
		poseStack.translate((double)((float)i + 0.5F), (double)(j + 16), 0.0);
		poseStack.scale(6.0F, -6.0F, 1.0F);
		poseStack.translate(0.5, 0.5, 0.0);
		poseStack.translate(0.5, 0.5, 0.5);
		float f = 0.6666667F;
		poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
		MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
		this.flag.xRot = 0.0F;
		this.flag.y = -32.0F;
		List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(DyeColor.GRAY, BannerBlockEntity.getItemPatterns(itemStack));
		BannerRenderer.renderPatterns(poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, list);
		poseStack.popPose();
		bufferSource.endBatch();
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		this.scrolling = false;
		if (this.displayPatterns) {
			int j = this.leftPos + 60;
			int k = this.topPos + 13;

			for (int l = 0; l < 4; l++) {
				for (int m = 0; m < 4; m++) {
					double f = d - (double)(j + m * 14);
					double g = e - (double)(k + l * 14);
					int n = l + this.startRow;
					int o = n * 4 + m;
					if (f >= 0.0 && g >= 0.0 && f < 14.0 && g < 14.0 && this.menu.clickMenuButton(this.minecraft.player, o)) {
						Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F));
						this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, o);
						return true;
					}
				}
			}

			j = this.leftPos + 119;
			k = this.topPos + 9;
			if (d >= (double)j && d < (double)(j + 12) && e >= (double)k && e < (double)(k + 56)) {
				this.scrolling = true;
			}
		}

		return super.mouseClicked(d, e, i);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		int j = this.totalRowCount() - 4;
		if (this.scrolling && this.displayPatterns && j > 0) {
			int k = this.topPos + 13;
			int l = k + 56;
			this.scrollOffs = ((float)e - (float)k - 7.5F) / ((float)(l - k) - 15.0F);
			this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
			this.startRow = Math.max((int)((double)(this.scrollOffs * (float)j) + 0.5), 0);
			return true;
		} else {
			return super.mouseDragged(d, e, i, f, g);
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		int i = this.totalRowCount() - 4;
		if (this.displayPatterns && i > 0) {
			float g = (float)f / (float)i;
			this.scrollOffs = Mth.clamp(this.scrollOffs - g, 0.0F, 1.0F);
			this.startRow = Math.max((int)(this.scrollOffs * (float)i + 0.5F), 0);
		}

		return true;
	}

	@Override
	protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
		return d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
	}

	private void containerChanged() {
		ItemStack itemStack = this.menu.getResultSlot().getItem();
		if (itemStack.isEmpty()) {
			this.resultBannerPatterns = null;
		} else {
			this.resultBannerPatterns = BannerBlockEntity.createPatterns(((BannerItem)itemStack.getItem()).getColor(), BannerBlockEntity.getItemPatterns(itemStack));
		}

		ItemStack itemStack2 = this.menu.getBannerSlot().getItem();
		ItemStack itemStack3 = this.menu.getDyeSlot().getItem();
		ItemStack itemStack4 = this.menu.getPatternSlot().getItem();
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack2);
		this.hasMaxPatterns = compoundTag != null && compoundTag.contains("Patterns", 9) && !itemStack2.isEmpty() && compoundTag.getList("Patterns", 10).size() >= 6;
		if (this.hasMaxPatterns) {
			this.resultBannerPatterns = null;
		}

		if (!ItemStack.matches(itemStack2, this.bannerStack) || !ItemStack.matches(itemStack3, this.dyeStack) || !ItemStack.matches(itemStack4, this.patternStack)) {
			this.displayPatterns = !itemStack2.isEmpty() && !itemStack3.isEmpty() && !this.hasMaxPatterns && !this.menu.getSelectablePatterns().isEmpty();
		}

		if (this.startRow >= this.totalRowCount()) {
			this.startRow = 0;
			this.scrollOffs = 0.0F;
		}

		this.bannerStack = itemStack2.copy();
		this.dyeStack = itemStack3.copy();
		this.patternStack = itemStack4.copy();
	}
}
