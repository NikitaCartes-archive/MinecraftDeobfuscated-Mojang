package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotionItem extends Item {
	public PotionItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		ItemStack itemStack = super.getDefaultInstance();
		itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
		return itemStack;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		Player player = useOnContext.getPlayer();
		ItemStack itemStack = useOnContext.getItemInHand();
		PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		BlockState blockState = level.getBlockState(blockPos);
		if (useOnContext.getClickedFace() != Direction.DOWN && blockState.is(BlockTags.CONVERTABLE_TO_MUD) && potionContents.is(Potions.WATER)) {
			level.playSound(null, blockPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
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
			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return Potion.getName(itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion(), this.getDescriptionId() + ".effect.");
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
		if (potionContents != null) {
			potionContents.addPotionTooltip(list::add, 1.0F, tooltipContext.tickRate());
		}
	}
}
