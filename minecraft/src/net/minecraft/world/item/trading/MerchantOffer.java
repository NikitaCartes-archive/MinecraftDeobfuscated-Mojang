package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class MerchantOffer {
	public static final Codec<MerchantOffer> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ItemCost.CODEC.fieldOf("buy").forGetter(merchantOffer -> merchantOffer.baseCostA),
					ItemCost.CODEC.lenientOptionalFieldOf("buyB").forGetter(merchantOffer -> merchantOffer.costB),
					ItemStack.CODEC.fieldOf("sell").forGetter(merchantOffer -> merchantOffer.result),
					Codec.INT.lenientOptionalFieldOf("uses", Integer.valueOf(0)).forGetter(merchantOffer -> merchantOffer.uses),
					Codec.INT.lenientOptionalFieldOf("maxUses", Integer.valueOf(4)).forGetter(merchantOffer -> merchantOffer.maxUses),
					Codec.BOOL.lenientOptionalFieldOf("rewardExp", Boolean.valueOf(true)).forGetter(merchantOffer -> merchantOffer.rewardExp),
					Codec.INT.lenientOptionalFieldOf("specialPrice", Integer.valueOf(0)).forGetter(merchantOffer -> merchantOffer.specialPriceDiff),
					Codec.INT.lenientOptionalFieldOf("demand", Integer.valueOf(0)).forGetter(merchantOffer -> merchantOffer.demand),
					Codec.FLOAT.lenientOptionalFieldOf("priceMultiplier", Float.valueOf(0.0F)).forGetter(merchantOffer -> merchantOffer.priceMultiplier),
					Codec.INT.lenientOptionalFieldOf("xp", Integer.valueOf(1)).forGetter(merchantOffer -> merchantOffer.xp)
				)
				.apply(instance, MerchantOffer::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, MerchantOffer> STREAM_CODEC = StreamCodec.of(
		MerchantOffer::writeToStream, MerchantOffer::createFromStream
	);
	private final ItemCost baseCostA;
	private final Optional<ItemCost> costB;
	private final ItemStack result;
	private int uses;
	private final int maxUses;
	private final boolean rewardExp;
	private int specialPriceDiff;
	private int demand;
	private final float priceMultiplier;
	private final int xp;

	private MerchantOffer(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int i, int j, boolean bl, int k, int l, float f, int m) {
		this.baseCostA = itemCost;
		this.costB = optional;
		this.result = itemStack;
		this.uses = i;
		this.maxUses = j;
		this.rewardExp = bl;
		this.specialPriceDiff = k;
		this.demand = l;
		this.priceMultiplier = f;
		this.xp = m;
	}

	public MerchantOffer(ItemCost itemCost, ItemStack itemStack, int i, int j, float f) {
		this(itemCost, Optional.empty(), itemStack, i, j, f);
	}

	public MerchantOffer(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int i, int j, float f) {
		this(itemCost, optional, itemStack, 0, i, j, f);
	}

	public MerchantOffer(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int i, int j, int k, float f) {
		this(itemCost, optional, itemStack, i, j, k, f, 0);
	}

	public MerchantOffer(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int i, int j, int k, float f, int l) {
		this(itemCost, optional, itemStack, i, j, true, 0, l, f, k);
	}

	private MerchantOffer(MerchantOffer merchantOffer) {
		this(
			merchantOffer.baseCostA,
			merchantOffer.costB,
			merchantOffer.result.copy(),
			merchantOffer.uses,
			merchantOffer.maxUses,
			merchantOffer.rewardExp,
			merchantOffer.specialPriceDiff,
			merchantOffer.demand,
			merchantOffer.priceMultiplier,
			merchantOffer.xp
		);
	}

	public ItemStack getBaseCostA() {
		return this.baseCostA.itemStack();
	}

	public ItemStack getCostA() {
		return this.baseCostA.itemStack().copyWithCount(this.getModifiedCostCount(this.baseCostA));
	}

	private int getModifiedCostCount(ItemCost itemCost) {
		int i = itemCost.count();
		int j = Math.max(0, Mth.floor((float)(i * this.demand) * this.priceMultiplier));
		return Mth.clamp(i + j + this.specialPriceDiff, 1, itemCost.itemStack().getMaxStackSize());
	}

	public ItemStack getCostB() {
		return (ItemStack)this.costB.map(ItemCost::itemStack).orElse(ItemStack.EMPTY);
	}

	public ItemCost getItemCostA() {
		return this.baseCostA;
	}

	public Optional<ItemCost> getItemCostB() {
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

	public boolean satisfiedBy(ItemStack itemStack, ItemStack itemStack2) {
		if (!this.baseCostA.test(itemStack) || itemStack.getCount() < this.getModifiedCostCount(this.baseCostA)) {
			return false;
		} else {
			return !this.costB.isPresent()
				? itemStack2.isEmpty()
				: ((ItemCost)this.costB.get()).test(itemStack2) && itemStack2.getCount() >= ((ItemCost)this.costB.get()).count();
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
		ItemCost.STREAM_CODEC.encode(registryFriendlyByteBuf, merchantOffer.getItemCostA());
		ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, merchantOffer.getResult());
		ItemCost.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, merchantOffer.getItemCostB());
		registryFriendlyByteBuf.writeBoolean(merchantOffer.isOutOfStock());
		registryFriendlyByteBuf.writeInt(merchantOffer.getUses());
		registryFriendlyByteBuf.writeInt(merchantOffer.getMaxUses());
		registryFriendlyByteBuf.writeInt(merchantOffer.getXp());
		registryFriendlyByteBuf.writeInt(merchantOffer.getSpecialPriceDiff());
		registryFriendlyByteBuf.writeFloat(merchantOffer.getPriceMultiplier());
		registryFriendlyByteBuf.writeInt(merchantOffer.getDemand());
	}

	public static MerchantOffer createFromStream(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		ItemCost itemCost = ItemCost.STREAM_CODEC.decode(registryFriendlyByteBuf);
		ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
		Optional<ItemCost> optional = ItemCost.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
		boolean bl = registryFriendlyByteBuf.readBoolean();
		int i = registryFriendlyByteBuf.readInt();
		int j = registryFriendlyByteBuf.readInt();
		int k = registryFriendlyByteBuf.readInt();
		int l = registryFriendlyByteBuf.readInt();
		float f = registryFriendlyByteBuf.readFloat();
		int m = registryFriendlyByteBuf.readInt();
		MerchantOffer merchantOffer = new MerchantOffer(itemCost, optional, itemStack, i, j, k, f, m);
		if (bl) {
			merchantOffer.setToOutOfStock();
		}

		merchantOffer.setSpecialPriceDiff(l);
		return merchantOffer;
	}
}
