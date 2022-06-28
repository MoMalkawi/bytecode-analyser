package org.rsminion.tools.searchers;

import javafx.util.Pair;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Opcodes;
import org.rsminion.tools.searchers.data.Pattern;
import org.objectweb.asm.tree.*;
import org.rsminion.tools.utils.Filter;
import org.rsminion.tools.utils.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class MethodSearcher {

    private @Getter MethodNode method;

    private @Getter AbstractInsnNode[] instructions;

    public MethodSearcher(MethodNode method) {
        this.method = method;
        this.instructions = method.instructions.toArray();
    }

    /* --------------- Linear --------------- */

    public Pattern linearSearch(Filter<Pattern> patternCondition,
                                int startLine, int endLine, int patternInstance, int... pattern) {
        instructions = method.instructions.toArray();
        int instancesFound = 0;
        outerLoop:
        for(int i = startLine; i < endLine; i++) {
            int instSetIndex = i;
            int patIndex = 0;
            AbstractInsnNode instruction;
            while ((instruction = instructions[instSetIndex]) != null &&
                    verifyPatternOpcode(instruction, pattern[patIndex])) {

                if (patIndex == (pattern.length - 1)) {
                    Pattern result = Pattern.createLinear(method, i, pattern);
                    if (patternCondition == null || patternCondition.verify(result)) {

                        if (instancesFound == patternInstance)
                            return result;

                        instancesFound++;
                        break;
                    }
                }

                instSetIndex++;
                patIndex++;

                if (instructions[instSetIndex].getOpcode() == -1)
                    instSetIndex++;

                if (instSetIndex == (instructions.length))
                    break outerLoop;

                if(patIndex == pattern.length)
                    break;

            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern linearSearch(int patternInstance, int... pattern) {
        return linearSearch(null,0, method.instructions.size(), patternInstance, pattern);
    }

    public Pattern linearMultiSearch(Filter<Pattern>[] patternConditions, int startLine, int endLine,
                                     int[] patternsInstances, int[][] patterns) {
        for(int i = 0; i < patterns.length; i++) {
            Pattern result = linearSearch(patternConditions[i],startLine,endLine,
                    patternsInstances[i],patterns[i]);
            if(!result.isEmpty())
                return result;
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern linearMultiSearch(int startLine, int endLine,
                                     int[] patternsInstances, int[][] patterns) {
        return linearMultiSearch(null,  startLine, endLine, patternsInstances, patterns);
    }

    public Pattern linearMultiSearch(int[] patternsInstances, int[][] patterns) {
        return linearMultiSearch(0, method.instructions.size(), patternsInstances, patterns);
    }

    public Pattern linearMultiSearch(int patternsInstance, int[][] patterns) {
        for (int[] pattern : patterns) {
            Pattern result = linearSearch(patternsInstance, pattern);
            if (!result.isEmpty())
                return result;
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern[] linearSearchAll(Filter<Pattern> filter, int... pattern) {
        return cycleInstancesAll(
                i -> linearSearch(i, pattern), filter, 100);
    }

    public Pattern[] linearSearchAll(int... pattern) {
        return linearSearchAll(a -> true, pattern);
    }

    /* --------------- Jumps/Branches --------------- */

    public Pattern jumpSearch(Filter<Pattern> patternCondition, int jumpOpcode, int startLine,
                              int maxLinesToSearch, int patternInstance, int... pattern) {
        instructions = method.instructions.toArray();
        int instance = 0;
        for(int i = startLine, linesSearched = 0; linesSearched <= maxLinesToSearch; i++) {
            if(i >= instructions.length)
                break;

            linesSearched++;
            AbstractInsnNode ain;
            if((ain = instructions[i]).getOpcode() == jumpOpcode)
                i = method.instructions.indexOf(((JumpInsnNode)ain).label);

            Pattern result;
            if ((result = linearSearch(patternCondition,i,i + (pattern.length + 1),
                    0, pattern)).isFound()) {
                if (instance == patternInstance)
                    return result;
                instance++;
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern jumpSearch(int jumpOpcode, int maxLinesToSearch, int patternInstance, int... patterns) {
        return jumpSearch(null, jumpOpcode, 0, maxLinesToSearch, patternInstance,
                patterns);
    }

    public Pattern singularJumpSearch(Filter<AbstractInsnNode> patternCondition, int jumpOpcode,
                                      int startLine, int maxLinesToSearch,
                                      int patternInstance, int pattern) {
        instructions = method.instructions.toArray();
        int instance = 0;
        for(int i = startLine, linesSearched = 0; linesSearched <= maxLinesToSearch; i++) {
            if(i >= instructions.length)
                break;

            linesSearched++;
            AbstractInsnNode ain;
            if((ain = instructions[i]).getOpcode() == jumpOpcode)
                i = method.instructions.indexOf(((JumpInsnNode)ain).label);

            if (ain.getOpcode() == pattern && (patternCondition == null || patternCondition.verify(ain))) {
                if (instance == patternInstance)
                    return Pattern.createSingular(method, ain.getOpcode(), i);
                instance++;
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern singularJumpSearch(int jumpOpcode, int maxLinesToSearch, int patternInstance, int pattern) {
        return singularJumpSearch(null, jumpOpcode, 0, maxLinesToSearch, patternInstance, pattern);
    }

    /* --------------- Singular Linear --------------- */

    public Pattern singularSearch(int startLine, int endLine, int patternInstance, int opcode) {
        instructions = method.instructions.toArray();
        int instance = 0;
        for(int i = startLine; i < endLine; i++) {
            AbstractInsnNode instruction;
            if((instruction = instructions[i]) != null && verifyPatternOpcode(instruction, opcode)) {
                if(instance == patternInstance)
                    return Pattern.createSingular(method, opcode, i);
                instance++;
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern singularSearch(int patternInstance, int opcode) {
        return singularSearch(0, instructions.length, patternInstance, opcode);
    }

    public Pattern singularSearch(Filter<AbstractInsnNode> condition, int startLine, int endLine,
                                  int patternInstance, int opcode) {
        if(startLine < 0) startLine = 0;
        instructions = method.instructions.toArray();
        int instance = 0;
        for(int i = startLine; i < endLine; i++) {
            AbstractInsnNode ain;
            if((ain = instructions[i]).getOpcode() == opcode && condition.verify(ain)) {
                if(instance == patternInstance)
                    return Pattern.createSingular(method, opcode, i);
                instance++;
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern singularPatternSearch(Filter<Pattern> condition, int startLine, int endLine,
                                  int patternInstance, int opcode) {
        if(startLine < 0) startLine = 0;
        instructions = method.instructions.toArray();
        int instance = 0;
        Pattern result;
        for(int i = startLine; i < endLine; i++) {
            if(instructions[i].getOpcode() == opcode) {
                result = Pattern.createSingular(method, opcode, i);
                if(condition.verify(result)) {
                    if (instance == patternInstance)
                        return result;
                    instance++;
                }
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern singularSearch(Filter<AbstractInsnNode> condition, int patternInstance, int opcode) {
        return singularSearch(condition, 0, instructions.length, patternInstance, opcode);
    }

    public Pattern singularLdcSearch(Number value, int startLine, int endLine, int patternInstance, int opcode) {
        instructions = method.instructions.toArray();
        int instance = 0;
        for(int i = startLine; i < endLine; i++) {
            AbstractInsnNode ain;
            if((ain = instructions[i]).getOpcode() == opcode && ((LdcInsnNode)ain).cst.equals(value)) {
                if(instance == patternInstance)
                    return Pattern.createSingular(method, opcode, i);
                instance++;
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern singularLdcSearch(Number value, int patternInstance, int opcode) {
        return singularLdcSearch(value, 0, instructions.length, patternInstance, opcode);
    }

    public Pattern singularIntSearch(int value, int startLine, int endLine, int patternInstance, int opcode) {
        instructions = method.instructions.toArray();
        int instance = 0;
        for(int i = startLine; i < endLine; i++) {
            AbstractInsnNode ain;
            if((ain = instructions[i]).getOpcode() == opcode && ((IntInsnNode)ain).operand == value) {
                if(instance == patternInstance)
                    return Pattern.createSingular(method, opcode, i);
                instance++;
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern singularIntSearch(int value, int patternInstance, int opcode) {
        return singularIntSearch(value, 0, method.instructions.size(), patternInstance, opcode);
    }

    public Pattern[] singularSearchAll(Filter<Pattern> filter, int pattern) {
        return cycleInstancesAll(
                i -> singularSearch(i, pattern), filter, 100);
    }

    public Pattern[] singularSearchAll(int pattern) {
        return singularSearchAll(a -> true, pattern);
    }

    public Pattern cycleInstances(Function<Pattern, Integer> searchFunction,
                                  Filter<Pattern> returnCondition,
                                  int maxLoops) {
        int instance = 0;
        int loops = 0;
        Pattern pattern;
        while((pattern = searchFunction.execute(instance)).isFound()) {
            if(loops > maxLoops) break;
            if(returnCondition.verify(pattern))
                return pattern;
            instance++;
            loops++;
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern[] cycleInstancesAll(Function<Pattern, Integer> searchFunction,
                                  Filter<Pattern> returnCondition,
                                  int maxLoops) {
        List<Pattern> patterns = new ArrayList<>();
        int instance = 0;
        int loops = 0;
        Pattern pattern;
        while((pattern = searchFunction.execute(instance)).isFound()) {
            if(loops > maxLoops) break;
            if(returnCondition.verify(pattern))
                patterns.add(pattern);
            instance++;
            loops++;
        }
        return patterns.toArray(new Pattern[0]);
    }

    /* --------------- Misc ----------------------- */

    public List<AbstractInsnNode> getInstructions(Filter<AbstractInsnNode> condition) {
        instructions = method.instructions.toArray();
        List<AbstractInsnNode> nodes = new ArrayList<>();
        for(AbstractInsnNode abstractInsnNode : instructions) {
            if(condition.verify(abstractInsnNode))
                nodes.add(abstractInsnNode);

        }
        return nodes;
    }

    public int[] getInstructionsFrequency(AbstractInsnNode... opcodes) {
        instructions = method.instructions.toArray();
        int[] counts = new int[opcodes.length];
        for(AbstractInsnNode ain : instructions) {
            for(int i = 0; i < opcodes.length; i++) {
                if(opcodes[i] instanceof FieldInsnNode) { //support for Fields only right now.
                    if(ain instanceof FieldInsnNode) {
                        FieldInsnNode fin = (FieldInsnNode) ain;
                        FieldInsnNode opcodes_fin = (FieldInsnNode) opcodes[i];
                        if(fin.owner.equals(opcodes_fin.owner) && fin.name.equals(opcodes_fin.name)
                                && fin.desc.equals(opcodes_fin.desc)) counts[i]++;
                    }
                } //TODO: add support for Ldc, etc... here
            }
        }
        return counts;
    }

    public int compareInstructionFrequency(AbstractInsnNode ain1, AbstractInsnNode ain2) {
        int[] result = getInstructionsFrequency(ain1, ain2);
        return result[0] - result[1];
    }

    public AbstractInsnNode getAbstractInsnNode(int instance, int opcode, int startLine, int maxLines,
                                                boolean stopAtFields, boolean stopAtJump) {
        instructions = method.instructions.toArray();
        int currInstance = 0;
        for(int i = startLine; i <= (startLine + maxLines); i++) {
            if(i == method.instructions.size())
                break;
            AbstractInsnNode ain = instructions[i];
            if((stopAtFields && ain instanceof FieldInsnNode) ||
                    (stopAtJump && ain instanceof JumpInsnNode))
                break;

            if(ain.getOpcode() == opcode) {
                if(currInstance == instance) return ain;
                currInstance++;
            }
        }
        return null;
    }

    public Pattern searchForKnown(String obfOwner, String obfName) {
        instructions = method.instructions.toArray();
        FieldInsnNode fin;
        int count = 0;
        for(AbstractInsnNode ain : instructions) {
            if(ain instanceof FieldInsnNode) {
                fin = (FieldInsnNode) ain;
                if(fin.owner.equals(obfOwner) && fin.name.equals(obfName))
                    return Pattern.createSingular(method, ain.getOpcode(), count);
            }
            count++;
        }
        return Pattern.EMPTY_PATTERN;
    }

    public Pattern[] searchForAllKnown(String obfOwner, String obfName) {
        List<Pattern> result = new ArrayList<>();
        instructions = method.instructions.toArray();
        FieldInsnNode fin;
        int count = 0;
        for(AbstractInsnNode ain : instructions) {
            if(ain instanceof FieldInsnNode) {
                fin = (FieldInsnNode) ain;
                if(fin.owner.equals(obfOwner) && fin.name.equals(obfName))
                    result.add(Pattern.createSingular(method, ain.getOpcode(), count));
            }
            count++;
        }
        return result.toArray(new Pattern[0]);
    }

    public Pattern searchForFrequent(Filter<AbstractInsnNode> filter, int opcode) {
        Map<String, Pair<Integer,Integer>> frequencies = new HashMap<>(); //Name : frequency
        instructions = method.instructions.toArray();
        int line = 0;
        for(AbstractInsnNode ain : instructions) {
            if(ain.getOpcode() == opcode && filter.verify(ain)) {
                FieldInsnNode fin = (FieldInsnNode) ain;
                Pair<Integer,Integer> frequency = frequencies.get(fin.name);
                frequencies.put(fin.name,
                        new Pair<>(line, ((frequency != null ? frequency.getValue() : 0) + 1)));
            }
            line++;
        }
        String currentField = null;
        Pair<Integer,Integer> currentFrequency = null;
        for(String i : frequencies.keySet()) {
            Pair<Integer,Integer> frequency = frequencies.get(i);
            if(currentField == null || frequency.getValue() > currentFrequency.getValue()) {
                currentField = i;
                currentFrequency = frequency;
            }
        }
        return currentField != null ? Pattern.createSingular(method, opcode, currentFrequency.getKey()) :
                Pattern.EMPTY_PATTERN;
    }

    public Pattern searchLocalJump(int opcode, int jumpOpcode, String desc,
                                    int startLine, int instance, String clazzName) {
        return jumpSearch(p -> {
                    FieldInsnNode fin = p.getFirstFieldNode();
                    return fin.owner.equals(clazzName) && fin.desc.equals(desc);
                }, jumpOpcode, startLine,
                100, instance, opcode);
    }

    public Pattern searchGotoJump(int opcode, String desc, int startLine,
                                  int instance, String clazzName) {
        return searchLocalJump(opcode, Opcodes.GOTO, desc, startLine, instance, clazzName);
    }

    /* --------------- Dependencies --------------- */

    private boolean verifyPatternOpcode(AbstractInsnNode instruction, int currPatternOpcode) {
        int instOpcode = instruction.getOpcode();
        return  //Verify Pattern Instruction Opcode OR is it a Wildcard?
                (instOpcode == currPatternOpcode || currPatternOpcode == Pattern.SKIP_WILDCARD) ||
                        //Check if it's a General Branch Wildcard
                        (currPatternOpcode == Pattern.BRANCH_WILDCARD && (instOpcode >= 159 && instOpcode <= 166)) ||
                        //Check if it's a Constant Wildcard
                        (currPatternOpcode == Pattern.CONST_WILDCARD && (instOpcode >= 1 && instOpcode <= 17)) ||
                        //Check if it's a IF-Branch Wildcard
                        (currPatternOpcode == Pattern.IF_WILDCARD && (instOpcode >= 153 && instOpcode <= 158)) ||
                        //Check if it's a MUL Wildcard
                        (currPatternOpcode == Pattern.MUL_WILDCARD && (instOpcode >= 104 && instOpcode <= 107)) ||
                        //Check if it's a GET Wildcard
                        (currPatternOpcode == Pattern.GET_WILDCARD && (instOpcode == 178 || instOpcode == 180)) ||
                        //Check if it's a PUT Wildcard
                        (currPatternOpcode == Pattern.PUT_WILDCARD && (instOpcode >= 179 && instOpcode <= 181)) ||
                        //Check if it's a LOAD Wildcard
                        (currPatternOpcode == Pattern.LOAD_WILDCARD && (instOpcode >= 21 && instOpcode <= 25));
    }
    
    public void setMethod(MethodNode method) {
        this.method = method;
        this.instructions = method.instructions.toArray();
    }

    public MethodSearcher get(MethodNode method) {
        setMethod(method);
        return this;
    }
}
