package malte0811.controlengineering.client.render.tape;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.blockentity.tape.SequencerBlockEntity;
import malte0811.controlengineering.blocks.tape.SequencerBlock;
import malte0811.controlengineering.client.render.utils.BERUtils;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public class SequencerRenderer implements BlockEntityRenderer<SequencerBlockEntity> {
    private static final TapeDriveRender TAPE_DRIVE = new TapeDriveRender(
            2, 1, new Vec2d(5, 8), new Vec2d(7, 5), new Vec2d(11, 8), new Vec2d(9, 5)
    );
    private static final Quaternionf TAPE_ROTATION = new Quaternionf().rotateX(Mth.HALF_PI);

    public SequencerRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(
            @Nonnull SequencerBlockEntity sequencer, float partialTicks,
            @Nonnull PoseStack transform, @Nonnull MultiBufferSource buffers,
            int light, int overlay
    ) {
        final long totLength = sequencer.getTapeLength();
        if (totLength > 0) {
            transform.pushPose();
            BERUtils.rotateAroundCenter(
                    180 - sequencer.getBlockState().getValue(SequencerBlock.FACING).toYRot(), transform
            );
            transform.scale(1 / 16f, 1 / 16f, 1 / 16f);
            transform.translate(0, 16, 14);
            transform.mulPose(TAPE_ROTATION);
            TAPE_DRIVE.setTotalLength(totLength);
            TAPE_DRIVE.updateTapeProgress(sequencer.getCurrentTapePosition());
            TAPE_DRIVE.render(buffers.getBuffer(RenderType.solid()), transform, light, overlay);
            transform.popPose();
        }
    }
}
