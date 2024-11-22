import wsManager from './websocket.js';

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





    const memoryInfoPercentage = document.getElementById('memory-info-percentage').textContent;
    const diskInfoPercentage = document.getElementById('disk-info-percentage').textContent;

    const allProgress = document.querySelectorAll('main .card .progress');
    const scaleFactor = 3;


    if (allProgress.length > 0) {
        const scaledMemoryPercentage = Math.min(memoryInfoPercentage * scaleFactor, 100);
        allProgress[0].setAttribute('data-value', memoryInfoPercentage);
        allProgress[0].style.setProperty('--value', scaledMemoryPercentage);
    }

    if (allProgress.length > 1) {
        const scaledDiskPercentage = Math.min(diskInfoPercentage * scaleFactor, 100);
        allProgress[1].setAttribute('data-value', diskInfoPercentage);
        allProgress[1].style.setProperty('--value', scaledDiskPercentage);
    }


    wsManager.connect();
    wsManager.onMessage((event) => {
        const message = JSON.parse(event.data);
        switch (message.action) {
            case "sendSystemInfo":
                const systemInfo = JSON.parse(message.data);
                updateSystemInfo(systemInfo);
                break;
            default:
                console.warn("Unknown action:", message.action);
        }
    });
    setInterval(() => {
        const request = {
            action: "getSystemInfo"
        };

        if (wsManager.isConnected) {
            wsManager.send(request);
        }
    }, 1000);

    function updateSystemInfo(data) {
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
    }
});