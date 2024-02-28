package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;

public record EntityEquipmentPredicate(
	Optional<ItemPredicate> head,
	Optional<ItemPredicate> chest,
	Optional<ItemPredicate> legs,
	Optional<ItemPredicate> feet,
	Optional<ItemPredicate> mainhand,
	Optional<ItemPredicate> offhand
) {
	public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "head").forGetter(EntityEquipmentPredicate::head),
					ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "chest").forGetter(EntityEquipmentPredicate::chest),
					ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "legs").forGetter(EntityEquipmentPredicate::legs),
					ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "feet").forGetter(EntityEquipmentPredicate::feet),
					ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "mainhand").forGetter(EntityEquipmentPredicate::mainhand),
					ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "offhand").forGetter(EntityEquipmentPredicate::offhand)
				)
				.apply(instance, EntityEquipmentPredicate::new)
	);
	public static final EntityEquipmentPredicate CAPTAIN = EntityEquipmentPredicate.Builder.equipment()
		.head(ItemPredicate.Builder.item().of(Items.WHITE_BANNER).hasComponents(DataComponentPredicate.allOf(Raid.getLeaderBannerInstance().getComponents())))
		.build();

	public boolean matches(@Nullable Entity entity) {
		if (entity instanceof LivingEntity livingEntity) {
			if (this.head.isPresent() && !((ItemPredicate)this.head.get()).matches(livingEntity.getItemBySlot(EquipmentSlot.HEAD))) {
				return false;
			} else if (this.chest.isPresent() && !((ItemPredicate)this.chest.get()).matches(livingEntity.getItemBySlot(EquipmentSlot.CHEST))) {
				return false;
			} else if (this.legs.isPresent() && !((ItemPredicate)this.legs.get()).matches(livingEntity.getItemBySlot(EquipmentSlot.LEGS))) {
				return false;
			} else if (this.feet.isPresent() && !((ItemPredicate)this.feet.get()).matches(livingEntity.getItemBySlot(EquipmentSlot.FEET))) {
				return false;
			} else {
				return this.mainhand.isPresent() && !((ItemPredicate)this.mainhand.get()).matches(livingEntity.getItemBySlot(EquipmentSlot.MAINHAND))
					? false
					: !this.offhand.isPresent() || ((ItemPredicate)this.offhand.get()).matches(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND));
			}
		} else {
			return false;
		}
	}

	public static class Builder {
		private Optional<ItemPredicate> head = Optional.empty();
		private Optional<ItemPredicate> chest = Optional.empty();
		private Optional<ItemPredicate> legs = Optional.empty();
		private Optional<ItemPredicate> feet = Optional.empty();
		private Optional<ItemPredicate> mainhand = Optional.empty();
		private Optional<ItemPredicate> offhand = Optional.empty();

		public static EntityEquipmentPredicate.Builder equipment() {
			return new EntityEquipmentPredicate.Builder();
		}

		public EntityEquipmentPredicate.Builder head(ItemPredicate.Builder builder) {
			this.head = Optional.of(builder.build());
			return this;
		}

		public EntityEquipmentPredicate.Builder chest(ItemPredicate.Builder builder) {
			this.chest = Optional.of(builder.build());
			return this;
		}

		public EntityEquipmentPredicate.Builder legs(ItemPredicate.Builder builder) {
			this.legs = Optional.of(builder.build());
			return this;
		}

		public EntityEquipmentPredicate.Builder feet(ItemPredicate.Builder builder) {
			this.feet = Optional.of(builder.build());
			return this;
		}

		public EntityEquipmentPredicate.Builder mainhand(ItemPredicate.Builder builder) {
			this.mainhand = Optional.of(builder.build());
			return this;
		}

		public EntityEquipmentPredicate.Builder offhand(ItemPredicate.Builder builder) {
			this.offhand = Optional.of(builder.build());
			return this;
		}

		public EntityEquipmentPredicate build() {
			return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.mainhand, this.offhand);
		}
	}
}
