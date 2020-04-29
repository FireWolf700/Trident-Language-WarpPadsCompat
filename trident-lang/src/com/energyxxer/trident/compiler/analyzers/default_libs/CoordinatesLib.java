package com.energyxxer.trident.compiler.analyzers.default_libs;

import com.energyxxer.commodore.functionlogic.coordinates.Coordinate;
import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.semantics.Symbol;
import com.energyxxer.trident.compiler.semantics.custom.classes.CustomClass;
import com.energyxxer.trident.compiler.semantics.symbols.ISymbolContext;

@AnalyzerMember(key = "Coordinates")
public class CoordinatesLib implements DefaultLibraryProvider {

    public static final String COORDINATE_TYPE_ABSOLUTE =   "Coordinates.ABSOLUTE";
    public static final String COORDINATE_TYPE_RELATIVE =   "Coordinates.RELATIVE";
    public static final String COORDINATE_TYPE_LOCAL =      "Coordinates.LOCAL";

    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;


    @Override
    public void populate(ISymbolContext globalCtx, TridentCompiler compiler) {
        CustomClass coord = new CustomClass("Coordinates", "trident-util:native", globalCtx);
        coord.setConstructor(Symbol.SymbolVisibility.PRIVATE, null);

        coord.putStaticFinalMember("ABSOLUTE", COORDINATE_TYPE_ABSOLUTE);
        coord.putStaticFinalMember("RELATIVE", COORDINATE_TYPE_RELATIVE);
        coord.putStaticFinalMember("LOCAL",    COORDINATE_TYPE_LOCAL);

        globalCtx.put(new Symbol("Coordinates", Symbol.SymbolVisibility.GLOBAL, coord));


        CustomClass axis = new CustomClass("Axis", "trident-util:native", globalCtx);
        axis.setConstructor(Symbol.SymbolVisibility.PRIVATE, null);

        axis.putStaticFinalMember("X", AXIS_X);
        axis.putStaticFinalMember("Y", AXIS_Y);
        axis.putStaticFinalMember("Z", AXIS_Z);

        globalCtx.put(new Symbol("Axis", Symbol.SymbolVisibility.GLOBAL, axis));
    }

    public static String coordTypeToConstant(Coordinate.Type type) {
        return type == Coordinate.Type.RELATIVE ? COORDINATE_TYPE_RELATIVE : type == Coordinate.Type.LOCAL ? COORDINATE_TYPE_LOCAL : COORDINATE_TYPE_ABSOLUTE;
    }

    public static Coordinate.Type constantToCoordType(String cons) {
        switch(cons) {
            case COORDINATE_TYPE_ABSOLUTE: return Coordinate.Type.ABSOLUTE;
            case COORDINATE_TYPE_RELATIVE: return Coordinate.Type.RELATIVE;
            case COORDINATE_TYPE_LOCAL: return Coordinate.Type.LOCAL;
            default: throw new IllegalArgumentException("Invalid axis argument '" + cons + "'. Use constants Coordinates.ABSOLUTE, Coordinates.RELATIVE and Coordinates.LOCAL to specify a coordinate type.");
        }
    }
}
