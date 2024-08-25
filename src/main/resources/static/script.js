let lastEventTimestamp = new Date(0); // Epoch (1970-01-01T00:00:00Z)
let lastWAFEventTimestamp = new Date(0);
let lastEventOffset = 0;
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

    const allProgress = document.querySelectorAll('main .card .progress');

    allProgress.forEach(item => {
        item.style.setProperty('--value', item.dataset.value);
    });


    var spanElments = document.querySelectorAll('.progress');

    spanElments.forEach(function(spanElment) {
        var spanText = spanElment.textContent;
        spanElment.setAttribute('data-value', spanText);
    });


    let previousProcessors = 0;
    let previousLoadAverage = 0;
    let previousMemory = 0;
    let previousHeapMemory = 0;

    function updateDiskInfo(data) {
        const currentProcessors = data.availableProcessors;
        const currentLoadAverage = data.systemLoadAverage;
        const currentMemory = data.freeMemory;
        const currentHeapMemory = data.usedHeapMemory;

        const trendingDownOs = document.getElementById('trending-down-os');
        const trendingUpOs = document.getElementById('trending-up-os');
        const trendingFlatOs = document.getElementById('trending-flat-os');

        const trendingDownCpu = document.getElementById('trending-down-cpu');
        const trendingUpCpu = document.getElementById('trending-up-cpu');
        const trendingFlatCpu = document.getElementById('trending-flat-cpu');

        const trendingDownMemory = document.getElementById('trending-down-memory');
        const trendingUpMemory = document.getElementById('trending-up-memory');
        const trendingFlatMemory = document.getElementById('trending-flat-memory');

        const trendingDownDisk = document.getElementById('trending-down-disk');
        const trendingUpDisk = document.getElementById('trending-up-disk');
        const trendingFlatDisk = document.getElementById('trending-flat-disk');

        if (previousProcessors > currentProcessors) {
            trendingDownOs.classList.remove('hidden');
            trendingUpOs.classList.add('hidden');
            trendingFlatOs.classList.add('hidden');
        }
        else if (previousProcessors < currentProcessors) {
            trendingDownOs.classList.add('hidden');
            trendingUpOs.classList.remove('hidden');
            trendingFlatOs.classList.add('hidden');
        }
        else {
            trendingDownOs.classList.add('hidden');
            trendingUpOs.classList.add('hidden');
            trendingFlatOs.classList.remove('hidden');
        }

        if (previousLoadAverage > currentLoadAverage) {
            trendingDownCpu.classList.remove('hidden');
            trendingUpCpu.classList.add('hidden');
            trendingFlatCpu.classList.add('hidden');
        }
        else if (previousLoadAverage < currentLoadAverage) {
            trendingDownCpu.classList.add('hidden');
            trendingUpCpu.classList.remove('hidden');
            trendingFlatCpu.classList.add('hidden');
        }
        else {
            trendingDownCpu.classList.add('hidden');
            trendingUpCpu.classList.add('hidden');
            trendingFlatCpu.classList.remove('hidden');
        }

        if (previousMemory > currentMemory) {
            trendingDownMemory.classList.remove('hidden');
            trendingUpMemory.classList.add('hidden');
            trendingFlatMemory.classList.add('hidden');
        }
        else if (previousMemory < currentMemory) {
            trendingDownMemory.classList.add('hidden');
            trendingUpMemory.classList.remove('hidden');
            trendingFlatMemory.classList.add('hidden');
        }
        else {
            trendingDownMemory.classList.add('hidden');
            trendingUpMemory.classList.add('hidden');
            trendingFlatMemory.classList.remove('hidden');
        }

        if (previousHeapMemory > currentHeapMemory) {
            trendingDownDisk.classList.remove('hidden');
            trendingUpDisk.classList.add('hidden');
            trendingFlatDisk.classList.add('hidden');
        }
        else if (previousHeapMemory < currentHeapMemory) {
            trendingDownDisk.classList.add('hidden');
            trendingUpDisk.classList.remove('hidden');
            trendingFlatDisk.classList.add('hidden');
        }
        else {
            trendingDownDisk.classList.add('hidden');
            trendingUpDisk.classList.add('hidden');
            trendingFlatDisk.classList.remove('hidden');
        }

        previousProcessors = currentProcessors;
        previousLoadAverage = currentLoadAverage;
        previousMemory = currentMemory;
        previousHeapMemory = currentHeapMemory;
    }



    async function fetchSystemInfo() {
        try {
            const response = await fetch('/system/info');
            const data = await response.json();

            const osInfo = document.getElementById('os-info');
            const cpuInfo = document.getElementById('cpu-info');
            const memoryInfo = document.getElementById('memory-info');
            const memoryInfoFree = document.getElementById('memory-info-free');
            const memoryInfoTotal = document.getElementById('memory-info-total');
            const memoryInfoPercentage = document.getElementById('memory-info-percentage');
            const diskInfo = document.getElementById('disk-info');
            const diskInfoUsed = document.getElementById('disk-info-used');
            const diskInfoMax = document.getElementById('disk-info-max');
            const diskInfoPercentage = document.getElementById('disk-info-percentage');

            if (osInfo) osInfo.textContent = `Available Processors: ${data.availableProcessors}`;
            if (cpuInfo) cpuInfo.textContent = `실행 대기중인 Task의 평균 수: ${data.systemLoadAverage}`;
            if (memoryInfo) memoryInfo.textContent = `Free Memory: ${data.freeMemory} / ${data.totalMemory}`;
            if (memoryInfoFree) memoryInfoFree.textContent = `${data.freeMemory}`;
            if (memoryInfoTotal) memoryInfoTotal.textContent = `${data.totalMemory}`;
            if (memoryInfoPercentage) memoryInfoPercentage.textContent = `${data.freeMemoryPercentage}`;
            if (diskInfo) diskInfo.textContent = `Used Heap Memory: ${data.usedHeapMemory} / ${data.maxHeapMemory}`;
            if (diskInfoUsed) diskInfoUsed.textContent = `${data.usedHeapMemory}`;
            if (diskInfoMax) diskInfoMax.textContent = `${data.maxHeapMemory}`;
            if (diskInfoPercentage) diskInfoPercentage.textContent = `${data.usedHeapPercentage}`;

        } catch (error) {
            console.error('Error fetching system info:', error);
        }
    }

    async function fetchEvents(url, elementId, lastTimestamp, offset = 0) {
        try {
            const eventsElement = document.getElementById(elementId);
            const currentEventCount = eventsElement.querySelectorAll('.event-container').length;

            if (currentEventCount >= MAX_EVENTS_DISPLAYED) {
                // console.log(` ${elementId} fulled.`);
                return {lastTimestamp, offset};
            }

            const response = await fetch(`${url}?lastTimestamp=${lastTimestamp.toISOString()}&size=20&offset=${offset}`);
            const events = await response.json();

            if (events.length > 0) {
                const newTimestamp = new Date(events[events.length - 1].timestamp);
                // console.log(`Length ${events.length} ,timestamp ${newTimestamp}`);

                if (newTimestamp.getTime() === lastTimestamp.getTime()) {
                    offset += 1;
                } else {
                    lastTimestamp = newTimestamp;
                    offset = 0;
                }

                let existingEventCount = eventsElement.querySelectorAll('.event-container').length;
                const totalEventCount = existingEventCount + events.length;

                // console.log(`count : ${totalEventCount}`);
                const fragment = document.createDocumentFragment();
                events.forEach(event => {
                    if (existingEventCount < MAX_EVENTS_DISPLAYED) {
                        const eventLi = document.createElement('li');
                        // console.log('event:', event);

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
                // console.log(`No events ${elementId}.`);
            }

            return {lastTimestamp, offset};
        } catch (error) {
            // console.error(`Error ${elementId} `, error);
            return {lastTimestamp, offset};
        }
    }

    async function fetchEventStreams() {
        const result = await fetchEvents('/detect', 'event-streams', lastEventTimestamp, lastEventOffset);
        lastEventTimestamp = result.lastTimestamp;
        lastEventOffset = result.offset;
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
            await fetchSystemInfo();
            await fetchEventStreams();
            await fetchWAFEvents();
        } catch (error) {
            console.error('Error in pollData:', error);
        } finally {
            setTimeout(pollData, 1000);
        }
    }

    pollData();
});