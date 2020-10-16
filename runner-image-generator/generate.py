import docker
import os

client = docker.from_env()
repos = [
    ## ("https://github.com/open-wc/open-wc.git", "npm run test:node", "npm install && lerna run build"), # Broken?
    # ("https://github.com/davidmerfield/Typeset.git", "npm run test", "npm install"),
    ## ("https://github.com/ipfs/js-ipfs.git", "npm run test", "lerna bootstrap"),
    ## ("https://github.com/trufflesuite/truffle.git", "npm run test", "yarn bootstrap"),
    ## ("https://github.com/DevExpress/testcafe.git", "gulp test-server", "npm install"),
    # ("https://github.com/adleroliveira/dreamjs.git", "npm run test", "npm install"),
    # ("https://github.com/dareid/chakram.git", "npm run test", "npm install"),
    ## ("https://github.com/sourcegraph/javascript-typescript-langserver.git", "npm test", "npm install && npm run build"),
    ## ("https://github.com/Polymer/tools.git", "npm run test:unit", "npm install && npm run bootstrap"),
    ## ("https://github.com/TheBrainFamily/cypress-cucumber-preprocessor.git", "npm test", "npm install"),
    # ("https://github.com/exratione/lambda-complex.git", "grunt mochaTest:test", "npm install"), # Need to double check this one
    ## ("https://github.com/segmentio/nightmare.git", "make test", "npm install"),
    # ("https://github.com/lonelyplanet/backpack-ui.git", "npm run test", "npm install"),
    # ("https://github.com/jsillitoe/react-currency-input.git", "npm run test", "npm install"),
    # ("https://github.com/rapid-sensemaking-framework/noflo-rsf.git", "npm run test", "npm install && npm run build"), # Builds on test run, should prob edit?
    # ("https://github.com/thetutlage/japa.git", "npm run test:win", "npm install && npm run compile"),
    # ("https://github.com/trufflesuite/ganache-core.git", "npm run _mocha", "npm install"),
    ## ("https://github.com/sebpiq/rhizome.git", "npm test", "npm install"),
    ## ("https://github.com/nomiclabs/buidler", "npm run test", "npm install && npm run build-test"),
    # ("https://github.com/philcockfield/ui-harness.git", "npm run test", "npm install || echo \"suppress fail\"")
]
# Double ## means may be broken

for repo in repos:
    url = repo[0]
    run_com = repo[1]
    build_arg = repo[2]
    name = url.replace("https://github.com/", "").replace(".git", "").replace("/", "-").lower()
    image_name = "assertion-%s-runner" % name
    args = {
        "REPO": url,
        "RUN_COM": run_com,
        "BUILD_COM": build_arg
    }
    client.images.build(path=".", tag=image_name, buildargs=args)
    print("Built", image_name)