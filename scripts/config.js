#!/usr/bin/env node

global.Promise = require("bluebird");
const R = require('rambdax');
const c = require('chalk');
const axios = require('axios');
const inquirer = require('inquirer');
const BigNumber = require('bignumber.js');
const {replaceInFile} = require('./lib/utils');

function text(msg) {
    console.log(c.green(msg));
}

(async () => {
    text('This script will help you do some basic configuration on the app.');
    text('To leave something unchanged, just hit enter.\n');

    let answers = await inquirer.prompt([
        {
            type: 'input',
            name: 'name',
            message: 'Enter a name for the app'
        },
        {
            type: 'input',
            name: 'id',
            message: 'Enter a lowercase identifier for the app',
            validate: val => {
                if (/^[a-z][a-z0-9-]+$/.test(val)) {
                    return true;
                } else {
                    return 'Identifier can only start with a lowercase letter, and then contain lowercase letters, numbers and dashes.';
                }
            }
        },
        {
            type: 'confirm',
            name: 'createContract',
            message: 'Do you want to create a new token contract for this app?'
        }
    ]);

    /**
     * @type {AxiosInstance}
     */
    const client = axios.create({
        baseURL: 'https://test.apiminer.com',
        maxContentLength: 1000000,
        headers: {
            'Authorization': `Bearer 6c9bcc71f795a19bb3086249a5e7be44d078f888772e7a9306fe25dc7e9883c9d6e6c0e418c4229938bb801c778a8534`,
            'User-Agent': `Blockwell QR Android script`
        }
    });

    let appId = answers.id;
    let appName = answers.name;
    let tokenId;
    let tokenDecimals = 18;
    let tokenName;
    let tokenSymbol;
    let tokenType;
    let tokenAddress;
    let account = '0x427313d83777f2457cd44daf27c4006adc915276';
    let serverAccount = '0x8f5dfa044fb51a4f392689f0f67ab19fbb869be4';

    if (answers.createContract) {
        let network = 'rinkeby';

        answers = await inquirer.prompt([
            {
                type: 'input',
                name: 'tokenName',
                message: 'Enter the token name'
            }, {
                type: 'input',
                name: 'tokenSymbol',
                message: 'Enter the token symbol'
            }, {
                type: 'input',
                name: 'tokenSupply',
                message: 'Enter total token supply, in whole units',
                validate: val => {
                    let num = new BigNumber(val);
                    if (!num.isNaN() && num.isGreaterThan(0)) {
                        return true;
                    }
                    return 'Enter a valid number'
                }
            }
        ]);

        tokenName = answers.tokenName;
        tokenSymbol = answers.tokenSymbol;
        tokenType = "foodcoin";

        text('Submitting token contract...');

        let res = await client.request({
            url: `accounts`
        });

        let supply = new BigNumber(answers.tokenSupply).times(`1e${tokenDecimals}`).toFixed(0);
        let body = {
            name: tokenName,
            network: 'rinkeby',
            type: tokenType,
            parameters: {
                symbol: tokenSymbol,
                decimals: tokenDecimals
            }
        };

        if (tokenType === "prime" || tokenType === "foodcoin") {
            body.parameters.totalSupply = supply;
            body.parameters.unlockTime = 0;
        } else if (tokenType === "fire_token") {
            body.parameters.pool = supply;
            body.parameters.degradation = "1000000000000000000";
            body.parameters.stakingAddress = serverAccount;
        }

        res = await client.request({
            method: 'post',
            url: `contracts`,
            data: body
        });
        tokenId = res.data.data.contractId;

        text(`Submitted with ID ${tokenId}. Waiting for contract to finish deploying...`);

        let i = 0;
        tokenAddress = await new Promise(function (resolve, reject) {
            let timer = setInterval(async () => {
                i++;
                let res;
                try {
                    res = await client.request({
                        url: `contracts/${tokenId}`
                    });
                } catch (e) {
                    console.log(e);
                }

                if (res && res.data && res.data.data.address) {
                    clearInterval(timer);
                    resolve(res.data.data.address);
                }
                if (i > 10) {
                    clearInterval(timer);
                    reject(new Error("Contract deployment timed out, something went wrong. Contact us if this issue persists."));
                }
            }, 5000);
        });

        let value = new BigNumber(supply).div(2).toFixed(0);
        if (tokenType === "prime" || tokenType === "foodcoin") {
            text('Sending half of the new tokens to the app server.');
            await client.request({
                method: 'post',
                url: `tokens/${tokenId}/transfers`,
                data: {
                    to: serverAccount,
                    value
                }
            });
        } else if (tokenType === "fire_token") {
            text('Adding app server to whitelist, and fueling it up with half of the pool.');
            await client.request({
                method: 'post',
                url: `contracts/${tokenId}/send/addToWhitelist`,
                data: {
                    arg: [
                        serverAccount
                    ]
                }
            });
            await client.request({
                method: 'post',
                url: `contracts/${tokenId}/send/fuelUp`,
                data: {
                    arg: [
                        serverAccount,
                        value
                    ]
                }
            });
        }

        text(`Token contract deployed!`);
    } else {
        let answers = await inquirer.prompt([
            {
                type: 'input',
                name: 'tokenId',
                message: 'Enter the ID of an existing token contract to use'
            }
        ]);
        tokenId = answers.tokenId;
        let res = await client.request({
            url: `contracts/${tokenId}`
        });

        let tokenNetwork = res.data.data.network;
        tokenAddress = res.data.data.address;

        res = await client.request({
            url: `contracts/${tokenId}/call/name`
        });

        tokenName = res.data.data;

        answers = await inquirer.prompt([{
            type: 'confirm',
            name: 'correct',
            message: `Found ${tokenName} on the ${tokenNetwork} network. Is this correct?`,
            default: true
        }]);

        if (!answers.correct) {
            console.log('Aborting...');
            process.exit();
        }

        text(`You must send some tokens to the app server's wallet ${serverAccount} for users to automatically get tokens.`);
    }

    await replaceInFile(/TOKEN_ID=[a-zA-Z0-9-]+/, `TOKEN_ID=${tokenId}`, 'app/config.properties');
    await replaceInFile(/APP_ID=[a-z][a-z0-9-]+/, `APP_ID=${appId}`, 'app/config.properties');
    await replaceInFile(/<string name="app_name">[^<]+<\/string>/, `<string name="app_name">${appName}</string>`, 'app/src/main/res/values/strings.xml');

    answers = await inquirer.prompt([
        {
            type: 'input',
            name: 'address',
            message: 'Enter a wallet address payments should go into (just hit enter to skip)'
        }
    ]);

    if (answers.address) {
        let res = await axios.post('https://qr.blockwell.ai/api/qr/code', {
            contractId: tokenId,
            method: 'payment',
            creator: 'Blockwell',
            arguments: [
                {
                    "label": "To Wallet",
                    "value": answers.address
                },
                {
                    "label": "Amount",
                    "dynamic": "value",
                    "decimals": "18",
                    "symbol": "FC"
                },
                {
                    "label": "Order #",
                    "dynamic": "order"
                }
            ]
        });

        let shortcode = res.data.shortcode;

        if (shortcode) {
            text('\nA Payment QR Code has been created for you:');
            console.log(c.cyanBright(`https://qr.blockwell.ai/code/${shortcode}\n`));
        }
    }

    console.log(c.greenBright('Configuration done.'));
    text('You can create QR Codes for your contract with the following link:');
    console.log(c.cyanBright(`https://qr.blockwell.ai/code?contract=${tokenAddress}`));

})()
    .catch(console.error);
