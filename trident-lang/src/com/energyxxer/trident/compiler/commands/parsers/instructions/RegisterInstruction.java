package com.energyxxer.trident.compiler.commands.parsers.instructions;

import com.energyxxer.commodore.textcomponents.TextComponent;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.trident.compiler.commands.parsers.constructs.TextParser;
import com.energyxxer.trident.compiler.commands.parsers.general.ParserMember;
import com.energyxxer.trident.compiler.semantics.TridentFile;

@ParserMember(key = "register")
public class RegisterInstruction implements Instruction {
    @Override
    public void run(TokenPattern<?> pattern, TridentFile file) {
        TokenPattern<?> inner = ((TokenStructure)pattern.find("CHOICE")).getContents();
        switch(inner.getName()) {
            case "REGISTER_OBJECTIVE":
                register(inner, file);
                break;
            default: {
                file.getCompiler().getReport().addNotice(new Notice(NoticeType.ERROR, "Unknown grammar branch name '" + inner.getName() + "'", inner));
            }
        }
    }

    private void register(TokenPattern<?> pattern, TridentFile file) {
        String objectiveName = pattern.find("OBJECTIVE_NAME").flatten(false);
        String criteria = "dummy";
        TextComponent displayName = null;

        TokenPattern<?> sub = pattern.find("");
        if(sub != null) {
            criteria = sub.find("CRITERIA").flatten(false);
            TokenPattern<?> rawDisplayName = sub.find(".TEXT_COMPONENT");
            if(rawDisplayName != null) {
                displayName = TextParser.parseTextComponent(rawDisplayName, file.getCompiler());
            }
        }

        if(file.getCompiler().getModule().getObjectiveManager().contains(objectiveName)) {
            file.getCompiler().getReport().addNotice(new Notice(NoticeType.ERROR, "An objective with the name '" + objectiveName + "' has already been registered", pattern));
        } else {
            file.getCompiler().getModule().getObjectiveManager().create(objectiveName, criteria, displayName, true);
        }
    }
}
