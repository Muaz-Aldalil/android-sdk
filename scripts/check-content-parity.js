#!/usr/bin/env node
/**
 * Verify Android content assets match the web constants (single source of truth).
 *
 * Usage: node scripts/check-content-parity.js [path-to-SH-constants]
 * Defaults to the sibling repo at ../../SH/src/constants.
 *
 * Exits 0 when every mapped JSON asset is semantically identical to its JS
 * source; exits 1 listing the first drift per file. Run after every copy job.
 */
const fs = require("fs");
const path = require("path");

const SH_CONSTANTS = process.argv[2] || path.resolve(__dirname, "../../SH/src/constants");
const ASSETS = path.resolve(__dirname, "../app/src/main/assets/content");

// JS constant name -> Android asset file. social_links is excluded: it adds an
// iconKey field by design.
const PAIRS = [
  ["QUIZZES", "quizzes.json"],
  ["KIDS_GAMES", "kids_games.json"],
];

/** Parse an ES-module constant file (`export const NAME = [...]`) into a value. */
function parseJsConstant(file, name) {
  const src = fs.readFileSync(file, "utf8");
  const body = src.replace(new RegExp(`export const ${name} = `), "").replace(/;\s*$/, "");
  // eslint-disable-next-line no-new-func
  return Function(`"use strict"; return (${body});`)();
}

let failed = false;
for (const [jsName, jsonFile] of PAIRS) {
  const jsPath = path.join(SH_CONSTANTS, `${jsName}.js`);
  const jsonPath = path.join(ASSETS, jsonFile);
  if (!fs.existsSync(jsPath) || !fs.existsSync(jsonPath)) {
    console.error(`MISSING ${jsPath} or ${jsonPath}`);
    failed = true;
    continue;
  }
  const jsValue = parseJsConstant(jsPath, jsName);
  const jsonValue = JSON.parse(fs.readFileSync(jsonPath, "utf8"));
  const jsText = JSON.stringify(jsValue, null, 2);
  const jsonText = JSON.stringify(jsonValue, null, 2);
  if (jsText === jsonText) {
    console.log(`OK   ${jsonFile} == ${jsName}.js`);
  } else {
    failed = true;
    console.error(`DIFF ${jsonFile} != ${jsName}.js`);
    const a = jsText.split("\n");
    const b = jsonText.split("\n");
    for (let i = 0; i < Math.max(a.length, b.length); i++) {
      if (a[i] !== b[i]) {
        console.error(`  line ${i + 1}:\n    js:   ${a[i]}\n    json: ${b[i]}`);
        break;
      }
    }
  }
}

process.exit(failed ? 1 : 0);