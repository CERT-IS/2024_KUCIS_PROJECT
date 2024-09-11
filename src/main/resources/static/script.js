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



    const memoryInfoPercentage = document.getElementById('memory-info-percentage').textContent;
    const diskInfoPercentage = document.getElementById('disk-info-percentage').textContent;

    const allProgress = document.querySelectorAll('main .card .progress');

    if (allProgress.length > 0) {
        allProgress[0].setAttribute('data-value', memoryInfoPercentage);
        allProgress[0].style.setProperty('--value', memoryInfoPercentage);
    }

    if (allProgress.length > 1) {
        allProgress[1].setAttribute('data-value', diskInfoPercentage);
        allProgress[1].style.setProperty('--value', diskInfoPercentage);
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

    async function pollData() {
        try {
            await fetchSystemInfo();
        } catch (error) {
            console.error('Error in pollData:', error);
        } finally {
            setTimeout(pollData, 1000);
        }
    }

    pollData();
});