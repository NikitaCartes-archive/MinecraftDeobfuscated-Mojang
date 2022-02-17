package net.minecraft.client.animation.definitions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

@Environment(EnvType.CLIENT)
public class WardenAnimation {
	public static final AnimationDefinition WARDEN_EMERGE = AnimationDefinition.Builder.withLength(6.68F)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -22.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.76F, KeyframeAnimations.degreeVec(25.0F, 0.0F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.92F, KeyframeAnimations.degreeVec(35.0F, 0.0F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.08F, KeyframeAnimations.degreeVec(25.0F, 0.0F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.44F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.68F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -63.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.posVec(0.0F, -56.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.16F, KeyframeAnimations.posVec(0.0F, -27.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.76F, KeyframeAnimations.posVec(0.0F, -14.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.92F, KeyframeAnimations.posVec(0.0F, -11.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.08F, KeyframeAnimations.posVec(0.0F, -14.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.44F, KeyframeAnimations.posVec(0.0F, -6.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.posVec(0.0F, -4.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.68F, KeyframeAnimations.posVec(0.0F, -6.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.posVec(0.0F, -3.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.posVec(0.0F, -3.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.92F, KeyframeAnimations.degreeVec(0.74F, 0.0F, -40.38F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.16F, KeyframeAnimations.degreeVec(-67.5F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.degreeVec(-67.5F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.32F, KeyframeAnimations.degreeVec(-47.5F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4F, KeyframeAnimations.degreeVec(-67.5F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.degreeVec(-67.5F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.76F, KeyframeAnimations.degreeVec(-67.5F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.84F, KeyframeAnimations.degreeVec(-52.5F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.92F, KeyframeAnimations.degreeVec(-67.5F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.64F, KeyframeAnimations.degreeVec(-17.5F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.76F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.04F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.12F, KeyframeAnimations.degreeVec(80.0F, 0.0F, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.24F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(77.5F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.posVec(-8.0F, -11.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.92F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.posVec(0.0F, 0.47F, -0.95F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.32F, KeyframeAnimations.posVec(0.0F, 0.47F, -0.95F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, 0.47F, -0.95F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 1.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.76F, KeyframeAnimations.posVec(0.0F, 1.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.84F, KeyframeAnimations.posVec(0.0F, 1.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.92F, KeyframeAnimations.posVec(0.0F, 1.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.64F, KeyframeAnimations.posVec(0.0F, -2.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.76F, KeyframeAnimations.posVec(0.0F, -4.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.04F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.12F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.24F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_ear",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_ear",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_ear",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_ear",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_arm",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.degreeVec(-152.5F, 2.5F, 7.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.degreeVec(-180.0F, 12.5F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(-90.0F, 12.5F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.degreeVec(-90.0F, 12.5F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(-90.0F, 12.5F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.08F, KeyframeAnimations.degreeVec(-95.0F, 12.5F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.24F, KeyframeAnimations.degreeVec(-83.93F, 3.93F, 5.71F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.degreeVec(-80.0F, 7.5F, 17.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.76F, KeyframeAnimations.degreeVec(-67.5F, 2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.08F, KeyframeAnimations.degreeVec(-67.5F, 2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.44F, KeyframeAnimations.degreeVec(-55.0F, 2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.degreeVec(-60.0F, 2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.68F, KeyframeAnimations.degreeVec(-55.0F, 2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(-67.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.56F, KeyframeAnimations.degreeVec(-50.45F, 0.0F, 2.69F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.08F, KeyframeAnimations.degreeVec(-62.72F, 0.0F, 4.3F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_arm",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -21.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.posVec(2.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.posVec(2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.posVec(2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.posVec(2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.08F, KeyframeAnimations.posVec(2.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.24F, KeyframeAnimations.posVec(2.0F, 2.71F, 3.86F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.posVec(2.0F, 1.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.76F, KeyframeAnimations.posVec(2.0F, 3.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.08F, KeyframeAnimations.posVec(2.0F, 3.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.44F, KeyframeAnimations.posVec(2.67F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.posVec(2.67F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.68F, KeyframeAnimations.posVec(2.67F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.posVec(0.67F, 3.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_arm",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.12F, KeyframeAnimations.degreeVec(-167.5F, -17.5F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.6F, KeyframeAnimations.degreeVec(-167.5F, -17.5F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.88F, KeyframeAnimations.degreeVec(-175.0F, -17.5F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.16F, KeyframeAnimations.degreeVec(-190.0F, -17.5F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.28F, KeyframeAnimations.degreeVec(-90.0F, -5.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.degreeVec(-90.0F, -17.5F, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(-90.0F, -17.5F, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.degreeVec(-90.0F, -17.5F, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(-90.0F, -17.5F, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.04F, KeyframeAnimations.degreeVec(-81.29F, -10.64F, -14.21F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.16F, KeyframeAnimations.degreeVec(-83.5F, -5.5F, -15.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.76F, KeyframeAnimations.degreeVec(-62.5F, -7.5F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.92F, KeyframeAnimations.degreeVec(-58.75F, -3.75F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.08F, KeyframeAnimations.degreeVec(-55.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.44F, KeyframeAnimations.degreeVec(-52.5F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.68F, KeyframeAnimations.degreeVec(-52.5F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(-72.5F, -2.5F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.56F, KeyframeAnimations.degreeVec(-57.5F, -4.54F, 2.99F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.08F, KeyframeAnimations.degreeVec(-70.99F, -5.77F, 1.78F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_arm",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.12F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.posVec(-2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.posVec(-4.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.posVec(-4.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.posVec(-4.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.posVec(-4.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.04F, KeyframeAnimations.posVec(-3.23F, 5.7F, 4.97F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.16F, KeyframeAnimations.posVec(-1.49F, 2.22F, 5.25F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.76F, KeyframeAnimations.posVec(-1.14F, 1.71F, 1.86F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.92F, KeyframeAnimations.posVec(-1.14F, 1.21F, 3.86F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.08F, KeyframeAnimations.posVec(-1.14F, 2.71F, 4.86F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.44F, KeyframeAnimations.posVec(-1.0F, 1.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.56F, KeyframeAnimations.posVec(0.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.68F, KeyframeAnimations.posVec(0.0F, 1.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.posVec(-2.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.32F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.48F, KeyframeAnimations.degreeVec(55.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -63.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.posVec(0.0F, -56.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.posVec(0.0F, -22.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.76F, KeyframeAnimations.posVec(0.0F, -12.28F, 2.48F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.92F, KeyframeAnimations.posVec(0.0F, -9.28F, 2.48F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.08F, KeyframeAnimations.posVec(0.0F, -12.28F, 2.48F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.32F, KeyframeAnimations.posVec(0.0F, -4.14F, 4.14F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.48F, KeyframeAnimations.posVec(0.0F, -0.57F, -8.43F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.6F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.84F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -17.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.68F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.84F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -63.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.posVec(0.0F, -56.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.36F, KeyframeAnimations.posVec(0.0F, -22.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.84F, KeyframeAnimations.posVec(-4.0F, 2.0F, -7.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.0F, KeyframeAnimations.posVec(-4.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.68F, KeyframeAnimations.posVec(-4.0F, 0.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.84F, KeyframeAnimations.posVec(-2.0F, 2.0F, -3.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(5.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(6.64F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.build();
	public static final AnimationDefinition WARDEN_DIG = AnimationDefinition.Builder.withLength(5.0F)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(4.13441F, 0.94736F, 1.2694F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.degreeVec(54.45407F, -13.53935F, -18.14183F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0417F, KeyframeAnimations.degreeVec(59.46442F, -10.8885F, 35.7954F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.3333F, KeyframeAnimations.degreeVec(82.28261F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.625F, KeyframeAnimations.degreeVec(53.23606F, 10.04715F, -29.72932F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2083F, KeyframeAnimations.degreeVec(-17.71739F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.5417F, KeyframeAnimations.degreeVec(112.28261F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.6667F, KeyframeAnimations.degreeVec(116.06889F, 5.11581F, -24.50117F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.8333F, KeyframeAnimations.degreeVec(121.56244F, -4.17248F, 19.57737F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.0417F, KeyframeAnimations.degreeVec(138.5689F, 5.11581F, -24.50117F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.25F, KeyframeAnimations.degreeVec(144.06244F, -4.17248F, 19.57737F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.375F, KeyframeAnimations.degreeVec(147.28261F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.625F, KeyframeAnimations.degreeVec(147.28261F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.875F, KeyframeAnimations.degreeVec(134.36221F, 8.81113F, -8.90172F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.0417F, KeyframeAnimations.degreeVec(132.05966F, -8.35927F, 9.70506F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.25F, KeyframeAnimations.degreeVec(134.36221F, 8.81113F, -8.90172F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.5F, KeyframeAnimations.degreeVec(147.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -16.48454F, -6.5784F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, -16.48454F, -6.5784F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0417F, KeyframeAnimations.posVec(0.0F, -16.97F, -7.11F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.625F, KeyframeAnimations.posVec(0.0F, -13.97F, -7.11F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2083F, KeyframeAnimations.posVec(0.0F, -11.48454F, -0.5784F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.5417F, KeyframeAnimations.posVec(0.0F, -16.48454F, -6.5784F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.6667F, KeyframeAnimations.posVec(0.0F, -20.27F, -5.42F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.375F, KeyframeAnimations.posVec(0.0F, -21.48454F, -5.5784F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.0417F, KeyframeAnimations.posVec(0.0F, -22.48454F, -5.5784F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.5F, KeyframeAnimations.posVec(0.0F, -40.0F, -8.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.6667F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2083F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.75F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.375F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.5417F, KeyframeAnimations.degreeVec(67.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.375F, KeyframeAnimations.degreeVec(67.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.375F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_arm",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(-101.8036F, -21.29587F, 30.61478F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-101.8036F, -21.29587F, 30.61478F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0F, KeyframeAnimations.degreeVec(48.7585F, -17.61941F, 9.9865F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.1667F, KeyframeAnimations.degreeVec(48.7585F, -17.61941F, 9.9865F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4583F, KeyframeAnimations.degreeVec(-101.8036F, -21.29587F, 30.61478F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.75F, KeyframeAnimations.degreeVec(-89.04994F, -4.19657F, -1.47845F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2083F, KeyframeAnimations.degreeVec(-158.30728F, 3.7152F, -1.52352F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.5417F, KeyframeAnimations.degreeVec(-89.04994F, -4.19657F, -1.47845F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.375F, KeyframeAnimations.degreeVec(-120.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_arm",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.posVec(2.22F, 0.0F, 0.86F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0F, KeyframeAnimations.posVec(3.12F, 0.0F, 4.29F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2083F, KeyframeAnimations.posVec(1.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.375F, KeyframeAnimations.posVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_arm",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-63.89288F, -0.52011F, 2.09491F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(-63.89288F, -0.52011F, 2.09491F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-62.87857F, 15.15061F, 9.97445F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-86.93642F, 17.45026F, 4.05284F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.1667F, KeyframeAnimations.degreeVec(-86.93642F, 17.45026F, 4.05284F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4583F, KeyframeAnimations.degreeVec(-86.93642F, 17.45026F, 4.05284F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.6667F, KeyframeAnimations.degreeVec(63.0984F, 8.83573F, -8.71284F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8333F, KeyframeAnimations.degreeVec(35.5984F, 8.83573F, -8.71284F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2083F, KeyframeAnimations.degreeVec(-153.27473F, -0.02953F, 3.5235F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.5417F, KeyframeAnimations.degreeVec(-87.07754F, -0.02625F, 3.132F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.375F, KeyframeAnimations.degreeVec(-120.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_arm",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.posVec(-0.28F, 5.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.posVec(-1.51F, 4.35F, 4.33F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.9167F, KeyframeAnimations.posVec(-0.6F, 3.61F, 4.63F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.1667F, KeyframeAnimations.posVec(-0.6F, 3.61F, 0.63F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.6667F, KeyframeAnimations.posVec(-2.85F, -0.1F, 3.33F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2083F, KeyframeAnimations.posVec(-1.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.375F, KeyframeAnimations.posVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(113.27F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.degreeVec(113.27F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.3333F, KeyframeAnimations.degreeVec(113.27F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.5833F, KeyframeAnimations.degreeVec(182.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.8333F, KeyframeAnimations.degreeVec(120.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.0833F, KeyframeAnimations.degreeVec(182.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2917F, KeyframeAnimations.degreeVec(120.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.5F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -13.98F, -2.37F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, -13.98F, -2.37F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.3333F, KeyframeAnimations.posVec(0.0F, -13.98F, -2.37F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.5833F, KeyframeAnimations.posVec(0.0F, -7.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.8333F, KeyframeAnimations.posVec(0.0F, -9.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.0833F, KeyframeAnimations.posVec(0.0F, -16.71F, -3.69F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2917F, KeyframeAnimations.posVec(0.0F, -28.0F, -5.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(114.98F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.degreeVec(114.98F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.3333F, KeyframeAnimations.degreeVec(114.98F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.5833F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.8333F, KeyframeAnimations.degreeVec(172.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.0833F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2917F, KeyframeAnimations.degreeVec(197.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.5F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -14.01F, -2.35F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, -14.01F, -2.35F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.3333F, KeyframeAnimations.posVec(0.0F, -14.01F, -2.35F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.5833F, KeyframeAnimations.posVec(0.0F, -5.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.8333F, KeyframeAnimations.posVec(0.0F, -7.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.0833F, KeyframeAnimations.posVec(0.0F, -15.5F, -3.76F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2917F, KeyframeAnimations.posVec(0.0F, -28.0F, -5.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.build();
	public static final AnimationDefinition WARDEN_ROAR = AnimationDefinition.Builder.withLength(4.2F)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.6F, KeyframeAnimations.degreeVec(32.5F, 0.0F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.84F, KeyframeAnimations.degreeVec(38.33F, 0.0F, 2.99F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.08F, KeyframeAnimations.degreeVec(40.97F, 0.0F, -4.3F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.36F, KeyframeAnimations.degreeVec(44.41F, 0.0F, 6.29F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.0F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.posVec(0.0F, -1.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.6F, KeyframeAnimations.posVec(0.0F, -3.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, -3.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.6F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, -27.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 26.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.04F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, -27.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.44F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 26.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.84F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.6F, KeyframeAnimations.posVec(0.0F, -2.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2F, KeyframeAnimations.posVec(0.0F, -2.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.48F, KeyframeAnimations.posVec(0.0F, -2.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_ear",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.76F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.85F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.08F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.85F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.72F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.85F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_ear",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.76F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.85F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.08F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.85F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.72F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.85F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_arm",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.72F, KeyframeAnimations.degreeVec(-120.0F, 0.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.degreeVec(-77.5F, 3.75F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.48F, KeyframeAnimations.degreeVec(67.5F, -32.5F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.48F, KeyframeAnimations.degreeVec(37.5F, -32.5F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(27.6F, -17.1F, 32.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_arm",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.72F, KeyframeAnimations.posVec(3.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.48F, KeyframeAnimations.posVec(4.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.48F, KeyframeAnimations.posVec(4.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_arm",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.72F, KeyframeAnimations.degreeVec(-125.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.degreeVec(-76.25F, -17.5F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.48F, KeyframeAnimations.degreeVec(62.5F, 42.5F, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.48F, KeyframeAnimations.degreeVec(37.5F, 27.5F, -27.5F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(25.0F, 18.4F, -30.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_arm",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.72F, KeyframeAnimations.posVec(-3.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.48F, KeyframeAnimations.posVec(-4.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.48F, KeyframeAnimations.posVec(-4.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(4.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.build();
	public static final AnimationDefinition WARDEN_SNIFF = AnimationDefinition.Builder.withLength(4.16F)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.56F, KeyframeAnimations.degreeVec(17.5F, 32.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.96F, KeyframeAnimations.degreeVec(0.0F, 32.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.8F, KeyframeAnimations.degreeVec(10.0F, -30.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.32F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.68F, KeyframeAnimations.degreeVec(0.0F, 40.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.96F, KeyframeAnimations.degreeVec(-22.5F, 40.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.24F, KeyframeAnimations.degreeVec(0.0F, 20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.52F, KeyframeAnimations.degreeVec(-35.0F, 20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.76F, KeyframeAnimations.degreeVec(0.0F, 20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.32F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_arm",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.96F, KeyframeAnimations.degreeVec(17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.76F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.32F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_arm",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.96F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.76F, KeyframeAnimations.degreeVec(17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.32F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.build();
}
