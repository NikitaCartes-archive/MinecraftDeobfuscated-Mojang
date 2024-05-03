package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record SummonEntityEffect(HolderSet<EntityType<?>> entityTypes, boolean joinTeam) implements EnchantmentEntityEffect {
	public static final MapCodec<SummonEntityEffect> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity").forGetter(SummonEntityEffect::entityTypes),
					Codec.BOOL.optionalFieldOf("join_team", Boolean.valueOf(false)).forGetter(SummonEntityEffect::joinTeam)
				)
				.apply(instance, SummonEntityEffect::new)
	);

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		BlockPos blockPos = BlockPos.containing(vec3);
		if (Level.isInSpawnableBounds(blockPos)) {
			Optional<Holder<EntityType<?>>> optional = this.entityTypes().getRandomElement(serverLevel.getRandom());
			if (!optional.isEmpty()) {
				Entity entity2 = ((EntityType)((Holder)optional.get()).value()).spawn(serverLevel, blockPos, MobSpawnType.TRIGGERED);
				if (entity2 != null) {
					if (entity2 instanceof LightningBolt lightningBolt && entity instanceof ServerPlayer serverPlayer) {
						lightningBolt.setCause(serverPlayer);
					}

					if (this.joinTeam && entity.getTeam() != null) {
						serverLevel.getScoreboard().addPlayerToTeam(entity2.getScoreboardName(), entity.getTeam());
					}

					entity2.moveTo(vec3.x, vec3.y, vec3.z, entity2.getYRot(), entity2.getXRot());
				}
			}
		}
	}

	@Override
	public MapCodec<SummonEntityEffect> codec() {
		return CODEC;
	}
}
