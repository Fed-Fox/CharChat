class ChatCache {
    constructor(maxMessagesPerChat = 100) {
        this.messages = new Map();
        this.chats = new Map();
        this.maxMessages = maxMessagesPerChat;
    }

    addMessage(chatId, message) {
        if (!this.messages.has(chatId)) {
            this.messages.set(chatId, []);
        }

        const messages = this.messages.get(chatId);
        messages.push(message);

        if (messages.length > this.maxMessages) {
            messages.shift();
        }
    }

    loadMessages(chatId, list) {
        this.messages.set(chatId, list);
    }

    getLength() {
        return this.messages.size;
    }

    getChatData(chatId) {
        return this.chats.get(chatId);
    }

    getMessagesLength(chatId) {
        return this.messages.get(chatId).size;
    }

    createChat(chatId, chat) {
        this.messages.set(chatId, []);
        this.chats.set(chatId, chat);
    }

    getMessages(chatId) {
        return this.messages.get(chatId) || [];
    }
}



let chats = new ChatCache(100);
let stompClient = null;
let me = null;
let openChatId = -1;


let themeColor = "#8400ff";


let menuBgElement = document.getElementById("menu-bg");
let chatsElement = document.getElementById("chats");
let messagesElement = document.getElementById("messages");
let activeChatElement = document.getElementById("active-chat");
let chatDataElement = document.getElementById("chat-data");
let inputElement = document.getElementById("input");