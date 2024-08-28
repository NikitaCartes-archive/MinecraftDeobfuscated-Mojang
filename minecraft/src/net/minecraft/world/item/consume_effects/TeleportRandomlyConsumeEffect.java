package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record TeleportRandomlyConsumeEffect(float diameter) implements ConsumeEffect {
	private static final float DEFAULT_DIAMETER = 16.0F;
	public static final MapCodec<TeleportRandomlyConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("diameter", 16.0F).forGetter(TeleportRandomlyConsumeEffect::diameter))
				.apply(instance, TeleportRandomlyConsumeEffect::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, TeleportRandomlyConsumeEffect> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, TeleportRandomlyConsumeEffect::diameter, TeleportRandomlyConsumeEffect::new
	);

	public TeleportRandomlyConsumeEffect() {
		this(16.0F);
	}

	@Override
	public ConsumeEffect.Type<TeleportRandomlyConsumeEffect> getType() {
		return ConsumeEffect.Type.TELEPORT_RANDOMLY;
	}

	@Override
	public boolean apply(Level level, ItemStack itemStack, LivingEntity livingEntity) {
		boolean bl = false;

		for (int i = 0; i < 16; i++) {
			double d = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5) * (double)this.diameter;
			double e = Mth.clamp(
				livingEntity.getY() + (livingEntity.getRandom().nextDouble() - 0.5) * (double)this.diameter,
				(double)level.getMinY(),
				(double)(level.getMinY() + ((ServerLevel)level).getLogicalHeight() - 1)
			);
			double f = livingEntity.getZ() + (livingEntity.getRandom().nextDouble() - 0.5) * (double)this.diameter;
			if (livingEntity.isPassenger()) {
				livingEntity.stopRiding();
			}

			Vec3 vec3 = livingEntity.position();
			if (livingEntity.randomTeleport(d, e, f, true)) {
				level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(livingEntity));
				SoundSource soundSource;
				SoundEvent soundEvent;
				if (livingEntity instanceof Fox) {
					soundEvent = SoundEvents.FOX_TELEPORT;
					soundSource = SoundSource.NEUTRAL;
				} else {
					soundEvent = SoundEvents.CHORUS_FRUIT_TELEPORT;
					soundSource = SoundSource.PLAYERS;
				}

				level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), soundEvent, soundSource);
				livingEntity.resetFallDistance();
				bl = true;
				break;
			}
		}

		if (bl && livingEntity instanceof Player player) {
			player.resetCurrentImpulseContext();
		}

		return bl;
	}
}
