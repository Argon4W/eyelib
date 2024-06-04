package io.github.tt432.eyelib.client.render.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.tt432.eyelib.client.model.bedrock.BrBone;
import io.github.tt432.eyelib.client.model.bedrock.BrCube;
import io.github.tt432.eyelib.client.model.bedrock.BrFace;
import io.github.tt432.eyelib.client.model.bedrock.BrModel;
import io.github.tt432.eyelib.client.render.BrModelTextures;
import io.github.tt432.eyelib.client.render.bone.BoneRenderInfoEntry;
import io.github.tt432.eyelib.client.render.bone.BoneRenderInfos;
import io.github.tt432.eyelib.client.render.visitor.builtin.ModelRenderVisitor;
import io.github.tt432.eyelib.util.math.EyeMath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author TT432
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BrModelRenderer {
    private static final Vector3f nPivot = new Vector3f();

    private static final float R180 = 180 * EyeMath.DEGREES_TO_RADIANS;

    public static void render(BrModel model, BoneRenderInfos infos, RenderType renderType, PoseStack poseStack,
                              VertexConsumer consumer, ModelRenderVisitor visitor) {
        render(model, infos, poseStack, renderType, consumer, null, visitor);
    }

    private static final Deque<BrModelTextures.TwoSideInfoMap> twoSideInfoMapStack = new ArrayDeque<>();

    public static void render(BrModel model, BoneRenderInfos infos, PoseStack poseStack, RenderType renderType,
                              VertexConsumer consumer, BrModelTextures.TwoSideInfoMap map, ModelRenderVisitor visitor) {
        twoSideInfoMapStack.push(map);
        poseStack.pushPose();

        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        pose.rotateY(R180);
        Matrix3f normal = last.normal();
        normal.rotateY(R180);

        for (BrBone toplevelBone : model.toplevelBones()) {
            renderBone(poseStack, visitor, renderType, infos, toplevelBone, consumer);
        }

        poseStack.popPose();
        twoSideInfoMapStack.pop();
    }

    private static void renderBone(PoseStack poseStack, ModelRenderVisitor visitor, RenderType renderType,
                                   BoneRenderInfos infos, BrBone bone, VertexConsumer consumer) {
        poseStack.pushPose();

        BoneRenderInfoEntry boneRenderInfoEntry = infos.get(bone.name());

        visitor.visitBone(poseStack, bone, renderType, boneRenderInfoEntry, consumer, true);

        PoseStack.Pose last = poseStack.last();
        Matrix4f m4 = last.pose();

        m4.translate(boneRenderInfoEntry.getRenderPosition());

        Vector3f renderPivot = bone.pivot();

        m4.translate(renderPivot);

        Vector3f rotation = boneRenderInfoEntry.getRenderRotation();

        Matrix3f normal = last.normal();
        normal.rotateZYX(rotation);
        m4.rotateZYX(rotation);

        Vector3f boneRotation = bone.rotation();

        normal.rotateZYX(boneRotation);
        m4.rotateZYX(boneRotation);

        Vector3f scale = boneRenderInfoEntry.getRenderScala();

        poseStack.scale(scale.x, scale.y, scale.z);

        m4.translate(renderPivot.negate(nPivot));

        visitor.visitBone(poseStack, bone, renderType, boneRenderInfoEntry, consumer, false);

        bone.locators().forEach((name, locator) -> {
            poseStack.pushPose();

            PoseStack.Pose last1 = poseStack.last();
            Matrix4f pose = last1.pose();
            pose.translate(locator.getOffset());
            pose.rotateZYX(locator.getRotation());
            last1.normal().rotateZYX(locator.getRotation());

            visitor.visitLocator(poseStack, bone, renderType, name, locator, boneRenderInfoEntry, consumer);

            poseStack.popPose();
        });

        for (int i = 0; i < bone.cubes().size(); i++) {
            BrCube brCube = bone.cubes().get(i);
            BrModelTextures.TwoSideInfoMap lastTwoSideInfoMap = twoSideInfoMapStack.getLast();
            renderCube(poseStack, visitor, renderType,
                    brCube, lastTwoSideInfoMap == null || lastTwoSideInfoMap.isTwoSide(bone.name(), i), consumer);
        }

        for (BrBone child : bone.children()) {
            renderBone(poseStack, visitor, renderType, infos, child, consumer);
        }

        poseStack.popPose();
    }

    private static void renderCube(PoseStack poseStack, ModelRenderVisitor visitor, RenderType renderType, BrCube cube,
                                   boolean needTwoSide, VertexConsumer consumer) {
        visitor.visitCube(poseStack, cube, renderType, consumer);

        for (BrFace face : cube.faces()) {
            for (int i = 0; i < face.getVertex().length; i++) {
                visitor.visitVertex(poseStack, cube, renderType, face, i, consumer);
            }

            if (needTwoSide) {
                for (int i = face.getVertex().length - 1; i >= 0; i--) {
                    visitor.visitVertex(poseStack, cube, renderType, face, i, consumer);
                }
            }
        }
    }
}
