package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;

public record EntityEquipmentPredicate(
	Optional<ItemPredicate> head,
	Optional<ItemPredicate> chest,
	Optional<ItemPredicate> legs,
	Optional<ItemPredicate> feet,
	Optional<ItemPredicate> body,
	Optional<ItemPredicate> mainhand,
	Optional<ItemPredicate> offhand
) {
	public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ItemPredicate.CODEC.optionalFieldOf("head").forGetter(EntityEquipmentPredicate::head),
					ItemPredicate.CODEC.optionalFieldOf("chest").forGetter(EntityEquipmentPredicate::chest),
					ItemPredicate.CODEC.optionalFieldOf("legs").forGetter(EntityEquipmentPredicate::legs),
					ItemPredicate.CODEC.optionalFieldOf("feet").forGetter(EntityEquipmentPredicate::feet),
					ItemPredicate.CODEC.optionalFieldOf("body").forGetter(EntityEquipmentPredicate::body),
					ItemPredicate.CODEC.optionalFieldOf("mainhand").forGetter(EntityEquipmentPredicate::mainhand),
					ItemPredicate.CODEC.optionalFieldOf("offhand").forGetter(EntityEquipmentPredicate::offhand)
				)
				.apply(instance, EntityEquipmentPredicate::new)
	);

	public static EntityEquipmentPredicate captainPredicate(HolderGetter<Item> holderGetter, HolderGetter<BannerPattern> holderGetter2) {
		return EntityEquipmentPredicate.Builder.equipment()
			.head(
				ItemPredicate.Builder.item()
					.of(holderGetter, Items.WHITE_BANNER)
					.hasComponents(DataComponentPredicate.allOf(Raid.getOminousBannerInstance(holderGetter2).getComponents()))
			)
			.build();
	}

	public boolean matches(@Nullable Entity entity) {
		if (entity instanceof LivingEntity livingEntity) {
			if (this.head.isPresent() && !((ItemPredicate)this.head.get()).test(livingEntity.getItemBySlot(EquipmentSlot.HEAD))) {
				return false;
			} else if (this.chest.isPresent() && !((ItemPredicate)this.chest.get()).test(livingEntity.getItemBySlot(EquipmentSlot.CHEST))) {
				return false;
			} else if (this.legs.isPresent() && !((ItemPredicate)this.legs.get()).test(livingEntity.getItemBySlot(EquipmentSlot.LEGS))) {
				return false;
			} else if (this.feet.isPresent() && !((ItemPredicate)this.feet.get()).test(livingEntity.getItemBySlot(EquipmentSlot.FEET))) {
				return false;
			} else if (this.body.isPresent() && !((ItemPredicate)this.body.get()).test(livingEntity.getItemBySlot(EquipmentSlot.BODY))) {
				return false;
			} else {
				return this.mainhand.isPresent() && !((ItemPredicate)this.mainhand.get()).test(livingEntity.getItemBySlot(EquipmentSlot.MAINHAND))
					? false
					: !this.offhand.isPresent() || ((ItemPredicate)this.offhand.get()).test(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND));
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
		private Optional<ItemPredicate> body = Optional.empty();
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

		public EntityEquipmentPredicate.Builder body(ItemPredicate.Builder builder) {
			this.body = Optional.of(builder.build());
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
			return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.body, this.mainhand, this.offhand);
		}
	}
}
