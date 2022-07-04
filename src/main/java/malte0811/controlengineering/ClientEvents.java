package malte0811.controlengineering;

import blusunrize.immersiveengineering.api.IETags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import malte0811.controlengineering.blockentity.bus.LineAccessBlockEntity;
import malte0811.controlengineering.blockentity.bus.RSRemapperBlockEntity;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.gui.misc.BusSignalSelector;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.items.PCBStackItem;
import malte0811.controlengineering.util.RaytraceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT, bus = Bus.FORGE)
public class ClientEvents {
    @SubscribeEvent
    public static void renderSelectionShape(DrawSelectionEvent.HighlightBlock ev) {
        BlockHitResult target = ev.getTarget();
        BlockPos highlighted = target.getBlockPos();
        List<? extends SelectionShapes> selectedStack = getSelectedStack();
        if (selectedStack != null) {
            PoseStack transform = ev.getPoseStack();
            transform.pushPose();
            Vec3 projectedView = Vec3.atLowerCornerOf(highlighted).subtract(ev.getCamera().getPosition());
            transform.translate(projectedView.x, projectedView.y, projectedView.z);
            VertexConsumer builder = ev.getMultiBufferSource().getBuffer(RenderType.lines());
            final int pushCount = selectedStack.size() - 1;
            for (int i = 0; i < pushCount; ++i) {
                SelectionShapes nonTopShape = selectedStack.get(i);
                if (nonTopShape.shouldRenderNonTop()) {
                    renderShape(transform, nonTopShape, builder);
                }
                var inverse = nonTopShape.outerToInnerPosition().copy();
                //TODO cache?
                inverse.invert();
                new Transformation(inverse).push(transform);
            }
            renderShape(transform, selectedStack.get(pushCount), builder);
            for (int i = 0; i < pushCount; ++i) {
                transform.popPose();
            }
            transform.popPose();
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        final ItemStack held = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(mc.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        final BlockPos pos = hitResult.getBlockPos();
        var targetBE = mc.player.level.getBlockEntity(pos);
        if (held.is(IETags.screwdrivers)) {
            if (targetBE instanceof LineAccessBlockEntity access) {
                lines.add(Component.translatable(BusSignalSelector.BUS_LINE_INDEX_KEY, access.selectedLine));
            } else if (targetBE instanceof RSRemapperBlockEntity remapper) {
                remapper.addOverlay(lines, hitResult);
            }
        }
        List<? extends SelectionShapes> shapeStack = getSelectedStack();
        if (shapeStack != null) {
            for (int i = shapeStack.size() - 1; i >= 0; i--) {
                final Component line = shapeStack.get(i).getOverlayText();
                if (line != null) {
                    lines.add(line);
                }
            }
        }
        for (int i = 0; i < lines.size(); ++i) {
            mc.font.draw(
                    event.getPoseStack(),
                    lines.get(i),
                    mc.getWindow().getGuiScaledWidth() / 2f + 8,
                    mc.getWindow().getGuiScaledHeight() / 2f + 8 + i * (mc.font.lineHeight + 2),
                    -1
            );
        }
    }

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent ev) {
        if (ev.getItemStack().is(IEItemRefs.LOGIC_CIRCUIT.asItem())) {
            ev.getToolTip().add(PCBStackItem.useIn(IEItemRefs.LOGIC_UNIT));
        }
    }

    private static void renderShape(PoseStack transform, SelectionShapes shape, VertexConsumer builder) {
        shape.plotBox((v1, v2) -> {
            Vec3 normal = v2.subtract(v1);
            var poseMatrix = transform.last().pose();
            var normalMatrix = transform.last().normal();
            addPoint(builder, poseMatrix, normalMatrix, v1, normal);
            addPoint(builder, poseMatrix, normalMatrix, v2, normal);
        });
    }

    private static void addPoint(
            VertexConsumer builder, Matrix4f transform, Matrix3f normalTransform, Vec3 pos, Vec3 normal
    ) {
        builder.vertex(transform, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(0, 0, 0, 0.4F)
                .normal(normalTransform, (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    @Nullable
    private static List<? extends SelectionShapes> getSelectedStack() {
        Minecraft mc = Minecraft.getInstance();
        final HitResult mop = mc.hitResult;
        if (!(mop instanceof BlockHitResult target)) {
            return null;
        }
        BlockPos highlighted = target.getBlockPos();
        Level world = mc.level;
        Player player = mc.player;
        if (world == null || player == null) {
            return null;
        }
        if (world.getBlockEntity(highlighted) instanceof SelectionShapeOwner shapeOwner) {
            List<? extends SelectionShapes> selectedStack = shapeOwner.getShape()
                    .getTargeted(RaytraceUtils.create(player, mc.getFrameTime(), Vec3.atLowerCornerOf(highlighted)))
                    .getFirst();
            if (!selectedStack.isEmpty()) {
                return selectedStack;
            }
        }
        return null;
    }
}
