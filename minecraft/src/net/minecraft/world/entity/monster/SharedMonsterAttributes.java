package net.minecraft.world.entity.monster;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SharedMonsterAttributes {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Attribute MAX_HEALTH = new RangedAttribute(null, "generic.maxHealth", 20.0, 0.0, 1024.0).importLegacyName("Max Health").setSyncable(true);
	public static final Attribute FOLLOW_RANGE = new RangedAttribute(null, "generic.followRange", 32.0, 0.0, 2048.0).importLegacyName("Follow Range");
	public static final Attribute KNOCKBACK_RESISTANCE = new RangedAttribute(null, "generic.knockbackResistance", 0.0, 0.0, 1.0)
		.importLegacyName("Knockback Resistance");
	public static final Attribute MOVEMENT_SPEED = new RangedAttribute(null, "generic.movementSpeed", 0.7F, 0.0, 1024.0)
		.importLegacyName("Movement Speed")
		.setSyncable(true);
	public static final Attribute FLYING_SPEED = new RangedAttribute(null, "generic.flyingSpeed", 0.4F, 0.0, 1024.0)
		.importLegacyName("Flying Speed")
		.setSyncable(true);
	public static final Attribute ATTACK_DAMAGE = new RangedAttribute(null, "generic.attackDamage", 2.0, 0.0, 2048.0);
	public static final Attribute ATTACK_KNOCKBACK = new RangedAttribute(null, "generic.attackKnockback", 0.0, 0.0, 5.0);
	public static final Attribute ATTACK_SPEED = new RangedAttribute(null, "generic.attackSpeed", 4.0, 0.0, 1024.0).setSyncable(true);
	public static final Attribute ARMOR = new RangedAttribute(null, "generic.armor", 0.0, 0.0, 30.0).setSyncable(true);
	public static final Attribute ARMOR_TOUGHNESS = new RangedAttribute(null, "generic.armorToughness", 0.0, 0.0, 20.0).setSyncable(true);
	public static final Attribute LUCK = new RangedAttribute(null, "generic.luck", 0.0, -1024.0, 1024.0).setSyncable(true);

	public static ListTag saveAttributes(BaseAttributeMap baseAttributeMap) {
		ListTag listTag = new ListTag();

		for (AttributeInstance attributeInstance : baseAttributeMap.getAttributes()) {
			listTag.add(saveAttribute(attributeInstance));
		}

		return listTag;
	}

	private static CompoundTag saveAttribute(AttributeInstance attributeInstance) {
		CompoundTag compoundTag = new CompoundTag();
		Attribute attribute = attributeInstance.getAttribute();
		compoundTag.putString("Name", attribute.getName());
		compoundTag.putDouble("Base", attributeInstance.getBaseValue());
		Collection<AttributeModifier> collection = attributeInstance.getModifiers();
		if (collection != null && !collection.isEmpty()) {
			ListTag listTag = new ListTag();

			for (AttributeModifier attributeModifier : collection) {
				if (attributeModifier.isSerializable()) {
					listTag.add(saveAttributeModifier(attributeModifier));
				}
			}

			compoundTag.put("Modifiers", listTag);
		}

		return compoundTag;
	}

	public static CompoundTag saveAttributeModifier(AttributeModifier attributeModifier) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("Name", attributeModifier.getName());
		compoundTag.putDouble("Amount", attributeModifier.getAmount());
		compoundTag.putInt("Operation", attributeModifier.getOperation().toValue());
		compoundTag.putUUID("UUID", attributeModifier.getId());
		return compoundTag;
	}

	public static void loadAttributes(BaseAttributeMap baseAttributeMap, ListTag listTag) {
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			AttributeInstance attributeInstance = baseAttributeMap.getInstance(compoundTag.getString("Name"));
			if (attributeInstance == null) {
				LOGGER.warn("Ignoring unknown attribute '{}'", compoundTag.getString("Name"));
			} else {
				loadAttribute(attributeInstance, compoundTag);
			}
		}
	}

	private static void loadAttribute(AttributeInstance attributeInstance, CompoundTag compoundTag) {
		attributeInstance.setBaseValue(compoundTag.getDouble("Base"));
		if (compoundTag.contains("Modifiers", 9)) {
			ListTag listTag = compoundTag.getList("Modifiers", 10);

			for (int i = 0; i < listTag.size(); i++) {
				AttributeModifier attributeModifier = loadAttributeModifier(listTag.getCompound(i));
				if (attributeModifier != null) {
					AttributeModifier attributeModifier2 = attributeInstance.getModifier(attributeModifier.getId());
					if (attributeModifier2 != null) {
						attributeInstance.removeModifier(attributeModifier2);
					}

					attributeInstance.addModifier(attributeModifier);
				}
			}
		}
	}

	@Nullable
	public static AttributeModifier loadAttributeModifier(CompoundTag compoundTag) {
		UUID uUID = compoundTag.getUUID("UUID");

		try {
			AttributeModifier.Operation operation = AttributeModifier.Operation.fromValue(compoundTag.getInt("Operation"));
			return new AttributeModifier(uUID, compoundTag.getString("Name"), compoundTag.getDouble("Amount"), operation);
		} catch (Exception var3) {
			LOGGER.warn("Unable to create attribute: {}", var3.getMessage());
			return null;
		}
	}
}
