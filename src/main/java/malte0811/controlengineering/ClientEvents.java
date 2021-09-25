package malte0811.controlengineering;

import malte0811.controlengineering.blocks.bus.LineAccessBlock;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.gui.misc.BusSignalSelector;
import malte0811.controlengineering.tiles.bus.LineAccessTile;
import malte0811.controlengineering.util.RaytraceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT, bus = Bus.FORGE)
public class ClientEvents {
    @SubscribeEvent
    public static void renderSelectionShape(DrawHighlightEvent.HighlightBlock ev) {
        BlockHitResult target = ev.getTarget();
        BlockPos highlighted = target.getBlockPos();
        List<? extends SelectionShapes> selectedStack = getSelectedStack();
        if (selectedStack != null) {
            PoseStack transform = ev.getMatrix();
            transform.pushPose();
            Vec3 projectedView = Vec3.atLowerCornerOf(highlighted).subtract(ev.getInfo().getPosition());
            transform.translate(projectedView.x, projectedView.y, projectedView.z);
            VertexConsumer builder = ev.getBuffers().getBuffer(RenderType.lines());
            final int pushCount = selectedStack.size() - 1;
            for (int i = 0; i < pushCount; ++i) {
                SelectionShapes nonTopShape = selectedStack.get(i);
                if (nonTopShape.shouldRenderNonTop()) {
                    renderShape(transform, nonTopShape, builder);
                }
                nonTopShape.outerToInnerPosition()
                        .toTransformationMatrix()
                        //TODO cache?
                        .inverse()
                        .push(transform);
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
        final HitResult mop = mc.hitResult;
        if (!(mop instanceof BlockHitResult)) {
            return;
        }
        List<String> lines = new ArrayList<>();
        final BlockPos pos = ((BlockHitResult) mop).getBlockPos();
        final BlockEntity te = mc.player.level.getBlockEntity(pos);
        if (te instanceof LineAccessTile && held.getToolTypes().contains(LineAccessBlock.SCREWDRIVER_TOOL)) {
            final int line = ((LineAccessTile) te).selectedLine;
            lines.add(I18n.get(BusSignalSelector.BUS_LINE_INDEX_KEY, line));
        }
        List<? extends SelectionShapes> shapeStack = getSelectedStack();
        if (shapeStack != null) {
            for (int i = shapeStack.size() - 1; i >= 0; i--) {
                final String line = shapeStack.get(i).getOverlayText();
                if (line != null) {
                    lines.add(line);
                }
            }
        }
        for (int i = 0; i < lines.size(); ++i) {
            mc.font.draw(
                    event.getMatrixStack(),
                    lines.get(i),
                    mc.getWindow().getGuiScaledWidth() / 2f + 8,
                    mc.getWindow().getGuiScaledHeight() / 2f + 8 + i * (mc.font.lineHeight + 2),
                    -1
            );
        }
    }

    private static void renderShape(PoseStack transform, SelectionShapes shape, VertexConsumer builder) {
        Matrix4f currentMatrix = transform.last().pose();
        shape.plotBox((v1, v2) -> {
            addPoint(builder, currentMatrix, v1);
            addPoint(builder, currentMatrix, v2);
        });
    }

    private static void addPoint(VertexConsumer builder, Matrix4f transform, Vec3 pos) {
        builder.vertex(transform, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(0, 0, 0, 0.4F)
                .endVertex();
    }

    @Nullable
    private static List<? extends SelectionShapes> getSelectedStack() {
        Minecraft mc = Minecraft.getInstance();
        final HitResult mop = mc.hitResult;
        if (!(mop instanceof BlockHitResult)) {
            return null;
        }
        BlockHitResult target = (BlockHitResult) mop;
        BlockPos highlighted = target.getBlockPos();
        Level world = mc.level;
        Player player = mc.player;
        if (world == null || player == null) {
            return null;
        }
        BlockEntity te = world.getBlockEntity(highlighted);
        if (te instanceof SelectionShapeOwner) {
            List<? extends SelectionShapes> selectedStack = ((SelectionShapeOwner) te).getShape()
                    .getTargeted(RaytraceUtils.create(player, mc.getFrameTime(), Vec3.atLowerCornerOf(highlighted)));
            if (!selectedStack.isEmpty()) {
                return selectedStack;
            }
        }
        return null;
    }
}
