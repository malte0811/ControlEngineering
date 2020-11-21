package malte0811.controlengineering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.util.RaytraceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void renderSelectionShape(DrawHighlightEvent.HighlightBlock ev) {
        BlockRayTraceResult target = ev.getTarget();
        BlockPos highlighted = target.getPos();
        Minecraft mc = Minecraft.getInstance();
        World world = mc.world;
        PlayerEntity player = mc.player;
        if (world == null || player == null) {
            return;
        }
        TileEntity te = world.getTileEntity(highlighted);
        //TODO fix panels
        if (te instanceof SelectionShapeOwner) {
            MatrixStack transform = ev.getMatrix();
            transform.push();
            Vector3d projectedView = Vector3d.copy(highlighted).subtract(ev.getInfo().getProjectedView());
            transform.translate(projectedView.x, projectedView.y, projectedView.z);
            IVertexBuilder builder = ev.getBuffers().getBuffer(RenderType.getLines());
            List<? extends SelectionShapes> selectedStack = ((SelectionShapeOwner) te).getShape()
                    .getTargeted(RaytraceUtils.create(player, ev.getPartialTicks(), Vector3d.copy(highlighted)));
            for (SelectionShapes shape : selectedStack) {
                Matrix4f currentMatrix = transform.getLast().getMatrix();
                shape.plotBox((v1, v2) -> {
                    addPoint(builder, currentMatrix, v1);
                    addPoint(builder, currentMatrix, v2);
                });
                shape.outerToInnerPosition()
                        .toTransformationMatrix()
                        //TODO cache?
                        .inverse()
                        .push(transform);
            }
            transform.pop();

            ev.setCanceled(true);
        }
    }

    private static void addPoint(IVertexBuilder builder, Matrix4f transform, Vector3d pos) {
        builder.pos(transform, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(0, 0, 0, 0.4F)
                .endVertex();
    }
}
