import wsManager from "./websocket.js";
import {toggleMenu, getFormattedTime, updateSidebar, escapeHtml, formatChatDate} from "./utils.js";

const chatMessages = [];
let isChatOpen = false;

document.getElementById("chatButton").onclick = toggleChat;
document.querySelector(".close").onclick = toggleChat;

function toggleChat() {
    const chatModal = document.getElementById("chatModal");
    const chatButton = document.getElementById("chatButton");
    const notificationBadge = document.getElementById("notificationBadge");

    if (!isChatOpen) {
        chatModal.classList.add("open");
        chatButton.style.opacity = "0";
        notificationBadge.style.display = "none";
    } else {
        chatModal.classList.remove("open");
        chatButton.style.opacity = "1";
    }

    isChatOpen = !isChatOpen;
}

function sendMessage(event) {
    event.preventDefault();

    const inputField = isChatOpen ? document.getElementById('userInputModal') : document.getElementById('userInput');
    const userMessage = inputField.value.trim();

    if (userMessage !== '') {
        const formattedTime = getFormattedTime();

        const message = {
            type: 'user',
            text: userMessage,
            time: formattedTime
        };

        chatMessages.push(message);
        updateChatBoxes();

        inputField.value = '';

        const request = {
            action: "getChatMessage",
            message: userMessage
        };
        wsManager.send(request);
    }
}

function updateChatBoxes() {
    const chatBoxMain = document.getElementById('chatBoxMain');
    const chatBoxModal = document.getElementById('chatBoxModal');

    chatBoxMain.innerHTML = '';
    chatBoxModal.innerHTML = '';

    const date = formatChatDate(new Date());
    chatBoxMain.innerHTML += `<p class="day"><span>${date}</span></p>`;
    chatBoxModal.innerHTML += `<p class="day"><span>${date}</span></p>`;

    chatMessages.forEach(message => {
        appendMessage(chatBoxMain, message);
        appendMessage(chatBoxModal, message);
    });
}

function appendMessage(chatBox, message) {
    const msgDiv = document.createElement('div');
    msgDiv.className = `msg ${message.type === 'user' ? 'me' : ''}`;
    msgDiv.innerHTML = `
        <div class="chat">
            <div class="profile">
                <span class="time">${message.time}</span>
            </div>
            <p>${escapeHtml(message.text)}</p>
        </div>
    `;
    chatBox.appendChild(msgDiv);
    chatBox.scrollTop = chatBox.scrollHeight;
}

document.addEventListener("DOMContentLoaded", () => {
    const sidebar = document.getElementById('sidebar');
    const allSideDivider = document.querySelectorAll('#sidebar .divider');
    const allDropdown = document.querySelectorAll('#sidebar .side-dropdown');

    toggleMenu('main .head .menu');
    updateSidebar();

    const toggleSidebar = document.querySelector('nav .material-symbols-outlined[data-icon="menu"]');
    toggleSidebar.addEventListener('click', function() {
        sidebar.classList.toggle('hide');
        updateSidebar();
    });


    sidebar.addEventListener('mouseleave', function() {
        if (this.classList.contains('hide')) {
            allDropdown.forEach(item => {
                const a = item.parentElement.querySelector('a:first-child');
                a.classList.remove('active');
                item.classList.remove('show');
            });
            allSideDivider.forEach(item => {
                item.textContent = '-';
            });
        }
    });

    sidebar.addEventListener('mouseenter', function() {
        if (this.classList.contains('hide')) {
            allDropdown.forEach(item => {
                const a = item.parentElement.querySelector('a:first-child');
                a.classList.remove('active');
                item.classList.remove('show');
            });
            allSideDivider.forEach(item => {
                item.textContent = item.dataset.text;
            });
        }
    });

    const profile = document.querySelector('nav .profile');
    const imgProfile = profile.querySelector('img');
    const dropdownProfile = profile.querySelector('.profile-link');

    imgProfile.addEventListener('click', function() {
        dropdownProfile.classList.toggle('show');
    });

    const allMenu = document.querySelectorAll('main .content-data .head .menu');

    allMenu.forEach(item => {
        const icon = item.querySelector('.material-symbols-outlined[data-icon="more_horiz"]');
        const menuLink = item.querySelector('.menu-link');

        icon.addEventListener('click', function() {
            menuLink.classList.toggle('show');
        });
    });

    window.addEventListener('click', function(e) {
        if (e.target !== imgProfile && e.target !== dropdownProfile) {
            dropdownProfile.classList.remove('show');
        }

        allMenu.forEach(item => {
            const icon = item.querySelector('.material-symbols-outlined[data-icon="more_horiz"]');
            const menuLink = item.querySelector('.menu-link');

            if (e.target !== icon && e.target !== menuLink) {
                menuLink.classList.remove('show');
            }
        });
    });


    const allMenu2 = document.querySelectorAll('main .head .menu2');

    allMenu2.forEach(item => {
        const icon = item.querySelector('.material-symbols-outlined[data-icon="more_horiz"]');
        const menuLink = item.querySelector('.menu-link2');

        icon.addEventListener('click', function(event) {
            allMenu2.forEach(menu => {
                const otherMenuLink = menu.querySelector('.menu-link2');
                if (otherMenuLink !== menuLink) {
                    otherMenuLink.classList.remove('show');
                }
            });
            menuLink.classList.toggle('show');

            event.stopPropagation();
        });

        menuLink.addEventListener('click', function(event) {
            event.stopPropagation();
        });
    });

    window.addEventListener('click', function(e) {
        allMenu2.forEach(item => {
            const menuLink = item.querySelector('.menu-link2');
            menuLink.classList.remove('show');
        });
    });



    const chatFormMain = document.getElementById('chatFormMain');
    if (chatFormMain) {
        chatFormMain.addEventListener('submit', event => {
            sendMessage(event);
            console.log("main");
        });
    }

    const chatForm = document.getElementById('chatForm');
    if (chatForm) {
        chatForm.addEventListener('submit', event => {
            sendMessage(event);
            console.log("chatform");
        });
    }


    wsManager.connect();
    wsManager.onMessage((event) => {
        const message = JSON.parse(event.data);

        if (message.action === "askResponse") {
            onNewMessageReceived();

            const formattedTime = getFormattedTime();
            chatMessages.push({
                type: 'bot',
                text: message.data,
                time: formattedTime
            });
            updateChatBoxes();
        }
    });

    function showNotificationBadge() {
        const notificationBadge = document.getElementById("notificationBadge");
        if (!isChatOpen) {
            notificationBadge.style.display = "flex";
        }
    }

    function onNewMessageReceived() {
        showNotificationBadge();
    }
});
