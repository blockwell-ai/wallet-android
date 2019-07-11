const fs = require('fs-extra');

async function replaceInFile(pattern, replacement, file) {
    let contents = (await fs.readFile(file)).toString('UTF-8');

    await fs.writeFile(file, contents.replace(pattern, replacement));
}

module.exports = {
    replaceInFile
};
