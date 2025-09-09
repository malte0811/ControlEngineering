package malte0811.controlengineering;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import malte0811.controlengineering.blockentity.bus.LineAccessBlockEntity;
import malte0811.controlengineering.blockentity.bus.RSRemapperBlockEntity;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.client.model.panel.PanelItemRenderer;
import malte0811.controlengineering.gui.misc.BusSignalSelector;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.ControlPanelItem;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.items.PCBStackItem;
import malte0811.controlengineering.util.RaytraceUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void renderSelectionShape(RenderHighlightEvent.Block ev) {
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
                //TODO cache?
                var inverse = new Matrix4f(nonTopShape.outerToInnerPosition()).invert();
                transform.pushTransformation(new Transformation(inverse));
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
    public static void register(RegisterGuiLayersEvent ev)
    {
        ev.registerBelow(VanillaGuiLayers.DEBUG_OVERLAY, Lib.GuiLayers.ITEMS, ClientEvents::onRenderOverlayPost);
    }

    private static void onRenderOverlayPost(GuiGraphics graphics, DeltaTracker deltaTracker) {
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
        var targetBE = mc.player.level().getBlockEntity(pos);
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
            graphics.drawString(
                    mc.font,
                    lines.get(i),
                    (int) (mc.getWindow().getGuiScaledWidth() / 2f + 8),
                    (int) (mc.getWindow().getGuiScaledHeight() / 2f + 8 + i * (mc.font.lineHeight + 2)),
                    0xFFFFFFFF,
                    false
            );
        }
    }

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent ev) {
        if (ev.getItemStack().is(IEItemRefs.LOGIC_CIRCUIT.asItem())) {
            ev.getToolTip().add(PCBStackItem.useIn(IEItemRefs.LOGIC_UNIT));
        }
    }

    @SubscribeEvent
    public static void registerClientExtension(RegisterClientExtensionsEvent ev) {
        ev.registerItem(
                new IClientItemExtensions() {
                    private final Supplier<BlockEntityWithoutLevelRenderer> renderer = Suppliers.memoize(
                            () -> new PanelItemRenderer(ControlPanelItem::getPanelData)
                    );

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return renderer.get();
                    }
                },
                CEItems.CONTROL_PANEL
        );
    }

    private static void renderShape(PoseStack transform, SelectionShapes shape, VertexConsumer builder) {
        shape.plotBox((v1, v2) -> {
            Vec3 normal = v2.subtract(v1);
            addPoint(builder, transform.last(), v1, normal);
            addPoint(builder, transform.last(), v2, normal);
        });
    }

    private static void addPoint(
            VertexConsumer builder, PoseStack.Pose pose, Vec3 pos, Vec3 normal
    ) {
        builder.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(0, 0, 0, 0.4F)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
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
                    .getTargeted(RaytraceUtils.create(player, mc.getTimer().getGameTimeDeltaTicks(), Vec3.atLowerCornerOf(highlighted)))
                    .getFirst();
            if (!selectedStack.isEmpty()) {
                return selectedStack;
            }
        }
        return null;
    }
}
