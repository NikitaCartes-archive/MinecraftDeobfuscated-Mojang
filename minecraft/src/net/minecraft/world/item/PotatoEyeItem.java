package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfPotato;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotatoEyeItem extends Item {
	public PotatoEyeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResultHolder.consume(itemStack);
		} else {
			if (player.isChapterAndProgressPast("crafted_eyes", 2)) {
				player.setPotatoQuestChapter("thrown_eye");
			}

			if (player.isChapterAndProgressPast("potato_village", 7)) {
				player.setPotatoQuestChapter("thrown_eye_part_two");
			}

			BlockPos blockPos;
			if (!serverLevel.dimension().equals(Level.OVERWORLD) && !serverLevel.isPotato()) {
				blockPos = null;
			} else {
				blockPos = serverLevel.findNearestMapStructure(
					serverLevel.isPotato() ? StructureTags.COLOSSEUM : StructureTags.RUINED_PORTATOL, player.blockPosition(), 100, false
				);
			}

			if (blockPos != null) {
				if (player instanceof ServerPlayer serverPlayer) {
					if (level.isPotato()) {
						serverPlayer.setColosseum(blockPos);
					} else {
						serverPlayer.setRuinedPortatol(blockPos);
					}
				}

				EyeOfPotato eyeOfPotato = new EyeOfPotato(level, player.getX(), player.getY(0.5), player.getZ());
				eyeOfPotato.setItem(itemStack);
				eyeOfPotato.signalTo(blockPos);
				level.gameEvent(GameEvent.PROJECTILE_SHOOT, eyeOfPotato.position(), GameEvent.Context.of(player));
				level.addFreshEntity(eyeOfPotato);
				level.playSound(
					null,
					player.getX(),
					player.getY(),
					player.getZ(),
					SoundEvents.ENDER_EYE_LAUNCH,
					SoundSource.NEUTRAL,
					0.5F,
					0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
				);
				level.levelEvent(null, 1003, player.blockPosition(), 0);
				itemStack.consume(1, player);
				player.awardStat(Stats.ITEM_USED.get(this));
				player.swing(interactionHand, true);
			} else {
				level.playSound(
					null,
					player.getX(),
					player.getY(),
					player.getZ(),
					SoundEvents.ENDER_EYE_LAUNCH,
					SoundSource.NEUTRAL,
					0.5F,
					0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
				);
				itemStack.consume(1, player);
				player.drop(new ItemStack(Items.POTATO_EYE), true);
			}

			return InteractionResultHolder.success(itemStack);
		}
	}
}
