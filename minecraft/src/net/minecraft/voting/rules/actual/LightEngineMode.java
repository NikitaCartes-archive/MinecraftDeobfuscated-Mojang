package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

public enum LightEngineMode implements StringRepresentable {
	NONE("none"),
	LOADSHEDDING("loadshedding"),
	NEVER_LIGHT("never_light"),
	ALWAYS_LIGHT("always_light");

	public static final Codec<LightEngineMode> CODEC = StringRepresentable.fromEnum(LightEngineMode::values);
	private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::createNewThreadLocalInstance);
	private final String id;
	private final Component displayName;

	private LightEngineMode(String string2) {
		this.id = string2;
		this.displayName = Component.translatable("rule.optimize_light_engine." + string2);
	}

	public boolean isLightDisabled(Level level) {
		return switch (this) {
			case NEVER_LIGHT -> true;
			case LOADSHEDDING -> {
				int i = 2400;
				RandomSource randomSource = (RandomSource)RANDOM.get();
				randomSource.setSeed(HashCommon.mix(level.getGameTime() / 2400L));
				yield randomSource.nextBoolean();
			}
			default -> false;
		};
	}

	public boolean isLightForced(Level level) {
		return this == ALWAYS_LIGHT;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public Component displayName() {
		return this.displayName;
	}
}
