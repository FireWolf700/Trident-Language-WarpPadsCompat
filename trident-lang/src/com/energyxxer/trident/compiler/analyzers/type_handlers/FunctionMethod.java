package com.energyxxer.trident.compiler.analyzers.type_handlers;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.semantics.*;
import com.energyxxer.trident.compiler.semantics.symbols.ISymbolContext;
import com.energyxxer.trident.compiler.semantics.symbols.SymbolContext;

import java.util.ArrayList;
import java.util.Collection;

public class FunctionMethod implements VariableMethod {
    private TokenPattern<?> functionPattern;
    private ISymbolContext declaringContext;
    private ArrayList<String> formalParameters = new ArrayList<>();
    private Object thisObject;
    private String functionName = "<anonymous function>";

    public FunctionMethod(TokenPattern<?> functionPattern, ISymbolContext declaringContext, Collection<String> formalParameters, Object thisObject, String functionName) {
        this.functionPattern = functionPattern;
        this.declaringContext = declaringContext;
        this.formalParameters.addAll(formalParameters);
        this.thisObject = thisObject;
        if(functionName != null) this.functionName = functionName;
    }

    @Override
    public Object call(Object[] params, TokenPattern<?>[] patterns, TokenPattern<?> pattern, ISymbolContext ctx) {

        SymbolContext innerFrame = new SymbolContext(declaringContext);


        ctx.getCompiler().getCallStack().push(new CallStack.Call(functionName, functionPattern, declaringContext.getStaticParentFile(), pattern));

        for(int i = 0; i < formalParameters.size(); i++) {
            innerFrame.put(new Symbol(formalParameters.get(i), Symbol.SymbolVisibility.PRIVATE, i < params.length ? params[i] : null));
        }
        if(thisObject != null) innerFrame.put(new Symbol("this", Symbol.SymbolVisibility.PRIVATE, thisObject));

        try {
            TridentFile.resolveInnerFileIntoSection(functionPattern, innerFrame, ctx.getWritingFile().getFunction());
        } catch(StackOverflowError x) {
            throw new TridentException(TridentException.Source.INTERNAL_EXCEPTION, "Stack Overflow Error", pattern, ctx);
        } catch(ReturnException x) {
            return x.getValue();
        } finally {
            ctx.getCompiler().getCallStack().pop();
        }
        return null;
    }

    @Override
    public Object getMember(VariableMethod object, String member, TokenPattern<?> pattern, ISymbolContext file, boolean keepSymbol) {
        if(member.equals("formalParameters")) return new ListObject(formalParameters);
        if(member.equals("declaringFile")) return declaringContext.getStaticParentFile().getResourceLocation();
        throw new MemberNotFoundException();
    }

    @Override
    public String toString() {
        return "<function(" + String.join(", ", formalParameters) + ")>";
    }
}
