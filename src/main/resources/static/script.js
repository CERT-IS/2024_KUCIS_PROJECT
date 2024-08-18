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
        const trendingUpOs = docoument.getElementById('trending-up-os');
        const trendingFlatOs = document.getElementById('trending-flat-os');

        const trendingDownCpu = document.getElementById('trending-down-cpu');
        const trendingUpCpu = docoument.getElementById('trending-up-cpu');
        const trendingFlatCpu = document.getElementById('trending-flat-cpu');

        const trendingDownMemory = document.getElementById('trending-down-memory');
        const trendingUpMemory = docoument.getElementById('trending-up-memory');
        const trendingFlatMemory = document.getElementById('trending-flat-memory');

        const trendingDownDisk = document.getElementById('trending-down-disk');
        const trendingUpDisk = docoument.getElementById('trending-up-disk');
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

    async function fetchEventStreams() {
        try {
            const response = await fetch('/detect');
            const eventStreams = await response.json();
            const eventStreamsElement = document.getElementById('event-streams');
            eventStreamsElement.innerHTML = '';

            eventStreams.forEach(eventStream => {
                const streamLi = document.createElement('li');
                streamLi.textContent = `ID: ${eventStream.id}, Type: ${eventStream.type}, Level: ${eventStream.level}`;
                eventStreamsElement.appendChild(streamLi);

                if (eventStream.queue) {
                    eventStream.queue.forEach(eventLog => {
                        const logLi = document.createElement('li');
                        logLi.textContent = `EventLog Type: ${eventLog.type}`;
                        streamLi.appendChild(logLi);

                        if (eventLog.cloudTrailEvent) {
                            const cloudTrailLi = document.createElement('li');
                            cloudTrailLi.textContent = `CloudTrailEvent: ${JSON.stringify(eventLog.cloudTrailEvent)}`;
                            streamLi.appendChild(cloudTrailLi);
                        }

                        if (eventLog.wafEvent) {
                            const wafLi = document.createElement('li');
                            wafLi.textContent = `WAFEvent: ${JSON.stringify(eventLog.wafEvent)}`;
                            streamLi.appendChild(wafLi);
                        }

                        if (eventLog.flowLogEvent) {
                            const flowLogLi = document.createElement('li');
                            flowLogLi.textContent = `FlowLogEvent: ${JSON.stringify(eventLog.flowLogEvent)}`;
                            streamLi.appendChild(flowLogLi);
                        }
                    });
                }
            });
        } catch (error) {
            console.error('Error fetching event streams:', error);
        }
    }

    async function fetchDangerousEvents() {
        try {
            const response = await fetch('/detect/dangrous');
            const dangerousEvents = await response.json();
            const dangerousEventsElement = document.getElementById('dangerous-events');
            dangerousEventsElement.innerHTML = '';

            dangerousEvents.forEach(eventStream => {
                const streamLi = document.createElement('li');
                streamLi.textContent = `ID: ${eventStream.id}, Type: ${eventStream.type}, Level: ${eventStream.level}`;
                dangerousEventsElement.appendChild(streamLi);

                if (eventStream.queue) {
                    eventStream.queue.forEach(eventLog => {
                        const logLi = document.createElement('li');
                        logLi.textContent = `EventLog Type: ${eventLog.type}`;
                        streamLi.appendChild(logLi);

                        if (eventLog.cloudTrailEvent) {
                            const cloudTrailLi = document.createElement('li');
                            cloudTrailLi.textContent = `CloudTrailEvent: ${JSON.stringify(eventLog.cloudTrailEvent)}`;
                            streamLi.appendChild(cloudTrailLi);
                        }

                        if (eventLog.wafEvent) {
                            const wafLi = document.createElement('li');
                            wafLi.textContent = `WAFEvent: ${JSON.stringify(eventLog.wafEvent)}`;
                            streamLi.appendChild(wafLi);
                        }

                        if (eventLog.flowLogEvent) {
                            const flowLogLi = document.createElement('li');
                            flowLogLi.textContent = `FlowLogEvent: ${JSON.stringify(eventLog.flowLogEvent)}`;
                            streamLi.appendChild(flowLogLi);
                        }
                    });
                }
            });
        } catch (error) {
            console.error('Error fetching event streams:', error);
        }
    }



    async function pollData() {
        try {
            await fetchSystemInfo();
            await fetchEventStreams();
            await fetchDangerousEvents();
        } catch (error) {
            console.error('Error in pollData:', error);
        } finally {
            setTimeout(pollData, 1000);
        }
    }

    pollData();
});