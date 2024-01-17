package net.minecraft.world.item.trading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class MerchantOffer {
	public static final StreamCodec<RegistryFriendlyByteBuf, MerchantOffer> STREAM_CODEC = StreamCodec.of(
		MerchantOffer::writeToStream, MerchantOffer::createFromStream
	);
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
	private final boolean ignoreTags;

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
		this.ignoreTags = compoundTag.getBoolean("ignore_tags");
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
		this(itemStack, itemStack2, itemStack3, i, j, k, f, l, false);
	}

	public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, int i, int j, int k, float f, int l, boolean bl) {
		this.baseCostA = itemStack;
		this.costB = itemStack2;
		this.result = itemStack3;
		this.uses = i;
		this.maxUses = j;
		this.xp = k;
		this.priceMultiplier = f;
		this.demand = l;
		this.ignoreTags = bl;
	}

	private MerchantOffer(MerchantOffer merchantOffer) {
		this.baseCostA = merchantOffer.baseCostA.copy();
		this.costB = merchantOffer.costB.copy();
		this.result = merchantOffer.result.copy();
		this.uses = merchantOffer.uses;
		this.maxUses = merchantOffer.maxUses;
		this.rewardExp = merchantOffer.rewardExp;
		this.specialPriceDiff = merchantOffer.specialPriceDiff;
		this.demand = merchantOffer.demand;
		this.priceMultiplier = merchantOffer.priceMultiplier;
		this.xp = merchantOffer.xp;
		this.ignoreTags = merchantOffer.ignoreTags;
	}

	public ItemStack getBaseCostA() {
		return this.baseCostA;
	}

	public ItemStack getCostA() {
		if (this.baseCostA.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			int i = this.baseCostA.getCount();
			int j = Math.max(0, Mth.floor((float)(i * this.demand) * this.priceMultiplier));
			return this.baseCostA.copyWithCount(Mth.clamp(i + j + this.specialPriceDiff, 1, this.baseCostA.getItem().getMaxStackSize()));
		}
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

	public boolean getIgnoreTags() {
		return this.ignoreTags;
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
		compoundTag.putBoolean("ignore_tags", this.ignoreTags);
		return compoundTag;
	}

	public boolean satisfiedBy(ItemStack itemStack, ItemStack itemStack2) {
		return isRequiredItem(itemStack, this.getCostA(), this.ignoreTags)
			&& itemStack.getCount() >= this.getCostA().getCount()
			&& isRequiredItem(itemStack2, this.costB, this.ignoreTags)
			&& itemStack2.getCount() >= this.costB.getCount();
	}

	public static boolean isRequiredItem(ItemStack itemStack, ItemStack itemStack2, boolean bl) {
		if (itemStack2.isEmpty() && itemStack.isEmpty()) {
			return true;
		} else {
			ItemStack itemStack3 = itemStack.copy();
			ItemStack itemStack4 = itemStack2.copy();
			if (itemStack3.getItem().canBeDepleted()) {
				itemStack3.setDamageValue(itemStack3.getDamageValue());
			}

			return bl
				? ItemStack.isSameItem(itemStack3, itemStack4)
				: ItemStack.isSameItem(itemStack3, itemStack4)
					&& (!itemStack4.hasTag() || itemStack3.hasTag() && NbtUtils.compareNbt(itemStack4.getTag(), itemStack3.getTag(), false));
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

	public MerchantOffer copy() {
		return new MerchantOffer(this);
	}

	private static void writeToStream(RegistryFriendlyByteBuf registryFriendlyByteBuf, MerchantOffer merchantOffer) {
		ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, merchantOffer.getBaseCostA());
		ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, merchantOffer.getResult());
		ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, merchantOffer.getCostB());
		registryFriendlyByteBuf.writeBoolean(merchantOffer.isOutOfStock());
		registryFriendlyByteBuf.writeInt(merchantOffer.getUses());
		registryFriendlyByteBuf.writeInt(merchantOffer.getMaxUses());
		registryFriendlyByteBuf.writeInt(merchantOffer.getXp());
		registryFriendlyByteBuf.writeInt(merchantOffer.getSpecialPriceDiff());
		registryFriendlyByteBuf.writeFloat(merchantOffer.getPriceMultiplier());
		registryFriendlyByteBuf.writeInt(merchantOffer.getDemand());
		registryFriendlyByteBuf.writeBoolean(merchantOffer.getIgnoreTags());
	}

	public static MerchantOffer createFromStream(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
		ItemStack itemStack2 = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
		ItemStack itemStack3 = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
		boolean bl = registryFriendlyByteBuf.readBoolean();
		int i = registryFriendlyByteBuf.readInt();
		int j = registryFriendlyByteBuf.readInt();
		int k = registryFriendlyByteBuf.readInt();
		int l = registryFriendlyByteBuf.readInt();
		float f = registryFriendlyByteBuf.readFloat();
		int m = registryFriendlyByteBuf.readInt();
		boolean bl2 = registryFriendlyByteBuf.readBoolean();
		MerchantOffer merchantOffer = new MerchantOffer(itemStack, itemStack3, itemStack2, i, j, k, f, m, bl2);
		if (bl) {
			merchantOffer.setToOutOfStock();
		}

		merchantOffer.setSpecialPriceDiff(l);
		return merchantOffer;
	}
}
