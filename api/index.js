'use strict'
/* eslint-env node, es6 */

const bodyParser = require('body-parser');

const exec = require('child_process').exec;

const express = require("express");
const app = express();

const PORT = 6300;

const ideFront = require('./pages/index-get.js');
const path = require("path");

app.get('/', async(req, res) => {
    const pageHTML = await ideFront();
    res.send(pageHTML);
});

app.use('/css', express.static(path.resolve("./ui/css/")));
app.use('/js', express.static(path.resolve("./ui/js/")));
app.use('/js/lib', express.static(path.resolve("..=/ui/js/lib/")));
app.use('/js/lib/snippets', express.static(path.resolve("..=/ui/js/lib/snippets")));

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

// create application/json parser
const jsonParser = bodyParser.json()

// create application/x-www-form-urlencoded parser
const urlencodedParser = bodyParser.urlencoded({ extended: false })

app.post('/execute', jsonParser, async(req, res, next) => {
    const body = req.body;
    console.log(body);
    res.status(201).json({
        message: 'Received !'
    });
});

app.post('/directory', jsonParser, async(req, res, next) => {
    console.log("opening directory");
    res.status(201).json({
        message: 'Received !'
    });
});

app.listen(PORT, () => {
    console.log("PING IDE started !");
})

exec('cd JsToJava && mvn quarkus:dev',
    function (error, stdout, stderr) {
        console.log('stdout: ' + stdout);
        console.log('stderr: ' + stderr);
        if (error !== null) {
            console.log('exec error: ' + error);
        }
    });