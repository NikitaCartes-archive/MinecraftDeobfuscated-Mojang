package net.minecraft.world.item.trading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class MerchantOffer {
	private final CarryableTrade cost;
	private final CarryableTrade result;
	private int uses;
	private final int maxUses;
	private boolean rewardExp = true;
	private int demand;
	private float priceMultiplier;
	private int xp = 1;

	public MerchantOffer(CompoundTag compoundTag) {
		this.cost = (CarryableTrade)CarryableTrade.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("buy")).result().orElse(CarryableTrade.block(Blocks.AIR));
		this.result = (CarryableTrade)CarryableTrade.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("sell")).result().orElse(CarryableTrade.block(Blocks.AIR));
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

		this.demand = compoundTag.getInt("demand");
	}

	public MerchantOffer(CarryableTrade carryableTrade, CarryableTrade carryableTrade2, int i, int j, float f) {
		this(carryableTrade, carryableTrade2, 0, i, j, f);
	}

	public MerchantOffer(CarryableTrade carryableTrade, CarryableTrade carryableTrade2, int i, int j, int k, float f) {
		this(carryableTrade, carryableTrade2, i, j, k, f, 0);
	}

	public MerchantOffer(CarryableTrade carryableTrade, CarryableTrade carryableTrade2, int i, int j, int k, float f, int l) {
		this.cost = carryableTrade;
		this.result = carryableTrade2;
		this.uses = i;
		this.maxUses = j;
		this.xp = k;
		this.priceMultiplier = f;
		this.demand = l;
	}

	public CarryableTrade getCost() {
		return this.cost;
	}

	public CarryableTrade getResult() {
		return this.result;
	}

	public void updateDemand() {
		this.demand = this.demand + this.uses - (this.maxUses - this.uses);
	}

	public ItemStack getResultItemStack() {
		return this.result.asItemStack();
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
		CarryableTrade.CODEC.encodeStart(NbtOps.INSTANCE, this.cost).result().ifPresent(tag -> compoundTag.put("buy", tag));
		CarryableTrade.CODEC.encodeStart(NbtOps.INSTANCE, this.result).result().ifPresent(tag -> compoundTag.put("sell", tag));
		compoundTag.putInt("uses", this.uses);
		compoundTag.putInt("maxUses", this.maxUses);
		compoundTag.putBoolean("rewardExp", this.rewardExp);
		compoundTag.putInt("xp", this.xp);
		compoundTag.putFloat("priceMultiplier", this.priceMultiplier);
		compoundTag.putInt("demand", this.demand);
		return compoundTag;
	}

	public boolean accepts(CarryableTrade carryableTrade) {
		return this.cost.matches(carryableTrade);
	}
}
