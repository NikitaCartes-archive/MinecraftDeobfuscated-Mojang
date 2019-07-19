package net.minecraft.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.banner.BannerTextures;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class EntityBlockRenderer {
	private static final ShulkerBoxBlockEntity[] SHULKER_BOXES = (ShulkerBoxBlockEntity[])Arrays.stream(DyeColor.values())
		.sorted(Comparator.comparingInt(DyeColor::getId))
		.map(ShulkerBoxBlockEntity::new)
		.toArray(ShulkerBoxBlockEntity[]::new);
	private static final ShulkerBoxBlockEntity DEFAULT_SHULKER_BOX = new ShulkerBoxBlockEntity(null);
	public static final EntityBlockRenderer instance = new EntityBlockRenderer();
	private final ChestBlockEntity chest = new ChestBlockEntity();
	private final ChestBlockEntity trappedChest = new TrappedChestBlockEntity();
	private final EnderChestBlockEntity enderChest = new EnderChestBlockEntity();
	private final BannerBlockEntity banner = new BannerBlockEntity();
	private final BedBlockEntity bed = new BedBlockEntity();
	private final SkullBlockEntity skull = new SkullBlockEntity();
	private final ConduitBlockEntity conduit = new ConduitBlockEntity();
	private final ShieldModel shieldModel = new ShieldModel();
	private final TridentModel tridentModel = new TridentModel();

	public void renderByItem(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof BannerItem) {
			this.banner.fromItem(itemStack, ((BannerItem)item).getColor());
			BlockEntityRenderDispatcher.instance.renderItem(this.banner);
		} else if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof BedBlock) {
			this.bed.setColor(((BedBlock)((BlockItem)item).getBlock()).getColor());
			BlockEntityRenderDispatcher.instance.renderItem(this.bed);
		} else if (item == Items.SHIELD) {
			if (itemStack.getTagElement("BlockEntityTag") != null) {
				this.banner.fromItem(itemStack, ShieldItem.getColor(itemStack));
				Minecraft.getInstance()
					.getTextureManager()
					.bind(BannerTextures.SHIELD_CACHE.getTextureLocation(this.banner.getTextureHashName(), this.banner.getPatterns(), this.banner.getColors()));
			} else {
				Minecraft.getInstance().getTextureManager().bind(BannerTextures.NO_PATTERN_SHIELD);
			}

			GlStateManager.pushMatrix();
			GlStateManager.scalef(1.0F, -1.0F, -1.0F);
			this.shieldModel.render();
			if (itemStack.hasFoil()) {
				this.renderFoil(this.shieldModel::render);
			}

			GlStateManager.popMatrix();
		} else if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
			GameProfile gameProfile = null;
			if (itemStack.hasTag()) {
				CompoundTag compoundTag = itemStack.getTag();
				if (compoundTag.contains("SkullOwner", 10)) {
					gameProfile = NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
				} else if (compoundTag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
					GameProfile var6 = new GameProfile(null, compoundTag.getString("SkullOwner"));
					gameProfile = SkullBlockEntity.updateGameprofile(var6);
					compoundTag.remove("SkullOwner");
					compoundTag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameProfile));
				}
			}

			if (SkullBlockRenderer.instance != null) {
				GlStateManager.pushMatrix();
				GlStateManager.disableCull();
				SkullBlockRenderer.instance
					.renderSkull(0.0F, 0.0F, 0.0F, null, 180.0F, ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType(), gameProfile, -1, 0.0F);
				GlStateManager.enableCull();
				GlStateManager.popMatrix();
			}
		} else if (item == Items.TRIDENT) {
			Minecraft.getInstance().getTextureManager().bind(TridentModel.TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.scalef(1.0F, -1.0F, -1.0F);
			this.tridentModel.render();
			if (itemStack.hasFoil()) {
				this.renderFoil(this.tridentModel::render);
			}

			GlStateManager.popMatrix();
		} else if (item instanceof BlockItem && ((BlockItem)item).getBlock() == Blocks.CONDUIT) {
			BlockEntityRenderDispatcher.instance.renderItem(this.conduit);
		} else if (item == Blocks.ENDER_CHEST.asItem()) {
			BlockEntityRenderDispatcher.instance.renderItem(this.enderChest);
		} else if (item == Blocks.TRAPPED_CHEST.asItem()) {
			BlockEntityRenderDispatcher.instance.renderItem(this.trappedChest);
		} else if (Block.byItem(item) instanceof ShulkerBoxBlock) {
			DyeColor dyeColor = ShulkerBoxBlock.getColorFromItem(item);
			if (dyeColor == null) {
				BlockEntityRenderDispatcher.instance.renderItem(DEFAULT_SHULKER_BOX);
			} else {
				BlockEntityRenderDispatcher.instance.renderItem(SHULKER_BOXES[dyeColor.getId()]);
			}
		} else {
			BlockEntityRenderDispatcher.instance.renderItem(this.chest);
		}
	}

	private void renderFoil(Runnable runnable) {
		GlStateManager.color3f(0.5019608F, 0.2509804F, 0.8F);
		Minecraft.getInstance().getTextureManager().bind(ItemRenderer.ENCHANT_GLINT_LOCATION);
		ItemRenderer.renderFoilLayer(Minecraft.getInstance().getTextureManager(), runnable, 1);
	}
}
