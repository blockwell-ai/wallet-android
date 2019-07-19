const handlebars = require("handlebars");
const HandlebarsWax = require('handlebars-wax');
const fs = require('fs-extra');
const path = require('path');

const basePath = path.resolve(__dirname, "..");

const wax = HandlebarsWax(handlebars, {
    cwd: basePath
})
    .partials([
        `templates/*/**/*.hbs`,
        `!templates/partials/**`
    ])
    .partials(`templates/partials/**.hbs`)
    .helpers('templates/helpers/*.js');

const cache = {};
let globalData = {};

function setGlobalData(data) {
    globalData = Object.assign(globalData, data);
}

async function getTemplate(template) {
    let t = cache[template];

    if (!t) {
        let templateString = await fs.readFile(path.resolve(basePath, `templates`, `${template}.hbs`));
        t = wax.compile(templateString.toString());
    }

    return t;
}

async function render(template, data = {}) {
    const t = await getTemplate(template);

    let baseData = Object.assign({}, globalData);

    return t(Object.assign(baseData, data));
}

module.exports = {
    render,
    setGlobalData
};
