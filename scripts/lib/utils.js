const fs = require('fs-extra');
const axios = require('axios');

async function replaceInFile(pattern, replacement, file) {
    let contents = (await fs.readFile(file)).toString('UTF-8');

    await fs.writeFile(file, contents.replace(pattern, replacement));
}

async function loadAbi(path) {
    let abi;

    if (/^0x[a-f0-9]{40}/i.test(path)) {
        return loadAbi(`https://api.etherscan.io/api?module=contract&action=getabi&address=${path}`)
    }

    if (path.startsWith('https://') || path.startsWith('http://')) {
        let result = await axios({
            method: 'get',
            url: path,
            headers: {
                'User-Agent': 'Blockwell scripts'
            }
        });
        if (result.data.result) {
            abi = JSON.parse(result.data.result);
        } else if (Array.isArray(result.data)) {
            abi = result.data;
        }
    } else {
        let json = await fs.readJson(path);
        if (json['abi']) {
            abi = json.abi;
        } else {
            abi = json;
        }
    }

    return abi;
}

module.exports = {
    replaceInFile,
    loadAbi
};
