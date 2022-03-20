package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicNet;
import malte0811.controlengineering.logic.schematic.symbol.*;
import malte0811.controlengineering.util.math.Vec2d;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientSymbols {
    private static final Map<SchematicSymbol<?>, ClientSymbol<?, ?>> CLIENT_SYMBOLS = new HashMap<>();

    public static void init() {
        final int secondColumn = 13;
        register(SchematicSymbols.CONSTANT, ClientConstantSymbol::new);
        register(SchematicSymbols.INPUT_PIN, ClientIOSymbol::new);
        register(SchematicSymbols.OUTPUT_PIN, ClientIOSymbol::new);

        register(SchematicSymbols.AND2, 0, 0);
        register(SchematicSymbols.AND3, 0, 14);
        register(SchematicSymbols.OR2, 0, 7);
        register(SchematicSymbols.OR3, 0, 21);
        register(SchematicSymbols.NAND2, secondColumn, 0);
        register(SchematicSymbols.NAND3, secondColumn, 14);
        register(SchematicSymbols.NOR2, secondColumn, 7);
        register(SchematicSymbols.NOR3, secondColumn, 21);
        register(SchematicSymbols.XOR2, 0, 28);
        register(SchematicSymbols.XOR3, 0, 35);
        register(SchematicSymbols.NOT, secondColumn, 28);
        register(SchematicSymbols.RS_LATCH, secondColumn, 49);
        register(SchematicSymbols.SCHMITT_TRIGGER, 0, 42);
        register(SchematicSymbols.DIGITIZER, secondColumn, 42);
        register(SchematicSymbols.COMPARATOR, secondColumn, 35);
        register(SchematicSymbols.D_LATCH, 0, 49);
        register(SchematicSymbols.DELAY_LINE, 0, 56);
    }

    public static <State> void render(SymbolInstance<State> inst, PoseStack transform, int x, int y) {
        render(inst.getType(), transform, x, y, inst.getCurrentState());
    }

    public static <State>
    void render(SchematicSymbol<State> serverSymbol, PoseStack transform, int x, int y, State state) {
        getClientSymbol(serverSymbol).render(transform, x, y, state);
    }

    public static void render(PlacedSymbol placed, PoseStack transform) {
        render(placed.symbol(), transform, placed.position().x(), placed.position().y());
    }

    public static void render(Schematic schematic, PoseStack stack, Vec2d mouse) {
        for (PlacedSymbol s : schematic.getSymbols()) {
            render(s, stack);
        }
        for (SchematicNet net : schematic.getNets()) {
            net.render(stack, mouse, schematic.getSymbols());
        }
    }

    public static <State>
    void createInstanceWithUI(
            SchematicSymbol<State> symbol, Consumer<? super SymbolInstance<State>> onDone, State initialState
    ) {
        getClientSymbol(symbol).createInstanceWithUI(onDone, initialState);
    }

    public static <State>
    void createInstanceWithUI(SchematicSymbol<State> symbol, Consumer<? super SymbolInstance<State>> onDone) {
        createInstanceWithUI(symbol, onDone, symbol.getInitialState());
    }


    @SuppressWarnings("unchecked")
    private static <State, Symbol extends SchematicSymbol<State>>
    ClientSymbol<State, Symbol> getClientSymbol(Symbol server) {
        return (ClientSymbol<State, Symbol>) CLIENT_SYMBOLS.get(server);
    }

    private static void register(CellSymbol cell, int uMin, int vMin) {
        register(cell, new ClientCellSymbol(cell, uMin, vMin));
    }

    private static <State, Symbol extends SchematicSymbol<State>>
    void register(Symbol server, Function<Symbol, ClientSymbol<State, Symbol>> makeClient) {
        register(server, makeClient.apply(server));
    }

    private static <State, Symbol extends SchematicSymbol<State>>
    void register(Symbol server, ClientSymbol<State, Symbol> client) {
        CLIENT_SYMBOLS.put(server, client);
    }
}
