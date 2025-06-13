let editor;
let cmd = "open";

let fileList = [];
let darkMode = false;
let currentFile = null;
let projectPath = null;
let nbTabs = 0;
let toClose = false;

window.onload = function() {
    ace.require("ace/ext/language_tools");
    editor = ace.edit("editor");
    editor.setTheme("ace/theme/dracula");
    setDarkTheme();
    editor.setShowPrintMargin(false);
    editor.setOptions({
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: true,
        fontSize: "12pt",
    });
}

function changeLanguage() {
    let language = $("#languages").val();

    if(language === 'cpp')
        editor.session.setMode("ace/mode/c_cpp");
    else if(language === 'java')
        editor.session.setMode("ace/mode/java");
}

function setDarkTheme() {
    darkMode = true;
    document.body.style.backgroundColor = "#414450";

    document.getElementById('control-panel').style.backgroundColor = "#3a3d4c";
    document.getElementById('header').style.backgroundColor = "#282a36";
    document.getElementById('header').style.color = "white";

    document.getElementById('output').style.color = "white";
    document.getElementById('tree').style.color = "white";

    document.getElementById('editor-tabs').style.backgroundColor = "#414450";

    const buttons = document.getElementsByClassName('btn');
    for (let i = 0; i < buttons.length; i++) {
        buttons[i].addEventListener("mouseenter", function (event) {
            event.target.style.backgroundColor = "#3f4152";
        }, false);
        buttons[i].addEventListener("mouseleave", function (event) {
            event.target.style.backgroundColor = "#292b38";
        }, false);
        buttons[i].style.backgroundColor = "#292b38";
        buttons[i].style.color = "white";
    }
}

function setLightTheme(){
    darkMode = false;
    document.body.style.backgroundColor = "#eeeeee";

    document.getElementById('control-panel').style.backgroundColor = "lightgray";

    document.getElementById('header').style.backgroundColor = "#a9a9a9";
    document.getElementById('header').style.color = "black";

    document.getElementById('output').style.color = "black";
    document.getElementById('tree').style.color = "black";

    document.getElementById('editor-tabs').style.backgroundColor = "#eeeeee";

    const buttons = document.getElementsByClassName('btn');
    for (let i = 0; i < buttons.length; i++) {
        buttons[i].addEventListener("mouseenter", function( event ) {
            event.target.style.backgroundColor = "#686868";
        }, false);
        buttons[i].addEventListener("mouseleave", function( event ) {
            event.target.style.backgroundColor = "#acacac";
        }, false);
        buttons[i].style.backgroundColor = "#acacac";
        buttons[i].style.color = "black";
    }
}

async function onFileLoad(event) {
    const file = event.target.files.item(0)
    document.getElementById("fileRes").innerText = await file.text();

    loadConfig();
}

async function onDirectoryLoad(){

    let params = document.getElementById("directoryid").innerText;
    if (cmd === "open")
        openDirectory(document.getElementById('directoryid').value, document.getElementById("languages").value);
    else if (cmd === "add")
        Add(params);
    else if (cmd === "commit")
        Commit(params);
    else if (cmd === "mvnExec")
        mvnExec(params);
    else if (cmd === "mvnTree")
        mvnTree(params);
    else if (cmd == "cppCompile")
        compileCpp(params);
    document.getElementById('directorybtn').style.visibility = 'hidden';
    document.getElementById('directoryid').style.visibility = 'hidden';
}

function loadConfig() {
    const fileContent = document.getElementById("fileRes").innerText;
    const fcJson = JSON.parse(fileContent);


    if (fcJson.hasOwnProperty("margin") && typeof fcJson.margin === "boolean")
        editor.setShowPrintMargin(fcJson.margin)

    if (fcJson.hasOwnProperty("editortheme") && typeof fcJson.editortheme === "string")
        editor.setTheme("ace/theme/" + fcJson.editortheme)

    if (fcJson.hasOwnProperty("idetheme") && typeof fcJson.idetheme === "string"){
        if (fcJson.idetheme.toLowerCase() === "dark")
            setDarkTheme();
        else
            setLightTheme();
    }

    if (fcJson.hasOwnProperty("editorwidth") && typeof fcJson.editorwidth === "string")
        document.getElementById('editor').style.width = fcJson.editorwidth;
    else
        document.getElementById('editor').style.width = "80%";

    if (fcJson.hasOwnProperty("editorheight") && typeof fcJson.editorheight === "string")
        document.getElementById('editor').style.height = fcJson.editorheight;
    else
        document.getElementById('editor').style.height = "600px";

    if (fcJson.hasOwnProperty("editorfontsize") && typeof fcJson.editorfontsize === "string")
        editor.setFontSize(fcJson.editorfontsize);

}

function openFileExplorer(){
    document.getElementById('fileid').click();
}

function openDirectoryExplorer(){
    document.getElementById("directoryid").placeholder = "Enter your project path"
    document.getElementById('directorybtn').style.visibility = 'visible';
    document.getElementById('directoryid').style.visibility = 'visible';
    cmd = "open";
}

function executeButton(state1, state2) {
    document.getElementById('anybtn').hidden = state1;
    document.getElementById('gitbtn').hidden = state1;
    document.getElementById('jvbtn').hidden = state1;
    document.getElementById('cppbtn').hidden = state1;
    document.getElementById('return').hidden = state1;

    document.getElementById('exec').hidden = state2;
}

function anyButton(state1, state2) {
    document.getElementById('anyCleanup').hidden = state1;
    document.getElementById('anyDist').hidden = state1;
    document.getElementById('anyReturn').hidden = state1;

    document.getElementById('return').hidden = state2;
    document.getElementById('anybtn').hidden = state2;
    document.getElementById('gitbtn').hidden = state2;
    document.getElementById('jvbtn').hidden = state2;
    document.getElementById('cppbtn').hidden = state2;
}

function gitButton(state1, state2) {
    document.getElementById('gitPull').hidden = state1;
    document.getElementById('gitAdd').hidden = state1;
    document.getElementById('gitCommit').hidden = state1;
    document.getElementById('gitPush').hidden = state1;
    document.getElementById('gitReturn').hidden = state1;

    document.getElementById('return').hidden = state2;
    document.getElementById('anybtn').hidden = state2;
    document.getElementById('gitbtn').hidden = state2;
    document.getElementById('jvbtn').hidden = state2;
    document.getElementById('cppbtn').hidden = state2;
}

function javaButton(state1, state2){
    document.getElementById('mvnCompile').hidden = state1;
    document.getElementById('mvnClean').hidden = state1;
    document.getElementById('mvnTest').hidden = state1;
    document.getElementById('mvnPackage').hidden = state1;
    document.getElementById('mvnInstall').hidden = state1;
    document.getElementById('mvnExec').hidden = state1;
    document.getElementById('mvnTree').hidden = state1;
    document.getElementById('javaReturn').hidden = state1;

    document.getElementById('return').hidden = state2;
    document.getElementById('anybtn').hidden = state2;
    document.getElementById('gitbtn').hidden = state2;
    document.getElementById('jvbtn').hidden = state2;
    document.getElementById('cppbtn').hidden = state2;
}

function cppButton(state1, state2) {
    document.getElementById('compile').hidden = state1;
    document.getElementById('cppReturn').hidden = state1;

    document.getElementById('return').hidden = state2;
    document.getElementById('anybtn').hidden = state2;
    document.getElementById('gitbtn').hidden = state2;
    document.getElementById('jvbtn').hidden = state2;
    document.getElementById('cppbtn').hidden = state2;
}

async function Cleanup(){
    document.getElementById("output").innerText = "execution...";
    let resp = await fetch('http://localhost:8082/any/cleanup', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function Dist(){
    document.getElementById("output").innerText = "execution...";
    let resp = await fetch('http://localhost:8082/any/dist', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function Add(params){
    document.getElementById("output").innerText = "execution...";
    //find a way to take params from user input

    let resp = await fetch('http://localhost:8082/git/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({"params": params})
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function Commit(params){
    document.getElementById("output").innerText = "execution...";
    //find a way to take params from user input

    let resp = await fetch('http://localhost:8082/git/commit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({"params": params})
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function Push(){
    document.getElementById("output").innerText = "execution...";
    let resp = await fetch('http://localhost:8082/git/push', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });
    document.getElementById("output").innerText = body['Output'];
}

async function Pull(){
    document.getElementById("output").innerText = "execution...";
    let resp = await fetch('http://localhost:8082/git/pull', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function mvnCompile(){
    document.getElementById("output").innerText = "execution...";
    let resp = await fetch('http://localhost:8082/mvn/compile', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function mvnClean(){
    document.getElementById("output").innerText = "execution...";
    let resp = await fetch('http://localhost:8082/mvn/clean', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function mvnTest(){
    document.getElementById("output").innerText = "execution...";
    let resp = await fetch('http://localhost:8082/mvn/test', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function mvnPackage(){
    document.getElementById("output").innerText = "execution...";
    let resp = await fetch('http://localhost:8082/mvn/package', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function mvnInstall(){
    document.getElementById("output").innerText = "execution...";
    let resp = await fetch('http://localhost:8082/mvnInstall', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function mvnExec(params){

    document.getElementById("output").innerText = "execution...";
    //find a way to take params from user input

    let resp = await fetch('http://localhost:8082/mvn/exec', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({"params": params})
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}

async function mvnTree(params){
    document.getElementById("output").innerText = "execution...";
    //find a way to take params from user input

    let resp = await fetch('http://localhost:8082/mvn/tree', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({"params": params})
    });
    let body = await resp.json();
    document.getElementById("output").innerText = body["Output"];
}

async function openDirectory(path, language){
    document.getElementById("output").innerText = "File Explorer...";
    let resp = await fetch('http://localhost:8082/openProject', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({"Path": path, "language": language})
    });

    let body = await resp.json();
    projectPath = body['Path'];
    document.getElementById("tree").innerText = body['Tree'];
    document.getElementById('output').innerText = body['Output'];

    if (body['Language'] === "java"){
        document.getElementById('languages').value = "Java";
    }

    document.getElementById('loadfile').hidden = false;
    document.getElementById('saveFiles').hidden = false;
    document.getElementById('openProj').hidden = true;
    document.getElementById('exec').hidden = false;
}

function determineLanguage(file){
    let selectorValue = "none";
    let language = "";

    if (file.includes(".cpp")) {
        selectorValue = "cpp";
        language = "ace/mode/c_cpp";
    }
    else if (file.includes(".java")) {
        selectorValue = "java";
        language = "ace/mode/java";
    }

    editor.session.setMode(language);
    document.getElementById('languages').value = selectorValue;
}

function focusOnTab(button){
    if (currentFile !== null){
        fileList.forEach(f => {
            if (f.Path === currentFile)
                f.Content = editor.getValue();
        })
    }

    currentFile = button.id.toString();

    let tabsButtons = document.getElementsByClassName("tabs-buttons");
    for (let i = 0; i < tabsButtons.length; i++){
        tabsButtons[i].style.backgroundColor = "white";
        tabsButtons[i].style.color = "black";
    }

    if (darkMode){
        button.style.backgroundColor = "#282a36";
        button.style.color = "white";
    }
    else {
        button.style.backgroundColor = "#a9a9a9";
        button.style.color = "black";
    }

    determineLanguage(button.id.toString());
}

function editorTabsClick(button){
    if (toClose) {
        if (currentFile === button.id.toString())
            editor.setValue("");
        else if (currentFile !== null) {
            fileList.forEach(f => {
                if (f.Path === currentFile)
                    editor.getValue(f.Content);
            })
        }
        toClose = false;
    }
    else{
        focusOnTab(button);

        fileList.forEach(file => {
            if (file.Path === button.id.toString())
                editor.setValue(file.Content);
        });
    }
}

function createButton(body){
    let button = document.createElement("BUTTON");
    button.id = body['Path'];
    button.className += "tabs-buttons";

    button.innerHTML = body['Path'];
    focusOnTab(button);
    button.onclick = () => editorTabsClick(button);

    let close = document.createElement("BUTTON");
    close.id = "close" + nbTabs;
    nbTabs++;
    close.innerHTML = "&#x2715";
    close.style.marginLeft = "10px";
    close.className += "tabs-buttons";
    close.onclick = () => {
        button.hidden = true;
        toClose = true;
    };

    document.getElementById('editor-tabs').appendChild(button);
    button.appendChild(close);
}

async function loadFile(){
// add the button to the div
    document.getElementById("output").innerText = "Load File...";
    let resp = await fetch('http://localhost:8082/loadFile', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });

    let body = await resp.json();
    document.getElementById('output').innerText = body['Path'] + " loaded !";

    let newFile = { "Path": body['Path'], "Content": body['FileContent'] };

    if (fileList.length === 0){
        fileList.push(newFile);
        createButton(body);
    }
    else {
        let exists = false;
        fileList.forEach(f => {
            if (f.Path === newFile.Path)
                exists = true;
        });

        if (exists) {
            let tabsButtons = document.getElementsByClassName("tabs-buttons");
            for (let i = 0; i < tabsButtons.length; i++) {
                if (tabsButtons[i].id === newFile.Path) {
                    tabsButtons[i].hidden = false;
                    editorTabsClick(tabsButtons[i]);
                }
            }
        }
        else {
            fileList.push(newFile);
            createButton(body);
        }
    }

    editor.setValue(body['FileContent']);
    determineLanguage(newFile.Path);
}

async function saveFiles(){
    document.getElementById("output").innerText = "Saving Files...";

    let promises = [];

    fileList.forEach(f => {
        if (f.Path === currentFile.toString())
            f.Content = editor.getValue();
        promises.push(saveFile(f));
    })

    await Promise.all(promises);

    document.getElementById('output').innerText = "Files Saved !";

}

async function saveFile(file){
    console.log(file.Content)
    await fetch('http://localhost:8082/saveFiles', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },

        body: JSON.stringify(file)
    });
}

async function compileCpp(params){
    let resp = await fetch('http://localhost:8082/cpp/compile', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },

        body: JSON.stringify({"file": {"Path": currentFile, "Content": editor.getValue()}, "params": params})
    });

    let body = await resp.json();
    document.getElementById("output").innerText = body['Output'];
}


function changeCmd(command){
    cmd = command;
    document.getElementById('directorybtn').style.visibility = 'visible';
    document.getElementById('directoryid').style.visibility = 'visible';

    if (cmd !== "open")
        document.getElementById("directoryid").placeholder = "Enter your command parameters/tags";
    else
        document.getElementById("directoryid").placeholder = "Enter your project path";
}

async function loadIDE(){

    document.getElementById("output").innerText = "Loading Ping Ide...";
    let loaded = false;
    while (!loaded){
        try {
            let resp = await fetch('http://localhost:8082/check', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                },
            });
            if (resp.ok)
                loaded = true;
        } catch (e) {

        }
    }
    document.getElementById("output").innerText = "Ping IDE Loaded and ready !";
}

loadIDE();
