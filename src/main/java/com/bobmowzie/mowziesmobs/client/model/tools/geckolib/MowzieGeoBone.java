package com.bobmowzie.mowziesmobs.client.model.tools.geckolib;

import com.bobmowzie.mowziesmobs.client.model.tools.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.GeoBone;

import javax.annotation.Nullable;

public class MowzieGeoBone extends GeoBone {

    public Matrix4f rotationOverride;
    public boolean inheritRotation = true;
    public boolean inheritTranslation = true;
    protected boolean forceMatrixTransform = false;

    private boolean isDynamicJoint = false;

    public MowzieGeoBone(@Nullable GeoBone parent, String name, Boolean mirror, @Nullable Double inflate, @Nullable Boolean dontRender, @Nullable Boolean reset) {
        super(parent, name, mirror, inflate, dontRender, reset);
        rotationOverride = null;
    }

    public MowzieGeoBone(MowzieGeoBone geoBone) {
        super(null, geoBone.getName() + "_chain", geoBone.getMirror(), geoBone.getInflate(), geoBone.shouldNeverRender(), geoBone.getReset());
        this.setPos(geoBone.getPos());
        this.setRot(geoBone.getRot());
        this.setPivotX(geoBone.getPivotX());
        this.setPivotY(geoBone.getPivotY());
        this.setPivotZ(geoBone.getPivotZ());
        this.setScale(geoBone.getScale());

        this.getCubes().addAll(geoBone.getCubes());
        this.saveInitialSnapshot();
        this.getChildBones().addAll(geoBone.getChildBones());
    }

    public MowzieGeoBone getParent() {
        return (MowzieGeoBone) super.getParent();
    }

    // Position utils
    public void addPos(Vec3 vec) {
        addPos((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void addPos(float x, float y, float z) {
        addPosX(x);
        addPosY(y);
        addPosZ(z);
    }

    public void addPosX(float x) {
        setPosX(getPosX() + x);
    }

    public void addPosY(float y) {
        setPosY(getPosY() + y);
    }

    public void addPosZ(float z) {
        setPosZ(getPosZ() + z);
    }

    public void setPos(Vec3 vec) {
        setPos((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void setPos(float x, float y, float z) {
        setPosX(x);
        setPosY(y);
        setPosZ(z);
    }

    public Vec3 getPos() {
        return new Vec3(getPosX(), getPosY(), getPosZ());
    }

    // Rotation utils
    public void addRot(Vec3 vec) {
        addRot((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void addRot(float x, float y, float z) {
        addRotX(x);
        addRotY(y);
        addRotZ(z);
    }

    public void addRotX(float x) {
        setRotX(getRotX() + x);
    }

    public void addRotY(float y) {
        setRotY(getRotY() + y);
    }

    public void addRotZ(float z) {
        setRotZ(getRotZ() + z);
    }

    public void setRot(Vector3d vec) {
        setRot((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void setRot(Vec3 vec) {
        setRot((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void setRot(float x, float y, float z) {
        setRotX(x);
        setRotY(y);
        setRotZ(z);
    }

    public Vector3d getRot() {
        return new Vector3d(getRotX(), getRotY(), getRotZ());
    }

    // Scale utils
    public void multiplyScale(Vec3 vec) {
        multiplyScale((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void multiplyScale(float x, float y, float z) {
        setScaleX(getScaleX() * x);
        setScaleY(getScaleY() * y);
        setScaleZ(getScaleZ() * z);
    }

    public void setScale(Vec3 vec) {
        setScale((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void setScale(Vector3d vec) {
        setScale((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void setScale(float x, float y, float z) {
        setScaleX(x);
        setScaleY(y);
        setScaleZ(z);
    }

    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }

    public Vector3d getScale() {
        return new Vector3d(getScaleX(), getScaleY(), getScaleZ());
    }

    public void addRotationOffsetFromBone(MowzieGeoBone source) {
        setRotX(getRotX() + source.getRotX() - source.getInitialSnapshot().getRotX());
        setRotY(getRotY() + source.getRotY() - source.getInitialSnapshot().getRotY());
        setRotZ(getRotZ() + source.getRotZ() - source.getInitialSnapshot().getRotZ());
    }

    public void setForceMatrixTransform(boolean forceMatrixTransform) {
        this.forceMatrixTransform = forceMatrixTransform;
    }

    public boolean isForceMatrixTransform() {
        return forceMatrixTransform;
    }


    public Matrix4f getModelRotationMat() {
        Matrix4f matrix = new Matrix4f(getModelSpaceMatrix());
        removeMatrixTranslation(matrix);
        return matrix;
    }

    public static void removeMatrixTranslation(Matrix4f matrix) {
        matrix.m30(0);
        matrix.m31(0);
        matrix.m32(0);
    }

    public void setModelXformOverride(Matrix4f mat) {
        rotationOverride = mat;
    }

    public void setWorldPos(Entity entity, Vec3 worldPos, float delta) {
        PoseStack matrixStack = new PoseStack();
        float dx = (float) (entity.xOld + (entity.getX() - entity.xOld) * delta);
        float dy = (float) (entity.yOld + (entity.getY() - entity.yOld) * delta);
        float dz = (float) (entity.zOld + (entity.getZ() - entity.zOld) * delta);
        matrixStack.translate(dx, dy, dz);
        float dYaw = Mth.rotLerp(delta, entity.yRotO, entity.getYRot());
        matrixStack.mulPose(MathUtils.quatFromRotationXYZ(0, -dYaw + 180, 0, true));
        matrixStack.scale(-1, -1, 1);
        matrixStack.translate(0, -1.5f, 0);
        PoseStack.Pose matrixEntry = matrixStack.last();
        Matrix4f matrix4f = matrixEntry.pose();
        matrix4f.invert();

        Vector4f vec = new Vector4f((float) worldPos.x(), (float) worldPos.y(), (float) worldPos.z(), 1);
        vec.mul(matrix4f);
        setPosX(vec.x() * 16);
        setPosY(vec.y() * 16);
        setPosZ(vec.z() * 16);
    }

    public void setDynamicJoint(boolean dynamicJoint) {
        isDynamicJoint = dynamicJoint;
    }

    public boolean isDynamicJoint() {
        return isDynamicJoint;
    }
}