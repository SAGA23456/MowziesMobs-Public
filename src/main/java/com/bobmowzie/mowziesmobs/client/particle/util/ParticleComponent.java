package com.bobmowzie.mowziesmobs.client.particle.util;

import com.bobmowzie.mowziesmobs.client.model.tools.SimplexNoise;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class ParticleComponent {
    public ParticleComponent() {

    }

    public void init(AdvancedParticleBase particle) {

    }

    public void preUpdate(AdvancedParticleBase particle) {

    }

    public void postUpdate(AdvancedParticleBase particle) {

    }

    public void preRender(AdvancedParticleBase particle, float partialTicks) {

    }

    public void postRender(AdvancedParticleBase particle, VertexConsumer buffer, Camera renderInfo, float partialTicks, int lightmap) {

    }

    public abstract static class AnimData {
        public float evaluate(float t) {
            return 0;
        }
    }

    public static class KeyTrack extends AnimData {
        float[] values;
        float[] times;

        public KeyTrack(float[] values, float[] times) {
            this.values = values;
            this.times = times;
            if (values.length != times.length) System.out.println("Malformed key track. Must have same number of keys and values or key track will evaluate to 0.");
        }

        @Override
        public float evaluate(float t) {
            if (values.length != times.length) return 0;
            for (int i = 0; i < times.length; i++) {
                float time = times[i];
                if (t == time) return values[i];
                else if (t < time) {
                    if (i == 0) return values[0];
                    float a = (t - times[i - 1]) / (time - times[i - 1]);
                    return values[i - 1] * (1 - a) + values[i] * a;
                }
                else {
                    if (i == values.length - 1) return values[i];
                }
            }
            return 0;
        }

        public static KeyTrack startAndEnd(float startValue, float endValue) {
            return new KeyTrack(new float[] {startValue, endValue}, new float[] {0, 1});
        }

        public static KeyTrack oscillate(float value1, float value2, int frequency) {
            if (frequency <= 1) new KeyTrack(new float[] {value1, value2}, new float[] {0, 1});
            float step = 1.0f / frequency;
            float[] times = new float[frequency + 1];
            float[] values = new float[frequency + 1];
            for (int i = 0; i < frequency + 1; i++) {
                float value = i % 2 == 0 ? value1 : value2;
                times[i] = step * i;
                values[i] = value;
            }
            return new KeyTrack(values, times);
        }
    }

    public static class Oscillator extends AnimData {
        float value1, value2;
        float frequency;
        float phaseShift;

        public Oscillator(float value1, float value2, float frequency, float phaseShift) {
            this.value1 = value1;
            this.value2 = value2;
            this.frequency = frequency;
            this.phaseShift = phaseShift;
        }

        @Override
        public float evaluate(float t) {
            float a = (value2 - value1) / 2f;
            return (float) (value1 + a + a * Math.cos(t * frequency + phaseShift));
        }
    }

    public static class Constant extends AnimData {
        float value;

        public Constant(float value) {
            this.value = value;
        }

        @Override
        public float evaluate(float t) {
            return value;
        }
    }

    public static Constant constant(float value) {
        return new Constant(value);
    }

    public static class Gravity extends ParticleComponent {
        private final AnimData animData;

        public Gravity(AnimData gravityOverTime) {
            animData = gravityOverTime;
        }

        public Gravity(float gravity) {
            this(new Constant(gravity));
        }

        @Override
        public void init(AdvancedParticleBase particle) {
            particle.setGravity(animData.evaluate(0));
        }

        @Override
        public void preUpdate(AdvancedParticleBase particle) {
            super.preUpdate(particle);
            float ageFrac = particle.getAge() / particle.getLifetime();
            particle.setGravity(animData.evaluate(ageFrac));
        }
    }

    public static class PropertyControl extends ParticleComponent {
        public enum EnumParticleProperty {
            POS_X, POS_Y, POS_Z,
            MOTION_X, MOTION_Y, MOTION_Z,
            RED, GREEN, BLUE, ALPHA,
            SCALE,
            YAW, PITCH, ROLL, // For not facing camera
            PARTICLE_ANGLE, // For facing camera
            AIR_DRAG
        }

        private final AnimData animData;
        private final EnumParticleProperty property;
        private final boolean additive;
        public PropertyControl(EnumParticleProperty property, AnimData animData, boolean additive) {
            this.property = property;
            this.animData = animData;
            this.additive = additive;
        }

        @Override
        public void init(AdvancedParticleBase particle) {
            float value = animData.evaluate(0);
            applyUpdate(particle, value);
            applyRender(particle, value);
        }

        @Override
        public void preRender(AdvancedParticleBase particle, float partialTicks) {
            float ageFrac = (particle.getAge() + partialTicks) / particle.getLifetime();
            float value = animData.evaluate(ageFrac);
            applyRender(particle, value);
        }

        @Override
        public void preUpdate(AdvancedParticleBase particle) {
            float ageFrac = particle.getAge() / particle.getLifetime();
            float value = animData.evaluate(ageFrac);
            applyUpdate(particle, value);
        }

        private void applyUpdate(AdvancedParticleBase particle, float value) {
            if (property == EnumParticleProperty.POS_X) {
                if (additive) particle.setPosX(particle.getPosX() + value);
                else particle.setPosX(value);
            }
            else if (property == EnumParticleProperty.POS_Y) {
                if (additive) particle.setPosY(particle.getPosY() + value);
                else particle.setPosY(value);
            }
            else if (property == EnumParticleProperty.POS_Z) {
                if (additive) particle.setPosZ(particle.getPosZ() + value);
                else particle.setPosZ(value);
            }
            else if (property == EnumParticleProperty.MOTION_X) {
                if (additive) particle.setMotionX(particle.getMotionX() + value);
                else particle.setMotionX(value);
            }
            else if (property == EnumParticleProperty.MOTION_Y) {
                if (additive) particle.setMotionY(particle.getMotionY() + value);
                else particle.setMotionY(value);
            }
            else if (property == EnumParticleProperty.MOTION_Z) {
                if (additive) particle.setMotionZ(particle.getMotionZ() + value);
                else particle.setMotionZ(value);
            }
            else if (property == EnumParticleProperty.AIR_DRAG) {
                if (additive) particle.airDrag += value;
                else particle.airDrag = value;
            }
        }

        private void applyRender(AdvancedParticleBase particle, float value) {
            if (property == EnumParticleProperty.RED) {
                if (additive) particle.red += value;
                else particle.red = value;
            }
            else if (property == EnumParticleProperty.GREEN) {
                if (additive) particle.green += value;
                else particle.green = value;
            }
            else if (property == EnumParticleProperty.BLUE) {
                if (additive) particle.blue += value;
                else particle.blue = value;
            }
            else if (property == EnumParticleProperty.ALPHA) {
                if (additive) particle.alpha += value;
                else particle.alpha = value;
            }
            else if (property == EnumParticleProperty.SCALE) {
                if (additive) particle.scale += value;
                else particle.scale = value;
            }
            else if (property == EnumParticleProperty.YAW) {
                if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                    ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles) particle.rotation;
                    if (additive) eulerRot.yaw += value;
                    else eulerRot.yaw = value;
                }
            }
            else if (property == EnumParticleProperty.PITCH) {
                if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                    ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles) particle.rotation;
                    if (additive) eulerRot.pitch += value;
                    else eulerRot.pitch = value;
                }
            }
            else if (property == EnumParticleProperty.ROLL) {
                if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                    ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles) particle.rotation;
                    if (additive) eulerRot.roll += value;
                    else eulerRot.roll = value;
                }
            }
            else if (property == EnumParticleProperty.PARTICLE_ANGLE) {
                if (particle.rotation instanceof ParticleRotation.FaceCamera) {
                    ParticleRotation.FaceCamera faceCameraRot = (ParticleRotation.FaceCamera) particle.rotation;
                    if (additive) faceCameraRot.faceCameraAngle += value;
                    else faceCameraRot.faceCameraAngle = value;
                }
            }
        }
    }

    public static class PinLocation extends ParticleComponent {
        private final Vec3[] location;

        public PinLocation(Vec3[] location) {
            this.location = location;
        }

        @Override
        public void init(AdvancedParticleBase particle) {
            if (location != null && location.length > 0 && location[0] != null) {
                particle.setPos(location[0].x, location[0].y, location[0].z);
            }
        }

        @Override
        public void preUpdate(AdvancedParticleBase particle) {
            if (location != null && location.length > 0 && location[0] != null) {
                particle.setPos(location[0].x, location[0].y, location[0].z);
            }
        }

        @Override
        public void preRender(AdvancedParticleBase particle, float partialTicks) {
            super.preRender(particle, partialTicks);
            particle.doRender = location != null && location.length > 0 && location[0] != null;
        }
    }

    public static class Attractor extends ParticleComponent {
        public enum EnumAttractorBehavior {
            LINEAR,
            EXPONENTIAL,
            SIMULATED,
        }

        private final Vec3[] location;
        private final AnimData strengthData;
        private final float killDist;
        private final EnumAttractorBehavior behavior;
        private Vec3 startLocation;

        public Attractor(Vec3[] location, float strength, float killDist, EnumAttractorBehavior behavior) {
            this(location, new Constant(strength), killDist, behavior);
        }

        public Attractor(Vec3[] location, AnimData strength, float killDist, EnumAttractorBehavior behavior) {
            this.location = location;
            this.strengthData = strength;
            this.killDist = killDist;
            this.behavior = behavior;
        }

        @Override
        public void init(AdvancedParticleBase particle) {
            startLocation = new Vec3(particle.getPosX(), particle.getPosY(), particle.getPosZ());
        }

        @Override
        public void preUpdate(AdvancedParticleBase particle) {
            float ageFrac = particle.getAge() / (particle.getLifetime() - 1);
            double strength = strengthData.evaluate(ageFrac);
            if (location.length > 0) {
                Vec3 destinationVec = location[0];
                Vec3 currPos = new Vec3(particle.getPosX(), particle.getPosY(), particle.getPosZ());
                Vec3 diff = destinationVec.subtract(currPos);
                if (diff.length() < killDist) particle.remove();
                if (behavior == EnumAttractorBehavior.EXPONENTIAL) {
                    Vec3 path = destinationVec.subtract(startLocation).scale(Math.pow(ageFrac, strength)).add(startLocation).subtract(currPos);
                    particle.move(path.x, path.y, path.z);
                }
                else if (behavior == EnumAttractorBehavior.LINEAR) {
                    Vec3 path = destinationVec.subtract(startLocation).scale(ageFrac).add(startLocation).subtract(currPos);
                    particle.move(path.x, path.y, path.z);
                }
                else {
                    double dist = Math.max(diff.length(), 0.001);
                    diff = diff.normalize().scale(strength / (dist * dist));
                    particle.setMotionX(Math.min(particle.getMotionX() + diff.x, 5));
                    particle.setMotionY(Math.min(particle.getMotionY() + diff.y, 5));
                    particle.setMotionZ(Math.min(particle.getMotionZ() + diff.z, 5));
                }
            }
        }
    }

    public static class Orbit extends ParticleComponent {
        private final Vec3[] location;
        private final AnimData phase;
        private final AnimData radius;
        private final AnimData axisX;
        private final AnimData axisY;
        private final AnimData axisZ;
        private final boolean faceCamera;

        public Orbit(Vec3[] location, AnimData phase, AnimData radius, AnimData axisX, AnimData axisY, AnimData axisZ, boolean faceCamera) {
            this.location = location;
            this.phase = phase;
            this.radius = radius;
            this.axisX = axisX;
            this.axisY = axisY;
            this.axisZ = axisZ;
            this.faceCamera = faceCamera;
        }

        @Override
        public void init(AdvancedParticleBase particle) {
            apply(particle, 0);
        }

        @Override
        public void preUpdate(AdvancedParticleBase particle) {
            float ageFrac = particle.getAge() / particle.getLifetime();
            apply(particle, ageFrac);
        }

        private void apply(AdvancedParticleBase particle, float t) {
            float p = phase.evaluate(t);
            float r = radius.evaluate(t);
            Vector3f axis;
            if (faceCamera && Minecraft.getInstance().player != null) {
                Vec3 lookAngle = Minecraft.getInstance().player.getLookAngle();
                axis = new Vector3f((float) lookAngle.x(), (float) lookAngle.y(), (float) lookAngle.z());
                axis.normalize();
            }
            else {
                axis = new Vector3f(axisX.evaluate(t), axisY.evaluate(t), axisZ.evaluate(t));
                axis.normalize();
            }

            Quaternionf quat = new Quaternionf(new AxisAngle4f(p * (float) Math.PI * 2, axis));
            Vector3f up = new Vector3f(0, 1, 0);
            Vector3f start = axis;
            if (Math.abs(axis.dot(up)) > 0.99) {
                start = new Vector3f(1, 0, 0);
            }
            start.cross(up);
            start.normalize();
            Vector3f newPos = start;
            quat.transform(newPos);
            newPos.mul(r);

            if (location.length > 0 && location[0] != null) {
                newPos.add((float)location[0].x, (float)location[0].y, (float)location[0].z);
            }
            particle.setPos(newPos.x(), newPos.y(), newPos.z());
        }
    }

    public static class FaceMotion extends ParticleComponent {
        public FaceMotion() {

        }

        @Override
        public void preRender(AdvancedParticleBase particle, float partialTicks) {
            super.preRender(particle, partialTicks);
            double dx = particle.getPosX() - particle.getPrevPosX();
            double dy = particle.getPosY() - particle.getPrevPosY();
            double dz = particle.getPosZ() - particle.getPrevPosZ();
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d != 0) {
                if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                    ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles) particle.rotation;
                    double a = dy / d;
                    a = Math.max(-1, Math.min(1, a));
                    float pitch = -(float) Math.asin(a);
                    float yaw = -(float) (Math.atan2(dz, dx) + Math.PI);
                    eulerRot.roll = pitch;
                    eulerRot.yaw = yaw;
//                particle.roll = (float) Math.PI / 2;
                }
                else if (particle.rotation instanceof ParticleRotation.OrientVector) {
                    ParticleRotation.OrientVector orientRot = (ParticleRotation.OrientVector) particle.rotation;
                    orientRot.orientation = new Vec3(dx, dy, dz).normalize();
                }
            }
        }
    }

    public static class AnimatedTexture extends ParticleComponent {

    }

    public static class ForceOverTime extends ParticleComponent {
        AnimData fx;
        AnimData fy;
        AnimData fz;

        public ForceOverTime(Vec3 force) {
            this.fx = new Constant((float) force.x());
            this.fy = new Constant((float) force.y());
            this.fz = new Constant((float) force.z());
        }

        public ForceOverTime(AnimData fx, AnimData fy, AnimData fz) {
            this.fx = fx;
            this.fy = fy;
            this.fz = fz;
        }

        @Override
        public void preUpdate(AdvancedParticleBase particle) {
            super.preUpdate(particle);
            float ageFrac = particle.getAge() / particle.getLifetime();
            particle.setMotionX(particle.getMotionX() + fx.evaluate(ageFrac));
            particle.setMotionY(particle.getMotionY() + fy.evaluate(ageFrac));
            particle.setMotionZ(particle.getMotionZ() + fz.evaluate(ageFrac));
        }
    }

    public static class CurlNoise extends ParticleComponent {
        float strength;
        float frequency;

        public CurlNoise(float strength, float frequency) {
            this.strength = strength;
            this.frequency = frequency;
        }

        @Override
        public void preUpdate(AdvancedParticleBase particle) {
            super.preUpdate(particle);
            Vec3 curlNoise = curlNoise(particle.getPos().scale(frequency)).scale(strength);
            particle.setMotionX(particle.getMotionX() + curlNoise.x());
            particle.setMotionY(particle.getMotionY() + curlNoise.y());
            particle.setMotionZ(particle.getMotionZ() + curlNoise.z());
        }

        // From https://github.com/cabbibo/glsl-curl-noise/blob/master/curl.glsl
        Vec3 snoiseVec3(Vec3 x){
            double s = SimplexNoise.noise(x.x, x.y, x.z);
            double s1 = SimplexNoise.noise(x.y - 19.1 , x.z + 33.4 , x.x + 47.2);
            double s2 = SimplexNoise.noise(x.z + 74.2 , x.x - 124.5 , x.y + 99.4);
            return new Vec3(s, s1 ,s2 );
        }

        Vec3 curlNoise(Vec3 p) {
            float e = 0.1f;
            Vec3 dx = new Vec3( e   , 0.0 , 0.0 );
            Vec3 dy = new Vec3( 0.0 , e   , 0.0 );
            Vec3 dz = new Vec3( 0.0 , 0.0 , e   );

            Vec3 p_x0 = snoiseVec3(p.subtract(dx) );
            Vec3 p_x1 = snoiseVec3(p.add(dx) );
            Vec3 p_y0 = snoiseVec3(p.subtract(dy));
            Vec3 p_y1 = snoiseVec3(p.add(dy));
            Vec3 p_z0 = snoiseVec3(p.subtract(dz));
            Vec3 p_z1 = snoiseVec3(p.add(dz));

            double x = p_y1.z - p_y0.z - p_z1.y + p_z0.y;
            double y = p_z1.x - p_z0.x - p_x1.z + p_x0.z;
            double z = p_x1.y - p_x0.y - p_y1.x + p_y0.x;

            double divisor = 1.0 / ( 2.0 * e );
            return new Vec3(x ,y ,z ).scale(divisor).normalize();
        }
    }
}
