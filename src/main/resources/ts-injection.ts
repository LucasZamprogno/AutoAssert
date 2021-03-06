function elemUnderTestGenerator(elem: any) {
    const res: any = {};
    const type: any = typeof elem;
    res["type"] = type;
    switch (type) {
        case "boolean":
        case "number":
        case "string":
            res["value"] = elem;
            break;
        case "symbol":
            res["value"] = elem.toString(); // Symbol descriptor
            break;
        case "undefined":
            res["value"] = null; // Arbitrary choice
            break;
        case "function":
            let numArgs = elem.length;
            res["args"] = numArgs;
            if (numArgs === 0) { // Can't guess at args, check for throws only if none needed
                let throws = false;
                try {
                    elem();
                } catch (e) {
                    throws = true;
                }
                res["throws"] = throws;
            }
            res["value"] = elem.toString(); // Function as text, why not
            break;
        case "object":
            if (elem === null) {
                res["type"] = "null";
                res["value"] = null;
            } else if (Array.isArray(elem)) {
                res["type"] = "array";
                res["value"] = elem;
                res["length"] = elem.length;
            } else if (elem instanceof Set) { // Doesn't work on es2015
                res["type"] = "set";
                res["value"] = Array.from(elem);
                res["length"] = elem.size;
            } else {
                res["value"] = elem;
                res["methods"] = [];
                for (const key of Object.keys(elem)) {
                    if (typeof elem[key] === "function") {
                        res["methods"].push(key);
                    }
                }
            } // Consider adding promises
    }
    return res;
}
