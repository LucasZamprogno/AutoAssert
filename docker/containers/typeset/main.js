const fs = require("fs");
// const _ = require("lodash");
const execSync = require("child_process").execSync;

const projectDir = "../main";
const outputPath = `${projectDir}/test/.testOutput`;
const runCommand = "npm run test"; 
// Import these somehow?

console.log("Removing testOutput");
removeFile(outputPath);

console.log("Before run 1");
execSync(`cd ${projectDir} && ${runCommand}`, {timeout: 10 * 1000});
let result1 = loadOutput();

console.log("Before run 2");
execSync(`cd ${projectDir} && ${runCommand}`, {timeout: 10 * 1000});
let result2 = loadOutput();

console.log("Before difference");

// Simple difference, rewrite completely when doing for real
result1.hasDiff = false;
if (bothObjects(result1, result2) || bothArrOrSet(result1, result2)) {
    result1.hasDiff = JSON.stringify(result1.value) !== JSON.stringify(result2.value);
} else if (result1.type === result2.type && result1.value !== result2.value) {
    result1.hasDiff = true;
}
if (result1.hasDiff) {
  result1.difference = result2.value;
}

console.log("Before write");
fs.writeFileSync(outputPath, JSON.stringify(result1));

function loadOutput() {
    try {
        return JSON.parse(fs.readFileSync(outputPath).toString());
    } catch (e) {
        return {timeout: true, type: "none"};
    }
}

function bothObjects(res1, res2) {
    return res1.type === res2.type && res1.type === 'object';
}

function bothArrOrSet(res1, res2) {
    return res1.type === res2.type && (res1.type === 'array' || res2.type === 'set');
}

function removeFile(fileName) {
    try {
        if (fs.existsSync(fileName)) {
            fs.unlinkSync(fileName);
        }
    } catch (e) {
        // Just in case
    }
}

/*
// From https://gist.github.com/Yimiprod/7ee176597fef230d1451
function objDifference(object, base) {
	function changes(object, base) {
		return _.transform(object, function(result, value, key) {
			if (!_.isEqual(value, base[key])) {
				result[key] = (_.isObject(value) && _.isObject(base[key])) ? changes(value, base[key]) : value;
			}
		});
	}
	return changes(object, base);
}

function makeVarianceFreeObject(obj1, obj2) {
    if (_.isEqual(obj1, obj2)) {
        return obj1;
    }
    const out = {};
    const keys = _.intersection(Object.keys(obj1), Object.keys(obj2));
    for (const key of keys) {
        const val1 = obj1[key];
        const val2 = obj2[key];
        if (_.isEqual(val1, val2)) {
            out[key] = val1;
        } else if (typeof val1 === 'object' && typeof val2 === 'object') {
            out[key] = makeVarianceFreeObject(val1, val2);
        }
    }
}
*/
