package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.equipment.ArmorMaterial;

public class AnimalArmorItem extends Item {
	private final AnimalArmorItem.BodyType bodyType;

	public AnimalArmorItem(ArmorMaterial armorMaterial, AnimalArmorItem.BodyType bodyType, Item.Properties properties) {
		super(armorMaterial.animalProperties(properties, bodyType.allowedEntities));
		this.bodyType = bodyType;
	}

	public AnimalArmorItem(ArmorMaterial armorMaterial, AnimalArmorItem.BodyType bodyType, Holder<SoundEvent> holder, boolean bl, Item.Properties properties) {
		super(armorMaterial.animalProperties(properties, holder, bl, bodyType.allowedEntities));
		this.bodyType = bodyType;
	}

	@Override
	public SoundEvent getBreakingSound() {
		return this.bodyType.breakingSound;
	}

	public static enum BodyType {
		EQUESTRIAN(SoundEvents.ITEM_BREAK, EntityType.HORSE),
		CANINE(SoundEvents.WOLF_ARMOR_BREAK, EntityType.WOLF);

		final SoundEvent breakingSound;
		final HolderSet<EntityType<?>> allowedEntities;

		private BodyType(final SoundEvent soundEvent, final EntityType<?>... entityTypes) {
			this.breakingSound = soundEvent;
			this.allowedEntities = HolderSet.direct(EntityType::builtInRegistryHolder, entityTypes);
		}
	}
}
