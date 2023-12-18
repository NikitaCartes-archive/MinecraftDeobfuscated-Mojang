package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EntityAttachments {
	public static final EntityAttachments DEFAULTS = new EntityAttachments(Map.of());
	private final Map<EntityAttachment, List<Vec3>> attachments;

	EntityAttachments(Map<EntityAttachment, List<Vec3>> map) {
		this.attachments = map;
	}

	public static EntityAttachments.Builder builder() {
		return new EntityAttachments.Builder();
	}

	public EntityAttachments scale(float f, float g, float h) {
		Map<EntityAttachment, List<Vec3>> map = new EnumMap(EntityAttachment.class);

		for (Entry<EntityAttachment, List<Vec3>> entry : this.attachments.entrySet()) {
			map.put((EntityAttachment)entry.getKey(), scalePoints((List<Vec3>)entry.getValue(), f, g, h));
		}

		return new EntityAttachments(map);
	}

	private static List<Vec3> scalePoints(List<Vec3> list, float f, float g, float h) {
		List<Vec3> list2 = new ArrayList(list.size());

		for (Vec3 vec3 : list) {
			list2.add(vec3.multiply((double)f, (double)g, (double)h));
		}

		return list2;
	}

	@Nullable
	public Vec3 getNullable(EntityAttachment entityAttachment, int i, float f) {
		List<Vec3> list = (List<Vec3>)this.attachments.get(entityAttachment);
		return i >= 0 && i < list.size() ? transformPoint((Vec3)list.get(i), f) : null;
	}

	public Vec3 get(EntityAttachment entityAttachment, int i, float f) {
		Vec3 vec3 = this.getNullable(entityAttachment, i, f);
		if (vec3 == null) {
			throw new IllegalStateException("Had no attachment point of type: " + entityAttachment + " for index: " + i);
		} else {
			return vec3;
		}
	}

	public Vec3 getClamped(EntityAttachment entityAttachment, int i, float f) {
		List<Vec3> list = (List<Vec3>)this.attachments.get(entityAttachment);
		if (list.isEmpty()) {
			throw new IllegalStateException("Had no attachment points of type: " + entityAttachment);
		} else {
			Vec3 vec3 = (Vec3)list.get(Mth.clamp(i, 0, list.size() - 1));
			return transformPoint(vec3, f);
		}
	}

	private static Vec3 transformPoint(Vec3 vec3, float f) {
		return vec3.yRot(-f * (float) (Math.PI / 180.0));
	}

	public static class Builder {
		private final Map<EntityAttachment, List<Vec3>> attachments = new EnumMap(EntityAttachment.class);

		Builder() {
		}

		public EntityAttachments.Builder attach(EntityAttachment entityAttachment, float f, float g, float h) {
			return this.attach(entityAttachment, new Vec3((double)f, (double)g, (double)h));
		}

		public EntityAttachments.Builder attach(EntityAttachment entityAttachment, Vec3 vec3) {
			((List)this.attachments.computeIfAbsent(entityAttachment, entityAttachmentx -> new ArrayList(1))).add(vec3);
			return this;
		}

		public EntityAttachments build(float f, float g) {
			Map<EntityAttachment, List<Vec3>> map = new EnumMap(EntityAttachment.class);

			for (EntityAttachment entityAttachment : EntityAttachment.values()) {
				List<Vec3> list = (List<Vec3>)this.attachments.get(entityAttachment);
				map.put(entityAttachment, list != null ? List.copyOf(list) : entityAttachment.createFallbackPoints(f, g));
			}

			return new EntityAttachments(map);
		}
	}
}
