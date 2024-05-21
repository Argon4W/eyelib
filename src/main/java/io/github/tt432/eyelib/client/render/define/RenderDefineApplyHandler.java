package io.github.tt432.eyelib.client.render.define;

import io.github.tt432.eyelib.capability.AnimatableCapability;
import io.github.tt432.eyelib.client.animation.bedrock.BrAnimation;
import io.github.tt432.eyelib.client.animation.bedrock.controller.BrAnimationControllers;
import io.github.tt432.eyelib.client.animation.component.AnimationComponent;
import io.github.tt432.eyelib.client.animation.component.ModelComponent;
import io.github.tt432.eyelib.client.loader.BrAnimationControllerLoader;
import io.github.tt432.eyelib.client.loader.BrAnimationLoader;
import io.github.tt432.eyelib.client.loader.BrModelLoader;
import io.github.tt432.eyelib.client.loader.ModelReplacerLoader;
import io.github.tt432.eyelib.client.model.bedrock.BrModel;
import io.github.tt432.eyelib.client.model.bedrock.material.ModelMaterial;
import io.github.tt432.eyelib.client.render.visitor.BlankEntityModelRenderVisit;
import io.github.tt432.eyelib.event.InitComponentEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.function.Function;

/**
 * @author TT432
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RenderDefineApplyHandler {

    @SubscribeEvent
    public static void onEvent(InitComponentEvent event) {
        if (event.entity instanceof Entity entity && event.componentObject instanceof AnimatableCapability<?> capability) {
            ModelComponent modelComponent = capability.getModelComponent();

            RenderDefine renderDefine = ModelReplacerLoader.byTarget(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));

            if (renderDefine == null) {
                return;
            }

            BrModel model = BrModelLoader.getModel(renderDefine.model());

            if (model == null) {
                return;
            }

            ModelMaterial material = BrModelLoader.getMaterial(renderDefine.material());

            if (material == null) {
                return;
            }

            // new Random(seed).nextInt(bound) 在bound为2^n时存在问题
            int randomIdx = Math.abs(new Random(entity.getId()).nextInt()) % material.textures().size();
            ResourceLocation texture = material.textures().get(randomIdx);

            // TODO 补充更多的 renderType，或者找到一个检索的办法
            boolean isSolid;
            Function<ResourceLocation, RenderType> renderTypeFactory = switch (material.renderType().toString()) {
                case "minecraft:cutout" -> {
                    isSolid = false;
                    yield RenderType::entityCutout;
                }
                default -> {
                    isSolid = true;
                    yield RenderType::entitySolid;
                }// "minecraft:solid"
            };

            modelComponent.setInfo(new ModelComponent.Info(
                    model,
                    new ResourceLocation(texture.getNamespace(),
                            "textures/" + texture.getPath() + ".png"),
                    renderTypeFactory,
                    isSolid,
                    new BlankEntityModelRenderVisit()
            ));

            AnimationComponent animComponent = capability.getAnimationComponent();
            RenderDefine.RDAnimationController entry = renderDefine.animationControllerEntry();

            String animationName = entry.animation();
            String name = entry.name();

            if (!animationName.isBlank() && !name.isBlank()) {
                BrAnimationControllers controller = BrAnimationControllerLoader.getController(new ResourceLocation(name));
                BrAnimation animation = BrAnimationLoader.getAnimation(new ResourceLocation(animationName));

                animComponent.setup(controller, animation);
            }
        }
    }
}
