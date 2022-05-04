package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotionItem extends Item {
	private static final int DRINK_DURATION = 32;

	public PotionItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		Player player = livingEntity instanceof Player ? (Player)livingEntity : null;
		if (player instanceof ServerPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, itemStack);
		}

		if (!level.isClientSide) {
			for (MobEffectInstance mobEffectInstance : PotionUtils.getMobEffects(itemStack)) {
				if (mobEffectInstance.getEffect().isInstantenous()) {
					mobEffectInstance.getEffect().applyInstantenousEffect(player, player, livingEntity, mobEffectInstance.getAmplifier(), 1.0);
				} else {
					livingEntity.addEffect(new MobEffectInstance(mobEffectInstance));
				}
			}
		}

		if (player != null) {
			player.awardStat(Stats.ITEM_USED.get(this));
			if (!player.getAbilities().instabuild) {
				itemStack.shrink(1);
			}
		}

		if (player == null || !player.getAbilities().instabuild) {
			if (itemStack.isEmpty()) {
				return new ItemStack(Items.GLASS_BOTTLE);
			}

			if (player != null) {
				player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
			}
		}

		level.gameEvent(livingEntity, GameEvent.DRINK, livingEntity.getEyePosition());
		return itemStack;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		Player player = useOnContext.getPlayer();
		ItemStack itemStack = useOnContext.getItemInHand();
		BlockState blockState = level.getBlockState(blockPos);
		if (useOnContext.getClickedFace() != Direction.DOWN && blockState.is(BlockTags.CONVERTABLE_TO_MUD) && PotionUtils.getPotion(itemStack) == Potions.WATER) {
			level.playSound(null, blockPos, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
			player.setItemInHand(useOnContext.getHand(), ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
			player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
			if (!level.isClientSide) {
				ServerLevel serverLevel = (ServerLevel)level;

				for (int i = 0; i < 5; i++) {
					serverLevel.sendParticles(
						ParticleTypes.SPLASH,
						(double)blockPos.getX() + level.random.nextDouble(),
						(double)(blockPos.getY() + 1),
						(double)blockPos.getZ() + level.random.nextDouble(),
						1,
						0.0,
						0.0,
						0.0,
						1.0
					);
				}
			}

			level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
			level.setBlockAndUpdate(blockPos, Blocks.MUD.defaultBlockState());
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 32;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.DRINK;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		return ItemUtils.startUsingInstantly(level, player, interactionHand);
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return PotionUtils.getPotion(itemStack).getName(this.getDescriptionId() + ".effect.");
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		PotionUtils.addPotionTooltip(itemStack, list, 1.0F);
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return super.isFoil(itemStack) || !PotionUtils.getMobEffects(itemStack).isEmpty();
	}

	@Override
	public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
		if (this.allowedIn(creativeModeTab)) {
			for (Potion potion : Registry.POTION) {
				if (potion != Potions.EMPTY) {
					nonNullList.add(PotionUtils.setPotion(new ItemStack(this), potion));
				}
			}
		}
	}
}
