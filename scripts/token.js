#!/usr/bin/env node

global.Promise = require("bluebird");
const path = require('path');
const fs = require('fs-extra');
const {replaceInFile, loadAbi} = require('./lib/utils');
const axios = require('axios');

const address = process.argv[2];

if (!address || !/^0x[a-f0-9]{40}$/i.test(address)) {
    console.error('First argument must be a contract address');
    process.exit(1);
}

(async () => {
    let data = (await axios.get(`https://qr.blockwell.ai/api/qr/contract/${address}`)).data;

    if (!data.id) {
        console.error('No ABI could be retrieved');
        process.exit(1);
    }

    const contractId = data.id;

    await replaceInFile(
        /var trainerToken (by stringPref\(|=) *"[^"]*"\)?/,
        `var trainerToken = "${contractId}"`,
        path.resolve(__dirname, '../app/src/main/java/ai/blockwell/qrdemo/data/DataStore.kt'));
    await replaceInFile(
        /var trainerTokenAddress (by stringPref\(|=) *"[^"]*"\)?/,
        `var trainerTokenAddress = "${address}"`,
        path.resolve(__dirname, '../app/src/main/java/ai/blockwell/qrdemo/data/DataStore.kt'));
    await replaceInFile(
        /var override = false/,
        'var override = true',
        path.resolve(__dirname, '../app/src/main/java/ai/blockwell/qrdemo/data/DataStore.kt'));
})()
    .catch(console.error);

