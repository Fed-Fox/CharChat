let canLoad = false;
let loaded = false;
let savedScripts = new Map();
const sleep = ms => new Promise(res => setTimeout(res, ms));
let allEvents = [
    [
        "Получение сообщения",
        "event-message-recieved"
    ],
    [
        "Отправка сообщения",
        "event-message-send"
    ],
    [
        "Создание чата",
        "event-chat-create"
    ],
    [
        "Нажатие на чат",
        "event-chat-open"
    ]
]



function closeScripts() {
    document.getElementById("scripts-bg").style.display = "none";
}

function openScripts() {
    document.getElementById("scripts-bg").style.display = "flex";

    let scriptElement = document.getElementById("scripts");

    if (!loaded) {
        let allEventsTags = []

        allEvents.forEach((event) => {
            scriptElement.insertAdjacentHTML('beforeend', getEvent(event[0], event[1]))
            allEventsTags.push(event[1])
        })

        let scriptsData = JSON.parse(localStorage.getItem("scripts"));

        if (scriptElement !== null) {
            scriptsData.forEach((data) => {
                if (allEventsTags.includes(data.id)) {
                    document.getElementById(data.id).textContent = data.data;
                }
            })
        }

        loaded = true;
    }

    closeMenu();
}



async function script(element) {
    let script = new Scripts()

    script.loadScripts(savedScripts.get(element) || "")

    if (script.validate()) {
        scriptError("")

        for (let i = 0; i < script.getLength(); i++) {
            let data = script.getData(i);

            try {
                switch (data[0]) {
                    case "setCss":
                        if (data[1].includes("&")) {
                            let elemProg = data[1].split("&");

                            document.querySelectorAll(elemProg[0]).forEach(element => {
                                let felem = element;

                                for (let childId = 1 ; childId < elemProg.length; childId += 1) {
                                    felem = felem.querySelector(elemProg[childId]);
                                }

                                felem.setAttribute("style", "%style%: %value% !important".replace("%style%", data[2]).replace("%value%", data[3]));
                            });
                        } else {
                            document.querySelectorAll(data[1]).forEach(element => {
                                element.setAttribute("style", "%style%: %value% !important".replace("%style%", data[2]).replace("%value%", data[3]));
                            });
                        }
                        break;
                    case "sleep":
                        await sleep(data[1]);
                        break;
                }
            } catch (e) {
                scriptError("Ошибка выполенения")
            }
        }
    }

    script.clearErrors();
}

function saveScript(scriptId) {
    let inputElement = document.getElementById(scriptId);

    let script = new Scripts()

    script.loadScripts(inputElement.value);

    if (script.validate()) {
        scriptError("")
        savedScripts.set(scriptId, inputElement.value);
        this.script(scriptId);
        document.getElementById("script-succses").textContent = "Сохранено";

        let scriptData = JSON.parse(localStorage.getItem("scripts"));

        if (scriptData !== null) {
            for (let i = 0; i < scriptData.length; i++) {
                if (scriptData[i].id === scriptId) {
                    scriptData[i].data = inputElement.value;

                    localStorage.setItem("scripts", JSON.stringify(scriptData));
                    return;
                }
            }

            scriptData.push(
                {
                    id: scriptId,
                    data: inputElement.value
                }
            );

            localStorage.setItem("scripts", JSON.stringify(scriptData));
        } else {
            scriptData = [
                {
                    id: scriptId,
                    data: inputElement.value
                }
            ]

            localStorage.setItem("scripts", JSON.stringify(scriptData));
        }
    }
}

function scriptError(error) {
    canLoad = false;
    document.getElementById("script-error").textContent = error;
}



function getEvent(name, id) {
    return `
        <div>
            <h1>${name}</h1>
            <textarea id="${id}"></textarea>
            <p onclick="saveScript('${id}')"><span>[</span>Сохранить и исполнить<span>]</span></p>
        </div>
    `
}



class Scripts {
    constructor() {
        this.map= [];
        this.errors = 0;
    }

    addScript(script) {
        if (/^setCss\("[a-zA-Z0-9-_&.#]*","[a-zA-Z0-9-_]*","[a-zA-Z0-9#-_]*"\)$/.test(script)) {
            let args = script.replace("setCss(", "").replace(")", "").split(",");

            this.map.push(["setCss", args[0].replaceAll("\"", ""), args[1].replaceAll("\"", ""), args[2].replaceAll("\"", "")])
        } else if (/^sleep\([0-9]*\)$/.test(script)) {
            let args = script.replace("sleep(", "").replace(")", "");

            this.map.push(["sleep", args])
        } else {
            scriptError("Ошибка синтаксиса")

            this.errors++;
        }
    }

    loadScripts(scripts) {
        scripts.split("\n").forEach(script => {
            this.addScript(script);
        });
    }

    validate() {
        return this.errors === 0;
    }

    clearErrors() {
        this.errors = 0;
    }

    getLength() {
        return this.map.length;
    }

    getData(index) {
        return this.map[index];
    }
}