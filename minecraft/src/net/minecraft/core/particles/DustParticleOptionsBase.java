package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public abstract class DustParticleOptionsBase implements ParticleOptions {
	public static final float MIN_SCALE = 0.01F;
	public static final float MAX_SCALE = 4.0F;
	protected final Vector3f color;
	protected final float scale;

	public DustParticleOptionsBase(Vector3f vector3f, float f) {
		this.color = vector3f;
		this.scale = Mth.clamp(f, 0.01F, 4.0F);
	}

	public static Vector3f readVector3f(StringReader stringReader) throws CommandSyntaxException {
		stringReader.expect(' ');
		float f = stringReader.readFloat();
		stringReader.expect(' ');
		float g = stringReader.readFloat();
		stringReader.expect(' ');
		float h = stringReader.readFloat();
		return new Vector3f(f, g, h);
	}

	public static Vector3f readVector3f(FriendlyByteBuf friendlyByteBuf) {
		return new Vector3f(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.color.x());
		friendlyByteBuf.writeFloat(this.color.y());
		friendlyByteBuf.writeFloat(this.color.z());
		friendlyByteBuf.writeFloat(this.scale);
	}

	@Override
	public String writeToString() {
		return String.format(
			Locale.ROOT, "%s %.2f %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.color.x(), this.color.y(), this.color.z(), this.scale
		);
	}

	public Vector3f getColor() {
		return this.color;
	}

	public float getScale() {
		return this.scale;
	}
}
