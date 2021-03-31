package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class DustColorTransitionOptions extends DustParticleOptionsBase {
	public static final Vector3f SCULK_PARTICLE_COLOR = new Vector3f(Vec3.fromRGB24(3790560));
	public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(
		SCULK_PARTICLE_COLOR, DustParticleOptions.REDSTONE_PARTICLE_COLOR, 1.0F
	);
	public static final Codec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Vector3f.CODEC.fieldOf("fromColor").forGetter(dustColorTransitionOptions -> dustColorTransitionOptions.color),
					Vector3f.CODEC.fieldOf("toColor").forGetter(dustColorTransitionOptions -> dustColorTransitionOptions.toColor),
					Codec.FLOAT.fieldOf("scale").forGetter(dustColorTransitionOptions -> dustColorTransitionOptions.scale)
				)
				.apply(instance, DustColorTransitionOptions::new)
	);
	public static final ParticleOptions.Deserializer<DustColorTransitionOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustColorTransitionOptions>() {
		public DustColorTransitionOptions fromCommand(ParticleType<DustColorTransitionOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
			Vector3f vector3f = DustParticleOptionsBase.readVector3f(stringReader);
			stringReader.expect(' ');
			float f = stringReader.readFloat();
			Vector3f vector3f2 = DustParticleOptionsBase.readVector3f(stringReader);
			return new DustColorTransitionOptions(vector3f, vector3f2, f);
		}

		public DustColorTransitionOptions fromNetwork(ParticleType<DustColorTransitionOptions> particleType, FriendlyByteBuf friendlyByteBuf) {
			Vector3f vector3f = DustParticleOptionsBase.readVector3f(friendlyByteBuf);
			float f = friendlyByteBuf.readFloat();
			Vector3f vector3f2 = DustParticleOptionsBase.readVector3f(friendlyByteBuf);
			return new DustColorTransitionOptions(vector3f, vector3f2, f);
		}
	};
	private final Vector3f toColor;

	public DustColorTransitionOptions(Vector3f vector3f, Vector3f vector3f2, float f) {
		super(vector3f, f);
		this.toColor = vector3f2;
	}

	public Vector3f getFromColor() {
		return this.color;
	}

	public Vector3f getToColor() {
		return this.toColor;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		super.writeToNetwork(friendlyByteBuf);
		friendlyByteBuf.writeFloat(this.toColor.x());
		friendlyByteBuf.writeFloat(this.toColor.y());
		friendlyByteBuf.writeFloat(this.toColor.z());
	}

	@Override
	public String writeToString() {
		return String.format(
			Locale.ROOT,
			"%s %.2f %.2f %.2f %.2f %.2f %.2f %.2f",
			Registry.PARTICLE_TYPE.getKey(this.getType()),
			this.color.x(),
			this.color.y(),
			this.color.z(),
			this.scale,
			this.toColor.x(),
			this.toColor.y(),
			this.toColor.z()
		);
	}

	@Override
	public ParticleType<DustColorTransitionOptions> getType() {
		return ParticleTypes.DUST_COLOR_TRANSITION;
	}
}
