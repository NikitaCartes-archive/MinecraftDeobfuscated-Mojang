package net.minecraft.world.item.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record Equippable(
	EquipmentSlot slot,
	Holder<SoundEvent> equipSound,
	Optional<ResourceLocation> model,
	Optional<ResourceLocation> cameraOverlay,
	Optional<HolderSet<EntityType<?>>> allowedEntities,
	boolean dispensable,
	boolean swappable,
	boolean damageOnHurt
) {
	public static final Codec<Equippable> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					EquipmentSlot.CODEC.fieldOf("slot").forGetter(Equippable::slot),
					SoundEvent.CODEC.optionalFieldOf("equip_sound", SoundEvents.ARMOR_EQUIP_GENERIC).forGetter(Equippable::equipSound),
					ResourceLocation.CODEC.optionalFieldOf("model").forGetter(Equippable::model),
					ResourceLocation.CODEC.optionalFieldOf("camera_overlay").forGetter(Equippable::cameraOverlay),
					RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).optionalFieldOf("allowed_entities").forGetter(Equippable::allowedEntities),
					Codec.BOOL.optionalFieldOf("dispensable", Boolean.valueOf(true)).forGetter(Equippable::dispensable),
					Codec.BOOL.optionalFieldOf("swappable", Boolean.valueOf(true)).forGetter(Equippable::swappable),
					Codec.BOOL.optionalFieldOf("damage_on_hurt", Boolean.valueOf(true)).forGetter(Equippable::damageOnHurt)
				)
				.apply(instance, Equippable::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Equippable> STREAM_CODEC = StreamCodec.composite(
		EquipmentSlot.STREAM_CODEC,
		Equippable::slot,
		SoundEvent.STREAM_CODEC,
		Equippable::equipSound,
		ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional),
		Equippable::model,
		ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional),
		Equippable::cameraOverlay,
		ByteBufCodecs.holderSet(Registries.ENTITY_TYPE).apply(ByteBufCodecs::optional),
		Equippable::allowedEntities,
		ByteBufCodecs.BOOL,
		Equippable::dispensable,
		ByteBufCodecs.BOOL,
		Equippable::swappable,
		ByteBufCodecs.BOOL,
		Equippable::damageOnHurt,
		Equippable::new
	);

	public static Equippable llamaSwag(DyeColor dyeColor) {
		return builder(EquipmentSlot.BODY)
			.setEquipSound(SoundEvents.LLAMA_SWAG)
			.setModel((ResourceLocation)EquipmentModels.CARPETS.get(dyeColor))
			.setAllowedEntities(EntityType.LLAMA, EntityType.TRADER_LLAMA)
			.build();
	}

	public static Equippable.Builder builder(EquipmentSlot equipmentSlot) {
		return new Equippable.Builder(equipmentSlot);
	}

	public InteractionResult swapWithEquipmentSlot(ItemStack itemStack, Player player) {
		if (!player.canUseSlot(this.slot)) {
			return InteractionResult.PASS;
		} else {
			ItemStack itemStack2 = player.getItemBySlot(this.slot);
			if ((!EnchantmentHelper.has(itemStack2, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || player.isCreative())
				&& !ItemStack.isSameItemSameComponents(itemStack, itemStack2)) {
				if (!player.level().isClientSide()) {
					player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
				}

				if (itemStack.getCount() <= 1) {
					ItemStack itemStack3 = itemStack2.isEmpty() ? itemStack : itemStack2.copyAndClear();
					ItemStack itemStack4 = player.isCreative() ? itemStack.copy() : itemStack.copyAndClear();
					player.setItemSlot(this.slot, itemStack4);
					return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack3);
				} else {
					ItemStack itemStack3 = itemStack2.copyAndClear();
					ItemStack itemStack4 = itemStack.consumeAndReturn(1, player);
					player.setItemSlot(this.slot, itemStack4);
					if (!player.getInventory().add(itemStack3)) {
						player.drop(itemStack3, false);
					}

					return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack);
				}
			} else {
				return InteractionResult.FAIL;
			}
		}
	}

	public boolean canBeEquippedBy(EntityType<?> entityType) {
		return this.allowedEntities.isEmpty() || ((HolderSet)this.allowedEntities.get()).contains(entityType.builtInRegistryHolder());
	}

	public static class Builder {
		private final EquipmentSlot slot;
		private Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_GENERIC;
		private Optional<ResourceLocation> model = Optional.empty();
		private Optional<ResourceLocation> cameraOverlay = Optional.empty();
		private Optional<HolderSet<EntityType<?>>> allowedEntities = Optional.empty();
		private boolean dispensable = true;
		private boolean swappable = true;
		private boolean damageOnHurt = true;

		Builder(EquipmentSlot equipmentSlot) {
			this.slot = equipmentSlot;
		}

		public Equippable.Builder setEquipSound(Holder<SoundEvent> holder) {
			this.equipSound = holder;
			return this;
		}

		public Equippable.Builder setModel(ResourceLocation resourceLocation) {
			this.model = Optional.of(resourceLocation);
			return this;
		}

		public Equippable.Builder setCameraOverlay(ResourceLocation resourceLocation) {
			this.cameraOverlay = Optional.of(resourceLocation);
			return this;
		}

		public Equippable.Builder setAllowedEntities(EntityType<?>... entityTypes) {
			return this.setAllowedEntities(HolderSet.direct(EntityType::builtInRegistryHolder, entityTypes));
		}

		public Equippable.Builder setAllowedEntities(HolderSet<EntityType<?>> holderSet) {
			this.allowedEntities = Optional.of(holderSet);
			return this;
		}

		public Equippable.Builder setDispensable(boolean bl) {
			this.dispensable = bl;
			return this;
		}

		public Equippable.Builder setSwappable(boolean bl) {
			this.swappable = bl;
			return this;
		}

		public Equippable.Builder setDamageOnHurt(boolean bl) {
			this.damageOnHurt = bl;
			return this;
		}

		public Equippable build() {
			return new Equippable(this.slot, this.equipSound, this.model, this.cameraOverlay, this.allowedEntities, this.dispensable, this.swappable, this.damageOnHurt);
		}
	}
}
