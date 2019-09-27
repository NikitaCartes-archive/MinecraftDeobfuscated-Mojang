package net.minecraft.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.AbstractBannerBlock;
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
	private final ConduitBlockEntity conduit = new ConduitBlockEntity();
	private final ShieldModel shieldModel = new ShieldModel();
	private final TridentModel tridentModel = new TridentModel();

	public void renderByItem(ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		Item item = itemStack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem)item).getBlock();
			if (block instanceof AbstractBannerBlock) {
				this.banner.fromItem(itemStack, ((AbstractBannerBlock)block).getColor());
				BlockEntityRenderDispatcher.instance.renderItem(this.banner, poseStack, multiBufferSource, i);
			} else if (block instanceof BedBlock) {
				this.bed.setColor(((BedBlock)block).getColor());
				BlockEntityRenderDispatcher.instance.renderItem(this.bed, poseStack, multiBufferSource, i);
			} else if (block instanceof AbstractSkullBlock) {
				GameProfile gameProfile = null;
				if (itemStack.hasTag()) {
					CompoundTag compoundTag = itemStack.getTag();
					if (compoundTag.contains("SkullOwner", 10)) {
						gameProfile = NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
					} else if (compoundTag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
						GameProfile var12 = new GameProfile(null, compoundTag.getString("SkullOwner"));
						gameProfile = SkullBlockEntity.updateGameprofile(var12);
						compoundTag.remove("SkullOwner");
						compoundTag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameProfile));
					}
				}

				SkullBlockRenderer.renderSkull(null, 180.0F, ((AbstractSkullBlock)block).getType(), gameProfile, 0.0F, poseStack, multiBufferSource, i);
			} else if (block == Blocks.CONDUIT) {
				BlockEntityRenderDispatcher.instance.renderItem(this.conduit, poseStack, multiBufferSource, i);
			} else if (block == Blocks.CHEST) {
				BlockEntityRenderDispatcher.instance.renderItem(this.chest, poseStack, multiBufferSource, i);
			} else if (block == Blocks.ENDER_CHEST) {
				BlockEntityRenderDispatcher.instance.renderItem(this.enderChest, poseStack, multiBufferSource, i);
			} else if (block == Blocks.TRAPPED_CHEST) {
				BlockEntityRenderDispatcher.instance.renderItem(this.trappedChest, poseStack, multiBufferSource, i);
			} else if (block instanceof ShulkerBoxBlock) {
				DyeColor dyeColor = ShulkerBoxBlock.getColorFromItem(item);
				if (dyeColor == null) {
					BlockEntityRenderDispatcher.instance.renderItem(DEFAULT_SHULKER_BOX, poseStack, multiBufferSource, i);
				} else {
					BlockEntityRenderDispatcher.instance.renderItem(SHULKER_BOXES[dyeColor.getId()], poseStack, multiBufferSource, i);
				}
			}
		} else {
			if (item == Items.SHIELD) {
				ResourceLocation resourceLocation;
				if (itemStack.getTagElement("BlockEntityTag") != null) {
					this.banner.fromItem(itemStack, ShieldItem.getColor(itemStack));
					resourceLocation = BannerTextures.SHIELD_CACHE.getTextureLocation(this.banner.getTextureHashName(), this.banner.getPatterns(), this.banner.getColors());
				} else {
					resourceLocation = BannerTextures.NO_PATTERN_SHIELD;
				}

				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				VertexConsumer vertexConsumer = ItemRenderer.getFoilBuffer(multiBufferSource, resourceLocation, false, itemStack.hasFoil(), false);
				OverlayTexture.setDefault(vertexConsumer);
				this.shieldModel.render(poseStack, vertexConsumer, i);
				vertexConsumer.unsetDefaultOverlayCoords();
				poseStack.popPose();
			} else if (item == Items.TRIDENT) {
				Minecraft.getInstance().getTextureManager().bind(TridentModel.TEXTURE);
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				VertexConsumer vertexConsumer2 = ItemRenderer.getFoilBuffer(multiBufferSource, TridentModel.TEXTURE, false, itemStack.hasFoil(), false);
				OverlayTexture.setDefault(vertexConsumer2);
				this.tridentModel.render(poseStack, vertexConsumer2, i);
				vertexConsumer2.unsetDefaultOverlayCoords();
				poseStack.popPose();
			}
		}
	}
}
