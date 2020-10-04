package malte0811.controlengineering.controlpanels;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.tiles.panels.PanelTileEntity;
import malte0811.controlengineering.util.MatrixUtils;
import malte0811.controlengineering.util.RaytraceUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT)
public class ControlPanelClientEvents {
    @SubscribeEvent
    public static void renderSelectionShape(DrawHighlightEvent.HighlightBlock ev) {
        BlockRayTraceResult target = ev.getTarget();
        BlockPos highlighted = target.getPos();
        World world = Minecraft.getInstance().world;
        if (world == null) {
            return;
        }
        BlockState state = world.getBlockState(highlighted);
        if (state.getBlock() != CEBlocks.CONTROL_PANEL.get() || state.get(PanelBlock.IS_BASE)) {
            return;
        }
        PanelTileEntity panel = PanelBlock.getBase(world, state, highlighted);
        if (panel != null) {
            ev.getMatrix().push();
            Vector3d projectedView = Vector3d.copy(highlighted).subtract(ev.getInfo().getProjectedView());
            ev.getMatrix().translate(projectedView.x, projectedView.y, projectedView.z);
            IVertexBuilder builder = ev.getBuffers().getBuffer(RenderType.getLines());
            renderPanelOutline(panel, ev.getMatrix(), builder);
            Minecraft mc = Minecraft.getInstance();
            RayTraceContext raytraceCtx = RaytraceUtils.create(mc.player, mc.getRenderPartialTicks());
            Optional<PlacedComponent> selected = panel.getTargetedComponent(raytraceCtx);
            if (selected.isPresent()) {
                panel.getTransform().getPanelTopToWorld().toTransformationMatrix().push(ev.getMatrix());
                AxisAlignedBB aabb = selected.get().getSelectionShape();
                Preconditions.checkNotNull(aabb);
                WorldRenderer.drawBoundingBox(ev.getMatrix(), builder, aabb, 0, 0, 0, 0.4F);
                ev.getMatrix().pop();
            }
            ev.getMatrix().pop();

            ev.setCanceled(true);
        }
    }

    private static void renderPanelOutline(PanelTileEntity panel, MatrixStack matrix, IVertexBuilder builder) {
        Vector3d[] bottomVertices = layerVertices(1);
        Vector3d[] topVertices = layerVertices(panel.getTransform().getTopFaceHeight());
        for (int i = 0; i < 4; ++i) {
            bottomVertices[i] = panel.getTransform().getPanelBottomToWorld().apply(bottomVertices[i]);
            topVertices[i] = panel.getTransform().getPanelTopToWorld().apply(topVertices[i]);
        }
        Matrix4f transform = matrix.getLast().getMatrix();
        renderCircuit(bottomVertices, builder, transform);
        renderCircuit(topVertices, builder, transform);
        renderConnections(bottomVertices, topVertices, builder, transform);
    }

    private static void renderCircuit(Vector3d[] vertices, IVertexBuilder builder, Matrix4f transform) {
        for (int i = 0; i < vertices.length; ++i) {
            for (int offset = 0; offset < 2; ++offset) {
                Vector3d pos = vertices[(i + offset) % vertices.length];
                builder.pos(transform, (float) pos.x, (float) pos.y, (float) pos.z)
                        .color(0, 0, 0, 0.4F)
                        .endVertex();
            }
        }
    }

    private static void renderConnections(Vector3d[] first, Vector3d[] second, IVertexBuilder builder, Matrix4f transform) {
        Preconditions.checkArgument(first.length == second.length);
        for (int i = 0; i < first.length; ++i) {
            for (Vector3d[] arr : new Vector3d[][]{first, second}) {
                Vector3d pos = arr[i];
                builder.pos(transform, (float) pos.x, (float) pos.y, (float) pos.z)
                        .color(0, 0, 0, 0.4F)
                        .endVertex();
            }
        }
    }

    private static Vector3d[] layerVertices(double xMax) {
        return new Vector3d[]{
                new Vector3d(0, 0, 0),
                new Vector3d(xMax, 0, 0),
                new Vector3d(xMax, 0, 1),
                new Vector3d(0, 0, 1),
        };
    }
}
