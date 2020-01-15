package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarvedPumpkinBlock;

public enum EnchantmentCategory {
	ALL {
		@Override
		public boolean canEnchant(Item item) {
			for (EnchantmentCategory enchantmentCategory : EnchantmentCategory.values()) {
				if (enchantmentCategory != EnchantmentCategory.ALL && enchantmentCategory.canEnchant(item)) {
					return true;
				}
			}

			return false;
		}
	},
	ARMOR {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem;
		}
	},
	ARMOR_FEET {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem && ((ArmorItem)item).getSlot() == EquipmentSlot.FEET;
		}
	},
	ARMOR_LEGS {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem && ((ArmorItem)item).getSlot() == EquipmentSlot.LEGS;
		}
	},
	ARMOR_CHEST {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem && ((ArmorItem)item).getSlot() == EquipmentSlot.CHEST;
		}
	},
	ARMOR_HEAD {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem && ((ArmorItem)item).getSlot() == EquipmentSlot.HEAD;
		}
	},
	WEAPON {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof SwordItem || item instanceof AxeItem;
		}
	},
	DIGGER {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof DiggerItem;
		}
	},
	FISHING_ROD {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof FishingRodItem;
		}
	},
	TRIDENT {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof TridentItem;
		}
	},
	BREAKABLE {
		@Override
		public boolean canEnchant(Item item) {
			return item.canBeDepleted();
		}
	},
	BOW {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof BowItem;
		}
	},
	WEARABLE {
		@Override
		public boolean canEnchant(Item item) {
			Block block = Block.byItem(item);
			return item instanceof ArmorItem || item instanceof ElytraItem || block instanceof AbstractSkullBlock || block instanceof CarvedPumpkinBlock;
		}
	},
	CROSSBOW {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof CrossbowItem;
		}
	},
	AXE {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof AxeItem;
		}
	};

	private EnchantmentCategory() {
	}

	public abstract boolean canEnchant(Item item);
}
