checkCookies();

function connect() {
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    fetch('/app/chats')
        .then(response => {
            if (!response.ok) {
                returnToAuth();
            }
            return response.json();
        })
        .then(data => {
            if (data.length === 0) {
                chatsElement.innerHTML = chatsElement.innerHTML + `<p class="no-chats" id="no-chats">У вас нет чатов, создайте новый чат, чтобы начать общение</p>`
            } else {
                data.forEach(chat => {
                    chatsElement.insertAdjacentHTML('beforeend', getChat(chat.name, chat.status, chat.id));
                    chats.createChat(chat.id, chat);
                    chats.loadMessages(chat.id, chat.messages);
                })
            }

            stompClient.subscribe(
                "/topic/user/" + me.wsid,
                onReceived
            );
        })
        .catch(error => {
            console.error('Fetch error:', error);
        });
}

function onError() {
    document.getElementById("load-error").style.display = "flex";
}

function onReceived(payload) {
    let data = JSON.parse(payload.body);

    if (data.error === undefined) {
        switch (data.type) {
            case "INFO":
                if (openChatId === data.chatId) {
                    messagesElement.insertAdjacentHTML('beforeend', getMessageInfo(data.content));
                }
                chats.addMessage(data.chatId, data);
                break;
            case "MESSAGE":
                if (openChatId === data.chatId) {
                    messagesElement.insertAdjacentHTML('beforeend', getMessage(data.time, data.sender, data.content));
                }
                chats.addMessage(data.chatId, data);
                break;
            case "NEW_CHAT":
                document.getElementById("no-chats").style.display = "none";
                chatsElement.insertAdjacentHTML('beforeend', getChat(data.name, data.status, data.id));
                chats.createChat(data.id, data)
                break;
        }
    } else {
        alert(data.message)
    }
}

function checkCookies() {
    let cookie = getCookie("session");

    if (cookie !== undefined) {
        fetch('/app/validate-cookies/')
            .then(response => {
                if (!response.ok) {
                    returnToAuth();
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (!data.validate) {
                    returnToAuth();
                } else {
                    me = data.user;
                    connect();
                }
            }).catch(error => {
                console.error('Fetch error:', error);
                returnToAuth();
            }
        );
    } else {
        returnToAuth()
    }
}

function returnToAuth() {
    window.location.href = "/login";
    deleteCookie("session");
}



function selectChat(id) {
    if (openChatId !== id) {
        let chatData = chats.getChatData(id);

        openChatId = id;

        activeChatElement.style.display = "flex";

        if (window.screen.width <= 730) {
            chatsElement.style.display = "none";
        }

        document.querySelectorAll(".chat").forEach(chat => {
            if (parseInt(chat.getAttribute("chat-id")) === id) {
                chat.classList.add('active');
            } else {
                chat.classList.remove('active')
            }
        });

        chatDataElement.innerHTML = `
            <h2 onclick="closeChat()"><</h2>
            <div onclick="openOtherProfile()">
                <container>
                    <span>[</span><h1>${chatData.name}</h1><span>]</span>
                </container>
                <p>${chatData.status}</p>
            </div>
        `;

        messagesElement.innerHTML = "";

        if (chats.getMessagesLength(id) === 0) {
            messagesElement.insertAdjacentHTML('beforeend', getMessageInfo("Этот чат пуст! Напишите что-то чтобы начать переписку!"));
        } else {
            chats.getMessages(id).forEach(element => {

                if (element.type === "INFO") {
                    messagesElement.insertAdjacentHTML('beforeend', `
                            <p class="show-delay info-message"><span>---</span> ${element[1]} <span>---</span></p>
                        `);
                } else if (element.type === "MESSAGE") {
                    messagesElement.insertAdjacentHTML('beforeend', `
                            <p class="show-delay message"><slct>></slct> <time>${element.time}</time> <lb>[</lb><user>${element.sender}</user><lb>]</lb> <bp>></bp> ${element.content}</p>
                        `);
                }
            });
        }

        let items = document.querySelectorAll(".show-delay");

        items.forEach((item, index) => {
            setTimeout(() => {
                item.classList.add('show');
                item.scrollIntoView({ block: "end", behavior: "instant" });
            }, index * 10);
        });
    }
}

function createChat() {
    let tagElement = document.getElementById("tag");
    let value = tagElement.value;
    let errorElem = document.createElement("error");

    if (value.length >= 5 && !/[\d<>/'"$%\[\]#()^&?!=+№;:*`~|\\]/.test(value)) {
        fetch('/app/chat/create/' + value)
            .then(response => {
                return response.json().then(data => ({
                    ok: response.ok,
                    data: data
                }));
            })
            .then(({ ok, data }) => {
                if (!ok) {
                    errorElem.textContent = data.message;
                }
            }).catch(error => {
            console.error('Fetch error:', error);
        });
    }
}

function closeChat() {
    activeChatElement.style.display = "none";
    chatsElement.style.display = "flex";

    openChatId = -1;
}

function sendMessage() {
    let inputElement = document.getElementById("input");

    let time = new Date();

    if (inputElement.value.trim() === "") return;

    if (inputElement.value.length > 500) return;

    if (/[\d<>'"]/.test(inputElement.value)) return;

    messagesElement.scrollTop += 400;

    let chatMessage = {
        userId: me.id,
        chatId: openChatId,
        content: inputElement.value,
        time: time
    };

    stompClient.send(`/app/chat/send-message`, {}, JSON.stringify(chatMessage));

    inputElement.value = "";

    inputElement.focus();
}



document.addEventListener( "keydown", event => {
    if(event.code === "Enter") {
        if (document.activeElement === inputElement) {
            if (openChatId !== -1) {
                if (!/[\d<>'"]/.test(inputElement.value)) {
                    event.preventDefault();
                    sendMessage();
                }
            }
        } else {
            inputElement.focus()
        }
    } else if (event.code === "Escape") {
        closeMenu()
    } else if (event.altKey === true && event.code === "KeyP") {
        if (openChatId !== -1) {
            openOtherProfile()
        }
    } else if (event.altKey === true && event.code === "KeyS") {
        openSettings()
    } else if (event.altKey === true && event.code === "KeyM") {
        openMyProfile()
    } else if (event.altKey === true && event.code === "KeyC") {
        openCreateChat()
    }
});



function getChat(name, status, id) {
    return `
        <div class="chat"  onclick="selectChat('${id}\')">
            <div>
                <span>[</span><h1>${name}</h1><span>]</span>
            </div>
            <p>${status}</p>
        </div>
    `;
}

function getMessage(time, sender, content) {
    return `
        <p class="message"><slct>></slct> <time>${time}</time> <lb>[</lb><user>${sender}</user><lb>]</lb> <bp>></bp> ${content}</p>
    `;
}

function getMessageInfo(content) {
    return `
        <p class="info-message"><span>---</span> ${content} <span>---</span></p>
    `;
}