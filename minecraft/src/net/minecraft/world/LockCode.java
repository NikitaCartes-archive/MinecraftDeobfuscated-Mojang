package net.minecraft.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public record LockCode(ItemPredicate predicate) {
	public static final LockCode NO_LOCK = new LockCode(ItemPredicate.Builder.item().build());
	public static final Codec<LockCode> CODEC = ItemPredicate.CODEC.xmap(LockCode::new, LockCode::predicate);
	public static final String TAG_LOCK = "lock";

	public boolean unlocksWith(ItemStack itemStack) {
		return this.predicate.test(itemStack);
	}

	public void addToTag(CompoundTag compoundTag) {
		if (this != NO_LOCK) {
			DataResult<Tag> dataResult = CODEC.encode(this, NbtOps.INSTANCE, new CompoundTag());
			dataResult.result().ifPresent(tag -> compoundTag.put("lock", tag));
		}
	}

	public static LockCode fromTag(CompoundTag compoundTag) {
		if (compoundTag.contains("lock", 10)) {
			DataResult<Pair<LockCode, Tag>> dataResult = CODEC.decode(NbtOps.INSTANCE, compoundTag.get("lock"));
			if (dataResult.isSuccess()) {
				return dataResult.getOrThrow().getFirst();
			}
		}

		return NO_LOCK;
	}
}
