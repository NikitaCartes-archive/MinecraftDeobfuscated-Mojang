package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketItem extends Item {
	public static final String TAG_FIREWORKS = "Fireworks";
	public static final String TAG_EXPLOSION = "Explosion";
	public static final String TAG_EXPLOSIONS = "Explosions";
	public static final String TAG_FLIGHT = "Flight";
	public static final String TAG_EXPLOSION_TYPE = "Type";
	public static final String TAG_EXPLOSION_TRAIL = "Trail";
	public static final String TAG_EXPLOSION_FLICKER = "Flicker";
	public static final String TAG_EXPLOSION_COLORS = "Colors";
	public static final String TAG_EXPLOSION_FADECOLORS = "FadeColors";
	public static final double ROCKET_PLACEMENT_OFFSET = 0.15;

	public FireworkRocketItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		if (!level.isClientSide) {
			ItemStack itemStack = useOnContext.getItemInHand();
			Vec3 vec3 = useOnContext.getClickLocation();
			Direction direction = useOnContext.getClickedFace();
			FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(
				level,
				useOnContext.getPlayer(),
				vec3.x + (double)direction.getStepX() * 0.15,
				vec3.y + (double)direction.getStepY() * 0.15,
				vec3.z + (double)direction.getStepZ() * 0.15,
				itemStack
			);
			level.addFreshEntity(fireworkRocketEntity);
			itemStack.shrink(1);
		}

		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		if (player.isFallFlying()) {
			ItemStack itemStack = player.getItemInHand(interactionHand);
			if (!level.isClientSide) {
				FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(level, itemStack, player);
				level.addFreshEntity(fireworkRocketEntity);
				if (!player.getAbilities().instabuild) {
					itemStack.shrink(1);
				}

				player.awardStat(Stats.ITEM_USED.get(this));
			}

			return InteractionResultHolder.sidedSuccess(player.getItemInHand(interactionHand), level.isClientSide());
		} else {
			return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		CompoundTag compoundTag = itemStack.getTagElement("Fireworks");
		if (compoundTag != null) {
			if (compoundTag.contains("Flight", 99)) {
				list.add(
					Component.translatable("item.minecraft.firework_rocket.flight")
						.append(" ")
						.append(String.valueOf(compoundTag.getByte("Flight")))
						.withStyle(ChatFormatting.GRAY)
				);
			}

			ListTag listTag = compoundTag.getList("Explosions", 10);
			if (!listTag.isEmpty()) {
				for (int i = 0; i < listTag.size(); i++) {
					CompoundTag compoundTag2 = listTag.getCompound(i);
					List<Component> list2 = Lists.<Component>newArrayList();
					FireworkStarItem.appendHoverText(compoundTag2, list2);
					if (!list2.isEmpty()) {
						for (int j = 1; j < list2.size(); j++) {
							list2.set(j, Component.literal("  ").append((Component)list2.get(j)).withStyle(ChatFormatting.GRAY));
						}

						list.addAll(list2);
					}
				}
			}
		}
	}

	@Override
	public ItemStack getDefaultInstance() {
		ItemStack itemStack = new ItemStack(this);
		itemStack.getOrCreateTag().putByte("Flight", (byte)1);
		return itemStack;
	}

	public static enum Shape {
		SMALL_BALL(0, "small_ball"),
		LARGE_BALL(1, "large_ball"),
		STAR(2, "star"),
		CREEPER(3, "creeper"),
		BURST(4, "burst");

		private static final FireworkRocketItem.Shape[] BY_ID = (FireworkRocketItem.Shape[])Arrays.stream(values())
			.sorted(Comparator.comparingInt(shape -> shape.id))
			.toArray(FireworkRocketItem.Shape[]::new);
		private final int id;
		private final String name;

		private Shape(int j, String string2) {
			this.id = j;
			this.name = string2;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static FireworkRocketItem.Shape byId(int i) {
			return i >= 0 && i < BY_ID.length ? BY_ID[i] : SMALL_BALL;
		}
	}
}
