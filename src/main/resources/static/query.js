import wsManager from './websocket.js';

function toggleLogs(container) {
    const logsContent = container.querySelector('.logs-content');
    const toggleButton = container.querySelector('.logs-toggle');

    if (logsContent) {
        if (logsContent.style.display === 'none' || logsContent.style.display === '') {
            document.querySelectorAll('.event-container').forEach(function(el) {
                if (el !== container) {
                    const otherLogsContent = el.querySelector('.logs-content');
                    const otherToggleButton = el.querySelector('.logs-toggle');
                    if (otherLogsContent) {
                        otherLogsContent.style.opacity = 0;
                        setTimeout(() => {
                            otherLogsContent.style.display = 'none';
                            otherToggleButton.textContent = '▶';
                        }, 300);
                    }
                }
            });

            logsContent.style.display = 'block';
            logsContent.style.opacity = 0;
            logsContent.style.transition = 'opacity 0.3s ease';
            setTimeout(() => {
                logsContent.style.opacity = 1;
            }, 0);
            toggleButton.textContent = '▼';
        } else {
            logsContent.style.opacity = 0;
            setTimeout(() => {
                logsContent.style.display = 'none';
                toggleButton.textContent = '▶';
            }, 300);
        }
    } else {
        console.warn('Logs content not found for container:', container);
    }
}


document.addEventListener("DOMContentLoaded", function() {
    const searchForm = document.getElementById('searchForm');
    const searchInput = document.getElementById('searchInput');
    const events = document.getElementById('events');

    searchForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const searchValue = searchInput.value.trim();
        if (searchValue === '') {
            alert('검색어를 입력하세요.');
            return;
        }

        try {
            const response = await fetch('/api/opensearch/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ query: searchValue })
            });

            if (!response.ok) {
                throw new Error('Error fetching data from server');
            }

            const list = await response.json();
            console.log(list);

            const listItem = document.createElement('li');
            listItem.textContent = `검색 결과: ${searchValue}`;

            if (list && list.length > 0) {
                list.forEach(item => {
                    const eventHTML = createEventHTML();
                    const wrapper = document.createElement('div');
                    wrapper.innerHTML = eventHTML;
                    events.appendChild(wrapper.firstChild);
                });
            } else {
                const noResultItem = document.createElement('li');
                noResultItem.textContent = '검색 결과가 없습니다.';
                events.appendChild(noResultItem);
            }

        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred while processing your request');
        }

        searchInput.value = '';
    });

    const allDropdown = document.querySelectorAll('#sidebar .side-dropdown');
    const sidebar = document.getElementById('sidebar');

    allDropdown.forEach(item => {
        const a = item.parentElement.querySelector('a:first-child');
        a.addEventListener('click', function(e) {
            e.preventDefault();

            if (!this.classList.contains('active')) {
                allDropdown.forEach(i => {
                    const aLink = i.parentElement.querySelector('a:first-child');

                    aLink.classList.remove('active');
                    i.classList.remove('show');
                });
            }

            this.classList.toggle('active');
            item.classList.toggle('show');
        });
    });

    const toggleSidebar = document.querySelector('nav .material-symbols-outlined[data-icon="menu"]');
    const allSideDivider = document.querySelectorAll('#sidebar .divider');

    const updateSidebar = () => {
        if (sidebar.classList.contains('hide')) {
            allSideDivider.forEach(item => {
                item.textContent = '-';
            });

            allDropdown.forEach(item => {
                const a = item.parentElement.querySelector('a:first-child');
                a.classList.remove('active');
                item.classList.remove('show');
            });
        } else {
            allSideDivider.forEach(item => {
                item.textContent = item.dataset.text;
            });
        }
    };

    updateSidebar();

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


    function escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatJsonToHtml(jsonString) {
        return escapeHtml(jsonString)
            .replace(/\n/g, "<br>")
            .replace(/ /g, "&nbsp;");
    }

    function formatDate(timestampArray) {
        if (Array.isArray(timestampArray) && timestampArray.length >= 6) {
            const [year, month, day, hour, minute, second, millisecond] = timestampArray;
            return new Date(year, month - 1, day, hour, minute, second, millisecond / 1000000).toISOString();
        }
        return null;
    }


    function parseAndFormatLogs(logsString) {
        try {
            let logsArray = JSON.parse(logsString);
            if (Array.isArray(logsArray)) {
                logsString = logsArray[0];
            }

            const unescapedString = logsString
                .replace(/\\\"/g, '"')
                .replace(/\\"/g, '"');

            const parsedLogs = JSON.parse(unescapedString);

            if (parsedLogs.timestamp && Array.isArray(parsedLogs.timestamp)) {
                parsedLogs.timestamp = formatDate(parsedLogs.timestamp);
            }

            const formattedLogs = JSON.stringify(parsedLogs, null, 2);

            return formatJsonToHtml(formattedLogs);
        } catch (error) {
            console.error('Error parsing logs:', error);
            return 'Invalid logs format: ' + escapeHtml(logsString);
        }
    }




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


    function createEventHTML(event) {
        const logsContent = event.logs ? parseAndFormatLogs(event.logs) : 'No logs available';

        return `
        <div class="event-container" onclick="toggleLogs(this)">
            <div class="event-header">
                <strong>index:</strong> ${event.id || 'N/A'} <br>
                <strong>name:</strong> ${event.eventName || 'N/A'} <br>
                <strong>type:</strong> ${event.eventType || 'N/A'} <br>
                <strong>timestamp:</strong> ${event.timestamp} <br>
                <span class="logs-toggle" style="cursor: pointer;" onclick="toggleLogs(this)">▶</span>
            </div>
            <pre class="logs-content" style="display: none;">${logsContent}</pre>
        </div>
    `;
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
