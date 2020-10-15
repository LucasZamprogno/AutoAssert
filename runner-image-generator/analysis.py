import os
import json

res_dir = "/media/lucas/T71/lucas/results/"
res_map = {}
repos = os.listdir(res_dir)

def get_our_clusters(assertions):
    clusters = []
    for assertion in assertions:
        if "to.be.null" in assertion or "to.be.undefined" in assertion or "to.exist" in assertion:
            clusters.append("existence")
        elif "to.equal" in assertion or "to.deep.equal" in assertion:
            clusters.append("equality")
        elif "to.throw" in assertion or "to.not.throw" in assertion:
            clusters.append("throw")
        elif "to.have.length" in assertion:
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
        cluster = "true/false"
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

def add_to_cluster(clusters, tokens):
    for token in tokens:
        cluster = get_cluster_from_token(token)
        if cluster == None:
            continue
        if not cluster in clusters:
            clusters[cluster] = 0
        clusters[cluster] += 1

def load_result(item):
    with open(item) as f:
        return json.load(f)

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

pass_res = []
fail_res = []

for top_level_repo in repos:
    res_map[top_level_repo] = {}
    path = os.path.join(res_dir, top_level_repo)
    all_res = get_all_results(path)
    for res in all_res:
        if (failed(res)):
            fail_res.append(res)
        else:
            pass_res.append(res)
    res_map[top_level_repo]["pass"] = pass_res
    res_map[top_level_repo]["fail"] = fail_res
    # print(top_level_repo)
    # print("Pass:", len(pass_res))
    # print("Fail:", len(fail_res))

def pct_breakdown():
    clusters = {
        "total": 0
    }

    for res in pass_res:
        assertions = res["assertions"]
        for assertion in assertions:
            clusters["total"] += 1
            add_to_cluster(clusters, assertion["token"])
    final_total = clusters["total"]
    for cluster in clusters:
        clusters[cluster] = round((clusters[cluster] / final_total) * 100, 2)
    print(clusters)

def match_breakdown():
    hit = 0
    miss = 0
    for res in pass_res:
        assertions = res["assertions"]
        our_clusters = get_our_clusters(res["ours"].split("\n"))
        for assertion in assertions:
            their_clusters = [get_cluster_from_token(x) for x in assertion["token"]]
            their_clusters = [x for x in their_clusters if x is not None]
            for cluster in their_clusters:
                if cluster in our_clusters:
                    hit += 1
                    # print(cluster)
                    # print(assertion["original"])
                else:
                    miss += 1
                    # print(cluster)
                    # print(assertion["original"])
    print("Hit:", hit)
    print("Miss:", miss)


pct_breakdown()
match_breakdown()