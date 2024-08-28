package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class BlockEntityWithoutLevelRenderer implements ResourceManagerReloadListener {
	private static final ShulkerBoxBlockEntity[] SHULKER_BOXES = (ShulkerBoxBlockEntity[])Arrays.stream(DyeColor.values())
		.sorted(Comparator.comparingInt(DyeColor::getId))
		.map(dyeColor -> new ShulkerBoxBlockEntity(dyeColor, BlockPos.ZERO, Blocks.SHULKER_BOX.defaultBlockState()))
		.toArray(ShulkerBoxBlockEntity[]::new);
	private static final ShulkerBoxBlockEntity DEFAULT_SHULKER_BOX = new ShulkerBoxBlockEntity(BlockPos.ZERO, Blocks.SHULKER_BOX.defaultBlockState());
	private final ChestBlockEntity chest = new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
	private final ChestBlockEntity trappedChest = new TrappedChestBlockEntity(BlockPos.ZERO, Blocks.TRAPPED_CHEST.defaultBlockState());
	private final EnderChestBlockEntity enderChest = new EnderChestBlockEntity(BlockPos.ZERO, Blocks.ENDER_CHEST.defaultBlockState());
	private final BannerBlockEntity banner = new BannerBlockEntity(BlockPos.ZERO, Blocks.WHITE_BANNER.defaultBlockState());
	private final BedBlockEntity bed = new BedBlockEntity(BlockPos.ZERO, Blocks.RED_BED.defaultBlockState());
	private final ConduitBlockEntity conduit = new ConduitBlockEntity(BlockPos.ZERO, Blocks.CONDUIT.defaultBlockState());
	private final DecoratedPotBlockEntity decoratedPot = new DecoratedPotBlockEntity(BlockPos.ZERO, Blocks.DECORATED_POT.defaultBlockState());
	private ShieldModel shieldModel;
	private TridentModel tridentModel;
	private Map<SkullBlock.Type, SkullModelBase> skullModels;
	private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
	private final EntityModelSet entityModelSet;

	public BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
		this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
		this.entityModelSet = entityModelSet;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.shieldModel = new ShieldModel(this.entityModelSet.bakeLayer(ModelLayers.SHIELD));
		this.tridentModel = new TridentModel(this.entityModelSet.bakeLayer(ModelLayers.TRIDENT));
		this.skullModels = SkullBlockRenderer.createSkullRenderers(this.entityModelSet);
	}

	public void renderByItem(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		Item item = itemStack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem)item).getBlock();
			if (block instanceof AbstractSkullBlock abstractSkullBlock) {
				ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
				if (resolvableProfile != null && !resolvableProfile.isResolved()) {
					itemStack.remove(DataComponents.PROFILE);
					resolvableProfile.resolve().thenAcceptAsync(resolvableProfilex -> itemStack.set(DataComponents.PROFILE, resolvableProfilex), Minecraft.getInstance());
					resolvableProfile = null;
				}

				SkullModelBase skullModelBase = (SkullModelBase)this.skullModels.get(abstractSkullBlock.getType());
				RenderType renderType = SkullBlockRenderer.getRenderType(abstractSkullBlock.getType(), resolvableProfile);
				SkullBlockRenderer.renderSkull(null, 180.0F, 0.0F, poseStack, multiBufferSource, i, skullModelBase, renderType);
			} else {
				BlockState blockState = block.defaultBlockState();
				BlockEntity blockEntity;
				if (block instanceof AbstractBannerBlock) {
					this.banner.fromItem(itemStack, ((AbstractBannerBlock)block).getColor());
					blockEntity = this.banner;
				} else if (block instanceof BedBlock) {
					this.bed.setColor(((BedBlock)block).getColor());
					blockEntity = this.bed;
				} else if (blockState.is(Blocks.CONDUIT)) {
					blockEntity = this.conduit;
				} else if (blockState.is(Blocks.CHEST)) {
					blockEntity = this.chest;
				} else if (blockState.is(Blocks.ENDER_CHEST)) {
					blockEntity = this.enderChest;
				} else if (blockState.is(Blocks.TRAPPED_CHEST)) {
					blockEntity = this.trappedChest;
				} else if (blockState.is(Blocks.DECORATED_POT)) {
					this.decoratedPot.setFromItem(itemStack);
					blockEntity = this.decoratedPot;
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

				this.blockEntityRenderDispatcher.renderItem(blockEntity, poseStack, multiBufferSource, i, j);
			}
		} else {
			if (itemStack.is(Items.SHIELD)) {
				BannerPatternLayers bannerPatternLayers = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
				DyeColor dyeColor2 = itemStack.get(DataComponents.BASE_COLOR);
				boolean bl = !bannerPatternLayers.layers().isEmpty() || dyeColor2 != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material material = bl ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
				VertexConsumer vertexConsumer = material.sprite()
					.wrap(
						ItemRenderer.getFoilBuffer(
							multiBufferSource, this.shieldModel.renderType(material.atlasLocation()), itemDisplayContext == ItemDisplayContext.GUI, itemStack.hasFoil()
						)
					);
				this.shieldModel.handle().render(poseStack, vertexConsumer, i, j);
				if (bl) {
					BannerRenderer.renderPatterns(
						poseStack,
						multiBufferSource,
						i,
						j,
						this.shieldModel.plate(),
						material,
						false,
						(DyeColor)Objects.requireNonNullElse(dyeColor2, DyeColor.WHITE),
						bannerPatternLayers,
						itemStack.hasFoil(),
						false
					);
				} else {
					this.shieldModel.plate().render(poseStack, vertexConsumer, i, j);
				}

				poseStack.popPose();
			} else if (itemStack.is(Items.TRIDENT)) {
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				VertexConsumer vertexConsumer2 = ItemRenderer.getFoilBuffer(
					multiBufferSource, this.tridentModel.renderType(TridentModel.TEXTURE), false, itemStack.hasFoil()
				);
				this.tridentModel.renderToBuffer(poseStack, vertexConsumer2, i, j);
				poseStack.popPose();
			}
		}
	}
}
