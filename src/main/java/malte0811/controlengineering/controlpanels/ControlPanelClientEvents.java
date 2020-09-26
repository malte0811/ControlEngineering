package malte0811.controlengineering.controlpanels;

import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.panels.PanelTileEntity;
import malte0811.controlengineering.util.MatrixUtils;
import malte0811.modelsplitter.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT)
public class ControlPanelClientEvents {
    @SubscribeEvent
    public static void renderSelectionShape(DrawHighlightEvent.HighlightBlock ev) {
        BlockRayTraceResult target = ev.getTarget();
        BlockPos highlighted = target.getPos();
        TileEntity tile = Minecraft.getInstance().world.getTileEntity(highlighted);
        if (tile instanceof PanelTileEntity) {
            PanelTileEntity panel = (PanelTileEntity) tile;
            Vector3d[] bottomVertices = layerVertices(1);
            Vector3d[] topVertices = layerVertices(panel.getTransform().getTopFaceHeight());
            for (int i = 0; i < 4; ++i) {
                bottomVertices[i] = MatrixUtils.transform(bottomVertices[i], panel.getTransform().getPanelBottomToWorld());
                topVertices[i] = MatrixUtils.transform(topVertices[i], panel.getTransform().getPanelTopToWorld());
            }
            ev.getMatrix().push();
            Vector3d projectedView = Vector3d.copy(highlighted).subtract(ev.getInfo().getProjectedView());
            ev.getMatrix().translate(projectedView.x, projectedView.y, projectedView.z);
            Matrix4f transform = ev.getMatrix().getLast().getMatrix();
            IVertexBuilder builder = ev.getBuffers().getBuffer(RenderType.getLines());
            renderCircuit(bottomVertices, builder, transform);
            renderCircuit(topVertices, builder, transform);
            renderConnections(bottomVertices, topVertices, builder, transform);
            ev.getMatrix().pop();
            ev.setCanceled(true);
        }
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
