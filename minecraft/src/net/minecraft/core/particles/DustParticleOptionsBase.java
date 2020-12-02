package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class DustParticleOptionsBase implements ParticleOptions {
	protected final Vec3 color;
	protected final float scale;

	public DustParticleOptionsBase(Vec3 vec3, float f) {
		this.color = vec3;
		this.scale = Mth.clamp(f, 0.01F, 4.0F);
	}

	public static Vec3 readVec3(StringReader stringReader) throws CommandSyntaxException {
		stringReader.expect(' ');
		float f = (float)stringReader.readDouble();
		stringReader.expect(' ');
		float g = (float)stringReader.readDouble();
		stringReader.expect(' ');
		float h = (float)stringReader.readDouble();
		return new Vec3((double)f, (double)g, (double)h);
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.color.x);
		friendlyByteBuf.writeDouble(this.color.y);
		friendlyByteBuf.writeDouble(this.color.z);
		friendlyByteBuf.writeFloat(this.scale);
	}

	@Override
	public String writeToString() {
		return String.format(
			Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), this.color.x, this.color.y, this.color.z, this.scale
		);
	}

	@Environment(EnvType.CLIENT)
	public Vec3 getColor() {
		return this.color;
	}

	@Environment(EnvType.CLIENT)
	public float getScale() {
		return this.scale;
	}
}
