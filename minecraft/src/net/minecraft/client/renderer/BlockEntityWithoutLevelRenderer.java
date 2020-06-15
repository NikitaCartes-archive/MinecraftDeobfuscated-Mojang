package net.minecraft.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class BlockEntityWithoutLevelRenderer {
	private static final ShulkerBoxBlockEntity[] SHULKER_BOXES = (ShulkerBoxBlockEntity[])Arrays.stream(DyeColor.values())
		.sorted(Comparator.comparingInt(DyeColor::getId))
		.map(ShulkerBoxBlockEntity::new)
		.toArray(ShulkerBoxBlockEntity[]::new);
	private static final ShulkerBoxBlockEntity DEFAULT_SHULKER_BOX = new ShulkerBoxBlockEntity(null);
	public static final BlockEntityWithoutLevelRenderer instance = new BlockEntityWithoutLevelRenderer();
	private final ChestBlockEntity chest = new ChestBlockEntity();
	private final ChestBlockEntity trappedChest = new TrappedChestBlockEntity();
	private final EnderChestBlockEntity enderChest = new EnderChestBlockEntity();
	private final BannerBlockEntity banner = new BannerBlockEntity();
	private final BedBlockEntity bed = new BedBlockEntity();
	private final ConduitBlockEntity conduit = new ConduitBlockEntity();
	private final ShieldModel shieldModel = new ShieldModel();
	private final TridentModel tridentModel = new TridentModel();

	public void renderByItem(
		ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j
	) {
		Item item = itemStack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem)item).getBlock();
			if (block instanceof AbstractSkullBlock) {
				GameProfile gameProfile = null;
				if (itemStack.hasTag()) {
					CompoundTag compoundTag = itemStack.getTag();
					if (compoundTag.contains("SkullOwner", 10)) {
						gameProfile = NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
					} else if (compoundTag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
						GameProfile var16 = new GameProfile(null, compoundTag.getString("SkullOwner"));
						gameProfile = SkullBlockEntity.updateGameprofile(var16);
						compoundTag.remove("SkullOwner");
						compoundTag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameProfile));
					}
				}

				SkullBlockRenderer.renderSkull(null, 180.0F, ((AbstractSkullBlock)block).getType(), gameProfile, 0.0F, poseStack, multiBufferSource, i);
			} else {
				BlockEntity blockEntity;
				if (block instanceof AbstractBannerBlock) {
					this.banner.fromItem(itemStack, ((AbstractBannerBlock)block).getColor());
					blockEntity = this.banner;
				} else if (block instanceof BedBlock) {
					this.bed.setColor(((BedBlock)block).getColor());
					blockEntity = this.bed;
				} else if (block == Blocks.CONDUIT) {
					blockEntity = this.conduit;
				} else if (block == Blocks.CHEST) {
					blockEntity = this.chest;
				} else if (block == Blocks.ENDER_CHEST) {
					blockEntity = this.enderChest;
				} else if (block == Blocks.TRAPPED_CHEST) {
					blockEntity = this.trappedChest;
				} else {
					if (!(block instanceof ShulkerBoxBlock)) {
						return;
					}

					DyeColor dyeColor = ShulkerBoxBlock.getColorFromItem(item);
					if (dyeColor == null) {
						blockEntity = DEFAULT_SHULKER_BOX;
					} else {
						blockEntity = SHULKER_BOXES[dyeColor.getId()];
					}
				}

				BlockEntityRenderDispatcher.instance.renderItem(blockEntity, poseStack, multiBufferSource, i, j);
			}
		} else {
			if (item == Items.SHIELD) {
				boolean bl = itemStack.getTagElement("BlockEntityTag") != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material material = bl ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
				VertexConsumer vertexConsumer = material.sprite()
					.wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, this.shieldModel.renderType(material.atlasLocation()), true, itemStack.hasFoil()));
				this.shieldModel.handle().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				if (bl) {
					List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(itemStack), BannerBlockEntity.getItemPatterns(itemStack));
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, this.shieldModel.plate(), material, false, list, itemStack.hasFoil());
				} else {
					this.shieldModel.plate().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				}

				poseStack.popPose();
			} else if (item == Items.TRIDENT) {
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				VertexConsumer vertexConsumer2 = ItemRenderer.getFoilBufferDirect(
					multiBufferSource, this.tridentModel.renderType(TridentModel.TEXTURE), false, itemStack.hasFoil()
				);
				this.tridentModel.renderToBuffer(poseStack, vertexConsumer2, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				poseStack.popPose();
			}
		}
	}
}
