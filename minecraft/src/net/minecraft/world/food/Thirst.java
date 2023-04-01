package net.minecraft.world.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

public class Thirst {
	public static final int MAX_THIRST = 10;
	public static final int WATER_BOTTLE_AMOUNT = 10;
	private static final int REDUCE_THIRST_INTERVAL = 3600;
	private static final int DRINK_UNDERWATER_CHANCE = 20;
	private static final int DEHYDRATION_DAMAGE_INTERVAL = 20;
	public static final Codec<Thirst> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.INT.fieldOf("level").forGetter(thirst -> thirst.level), Codec.INT.fieldOf("time").forGetter(thirst -> thirst.time))
				.apply(instance, Thirst::new)
	);
	private int level;
	private int time;

	public Thirst() {
		this(10, 0);
	}

	private Thirst(int i, int j) {
		this.level = i;
		this.time = j;
	}

	public void drink(int i) {
		this.set(this.level + i);
	}

	public void set(int i) {
		this.level = Mth.clamp(i, 0, 10);
	}

	public void tick(Player player) {
		this.time = this.time + this.thirstRate(player);
		if (this.time > 3600) {
			this.set(this.level - 1);
			this.time = 0;
		}

		RandomSource randomSource = player.getRandom();
		if (player.isUnderWater() && randomSource.nextInt(20) == 0) {
			this.drink(1);
		}

		if (this.level == 0 && player.tickCount % 20 == 0) {
			player.hurt(player.damageSources().dryOut(), 1.0F);
		}
	}

	private int thirstRate(Player player) {
		BlockPos blockPos = player.blockPosition();
		Biome biome = player.level.getBiome(blockPos).value();
		return biome.increasedThirst(blockPos) ? 2 : 1;
	}

	public int level() {
		return this.level;
	}
}
