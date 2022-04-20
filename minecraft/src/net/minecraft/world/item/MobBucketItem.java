package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public class MobBucketItem extends BucketItem {
	private final EntityType<?> type;
	private final SoundEvent emptySound;

	public MobBucketItem(EntityType<?> entityType, Fluid fluid, SoundEvent soundEvent, Item.Properties properties) {
		super(fluid, properties);
		this.type = entityType;
		this.emptySound = soundEvent;
	}

	@Override
	public void checkExtraContent(@Nullable Player player, Level level, ItemStack itemStack, BlockPos blockPos) {
		if (level instanceof ServerLevel) {
			this.spawn((ServerLevel)level, itemStack, blockPos);
			level.gameEvent(player, GameEvent.ENTITY_PLACE, blockPos);
		}
	}

	@Override
	protected void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos) {
		levelAccessor.playSound(player, blockPos, this.emptySound, SoundSource.NEUTRAL, 1.0F, 1.0F);
	}

	private void spawn(ServerLevel serverLevel, ItemStack itemStack, BlockPos blockPos) {
		if (this.type.spawn(serverLevel, itemStack, null, blockPos, MobSpawnType.BUCKET, true, false) instanceof Bucketable bucketable) {
			bucketable.loadFromBucketTag(itemStack.getOrCreateTag());
			bucketable.setFromBucket(true);
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		if (this.type == EntityType.TROPICAL_FISH) {
			CompoundTag compoundTag = itemStack.getTag();
			if (compoundTag != null && compoundTag.contains("BucketVariantTag", 3)) {
				int i = compoundTag.getInt("BucketVariantTag");
				ChatFormatting[] chatFormattings = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};
				String string = "color.minecraft." + TropicalFish.getBaseColor(i);
				String string2 = "color.minecraft." + TropicalFish.getPatternColor(i);

				for (int j = 0; j < TropicalFish.COMMON_VARIANTS.length; j++) {
					if (i == TropicalFish.COMMON_VARIANTS[j]) {
						list.add(Component.translatable(TropicalFish.getPredefinedName(j)).withStyle(chatFormattings));
						return;
					}
				}

				list.add(Component.translatable(TropicalFish.getFishTypeName(i)).withStyle(chatFormattings));
				MutableComponent mutableComponent = Component.translatable(string);
				if (!string.equals(string2)) {
					mutableComponent.append(", ").append(Component.translatable(string2));
				}

				mutableComponent.withStyle(chatFormattings);
				list.add(mutableComponent);
			}
		}
	}
}
