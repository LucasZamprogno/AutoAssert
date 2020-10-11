"use strict";

const typeset = require("../src/index");
const expect = require("chai").expect;

function lig(html) {
  return typeset(html, {
    enable: ["ligatures"],
  });
}

describe("Ligatures", () => {

  it("should replace (fi) by (ﬁ)", () => {
    const html =
      "<p>A file folder</p>";
    // Target variable is 'withLigature'
    const withLigature = lig(html);
  });

});
