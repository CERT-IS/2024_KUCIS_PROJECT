let lastWAFEventTimestamp = new Date(0);
let lastWAFEventOffset = 0;
const MAX_EVENTS_DISPLAYED = 100;

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


function searchFunction() {
    let input = document.querySelector('.search-input').value.toUpperCase();
    let category = document.getElementById('search-category').value;
    let eventContainers = document.querySelectorAll('.event-container');

    eventContainers.forEach(container => {
        let searchValue = '';

        if (category === 'id') {
            searchValue = container.querySelector('.event-header strong:nth-of-type(1)').nextSibling.nodeValue.trim().toUpperCase();
        } else if (category === 'name') {
            searchValue = container.querySelector('.event-header strong:nth-of-type(2)').nextSibling.nodeValue.trim().toUpperCase();
        } else if (category === 'type') {
            searchValue = container.querySelector('.event-header strong:nth-of-type(3)').nextSibling.nodeValue.trim().toUpperCase();
        } else if (category === 'timestamp') {
            searchValue = container.querySelector('.event-header strong:nth-of-type(4)').nextSibling.nodeValue.trim().toUpperCase();
        }

        if (searchValue.indexOf(input) > -1) {
            container.style.display = "";
        } else {
            container.style.display = "none";
        }
    });
}




document.addEventListener("DOMContentLoaded", function() {
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


    const profile = document.querySelector('nav .profile');
    const imgProfile = profile.querySelector('img');
    const dropdownProfile = profile.querySelector('.profile-link');

    imgProfile.addEventListener('click', function() {
        dropdownProfile.classList.toggle('show');
    });



    const allMenu = document.querySelectorAll('main .head .menu');

    allMenu.forEach(item => {
        const icon = item.querySelector('.material-symbols-outlined[data-icon="more_horiz"]');
        const menuLink = item.querySelector('.menu-link');

        icon.addEventListener('click', function(event) {
            allMenu.forEach(menu => {
                const otherMenuLink = menu.querySelector('.menu-link');
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
        allMenu.forEach(item => {
            const menuLink = item.querySelector('.menu-link');
            menuLink.classList.remove('show');
        });
    });

    
    let isChatOpen = false;

    function toggleChat() {
        const chatModal = document.getElementById("chatModal");
        const chatButton = document.getElementById("chatButton");
        
        if (!isChatOpen) {
            chatModal.classList.add("open");
            chatButton.style.opacity = "0";
        } else {
            chatModal.classList.remove("open");
            chatButton.style.opacity = "1";
        }

        isChatOpen = !isChatOpen;
    }

    document.getElementById("chatButton").onclick = toggleChat;
    document.querySelector(".close").onclick = toggleChat;




    async function fetchEvents(url, elementId, lastTimestamp, offset = 0) {
        try {
            const eventsElement = document.getElementById(elementId);
            const currentEventCount = eventsElement.querySelectorAll('.event-container').length;

            if (currentEventCount >= MAX_EVENTS_DISPLAYED) {
                console.log(` ${elementId} fulled.`);
                return {lastTimestamp, offset};
            }

            const response = await fetch(`${url}?lastTimestamp=${lastTimestamp.toISOString()}&size=20&offset=${offset}`);
            const events = await response.json();

            if (events.length > 0) {
                const newTimestamp = new Date(events[events.length - 1].timestamp);
                console.log(`Length ${events.length} ,timestamp ${newTimestamp}`);

                if (newTimestamp.getTime() === lastTimestamp.getTime()) {
                    offset += 1;
                } else {
                    lastTimestamp = newTimestamp;
                    offset = 0;
                }

                let existingEventCount = eventsElement.querySelectorAll('.event-container').length;
                const totalEventCount = existingEventCount + events.length;

                console.log(`count : ${totalEventCount}`);
                const fragment = document.createDocumentFragment();
                events.forEach(event => {
                    if (existingEventCount < MAX_EVENTS_DISPLAYED) {
                        const eventLi = document.createElement('li');
                        console.log('event:', event);

                        eventLi.innerHTML = createEventHTML(event);
                        fragment.appendChild(eventLi);
                        existingEventCount++;
                    }
                });

                if (fragment.childNodes.length > 0) {
                    eventsElement.appendChild(fragment);
                }

                if (totalEventCount > MAX_EVENTS_DISPLAYED) {
                    const excessCount = totalEventCount - MAX_EVENTS_DISPLAYED;
                    const toRemove = Array.from(eventsElement.querySelectorAll('.event-container')).slice(0, excessCount);
                    toRemove.forEach(item => item.remove());
                }
            } else {
                console.log(`No events ${elementId}.`);
            }

            return {lastTimestamp, offset};
        } catch (error) {
            console.error(`Error ${elementId} `, error);
            return {lastTimestamp, offset};
        }
    }


    async function fetchWAFEvents() {
        const result = await fetchEvents('/detect/waf', 'waf-events', lastWAFEventTimestamp, lastWAFEventOffset);
        lastWAFEventTimestamp = result.lastTimestamp;
        lastWAFEventOffset = result.offset;
    }

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





    async function pollData() {
        try {
            await fetchWAFEvents();
        } catch (error) {
            console.error('Error in pollData:', error);
        } finally {
            setTimeout(pollData, 1000);
        }
    }

    pollData();
});