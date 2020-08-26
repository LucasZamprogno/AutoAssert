"use strict";

const typeset = require("../src/index");
const expect = require("chai").expect;

function hang(html) {
  return typeset(html, {
    enable: ["hangingPunctuation"],
  });
}

describe("Hanging Punctuation", () => {
  it("text without hangable punctuation should be the same after call the function", () => {
    const html = "<p>En un lugar de la mancha</p>";
    expect(hang(html)).to.equal("<p>En un lugar de la mancha</p>");
  });

  it("should hang punctuation in middle.", () => {
    const html = '<p>Hello, "Mr" Fox.</p>';
    expect(hang(html)).to.equal(
      '<p>Hello,<span class="push-double"></span> <span class="pull-double">"</span>Mr" Fox.</p>'
    );
  });

  it("should hang punctuation at start.", () => {
    const html = '<p>"Hello, Mr Fox."</p>';
    expect(hang(html)).to.equal(
      '<p><span class="pull-double">"</span>Hello, Mr Fox."</p>'
    );
  });

  it("should add a spacer when wrapping a node equaling a text node", () => {
    const html = '<p>X "<em>O</em>" X</p>';
    expect(hang(html)).to.equal(
      '<p>X<span class="push-double"></span> <span class="pull-double">"</span><em>O</em><span class="push-double"></span><span class="pull-double">"</span> X</p>'
    );
  });

  it("should hang single punction at start.", () => {
    const html = "<p>'Hello, Mr Fox.'</p>";
    expect(hang(html)).to.equal(
      "<p><span class=\"pull-single\">'</span>Hello, Mr Fox.'</p>"
    );
  });

  it("should add a spacer when inside a node next to a text node", () => {
    const html = '<p>X <em>"O"</em> X</p>';
    expect(hang(html)).to.equal(
      '<p>X <em><span class="push-double"></span><span class="pull-double">"</span>O"</em> X</p>'
    );
  });

  it("should hang punctuation with two p", () => {
    const html = "<p>ABC</p>\n" + '<p>"Hello Fox."</p>';
    expect(hang(html)).to.equal(
      "<p>ABC</p>\n" + '<p><span class="pull-double">"</span>Hello Fox."</p>'
    );
  });
});
