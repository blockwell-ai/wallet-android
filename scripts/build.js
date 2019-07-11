#!/usr/bin/env node

global.Promise = require("bluebird");
const R = require('rambdax');
const c = require('chalk');
const axios = require('axios');
const inquirer = require('inquirer');
const BigNumber = require('bignumber.js');
const {replaceInFile} = require('./lib/utils');
const execa = require('execa');
const fs = require('fs-extra');
const glob = Promise.promisify(require('glob').glob);
const path = require('path');

function text(msg) {
    console.log(c.green(msg));
}

(async () => {
    text('Building the app. The first time this will take 3-4 minutes, future builds will be faster.');

    for (let file of await glob('app/build/outputs/apk/release/*.apk')) {
        await fs.unlink(file);
    }

    let result = await execa('./gradlew', ['-q', 'assembleRelease']);

    if (result.failed) {
        console.log(result.all);
        process.exit(1);
    } else if (result.stderr) {
        console.log(stderr);
    }

    let apkPath = (await glob('app/build/outputs/apk/release/*.apk'))[0];
    let apk = path.basename(apkPath);
    let webpath = `/var/www/html/${process.env.USER}/${apk}`;

    await fs.copy(apkPath, webpath);
    await fs.chmod(webpath, 0o755);

    console.log(c.greenBright('\nBuild successful!'));
    text('You can always rebuild the app after changes by running "make".');
    console.log(c.greenBright(`Download link: `) + c.cyanBright(`https://android.apiminer.com/${process.env.USER}/${apk}`));
})()
    .catch(console.error);
