package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class ArmorItem extends Item implements Equipable {
	private static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
		enumMap.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
		enumMap.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
		enumMap.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
		enumMap.put(ArmorItem.Type.BODY, UUID.fromString("C1C72771-8B8E-BA4A-ACE0-81A93C8928B2"));
	});
	public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
		@Override
		protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
			return ArmorItem.dispenseArmor(blockSource, itemStack) ? itemStack : super.execute(blockSource, itemStack);
		}
	};
	protected final ArmorItem.Type type;
	protected final Holder<ArmorMaterial> material;
	private final Supplier<Multimap<Holder<Attribute>, AttributeModifier>> defaultModifiers;

	public static boolean dispenseArmor(BlockSource blockSource, ItemStack itemStack) {
		BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
		List<LivingEntity> list = blockSource.level()
			.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS.and(new EntitySelector.MobCanWearArmorEntitySelector(itemStack)));
		if (list.isEmpty()) {
			return false;
		} else {
			LivingEntity livingEntity = (LivingEntity)list.get(0);
			EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
			ItemStack itemStack2 = itemStack.split(1);
			livingEntity.setItemSlot(equipmentSlot, itemStack2);
			if (livingEntity instanceof Mob) {
				((Mob)livingEntity).setDropChance(equipmentSlot, 2.0F);
				((Mob)livingEntity).setPersistenceRequired();
			}

			return true;
		}
	}

	public ArmorItem(Holder<ArmorMaterial> holder, ArmorItem.Type type, Item.Properties properties) {
		super(properties);
		this.material = holder;
		this.type = type;
		DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
		this.defaultModifiers = Suppliers.memoize(() -> {
			int i = holder.value().getDefense(type);
			float f = holder.value().toughness();
			Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
			UUID uUID = (UUID)ARMOR_MODIFIER_UUID_PER_TYPE.get(type);
			builder.put(Attributes.ARMOR, new AttributeModifier(uUID, "Armor modifier", (double)i, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uUID, "Armor toughness", (double)f, AttributeModifier.Operation.ADDITION));
			float g = holder.value().knockbackResistance();
			if (g > 0.0F) {
				builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uUID, "Armor knockback resistance", (double)g, AttributeModifier.Operation.ADDITION));
			}

			return builder.build();
		});
	}

	public ArmorItem.Type getType() {
		return this.type;
	}

	@Override
	public int getEnchantmentValue() {
		return this.material.value().enchantmentValue();
	}

	public Holder<ArmorMaterial> getMaterial() {
		return this.material;
	}

	@Override
	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return ((Ingredient)this.material.value().repairIngredient().get()).test(itemStack2) || super.isValidRepairItem(itemStack, itemStack2);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		return this.swapWithEquipmentSlot(this, level, player, interactionHand);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		return equipmentSlot == this.type.getSlot() ? (Multimap)this.defaultModifiers.get() : super.getDefaultAttributeModifiers(equipmentSlot);
	}

	public int getDefense() {
		return this.material.value().getDefense(this.type);
	}

	public float getToughness() {
		return this.material.value().toughness();
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return this.type.getSlot();
	}

	@Override
	public Holder<SoundEvent> getEquipSound() {
		return this.getMaterial().value().equipSound();
	}

	public static enum Type implements StringRepresentable {
		HELMET(EquipmentSlot.HEAD, "helmet"),
		CHESTPLATE(EquipmentSlot.CHEST, "chestplate"),
		LEGGINGS(EquipmentSlot.LEGS, "leggings"),
		BOOTS(EquipmentSlot.FEET, "boots"),
		BODY(EquipmentSlot.BODY, "body");

		public static final Codec<ArmorItem.Type> CODEC = StringRepresentable.fromValues(ArmorItem.Type::values);
		private final EquipmentSlot slot;
		private final String name;

		private Type(EquipmentSlot equipmentSlot, String string2) {
			this.slot = equipmentSlot;
			this.name = string2;
		}

		public int getDurability(int i) {
			return switch (this) {
				case HELMET -> 11;
				case CHESTPLATE -> 16;
				case LEGGINGS -> 15;
				case BOOTS -> 13;
				case BODY -> 20;
			} * i;
		}

		public EquipmentSlot getSlot() {
			return this.slot;
		}

		public String getName() {
			return this.name;
		}

		public boolean hasTrims() {
			return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
