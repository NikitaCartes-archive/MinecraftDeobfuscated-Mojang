package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class WorldCoordinates implements Coordinates {
	private final WorldCoordinate x;
	private final WorldCoordinate y;
	private final WorldCoordinate z;

	public WorldCoordinates(WorldCoordinate worldCoordinate, WorldCoordinate worldCoordinate2, WorldCoordinate worldCoordinate3) {
		this.x = worldCoordinate;
		this.y = worldCoordinate2;
		this.z = worldCoordinate3;
	}

	@Override
	public Vec3 getPosition(CommandSourceStack commandSourceStack, boolean bl) {
		double d = this.x.isRelative() && bl ? 0.0 : commandSourceStack.getPosition().x;
		double e = this.y.isRelative() && bl ? 0.0 : commandSourceStack.getPosition().y;
		double f = this.z.isRelative() && bl ? 0.0 : commandSourceStack.getPosition().z;
		return new Vec3(this.x.get(d), this.y.get(e), this.z.get(f));
	}

	@Override
	public Vec2 getRotation(CommandSourceStack commandSourceStack, boolean bl) {
		double d = this.x.isRelative() && bl ? 0.0 : (double)commandSourceStack.getRotation().x;
		double e = this.y.isRelative() && bl ? 0.0 : (double)commandSourceStack.getRotation().y;
		return new Vec2((float)this.x.get(d), (float)this.y.get(e));
	}

	@Override
	public boolean isXRelative() {
		return this.x.isRelative();
	}

	@Override
	public boolean isYRelative() {
		return this.y.isRelative();
	}

	@Override
	public boolean isZRelative() {
		return this.z.isRelative();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof WorldCoordinates worldCoordinates)) {
			return false;
		} else if (!this.x.equals(worldCoordinates.x)) {
			return false;
		} else {
			return !this.y.equals(worldCoordinates.y) ? false : this.z.equals(worldCoordinates.z);
		}
	}

	public static WorldCoordinates parseInt(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		WorldCoordinate worldCoordinate = WorldCoordinate.parseInt(stringReader);
		if (stringReader.canRead() && stringReader.peek() == ' ') {
			stringReader.skip();
			WorldCoordinate worldCoordinate2 = WorldCoordinate.parseInt(stringReader);
			if (stringReader.canRead() && stringReader.peek() == ' ') {
				stringReader.skip();
				WorldCoordinate worldCoordinate3 = WorldCoordinate.parseInt(stringReader);
				return new WorldCoordinates(worldCoordinate, worldCoordinate2, worldCoordinate3);
			} else {
				stringReader.setCursor(i);
				throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringReader);
			}
		} else {
			stringReader.setCursor(i);
			throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringReader);
		}
	}

	public static WorldCoordinates parseDouble(StringReader stringReader, boolean bl) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		WorldCoordinate worldCoordinate = WorldCoordinate.parseDouble(stringReader, bl);
		if (stringReader.canRead() && stringReader.peek() == ' ') {
			stringReader.skip();
			WorldCoordinate worldCoordinate2 = WorldCoordinate.parseDouble(stringReader, false);
			if (stringReader.canRead() && stringReader.peek() == ' ') {
				stringReader.skip();
				WorldCoordinate worldCoordinate3 = WorldCoordinate.parseDouble(stringReader, bl);
				return new WorldCoordinates(worldCoordinate, worldCoordinate2, worldCoordinate3);
			} else {
				stringReader.setCursor(i);
				throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringReader);
			}
		} else {
			stringReader.setCursor(i);
			throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringReader);
		}
	}

	public static WorldCoordinates absolute(double d, double e, double f) {
		return new WorldCoordinates(new WorldCoordinate(false, d), new WorldCoordinate(false, e), new WorldCoordinate(false, f));
	}

	public static WorldCoordinates absolute(Vec2 vec2) {
		return new WorldCoordinates(new WorldCoordinate(false, (double)vec2.x), new WorldCoordinate(false, (double)vec2.y), new WorldCoordinate(true, 0.0));
	}

	public static WorldCoordinates current() {
		return new WorldCoordinates(new WorldCoordinate(true, 0.0), new WorldCoordinate(true, 0.0), new WorldCoordinate(true, 0.0));
	}

	public int hashCode() {
		int i = this.x.hashCode();
		i = 31 * i + this.y.hashCode();
		return 31 * i + this.z.hashCode();
	}
}
