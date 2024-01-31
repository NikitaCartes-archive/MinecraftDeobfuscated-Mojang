package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class MerchantOffer {
	public static final Codec<MerchantOffer> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ItemStack.CODEC.optionalFieldOf("buy", ItemStack.EMPTY).forGetter(merchantOffer -> merchantOffer.baseCostA),
					ItemStack.CODEC.optionalFieldOf("buyB", ItemStack.EMPTY).forGetter(merchantOffer -> merchantOffer.costB),
					ItemStack.CODEC.optionalFieldOf("sell", ItemStack.EMPTY).forGetter(merchantOffer -> merchantOffer.result),
					Codec.INT.optionalFieldOf("uses", Integer.valueOf(0)).forGetter(merchantOffer -> merchantOffer.uses),
					Codec.INT.optionalFieldOf("maxUses", Integer.valueOf(4)).forGetter(merchantOffer -> merchantOffer.maxUses),
					Codec.BOOL.optionalFieldOf("rewardExp", Boolean.valueOf(true)).forGetter(merchantOffer -> merchantOffer.rewardExp),
					Codec.INT.optionalFieldOf("specialPrice", Integer.valueOf(0)).forGetter(merchantOffer -> merchantOffer.specialPriceDiff),
					Codec.INT.optionalFieldOf("demand", Integer.valueOf(0)).forGetter(merchantOffer -> merchantOffer.demand),
					Codec.FLOAT.optionalFieldOf("priceMultiplier", Float.valueOf(0.0F)).forGetter(merchantOffer -> merchantOffer.priceMultiplier),
					Codec.INT.optionalFieldOf("xp", Integer.valueOf(1)).forGetter(merchantOffer -> merchantOffer.xp),
					Codec.BOOL.optionalFieldOf("ignore_tags", Boolean.valueOf(false)).forGetter(merchantOffer -> merchantOffer.ignoreTags)
				)
				.apply(instance, MerchantOffer::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, MerchantOffer> STREAM_CODEC = StreamCodec.of(
		MerchantOffer::writeToStream, MerchantOffer::createFromStream
	);
	private final ItemStack baseCostA;
	private final ItemStack costB;
	private final ItemStack result;
	private int uses;
	private final int maxUses;
	private final boolean rewardExp;
	private int specialPriceDiff;
	private int demand;
	private final float priceMultiplier;
	private final int xp;
	private final boolean ignoreTags;

	private MerchantOffer(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, int i, int j, boolean bl, int k, int l, float f, int m, boolean bl2) {
		this.baseCostA = itemStack;
		this.costB = itemStack2;
		this.result = itemStack3;
		this.uses = i;
		this.maxUses = j;
		this.rewardExp = bl;
		this.specialPriceDiff = k;
		this.demand = l;
		this.priceMultiplier = f;
		this.xp = m;
		this.ignoreTags = bl2;
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
		this(itemStack, itemStack2, itemStack3, i, j, true, 0, l, f, k, false);
	}

	public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, int i, int j, int k, float f, int l, boolean bl) {
		this(itemStack, itemStack2, itemStack3, i, j, true, 0, l, f, k, bl);
	}

	private MerchantOffer(MerchantOffer merchantOffer) {
		this(
			merchantOffer.baseCostA.copy(),
			merchantOffer.costB.copy(),
			merchantOffer.result.copy(),
			merchantOffer.uses,
			merchantOffer.maxUses,
			merchantOffer.rewardExp,
			merchantOffer.specialPriceDiff,
			merchantOffer.demand,
			merchantOffer.priceMultiplier,
			merchantOffer.xp,
			merchantOffer.ignoreTags
		);
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
