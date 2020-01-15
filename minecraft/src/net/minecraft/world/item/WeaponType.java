package net.minecraft.world.item;

import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;

enum WeaponType {
	SWORD,
	AXE,
	PICKAXE,
	HOE,
	SHOVEL,
	TRIDENT;

	protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
	protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
	protected static final UUID BASE_ATTACK_REACH_UUID = UUID.fromString("26cb07a3-209d-4110-8e10-1010243614c8");

	public void addCombatAttributes(Tier tier, Multimap<String, AttributeModifier> multimap) {
		float f = this.getSpeed(tier);
		float g = this.getDamage(tier);
		float h = this.getReach(tier);
		multimap.put(
			SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
			new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)g, AttributeModifier.Operation.ADDITION)
		);
		multimap.put(
			SharedMonsterAttributes.ATTACK_SPEED.getName(),
			new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)f, AttributeModifier.Operation.ADDITION)
		);
		if (h != 0.0F) {
			multimap.put(
				SharedMonsterAttributes.ATTACK_REACH.getName(),
				new AttributeModifier(BASE_ATTACK_REACH_UUID, "Weapon modifier", (double)h, AttributeModifier.Operation.ADDITION)
			);
		}
	}

	public float getDamage(Tier tier) {
		switch (this) {
			case SWORD:
				return tier.getAttackDamageBonus() + 2.0F;
			case AXE:
				return tier.getAttackDamageBonus() + 3.0F;
			case PICKAXE:
				return tier.getAttackDamageBonus() + 1.0F;
			case HOE:
				if (tier != Tiers.IRON && tier != Tiers.DIAMOND) {
					return 0.0F;
				}

				return 1.0F;
			case SHOVEL:
				return tier.getAttackDamageBonus();
			case TRIDENT:
				return 5.0F;
			default:
				return 0.0F;
		}
	}

	public float getSpeed(Tier tier) {
		switch (this) {
			case SWORD:
				return 0.5F;
			case AXE:
			case SHOVEL:
			case TRIDENT:
				return -0.5F;
			case PICKAXE:
				return 0.0F;
			case HOE:
				if (tier == Tiers.WOOD) {
					return -0.5F;
				} else if (tier == Tiers.IRON) {
					return 0.5F;
				} else if (tier == Tiers.DIAMOND) {
					return 1.0F;
				} else {
					if (tier == Tiers.GOLD) {
						return 1.0F;
					}

					return 0.0F;
				}
			default:
				return 0.0F;
		}
	}

	public float getReach(Tier tier) {
		switch (this) {
			case SWORD:
				return 0.5F;
			case AXE:
			case PICKAXE:
			case SHOVEL:
				return 0.0F;
			case HOE:
			case TRIDENT:
				return 1.0F;
			default:
				return 0.0F;
		}
	}
}
