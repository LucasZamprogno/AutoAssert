package com.lucasaz.intellij.AssertionGeneration.assertions;

import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IsomorphismSelector {
    private Map<AssertKind, IsomorphismGenerator> genMap;

    public IsomorphismSelector() {
        this.genMap = this.getDefaults();
    }

    public IsomorphismSelector(Map<AssertKind, String> isomorphisms) {
        this.genMap = this.mapMapToOtherMap(isomorphisms);
    }

    private Map<AssertKind, IsomorphismGenerator> mapMapToOtherMap(Map<AssertKind, String> asString) {
        // From https://stackoverflow.com/questions/25903137/java8-hashmapx-y-to-hashmapx-z-using-stream-map-reduce-collector/25903190
        return asString.entrySet().stream().collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> IsomorphismGeneratorFactory.generatorFromString(e.getValue())
                ));
    }

    private Map<AssertKind, IsomorphismGenerator> getDefaults() {
        Map<AssertKind, String> temp = new HashMap<>();
        temp.put(AssertKind.NULL, "expect(LHS).to.be.null;");
        temp.put(AssertKind.UNDEFINED, "expect(LHS).to.be.undefined;");
        temp.put(AssertKind.EXIST, "expect(LHS).to.exist;");
        temp.put(AssertKind.THROW, "expect(LHS).to.throw;");
        temp.put(AssertKind.NOT_THROW, "expect(LHS).to.not.throw;");
        temp.put(AssertKind.EQUAL, "expect(LHS).to.equal(RHS);");
        temp.put(AssertKind.DEEP_EQUAL, "expect(LHS).to.deep.equal(RHS);");
        temp.put(AssertKind.LENGTH, "expect(LHS).to.have.length(RHS);");
        temp.put(AssertKind.TYPE, "expect(LHS).to.be.a(RHS);");
        temp.put(AssertKind.BOOL, "expect(LHS).to.be.RHS;");
        return this.mapMapToOtherMap(temp);
    }

    public String getAssertion(AssertKind kind, String LHS, String RHS) {
        return this.genMap.get(kind).gen(LHS, RHS);
    }
}
/*
interface Gen {
    public String gen(String LHS, String RHS);
}

class Main {
  public static void main(String args[]) {
    String lhs = "foo";
    String rhs = "\"bar\"";
    String start1 = "expect(LHS).to.be.true;";
    String start2 = "expect(LHS).to.equal(RHS);";
    Gen gen1 = Main.makeGen(start1);
    Gen gen2 = Main.makeGen(start2);
    System.out.println(gen1.gen(lhs, rhs));
    System.out.println(gen2.gen(lhs, rhs));
  }

  private static Gen makeGen(String template) {
    String[]tokens = template.split("(LHS)|(RHS)");
    if (tokens.length == 2) { // LHS only
      return new Gen() {
        private String start = tokens[0];
        private String end = tokens[1];
        public String gen(String LHS, String RHS) {
          return start + LHS + end;
        }
      };
    } else if (tokens.length == 3) {
      return new Gen() {
          private String start = tokens[0];
          private String middle = tokens[1];
          private String end = tokens[2];
          public String gen(String LHS, String RHS) {
              return start + LHS + middle + RHS + end;
          }
      };
    } else {
        throw new RuntimeException();
    }

  }
}
 */