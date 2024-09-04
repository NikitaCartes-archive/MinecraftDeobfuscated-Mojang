package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.entity.player.Input;

public record InputPredicate(
	Optional<Boolean> forward,
	Optional<Boolean> backward,
	Optional<Boolean> left,
	Optional<Boolean> right,
	Optional<Boolean> jump,
	Optional<Boolean> sneak,
	Optional<Boolean> sprint
) {
	public static final Codec<InputPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.BOOL.optionalFieldOf("forward").forGetter(InputPredicate::forward),
					Codec.BOOL.optionalFieldOf("backward").forGetter(InputPredicate::backward),
					Codec.BOOL.optionalFieldOf("left").forGetter(InputPredicate::left),
					Codec.BOOL.optionalFieldOf("right").forGetter(InputPredicate::right),
					Codec.BOOL.optionalFieldOf("jump").forGetter(InputPredicate::jump),
					Codec.BOOL.optionalFieldOf("sneak").forGetter(InputPredicate::sneak),
					Codec.BOOL.optionalFieldOf("sprint").forGetter(InputPredicate::sprint)
				)
				.apply(instance, InputPredicate::new)
	);

	public boolean matches(Input input) {
		return this.matches(this.forward, input.forward())
			&& this.matches(this.backward, input.backward())
			&& this.matches(this.left, input.left())
			&& this.matches(this.right, input.right())
			&& this.matches(this.jump, input.jump())
			&& this.matches(this.sneak, input.shift())
			&& this.matches(this.sprint, input.sprint());
	}

	private boolean matches(Optional<Boolean> optional, boolean bl) {
		return (Boolean)optional.map(boolean_ -> boolean_ == bl).orElse(true);
	}
}
