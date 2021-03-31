package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LocalCoordinates implements Coordinates {
	public static final char PREFIX_LOCAL_COORDINATE = '^';
	private final double left;
	private final double up;
	private final double forwards;

	public LocalCoordinates(double d, double e, double f) {
		this.left = d;
		this.up = e;
		this.forwards = f;
	}

	@Override
	public Vec3 getPosition(CommandSourceStack commandSourceStack) {
		Vec2 vec2 = commandSourceStack.getRotation();
		Vec3 vec3 = commandSourceStack.getAnchor().apply(commandSourceStack);
		float f = Mth.cos((vec2.y + 90.0F) * (float) (Math.PI / 180.0));
		float g = Mth.sin((vec2.y + 90.0F) * (float) (Math.PI / 180.0));
		float h = Mth.cos(-vec2.x * (float) (Math.PI / 180.0));
		float i = Mth.sin(-vec2.x * (float) (Math.PI / 180.0));
		float j = Mth.cos((-vec2.x + 90.0F) * (float) (Math.PI / 180.0));
		float k = Mth.sin((-vec2.x + 90.0F) * (float) (Math.PI / 180.0));
		Vec3 vec32 = new Vec3((double)(f * h), (double)i, (double)(g * h));
		Vec3 vec33 = new Vec3((double)(f * j), (double)k, (double)(g * j));
		Vec3 vec34 = vec32.cross(vec33).scale(-1.0);
		double d = vec32.x * this.forwards + vec33.x * this.up + vec34.x * this.left;
		double e = vec32.y * this.forwards + vec33.y * this.up + vec34.y * this.left;
		double l = vec32.z * this.forwards + vec33.z * this.up + vec34.z * this.left;
		return new Vec3(vec3.x + d, vec3.y + e, vec3.z + l);
	}

	@Override
	public Vec2 getRotation(CommandSourceStack commandSourceStack) {
		return Vec2.ZERO;
	}

	@Override
	public boolean isXRelative() {
		return true;
	}

	@Override
	public boolean isYRelative() {
		return true;
	}

	@Override
	public boolean isZRelative() {
		return true;
	}

	public static LocalCoordinates parse(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		double d = readDouble(stringReader, i);
		if (stringReader.canRead() && stringReader.peek() == ' ') {
			stringReader.skip();
			double e = readDouble(stringReader, i);
			if (stringReader.canRead() && stringReader.peek() == ' ') {
				stringReader.skip();
				double f = readDouble(stringReader, i);
				return new LocalCoordinates(d, e, f);
			} else {
				stringReader.setCursor(i);
				throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringReader);
			}
		} else {
			stringReader.setCursor(i);
			throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringReader);
		}
	}

	private static double readDouble(StringReader stringReader, int i) throws CommandSyntaxException {
		if (!stringReader.canRead()) {
			throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext(stringReader);
		} else if (stringReader.peek() != '^') {
			stringReader.setCursor(i);
			throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(stringReader);
		} else {
			stringReader.skip();
			return stringReader.canRead() && stringReader.peek() != ' ' ? stringReader.readDouble() : 0.0;
		}
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof LocalCoordinates)) {
			return false;
		} else {
			LocalCoordinates localCoordinates = (LocalCoordinates)object;
			return this.left == localCoordinates.left && this.up == localCoordinates.up && this.forwards == localCoordinates.forwards;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.left, this.up, this.forwards});
	}
}
