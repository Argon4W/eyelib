package io.github.tt432.eyelib.util.math;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * @author TT432
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PoseStacks {
    public static boolean isPureTranslation(Matrix4f pMatrix) {
        return (pMatrix.properties() & 8) != 0;
    }

    public static boolean isOrthonormal(Matrix4f pMatrix) {
        return (pMatrix.properties() & 16) != 0;
    }

    public static void mulPose(PoseStack poseStack, Matrix4f pPose) {
        PoseStack.Pose pose = poseStack.last();
        pose.pose().mul(pPose);
        if (!isPureTranslation(pPose)) {
            if (isOrthonormal(pPose)) {
                pose.normal().mul(new Matrix3f(pPose));
            } else {
                pose.normal().set(pose.pose()).invert().transpose();
            }
        }
    }
}
