package com.energyxxer.trident.compiler.commands.parsers.type_handlers.default_libs;

import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.compiler.commands.parsers.general.ParserMember;
import com.energyxxer.trident.compiler.commands.parsers.type_handlers.DictionaryObject;
import com.energyxxer.trident.compiler.commands.parsers.type_handlers.ListType;
import com.energyxxer.trident.compiler.commands.parsers.type_handlers.MethodWrapper;
import com.energyxxer.trident.compiler.commands.parsers.type_handlers.VariableMethod;
import com.energyxxer.trident.compiler.semantics.Symbol;
import com.energyxxer.trident.compiler.semantics.SymbolStack;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.google.gson.*;

import java.util.Map;

import static com.energyxxer.trident.compiler.commands.parsers.type_handlers.VariableMethod.HelperMethods.assertOfType;

@ParserMember(key = "JSON")
public class JsonLib implements DefaultLibraryProvider {
    @Override
    public void populate(SymbolStack stack, TridentCompiler compiler) {
        DictionaryObject block = new DictionaryObject();

        block.put("parse",
                new MethodWrapper<>("parse", ((instance, params) -> parseJson(new Gson().fromJson((String) params[0], JsonElement.class))), String.class).createForInstance(null));
        block.put("stringify",
                (VariableMethod) (params, patterns, pattern, file) -> {
                    if(params.length < 1) {
                        throw new TridentException(TridentException.Source.INTERNAL_EXCEPTION, "Method 'stringify' requires 1 parameter, instead found " + params.length, pattern, file);
                    }
                    boolean prettyPrinting = false;

                    if(params.length >= 2) {
                        prettyPrinting = assertOfType(params[1], patterns[1], file, Boolean.class);
                    }

                    Object param = assertOfType(params[0], patterns[0], file, String.class, Number.class, Boolean.class, ListType.class, DictionaryObject.class);

                    GsonBuilder gb = new GsonBuilder();
                    if(prettyPrinting) gb.setPrettyPrinting();

                    return gb.create().toJson(toJson(param));
                });
        stack.getGlobal().put(new Symbol("JSON", Symbol.SymbolAccess.GLOBAL, block));
    }

    private Object parseJson(JsonElement elem) {
        if(elem.isJsonPrimitive()) {
            JsonPrimitive prim = elem.getAsJsonPrimitive();
            if(prim.isString()) {
                return prim.getAsString();
            } else if(prim.isBoolean()) {
                return prim.getAsBoolean();
            } else if(prim.isNumber()) {
                Number num = prim.getAsNumber();
                if(num.intValue() == num.doubleValue()) {
                    return num.intValue();
                } else return num.doubleValue();
            } else {
                throw new IllegalArgumentException("Unknown primitive type: " + elem);
            }
        } else if(elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            DictionaryObject dict = new DictionaryObject();
            for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                dict.put(entry.getKey(), parseJson(entry.getValue()));
            }
            return dict;
        } else if(elem.isJsonArray()) {
            JsonArray arr = elem.getAsJsonArray();
            ListType list = new ListType();
            for(JsonElement entry : arr) {
                list.add(parseJson(entry));
            }
            return list;
        } else throw new IllegalArgumentException("Unknown json type: " + elem);
    }

    private JsonElement toJson(Object obj) {
        if(obj instanceof String) return new JsonPrimitive(obj.toString());
        if(obj instanceof Number) return new JsonPrimitive(((Number) obj));
        if(obj instanceof Boolean) return new JsonPrimitive((Boolean) obj);
        if(obj instanceof ListType) {
            JsonArray arr = new JsonArray();
            for(Object elem : ((ListType) obj)) {
                JsonElement result = toJson(elem);
                if(result != null) arr.add(result);
            }
            return arr;
        }
        if(obj instanceof DictionaryObject) {
            JsonObject jObj = new JsonObject();
            for(Map.Entry<String, Symbol> elem : ((DictionaryObject) obj).entrySet()) {
                JsonElement result = toJson(elem.getValue().getValue());
                if(result != null) jObj.add(elem.getKey(), result);
            }
            return jObj;
        }
        return null;
    }
}
