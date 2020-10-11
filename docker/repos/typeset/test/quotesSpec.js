"use strict";

const typeset = require("../src/index");
const expect = require("chai").expect;

function quotes(html) {
  return typeset(html, {
    enable: ["quotes"],
  });
}

describe("Quotes", () => {

  it("should replace quotes by unicode beginning and ending quotes characters", () => {
    const html = '<p>"Hello," said the fox.</p>';
    // Target variable is 'updated'
    const updated = quotes(html);
  });

});
