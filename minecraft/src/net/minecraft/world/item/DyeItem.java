package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class DyeItem extends Item implements SignApplicator {
	private static final Map<DyeColor, DyeItem> ITEM_BY_COLOR = Maps.newEnumMap(DyeColor.class);
	private final DyeColor dyeColor;

	public DyeItem(DyeColor dyeColor, Item.Properties properties) {
		super(properties);
		this.dyeColor = dyeColor;
		ITEM_BY_COLOR.put(dyeColor, this);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		if (livingEntity instanceof Sheep sheep && sheep.isAlive() && !sheep.isSheared() && sheep.getColor() != this.dyeColor) {
			sheep.level().playSound(player, sheep, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
			if (!player.level().isClientSide) {
				sheep.setColor(this.dyeColor);
				itemStack.shrink(1);
			}

			return InteractionResult.sidedSuccess(player.level().isClientSide);
		}

		return InteractionResult.PASS;
	}

	public DyeColor getDyeColor() {
		return this.dyeColor;
	}

	public static DyeItem byColor(DyeColor dyeColor) {
		return (DyeItem)ITEM_BY_COLOR.get(dyeColor);
	}

	@Override
	public boolean tryApplyToSign(Level level, SignBlockEntity signBlockEntity, boolean bl, Player player) {
		if (signBlockEntity.updateText(signText -> signText.setColor(this.getDyeColor()), bl)) {
			level.playSound(null, signBlockEntity.getBlockPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			return true;
		} else {
			return false;
		}
	}
}
