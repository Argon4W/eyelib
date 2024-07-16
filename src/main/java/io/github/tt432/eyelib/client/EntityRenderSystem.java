package io.github.tt432.eyelib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.tt432.eyelib.capability.RenderData;
import io.github.tt432.eyelib.capability.component.AnimationComponent;
import io.github.tt432.eyelib.capability.component.ModelComponent;
import io.github.tt432.eyelib.client.animation.BrAnimator;
import io.github.tt432.eyelib.client.model.bedrock.BrModel;
import io.github.tt432.eyelib.client.render.BrModelTextures;
import io.github.tt432.eyelib.client.render.RenderParams;
import io.github.tt432.eyelib.client.render.bone.BoneRenderInfos;
import io.github.tt432.eyelib.client.render.renderer.BrModelRenderer;
import io.github.tt432.eyelib.event.InitComponentEvent;
import io.github.tt432.eyelib.mixin.LivingEntityRendererAccessor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * @author TT432
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityRenderSystem {
    @SubscribeEvent
    public static void onEvent(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        RenderData<Entity> cap = RenderData.getComponent(entity);

        if (cap == null) return;

        MinecraftForge.EVENT_BUS.post(new InitComponentEvent(entity, cap));
    }

    @SubscribeEvent
    public static void onEvent(RenderLivingEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        RenderData<?> cap = RenderData.getComponent(entity);

        if (cap == null) return;

        if (cap.getAnimationComponent().getSerializableInfo() != null) {
            AnimationComponent component = cap.getAnimationComponent();
            var scope = cap.getScope();

            if (component.getAnimationController() != null) {
                BoneRenderInfos tickedInfos = BrAnimator.tickAnimation(component, scope,
                        ClientTickHandler.getTick() + Minecraft.getInstance().getPartialTick());
                cap.getModelComponent().getBoneInfos().set(tickedInfos);
            }
        }

        ModelComponent modelComponent = cap.getModelComponent();

        BrModel model = modelComponent.getModel();
        ResourceLocation texture = modelComponent.getTexture();

        if (model != null && texture != null) {
            event.setCanceled(true);

            PoseStack poseStack = event.getPoseStack();

            RenderType renderType = modelComponent.getRenderType(texture);
            VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(renderType);

            poseStack.pushPose();

            PoseStack.Pose last = poseStack.last();
            RenderParams renderParams = new RenderParams(entity,
                    new PoseStack.Pose(new Matrix4f(last.pose()), new Matrix3f(last.normal())),
                    poseStack, renderType, buffer, event.getPackedLight(),
                    LivingEntityRenderer.getOverlayCoords(entity,
                            ((LivingEntityRendererAccessor) (event.getRenderer()))
                                    .callGetWhiteOverlayProgress(entity, event.getPartialTick())));

            BrModelRenderer.render(renderParams, model, modelComponent.getBoneInfos(),
                    BrModelTextures.getTwoSideInfo(model, modelComponent.isSolid(), texture), modelComponent.getVisitors());

            poseStack.popPose();
        }
    }
}
