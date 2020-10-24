import os
import json

res_dir = "./results/"
res_map = {}
repos = os.listdir(res_dir)

def smush_extra_info_downward(results):
    for result in results:
        ours = [x.strip() for x in result["ours"].split("\n")]
        path = result["path"]
        for assertion in result["assertions"]:
            assertion["ours"] = ours
            assertion["path"] = path
            

def get_our_clusters(assertions):
    clusters = []
    for assertion in assertions:
        if "to.be.true" in assertion or "to.be.false" in assertion:
        # if "to.equal(true)" in assertion or "to.equal(false)" in assertion:
            clusters.append("boolean")
        elif "to.be.null" in assertion or "to.be.undefined" in assertion or "to.exist" in assertion:
            clusters.append("existence")
        elif "to.equal" in assertion or "to.deep.equal" in assertion:
            clusters.append("equality")
        elif "to.throw" in assertion or "to.not.throw" in assertion:
            clusters.append("throw")
        elif "to.have.length" in assertion or ".length" in assertion or ".size())." in assertion:
            clusters.append("length")
        elif "to.be.a" in assertion:
            clusters.append("type")
        else:
            clusters.append("Wat")
    return clusters
    
def get_cluster_from_token(token):
    if token in ["equal", "eql", "toEqual", "eq", "equals", "toStrictEqual", "equalIgnoreSpaces", "deepEqual", "strictEqual", "deepStrictEqual", "notEqual", "notStrictEqual", "notDeepStrictEqual", "notDeepEqual", "toBe"]:
        cluster = "equality"
    elif token in ["property", "json", "keys", "propertyVal", "containsAllKeys", "attribute", "notProperty", "nestedProperty", "hasAllKeys"]:
        cluster = "properties"
    elif token in ["length", "lengthOf", "toHaveLength", "arrayLength", "empty", "isEmpty", "isNotEmpty"]:
        cluster = "length"
    elif token in ["undefined", "toBeDefined", "toBeUndefined", "exist", "exists", "null", "toBeNull", "isUndefined", "isDefined", "isNotNull", "isNull", "doesNotExist"]:
        cluster = "existence"
    elif token in ["contain", "include", "includes", "contains", "toContain", "members", "arrayIncluding", "notInclude", "deepInclude"]:
        cluster = "inclusion"
    elif token in ["instanceof", "instanceOf", "toBeInstanceOf", "a", "an", "string", "isArray", "isObject", "isNumber", "isFunction", "isString", "isBigNumber"]:
        cluster = "type"
    elif token in ["toThrow", "throws", "throw", "toThrowError", "rejected", "rejectedWith", "rejects", "doesNotThrow"]:
        cluster = "throw"
    elif token in ["TRUE", "true", "FALSE", "false", "isTrue", "isFalse"]:
        cluster = "boolean"
    elif token in ["toBeTruthy", "toBeFalsy", "falsy", "truthy", "ok", "isOk", "notOk", "isNotOk"]:
        cluster = "truthy"
    elif token in ["above", "below", "gt", "gte", "lessThan", "greaterThan", "toBeGreaterThan", "least", "toBeLessThan", "lt", "lte", "isAbove", "isBelow", "closeTo", "isAtLeast", "isAtMost"]:
        cluster = "numerical"
    # elif token in ["to", "be", "be", "been", "is", "that", "which", "and", "has", "have", "with", "at", "of", "same", "but", "does", "still"]:
    #     cluster = "syntax"
    elif token in ["match", "matches", "toMatchObject", "toMatchSnapshot", "toMatch", "toMatchInlineSnapshot", "toMatchImageSnapshot"]:
        cluster = "pattern"
    elif token in ["calledWithMatch", "calledOnceWithExactly", "calledTwice", "called", "calledWith", "calledOnce", "toHaveBeenCalledWith", "toHaveBeenCalled", "calledThrice", "toHaveBeenCalledTimes"]:
        cluster = "call"
    # elif token in ["deep", "not", "nested", "dom", "bignumber"]:
    #     cluster = "modifiers"
    # elif token in ["expect", "assert"]:
    #     return
    else:
        cluster = None
    return cluster

def get_clusters_from_assertion(assertion):
    clusters = [get_cluster_from_token(x) for x in assertion["token"]] # Get clusters
    clusters = [x for x in clusters if x is not None] # Remove duplicates
    clusters = fix_eql_cluster(assertion, clusters) # Recategorize specific equals
    return clusters

def add_to_cluster(results, assertion):
    clusters = get_clusters_from_assertion(assertion)
    for cluster in clusters:
        if not cluster in results:
            results[cluster] = 0
        results[cluster] += 1
        results["total"] += 1

def load_result(item):
    with open(item, encoding="utf-8") as f:
        result = json.load(f)
        result["path"] = item
        return result

def get_all_results(current):
    res = []
    for item in os.listdir(current):
        path = os.path.join(current, item)
        if os.path.isdir(path):
            res.extend(get_all_results(path))
        else:
            res.append(load_result(path))
    return res

def failed(assertion):
    return assertion["ours"].strip().startswith("//")

def pct_breakdown():
    clusters = {
        "total": 0
    }

    for res in pass_res:
        assertions = res["assertions"]
        for assertion in assertions:
            add_to_cluster(clusters, assertion)
    final_total = clusters["total"]
    for cluster in clusters:
        clusters[cluster] = round((clusters[cluster] / final_total) * 100, 2)
    print(json.dumps(clusters))

def fix_eql_cluster(assertion, their_clusters):
    if "equality" in their_clusters and "equality" in assertion and len(assertion["equality"]) > 0:
        their_clusters.remove("equality")
        their_clusters.extend(assertion["equality"])
        their_clusters = [x.lower() for x in their_clusters]
        if "null" in their_clusters:
            their_clusters.remove("null")
            their_clusters.append("existence")
        if "undefined" in their_clusters:
            their_clusters.remove("undefined")
            their_clusters.append("existence")
    return their_clusters

def match_breakdown():
    scores = {
        "total": {
            "hit": 0,
            "miss": 0
        }
    }
    for res in pass_res:
        assertions = res["assertions"]
        our_clusters = get_our_clusters(res["ours"].split("\n"))
        for assertion in assertions:
            their_clusters = get_clusters_from_assertion(assertion)
            for cluster in their_clusters:
                if cluster not in scores:
                    scores[cluster] = {
                        "hit": 0,
                        "miss": 0,
                        "hits": [],
                        "misses": []
                    }
                if cluster in our_clusters:
                    scores["total"]["hit"] += 1
                    scores[cluster]["hit"] += 1
                    scores[cluster]["hits"].append(assertion)
                else:
                    scores["total"]["miss"] += 1
                    scores[cluster]["miss"] += 1
                    scores[cluster]["misses"].append(assertion)
    final = {}
    for score in scores:
        hit = scores[score]["hit"]
        miss = scores[score]["miss"]
        final[score] = round(100 * hit / (hit + miss), 2)
    print(json.dumps(final))
    print("Hit:", scores["total"]["hit"])
    print("Miss:", scores["total"]["miss"])
    for cluster in scores:
        if cluster == "equality" or cluster == "type" or cluster == "boolean" or cluster == "existence" or cluster == "throw" or cluster == "length":
            for miss in scores[cluster]["misses"]:
                print(miss["path"])

pass_res = []
fail_res = []

for top_level_repo in repos:
    res_map[top_level_repo] = {}
    path = os.path.join(res_dir, top_level_repo)
    all_res = get_all_results(path)
    # pass_res = []
    # fail_res = []
    for res in all_res:
        if (failed(res)):
            fail_res.append(res)
        else:
            pass_res.append(res)
    # res_map[top_level_repo]["pass"] = pass_res
    # res_map[top_level_repo]["fail"] = fail_res
    # print(top_level_repo)
    # print("Pass:", len(pass_res))
    # print("Fail:", len(fail_res))

smush_extra_info_downward(pass_res)
pct_breakdown()
match_breakdown()