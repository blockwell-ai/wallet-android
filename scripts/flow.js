#!/usr/bin/env node

global.Promise = require("bluebird");
const R = require('rambdax');
const c = require('chalk');
const axios = require('axios');
const inquirer = require('inquirer');
const BigNumber = require('bignumber.js');
const {replaceInFile, loadAbi} = require('./lib/utils');
const execa = require('execa');
const fs = require('fs-extra');
const glob = Promise.promisify(require('glob').glob);
const path = require('path');
const template = require('./lib/template');
const changeCase = require('change-case');


function text(msg) {
    console.log(c.green(msg));
}

let flow = process.argv[2];

if (!flow || !/^[a-z]+$/.test(flow)) {
    console.error('First argument needs to be an all-lowercase, no spaces identifier for the flow (eg. suggestions)');
    process.exit(1);
}

let abiPath = process.argv[3];

if (!abiPath) {
    console.error('Second argument needs to be a path to an ABI, a mainnet contract address, or web URL');
    process.exit(1);
}

let stepFuncs = process.argv.slice(4);

const classPath = path.resolve(__dirname, '../app/src/main/java/ai/blockwell/qrdemo/trainer', flow);
const layoutPath = path.resolve(__dirname, '../app/src/main/res/layout');

// Function name, number of arguments, argument indexes for decimals
let autoDecimals = [
    ['transfer', 2, [1]],
    ['approve', 2, [1]],
    ['increaseAllowance', 2, [1]],
    ['decreaseAllowance', 2, [1]],
    ['mint', 2, [1]],
    ['setMintLimit', 2, [1]],
    ['addMinterWithLimit', 2, [1]],
    ['transferAndLock', 3, [1]],
    ['transferFrom', 3, [2]]
];

(async () => {
    const funcs = (await loadAbi(abiPath)).filter(it => it.constant === false);

    const confs = {
        "-": "text"
    };
    for (let func of funcs) {
        let conf = {
            function: func.name,
            title: changeCase.titleCase(func.name),
            pascal: changeCase.pascalCase(func.name),
            lower: changeCase.lowerCase(func.name),
            args: func.inputs.map(it => {
                it.uint = /^uint\d*$/.test(it.type);
                return it
            })
        };

        let dec = autoDecimals.find(it => it[0] === conf.function && it[1] === conf.args.length);

        if (dec) {
            dec[2].forEach(it => {
                conf.args[it].decimals = true;
            });
        }

        confs[conf.function] = conf;
    }

    let steps = stepFuncs.map(it => {
        if (!confs[it]) {
            console.error(`Function ${it} wasn't found in the ABI, aborting`);
            process.exit(1);
        }
        return confs[it];
    });

    const flowPascal = changeCase.pascalCase(flow);

    try {
        await fs.mkdir(classPath);
    } catch (e) {
        // Silent catch
    }

    let fragments = [];
    let i = 1;
    for (let step of steps) {
        let layoutName = `fragment_${flow}_step${i}`;
        let fragment = `Step${i}Fragment`;
        fragments.push(fragment);

        if (step === "text") {
            let content = await template.render('TextStep.kt', {
                n: i,
                flow,
                layout: layoutName
            });
            await fs.writeFile(`${classPath}/${fragment}.kt`, content);

            let layout = await template.render('text_layout.xml');
            await fs.writeFile(`${layoutPath}/${layoutName}.xml`, layout);
        } else {
            step.layout = layoutName;
            step.pascal = `Step${i}`;
            step.flow = flow;

            let content = await template.render('Fragment.kt', step);
            await fs.writeFile(`${classPath}/${fragment}.kt`, content);

            let layout = await template.render('layout.xml', step);
            await fs.writeFile(`${layoutPath}/${layoutName}.xml`, layout);
        }

        ++i;
    }

    fragments = fragments.map(it => `{ ${it}() }`);

    let flowFragment = await template.render('FlowFragment.kt', {
        flow: flow,
        pascal: flowPascal,
        title: flowPascal,
        fragments: fragments.join(",\n            "),
    });

    await fs.writeFile(`${classPath}/${flowPascal}Fragment.kt`, flowFragment);

    let adapterPath = path.resolve(classPath, '../TrainerOptionAdapter.kt');

    let contents = (await fs.readFile(adapterPath)).toString('UTF-8');
    let imp = `import ai.blockwell.qrdemo.trainer.${flow}.${flowPascal}Fragment`;

    if (!contents.includes(imp)) {
        contents = contents.replace('//IMPORT', `import ai.blockwell.qrdemo.trainer.${flow}.${flowPascal}Fragment
//IMPORT`).replace('//FLOW', `,
            TrainerOption("Generated: ${flowPascal}",
                    "No description.") { ${flowPascal}Fragment() }
//FLOW`);
    }

    await fs.writeFile(adapterPath, contents);
})()
    .catch(console.error);
