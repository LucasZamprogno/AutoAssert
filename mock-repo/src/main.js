"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.SomeOtherClass = exports.Main = void 0;
let Main = /** @class */ (() => {
    class Main {
        static returnBoolean() {
            return true;
        }
        static returnNumber() {
            return 1;
        }
        static returnString() {
            return "foo";
        }
        static returnSymbol() {
            return this.someSymbol;
        }
        static returnFunction() {
            return function () {
                console.log("foo");
            };
        }
        static returnArray() {
            return ["foo", null, 1];
        }
        static returnNull() {
            return null;
        }
        static returnVoid() {
            console.log("side effect");
        }
        static returnSet() {
            return new Set(Main.returnArray());
        }
        static returnObject() {
            return {
                "foo": "bar",
                "num": 1,
                "arr": Main.returnArray(),
                "sub": { "a": 1 }
            };
        }
        static returnClass() {
            return new SomeOtherClass(1, "foo");
        }
    }
    Main.someSymbol = Symbol("descriptor");
    return Main;
})();
exports.Main = Main;
class SomeOtherClass {
    constructor(first, second) {
        this.field1 = first;
        this.field2 = second;
    }
    method1() {
        return this.field1;
    }
    method2() {
        return this.field2;
    }
}
exports.SomeOtherClass = SomeOtherClass;
