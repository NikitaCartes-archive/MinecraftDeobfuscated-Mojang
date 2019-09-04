package net.minecraft.world.item.trading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class MerchantOffer {
	private final ItemStack baseCostA;
	private final ItemStack costB;
	private final ItemStack result;
	private int uses;
	private final int maxUses;
	private boolean rewardExp = true;
	private int specialPriceDiff;
	private int demand;
	private float priceMultiplier;
	private int xp = 1;

	public MerchantOffer(CompoundTag compoundTag) {
		this.baseCostA = ItemStack.of(compoundTag.getCompound("buy"));
		this.costB = ItemStack.of(compoundTag.getCompound("buyB"));
		this.result = ItemStack.of(compoundTag.getCompound("sell"));
		this.uses = compoundTag.getInt("uses");
		if (compoundTag.contains("maxUses", 99)) {
			this.maxUses = compoundTag.getInt("maxUses");
		} else {
			this.maxUses = 4;
		}

		if (compoundTag.contains("rewardExp", 1)) {
			this.rewardExp = compoundTag.getBoolean("rewardExp");
		}

		if (compoundTag.contains("xp", 3)) {
			this.xp = compoundTag.getInt("xp");
		}

		if (compoundTag.contains("priceMultiplier", 5)) {
			this.priceMultiplier = compoundTag.getFloat("priceMultiplier");
		}

		this.specialPriceDiff = compoundTag.getInt("specialPrice");
		this.demand = compoundTag.getInt("demand");
	}

	public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, int i, int j, float f) {
		this(itemStack, ItemStack.EMPTY, itemStack2, i, j, f);
	}

	public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, int i, int j, float f) {
		this(itemStack, itemStack2, itemStack3, 0, i, j, f);
	}

	public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, int i, int j, int k, float f) {
		this(itemStack, itemStack2, itemStack3, i, j, k, f, 0);
	}

	public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, int i, int j, int k, float f, int l) {
		this.baseCostA = itemStack;
		this.costB = itemStack2;
		this.result = itemStack3;
		this.uses = i;
		this.maxUses = j;
		this.xp = k;
		this.priceMultiplier = f;
		this.demand = l;
	}

	public ItemStack getBaseCostA() {
		return this.baseCostA;
	}

	public ItemStack getCostA() {
		int i = this.baseCostA.getCount();
		ItemStack itemStack = this.baseCostA.copy();
		int j = Math.max(0, Mth.floor((float)(i * this.demand) * this.priceMultiplier));
		itemStack.setCount(Mth.clamp(i + j + this.specialPriceDiff, 1, this.baseCostA.getItem().getMaxStackSize()));
		return itemStack;
	}

	public ItemStack getCostB() {
		return this.costB;
	}

	public ItemStack getResult() {
		return this.result;
	}

	public void updateDemand() {
		this.demand = this.demand + this.uses - (this.maxUses - this.uses);
	}

	public ItemStack assemble() {
		return this.result.copy();
	}

	public int getUses() {
		return this.uses;
	}

	public void resetUses() {
		this.uses = 0;
	}

	public int getMaxUses() {
		return this.maxUses;
	}

	public void increaseUses() {
		this.uses++;
	}

	public int getDemand() {
		return this.demand;
	}

	public void addToSpecialPriceDiff(int i) {
		this.specialPriceDiff += i;
	}

	public void resetSpecialPriceDiff() {
		this.specialPriceDiff = 0;
	}

	public int getSpecialPriceDiff() {
		return this.specialPriceDiff;
	}

	public void setSpecialPriceDiff(int i) {
		this.specialPriceDiff = i;
	}

	public float getPriceMultiplier() {
		return this.priceMultiplier;
	}

	public int getXp() {
		return this.xp;
	}

	public boolean isOutOfStock() {
		return this.uses >= this.maxUses;
	}

	public void setToOutOfStock() {
		this.uses = this.maxUses;
	}

	public boolean needsRestock() {
		return this.uses > 0;
	}

	public boolean shouldRewardExp() {
		return this.rewardExp;
	}

	public CompoundTag createTag() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.put("buy", this.baseCostA.save(new CompoundTag()));
		compoundTag.put("sell", this.result.save(new CompoundTag()));
		compoundTag.put("buyB", this.costB.save(new CompoundTag()));
		compoundTag.putInt("uses", this.uses);
		compoundTag.putInt("maxUses", this.maxUses);
		compoundTag.putBoolean("rewardExp", this.rewardExp);
		compoundTag.putInt("xp", this.xp);
		compoundTag.putFloat("priceMultiplier", this.priceMultiplier);
		compoundTag.putInt("specialPrice", this.specialPriceDiff);
		compoundTag.putInt("demand", this.demand);
		return compoundTag;
	}

	public boolean satisfiedBy(ItemStack itemStack, ItemStack itemStack2) {
		return this.isRequiredItem(itemStack, this.getCostA())
			&& itemStack.getCount() >= this.getCostA().getCount()
			&& this.isRequiredItem(itemStack2, this.costB)
			&& itemStack2.getCount() >= this.costB.getCount();
	}

	private boolean isRequiredItem(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack2.isEmpty() && itemStack.isEmpty()) {
			return true;
		} else {
			ItemStack itemStack3 = itemStack.copy();
			if (itemStack3.getItem().canBeDepleted()) {
				itemStack3.setDamageValue(itemStack3.getDamageValue());
			}

			return ItemStack.isSame(itemStack3, itemStack2)
				&& (!itemStack2.hasTag() || itemStack3.hasTag() && NbtUtils.compareNbt(itemStack2.getTag(), itemStack3.getTag(), false));
		}
	}

	public boolean take(ItemStack itemStack, ItemStack itemStack2) {
		if (!this.satisfiedBy(itemStack, itemStack2)) {
			return false;
		} else {
			itemStack.shrink(this.getCostA().getCount());
			if (!this.getCostB().isEmpty()) {
				itemStack2.shrink(this.getCostB().getCount());
			}

			return true;
		}
	}
}
