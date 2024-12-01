const allSideDivider = document.querySelectorAll('#sidebar .divider');
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

export function toggleMenu(selector, toggleClass = 'show') {
    const allMenus = document.querySelectorAll(selector);
    allMenus.forEach(item => {
        const icon = item.querySelector('.material-symbols-outlined[data-icon="more_horiz"]');
        const menuLink = item.querySelector('.menu-link');
        icon.addEventListener('click', (event) => {
            allMenus.forEach(menu => {
                const otherMenuLink = menu.querySelector('.menu-link');
                if (otherMenuLink !== menuLink) {
                    otherMenuLink.classList.remove(toggleClass);
                }
            });
            menuLink.classList.toggle(toggleClass);
            event.stopPropagation();
        });
    });

    window.addEventListener('click', () => {
        allMenus.forEach(item => {
            const menuLink = item.querySelector('.menu-link');
            menuLink.classList.remove(toggleClass);
        });
    });
}

export function setProgressBar(element, value) {
    element.style.setProperty('--value', `${value}%`);
}

export function formatChatDate(date) {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const weekday = date.toLocaleString('en-En', { weekday: 'short' });
    return `${month}-${day} (${weekday})`;
}

export function getFormattedTime() {
    return new Date().toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    });
}

export const updateSidebar = () => {
    const isSidebarHidden = sidebar.classList.contains('hide');

    if (isSidebarHidden) {
        allSideDivider.forEach(divider => {
            divider.textContent = '-';
        });

        allDropdown.forEach(dropdown => {
            const anchor = dropdown.closest('li').querySelector('a:first-child');
            if (anchor) {
                anchor.classList.remove('active');
            }
            dropdown.classList.remove('show');
        });
    } else {
        allSideDivider.forEach(divider => {
            divider.textContent = divider.dataset.text;
        });
    }
};

export function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

export function formatJsonToHtml(jsonString) {
    return escapeHtml(jsonString)
        .replace(/\n/g, "<br>")
        .replace(/ /g, "&nbsp;");
}

export function formatDate(timestampArray) {
    if (Array.isArray(timestampArray) && timestampArray.length >= 6) {
        const [year, month, day, hour, minute, second, millisecond] = timestampArray;
        return new Date(year, month - 1, day, hour, minute, second, millisecond / 1000000).toISOString();
    }
    return null;
}

export function parseAndFormatLogs(logsString) {
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
        return formattedLogs;
    } catch (error) {
        console.error('Error parsing logs:', error);
        return 'Invalid logs format: ' + escapeHtml(logsString);
    }
}

export function createEventHTML(event) {
    const logsContent = event.logs ? parseAndFormatLogs(event.logs) : 'No logs available';

    const eventContainer = document.createElement('div');
    eventContainer.classList.add('event-container');

    const eventHeader = `
        <div class="event-header">
            <strong>index:</strong> ${event.id || 'N/A'} <br>
            <strong>name:</strong> ${event.eventName || 'N/A'} <br>
            <strong>type:</strong> ${event.eventType || 'N/A'} <br>
            <strong>timestamp:</strong> ${event.timestamp} <br>
            <span class="logs-toggle" style="cursor: pointer;">▶</span>
        </div>
    `;

    eventContainer.innerHTML = eventHeader;

    const logsContentElement = document.createElement('pre');
    logsContentElement.classList.add('logs-content');
    logsContentElement.style.display = 'none';
    logsContentElement.textContent = logsContent;

    eventContainer.appendChild(logsContentElement);

    eventContainer.addEventListener('click', function () {
        toggleLogs(eventContainer);
    });

    return eventContainer;
}

export function toggleLogs(container) {
    const logsContent = container.querySelector('.logs-content');
    const toggleButton = container.querySelector('.logs-toggle');

    if (logsContent && toggleButton) {
        const isVisible = logsContent.style.display === 'block';

        logsContent.style.display = isVisible ? 'none' : 'block';
        toggleButton.textContent = isVisible ? '▶' : '▼';

        if (!isVisible) {
            document.querySelectorAll('.event-container').forEach(el => {
                if (el !== container) {
                    const otherLogsContent = el.querySelector('.logs-content');
                    const otherToggleButton = el.querySelector('.logs-toggle');
                    if (otherLogsContent && otherToggleButton) {
                        otherLogsContent.style.display = 'none';
                        otherToggleButton.textContent = '▶';
                    }
                }
            });
        }

    } else {
        console.warn('Logs content 또는 toggle button을 찾을 수 없습니다:', container);
    }
}
