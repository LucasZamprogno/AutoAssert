import {expect} from "chai";
import {Main} from "../src/Main";


describe("Main Test Suite", function () {
    before(async function () {
        console.log("Suite start");
    });

    beforeEach(async function () {
        console.log("Test start: " + this.currentTest.title);
    });

    afterEach(function () {
        console.log("Test end: " + this.currentTest.title);
    });

    after(async function () {
        console.log("Suite end");
    });

    it("Boolean", function () {
        const res = Main.returnBoolean();
    });

    it("Number", function () {
        const res = Main.returnNumber();
    });

    it("String", function () {
        const res = Main.returnString();
    });

    it("Symbol", function () {
        const res = Main.returnSymbol();
    });

    it("Function throw", function () {
        const res = Main.returnFunctionNoArgsThrow();
    });

    it("Function no throw", function () {
        const res = Main.returnFunctionNoArgsNoThrow();
    });

    it("Function not callable", function () {
        const res = Main.returnFunctionArgs();
    });

    it("Array", function () {
        const res = Main.returnArray();
    });

    it("Null", function () {
        const res = Main.returnNull();
    });

    it("Void/Undefined", function () {
        const res = Main.returnVoid();
    });

    it("Set", function () {
        const res = Main.returnSet();
    });

    it("Object", function () {
        const res = Main.returnObject();
    });

    it("Class", function () {
        const res = Main.returnClass();
    });
});
