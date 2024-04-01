package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PotatoPeelsBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PotatoPeelerItem extends Item {
	public PotatoPeelerItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return !player.isCreative();
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
		return true;
	}

	public static ItemAttributeModifiers createAttributes(int i, float f) {
		return ItemAttributeModifiers.builder()
			.add(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)i, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.add(
				Attributes.ATTACK_SPEED,
				new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)f, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.build();
	}

	private static float getPeelSoundPitch(Level level) {
		return level.random.nextFloat(0.8F, 1.2F);
	}

	public static void playPeelSound(Level level, @Nullable Player player, BlockPos blockPos, SoundSource soundSource) {
		level.playSound(player, blockPos, SoundEvents.ENTITY_POTATO_PEEL, soundSource, 1.0F, getPeelSoundPitch(level));
	}

	public static void playPeelSound(Level level, Entity entity) {
		entity.playSound(SoundEvents.ENTITY_POTATO_PEEL, 1.0F, getPeelSoundPitch(level));
	}

	public static void playPeelSound(Level level, Entity entity, SoundSource soundSource) {
		level.playSound(null, entity, SoundEvents.ENTITY_POTATO_PEEL, soundSource, 1.0F, getPeelSoundPitch(level));
	}

	private static InteractionResult peelBlock(UseOnContext useOnContext, ItemStack itemStack, BlockState blockState) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		Player player = useOnContext.getPlayer();
		ItemStack itemStack2 = useOnContext.getItemInHand();
		playPeelSound(level, player, blockPos, SoundSource.BLOCKS);
		level.setBlockAndUpdate(blockPos, blockState);
		if (level instanceof ServerLevel) {
			if (blockState.isAir()) {
				Block.popResource(level, blockPos, itemStack);
			} else {
				Block.popResourceFromFace(level, blockPos, useOnContext.getClickedFace(), itemStack);
			}
		}

		if (player != null) {
			itemStack2.hurtAndBreak(1, player, LivingEntity.getSlotForHand(useOnContext.getHand()));
		}

		if (player instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.PEEL_BLOCK.trigger(serverPlayer);
		}

		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block instanceof PotatoPeelsBlock potatoPeelsBlock) {
			return peelBlock(useOnContext, new ItemStack(potatoPeelsBlock.getPeelsItem(), 9), Blocks.AIR.defaultBlockState());
		} else if (block == Blocks.PEELGRASS_BLOCK && useOnContext.getClickedFace() == Direction.UP) {
			return peelBlock(useOnContext, Items.POTATO_PEELS_MAP.get(PotatoPeelItem.PEELGRASS_PEEL_COLOR).getDefaultInstance(), Blocks.TERREDEPOMME.defaultBlockState());
		} else if (block == Blocks.CORRUPTED_PEELGRASS_BLOCK && useOnContext.getClickedFace() == Direction.UP) {
			return peelBlock(useOnContext, Items.CORRUPTED_POTATO_PEELS.getDefaultInstance(), Blocks.TERREDEPOMME.defaultBlockState());
		} else {
			return block == Blocks.POISONOUS_POTATO_BLOCK
				? peelBlock(useOnContext, new ItemStack(Items.POISONOUS_POTATO, 9), ((Block)Blocks.POTATO_PEELS_BLOCK_MAP.get(DyeColor.WHITE)).defaultBlockState())
				: super.useOn(useOnContext);
		}
	}
}
