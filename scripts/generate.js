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

const classPath = path.resolve(__dirname, '../app/src/main/java/ai/blockwell/qrdemo/generated');
const layoutPath = path.resolve(__dirname, '../app/src/main/res/layout');

function text(msg) {
    console.log(c.green(msg));
}

let abiPath = process.argv[2];

if (!abiPath) {
    console.error('First argument needs to be a path to an ABI, a mainnet contract address, or web URL');
    process.exit(1);
}

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
    ['transferFrom', 3, [2]],
];

(async () => {
    const funcs = (await loadAbi(abiPath)).filter(it => it.constant === false);

    let fragments = funcs
        .sort((a, b) => (a.name > b.name) - (a.name < b.name))
        .map(it => "{ " + changeCase.pascalCase(it.name) + "Fragment() }");
    let names = funcs
        .sort((a, b) => (a.name > b.name) - (a.name < b.name))
        .map(it => `"${changeCase.titleCase(it.name)}"`);

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

        let layout = await template.render('layout.xml', conf);
        let fragment = await template.render('Fragment.kt', conf);
        let generated = await template.render('GeneratedFragment.kt', {
            fragments: fragments.join(",\n            "),
            names: names.join(",\n        ")
        });

        await fs.writeFile(`${layoutPath}/fragment_generated_${conf.lower}.xml`, layout);
        await fs.writeFile(`${classPath}/${conf.pascal}Fragment.kt`, fragment);
        await fs.writeFile(`${classPath}/AAAGeneratedPagingFragment.kt`, generated);

    }
})()
    .catch(console.error);
