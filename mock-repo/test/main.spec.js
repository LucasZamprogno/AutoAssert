"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const Main_1 = require("../src/Main");
describe("Main Test Suite", function () {
    before(function () {
        return __awaiter(this, void 0, void 0, function* () {
            console.log("Suite start");
        });
    });
    beforeEach(function () {
        return __awaiter(this, void 0, void 0, function* () {
            console.log("Test start: " + this.currentTest.title);
        });
    });
    afterEach(function () {
        console.log("Test end: " + this.currentTest.title);
    });
    after(function () {
        return __awaiter(this, void 0, void 0, function* () {
            console.log("Suite end");
        });
    });
    it("Boolean", function () {
        const res = Main_1.Main.returnBoolean();
    });
    it("Number", function () {
        const res = Main_1.Main.returnNumber();
    });
    it("String", function () {
        const res = Main_1.Main.returnString();
    });
    it("Symbol", function () {
        const res = Main_1.Main.returnSymbol();
    });
    it("Function", function () {
        const res = Main_1.Main.returnFunction();
    });
    it("Array", function () {
        const res = Main_1.Main.returnArray();
    });
    it("Null", function () {
        const res = Main_1.Main.returnNull();
    });
    it("Void/Undefined", function () {
        const res = Main_1.Main.returnVoid();
    });
    it("Set", function () {
        const res = Main_1.Main.returnSet();
    });
    it("Object", function () {
        const res = Main_1.Main.returnObject();
    });
    it("Class", function () {
        const res = Main_1.Main.returnClass();
    });
});
