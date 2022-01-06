package malte0811.controlengineering.blockentity.panels;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock.Offset;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.cnc.CNCInstructionGenerator;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.math.MatrixUtils;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PanelDesignerBlockEntity extends CEBlockEntity implements SelectionShapeOwner {
    private static final Codec<List<PlacedComponent>> COMPONENTS_CODEC = Codec.list(PlacedComponent.CODEC);

    private final CachedValue<BlockState, SelectionShapes> shapes = new CachedValue<>(
            this::getBlockState,
            state -> {
                Offset offset = state.getValue(PanelDesignerBlock.OFFSET);
                Direction facing = state.getValue(PanelDesignerBlock.FACING);
                VoxelShape baseShape = PanelDesignerBlock.SHAPE.apply(offset, facing);
                if (offset == Offset.ORIGIN) {
                    Function<UseOnContext, InteractionResult> openUI = makeInteraction(
                            state, PanelDesignerBlockEntity::openUI
                    );
                    return new ListShapes(
                            baseShape,
                            MatrixUtils.inverseFacing(facing),
                            ImmutableList.of(
                                    new SingleShape(PanelDesignerBlock.TABLE_TOP, openUI)
                            ),
                            $ -> InteractionResult.PASS
                    );
                } else if (offset == Offset.BACK_TOP) {
                    Function<UseOnContext, InteractionResult> writeTape = makeInteraction(
                            state, PanelDesignerBlockEntity::writeTape
                    );
                    Function<UseOnContext, InteractionResult> addTape = makeInteraction(
                            state, (t, ctx) -> t.state.removeOrAddClearTape(ctx.getPlayer(), ctx.getItemInHand())
                    );
                    Function<UseOnContext, InteractionResult> takeTape = makeInteraction(
                            state, (t, ctx) -> t.state.removeWrittenTape(ctx.getPlayer())
                    );
                    return new ListShapes(
                            baseShape,
                            MatrixUtils.inverseFacing(facing),
                            ImmutableList.of(
                                    new SingleShape(PanelDesignerBlock.BUTTON, writeTape),
                                    new SingleShape(KeypunchBlockEntity.INPUT_SHAPE, addTape),
                                    new SingleShape(KeypunchBlockEntity.OUTPUT_SHAPE, takeTape)
                            ),
                            $ -> InteractionResult.PASS
                    );
                } else {
                    return new SingleShape(baseShape, $ -> InteractionResult.PASS);
                }
            }
    );
    private final CachedValue<List<PlacedComponent>, Integer> requiredLength;

    private final List<PlacedComponent> components = new ArrayList<>();
    private KeypunchState state = new KeypunchState(this::setChanged);

    public PanelDesignerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        requiredLength = new CachedValue<>(
                () -> components,
                l -> CNCInstructionGenerator.toInstructions(l).length(),
                l -> l.stream().map(c -> c.copy(false)).collect(Collectors.toList())
        );
    }

    private Function<UseOnContext, InteractionResult> makeInteraction(
            BlockState state, BiFunction<PanelDesignerBlockEntity, UseOnContext, InteractionResult> handler
    ) {
        return ctx -> {
            BlockPos origin = CEBlocks.PANEL_DESIGNER.get().getMainBlock(state, this);
            BlockEntity atOrigin = level.getBlockEntity(origin);
            if (atOrigin instanceof PanelDesignerBlockEntity master) {
                return handler.apply(master, ctx);
            } else {
                return InteractionResult.FAIL;
            }
        };
    }

    @Override
    public SelectionShapes getShape() {
        return shapes.get();
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        components.clear();
        components.addAll(Codecs.readOptional(COMPONENTS_CODEC, nbt.get("components")).orElse(ImmutableList.of()));
        this.state = new KeypunchState(this::setChanged, nbt.get("state"));
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("components", Codecs.encode(COMPONENTS_CODEC, components));
        compound.put("state", state.toNBT());
    }

    public List<PlacedComponent> getComponents() {
        return components;
    }

    private InteractionResult openUI(UseOnContext ctx) {
        if (ctx.getPlayer() instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openGui(serverPlayer, new SimpleMenuProvider(
                    CEContainers.PANEL_DESIGN.argConstructor(this), TextComponent.EMPTY
            ));
        }
        return InteractionResult.SUCCESS;
    }

    public int getLengthRequired() {
        return requiredLength.get();
    }

    private InteractionResult writeTape(UseOnContext ctx) {
        final String instructions = CNCInstructionGenerator.toInstructions(components);
        final byte[] bytes = BitUtils.toBytesWithParity(instructions);
        state.tryTypeAll(new ByteArrayList(bytes, 0, bytes.length));
        setChanged();
        return InteractionResult.SUCCESS;
    }

    public KeypunchState getKeypunch() {
        return state;
    }
}
