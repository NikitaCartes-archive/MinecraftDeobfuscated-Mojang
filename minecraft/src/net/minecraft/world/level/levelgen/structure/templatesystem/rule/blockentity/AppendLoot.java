package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

public class AppendLoot implements RuleBlockEntityModifier {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<AppendLoot> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(appendLoot -> appendLoot.lootTable))
				.apply(instance, AppendLoot::new)
	);
	private final ResourceKey<LootTable> lootTable;

	public AppendLoot(ResourceKey<LootTable> resourceKey) {
		this.lootTable = resourceKey;
	}

	@Override
	public CompoundTag apply(RandomSource randomSource, @Nullable CompoundTag compoundTag) {
		CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.copy();
		ResourceKey.codec(Registries.LOOT_TABLE)
			.encodeStart(NbtOps.INSTANCE, this.lootTable)
			.resultOrPartial(LOGGER::error)
			.ifPresent(tag -> compoundTag2.put("LootTable", tag));
		compoundTag2.putLong("LootTableSeed", randomSource.nextLong());
		return compoundTag2;
	}

	@Override
	public RuleBlockEntityModifierType<?> getType() {
		return RuleBlockEntityModifierType.APPEND_LOOT;
	}
}
