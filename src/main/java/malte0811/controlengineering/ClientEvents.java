package malte0811.controlengineering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.blocks.bus.LineAccessBlock;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.gui.misc.BusSignalSelector;
import malte0811.controlengineering.tiles.bus.LineAccessTile;
import malte0811.controlengineering.util.RaytraceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT, bus = Bus.FORGE)
public class ClientEvents {
    @SubscribeEvent
    public static void renderSelectionShape(DrawHighlightEvent.HighlightBlock ev) {
        BlockRayTraceResult target = ev.getTarget();
        BlockPos highlighted = target.getPos();
        List<? extends SelectionShapes> selectedStack = getSelectedStack();
        if (selectedStack != null) {
            MatrixStack transform = ev.getMatrix();
            transform.push();
            Vector3d projectedView = Vector3d.copy(highlighted).subtract(ev.getInfo().getProjectedView());
            transform.translate(projectedView.x, projectedView.y, projectedView.z);
            IVertexBuilder builder = ev.getBuffers().getBuffer(RenderType.getLines());
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
                transform.pop();
            }
            transform.pop();
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        final ItemStack held = mc.player.getHeldItem(Hand.MAIN_HAND);
        final RayTraceResult mop = mc.objectMouseOver;
        if (!(mop instanceof BlockRayTraceResult)) {
            return;
        }
        List<String> lines = new ArrayList<>();
        final BlockPos pos = ((BlockRayTraceResult) mop).getPos();
        final TileEntity te = mc.player.world.getTileEntity(pos);
        if (te instanceof LineAccessTile && held.getToolTypes().contains(LineAccessBlock.SCREWDRIVER_TOOL)) {
            final int line = ((LineAccessTile) te).selectedLine;
            lines.add(I18n.format(BusSignalSelector.BUS_LINE_INDEX_KEY, line));
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
            mc.fontRenderer.drawString(
                    event.getMatrixStack(),
                    lines.get(i),
                    mc.getMainWindow().getScaledWidth() / 2f + 8,
                    mc.getMainWindow().getScaledHeight() / 2f + 8 + i * (mc.fontRenderer.FONT_HEIGHT + 2),
                    -1
            );
        }
    }

    private static void renderShape(MatrixStack transform, SelectionShapes shape, IVertexBuilder builder) {
        Matrix4f currentMatrix = transform.getLast().getMatrix();
        shape.plotBox((v1, v2) -> {
            addPoint(builder, currentMatrix, v1);
            addPoint(builder, currentMatrix, v2);
        });
    }

    private static void addPoint(IVertexBuilder builder, Matrix4f transform, Vector3d pos) {
        builder.pos(transform, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(0, 0, 0, 0.4F)
                .endVertex();
    }

    @Nullable
    private static List<? extends SelectionShapes> getSelectedStack() {
        Minecraft mc = Minecraft.getInstance();
        final RayTraceResult mop = mc.objectMouseOver;
        if (!(mop instanceof BlockRayTraceResult)) {
            return null;
        }
        BlockRayTraceResult target = (BlockRayTraceResult) mop;
        BlockPos highlighted = target.getPos();
        World world = mc.world;
        PlayerEntity player = mc.player;
        if (world == null || player == null) {
            return null;
        }
        TileEntity te = world.getTileEntity(highlighted);
        if (te instanceof SelectionShapeOwner) {
            List<? extends SelectionShapes> selectedStack = ((SelectionShapeOwner) te).getShape()
                    .getTargeted(RaytraceUtils.create(player, mc.getRenderPartialTicks(), Vector3d.copy(highlighted)));
            if (!selectedStack.isEmpty()) {
                return selectedStack;
            }
        }
        return null;
    }
}
