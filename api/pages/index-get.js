'use strict'
/* eslint-env node, es6 */

const { readFile, readFileSync } = require('fs');
const { promisify } = require('util');
const readFileAsync = promisify(readFile);
const path = require('path');

module.exports = async() => {
    const relativePath = path.resolve("./ui/ide.html");
    const content = readFileSync(relativePath, { encoding: "utf-8" });
    return content;
}