package com.lucasaz.intellij.AssertionGeneration.assertions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IsomorphismSelector {
    // First elements will be made into defaults!
    public final static String[] NULL_OPTIONS = {"expect(LHS).to.be.null;", "expect(LHS).to.equal(null);"};
    public final static String[] UNDEFINED_OPTIONS = {"expect(LHS).to.be.undefined;", "expect(LHS).to.equal(undefined);"};
    public final static String[] EXIST_OPTIONS = {"expect(LHS).to.exist;"};
    public final static String[] THROW_OPTIONS = {"expect(LHS).to.throw;"};
    public final static String[] NOT_THROW_OPTIONS = {"expect(LHS).to.not.throw;"};
    public final static String[] EQUALITY_OPTIONS = {"expect(LHS).to.equal(RHS);", "expect(LHS).to.eq(RHS);"};
    public final static String[] DEEP_EQUALITY_OPTIONS = {"expect(LHS).to.deep.equal(RHS);", "expect(LHS).to.eql(RHS);"};
    public final static String[] LENGTH_OPTIONS = {"expect(LHS).to.have.length(RHS);", "expect(LHS.length).to.equal(RHS);"};
    public final static String[] TYPE_OPTIONS = {"expect(LHS).to.be.a(RHS);", "expect(LHS).to.be.an(RHS);"};
    public final static String[] BOOLEAN_OPTIONS = {"expect(LHS).to.be.RHS;", "expect(LHS).to.equal(RHS);"};
    public Map<AssertKind, String[]> optionsMap;
    public Map<AssertKind, String> defaults;
    private Map<AssertKind, IsomorphismGenerator> genMap;

    public IsomorphismSelector() {
        this.setupOptionsAndDefaults();
        this.genMap = this.mapStrMapToIsoGenMap(this.defaults);
    }

    public IsomorphismSelector(Map<AssertKind, String> isomorphisms) {
        this();
        Map<AssertKind, IsomorphismGenerator> asIsoGen = this.mapStrMapToIsoGenMap(isomorphisms);
        // Replace defaults
        for (Map.Entry<AssertKind,IsomorphismGenerator> entry : asIsoGen.entrySet()) {
            this.genMap.put(entry.getKey(), entry.getValue());
        }
    }

    private Map<AssertKind, IsomorphismGenerator> mapStrMapToIsoGenMap(Map<AssertKind, String> asString) {
        // From https://stackoverflow.com/questions/25903137/java8-hashmapx-y-to-hashmapx-z-using-stream-map-reduce-collector/25903190
        return asString.entrySet().stream().collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> IsomorphismGeneratorFactory.generatorFromString(e.getValue())
                ));
    }

    private Map<AssertKind, String> mapStrArrMapToFirstElemMap(Map<AssertKind, String[]> asStringArr) {
        // From https://stackoverflow.com/questions/25903137/java8-hashmapx-y-to-hashmapx-z-using-stream-map-reduce-collector/25903190
        return asStringArr.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue()[0]
        ));
    }

    private void setupOptionsAndDefaults() {
        Map<AssertKind, String[]> optionsMap = new HashMap<>();
        optionsMap.put(AssertKind.NULL, IsomorphismSelector.NULL_OPTIONS);
        optionsMap.put(AssertKind.UNDEFINED, IsomorphismSelector.UNDEFINED_OPTIONS);
        optionsMap.put(AssertKind.EXIST, IsomorphismSelector.EXIST_OPTIONS);
        optionsMap.put(AssertKind.THROW, IsomorphismSelector.THROW_OPTIONS);
        optionsMap.put(AssertKind.NOT_THROW, IsomorphismSelector.NOT_THROW_OPTIONS);
        optionsMap.put(AssertKind.EQUAL, IsomorphismSelector.EQUALITY_OPTIONS);
        optionsMap.put(AssertKind.DEEP_EQUAL, IsomorphismSelector.DEEP_EQUALITY_OPTIONS);
        optionsMap.put(AssertKind.LENGTH, IsomorphismSelector.LENGTH_OPTIONS);
        optionsMap.put(AssertKind.TYPE, IsomorphismSelector.TYPE_OPTIONS);
        optionsMap.put(AssertKind.BOOL, IsomorphismSelector.BOOLEAN_OPTIONS);
        this.optionsMap = optionsMap;
        this.defaults = mapStrArrMapToFirstElemMap(optionsMap);
    }

    public String getAssertion(AssertKind kind, String LHS, String RHS) {
        return this.genMap.get(kind).gen(LHS, RHS);
    }
}