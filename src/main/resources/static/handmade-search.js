import wsManager from './websocket.js';

export function sendMessage(event) {
    event.preventDefault();

    const inputField = document.getElementById('userInput');
    const chatBox = document.getElementById('chatBox');

    const userMessage = inputField.value;
    if (!userMessage.trim()) {
        console.warn('입력값이 비어있습니다!');
        return;
    }

    const userMsgDiv = document.createElement('div');
    userMsgDiv.className = 'msg me';
    userMsgDiv.innerHTML = `
        <div class="chat">
            <div class="profile">
                <span class="time">${new Date().toLocaleTimeString([], {hour: '2-digit', minute: '2-digit', hour12: false})}</span>
            </div>
            <p>${userMessage}</p>
        </div>
    `;

    chatBox.appendChild(userMsgDiv);
    inputField.value = '';
    chatBox.scrollTop = chatBox.scrollHeight;

    const request = {
        action: "getChatMessage",
        message: userMessage
    };
    if (wsManager.isConnected) {
        wsManager.send(request);
    }
}

document.getElementById('chatForm').addEventListener('submit', function(event) {
    sendMessage(event);
});

function formatDate(date) {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const weekday = date.toLocaleString('en-En', { weekday: 'short' });
    return `${month}-${day} (${weekday})`;
}

const today = new Date();

document.getElementById('date').textContent = formatDate(today);



document.addEventListener("DOMContentLoaded", function() {

wsManager.connect();

wsManager.onMessage((event) => {
    const message = JSON.parse(event.data);

    switch (message.action) {
        case "askResponse":
            handleAskResponse(message.data);
            break;

        default:
            console.warn("Unknown action:", message.action);
    }

});

function escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
function handleAskResponse(data) {
    const messagesContainer = document.getElementById('chatBox');
    if (!messagesContainer) {
        console.error("Chat messages container not found.");
        return;
    }

    const now = new Date();
    const formattedTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

    const msgElement = document.createElement('div');
    msgElement.classList.add('msg');

    msgElement.innerHTML = `
            <img src="https://images.unsplash.com/photo-1534723328310-e82dad3ee43f?q=80&w=2536&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D" alt="">
            <div class="chat">
                <div class="profile">
                    <span class="username">SIMEple Bot</span>
                    <span class="time">${formattedTime}</span>
                </div>
                <p>${escapeHtml(data)}</p>
            </div>
        `;

    messagesContainer.appendChild(msgElement);

    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

});