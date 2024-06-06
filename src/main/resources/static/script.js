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

    const ctx = document.getElementById('myChart');

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Red', 'Blue', 'Yellow', 'Green', 'Purple', 'Orange'],
            datasets: [{
                label: '# of Votes',
                data: [12, 19, 3, 5, 2, 3],
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    function fetchSystemInfo() {
        fetch('/system-info')
            .then(response => response.json())
            .then(data => displaySystemInfo(data))
            .catch(error => console.error('Error fetching system info:', error));
    }

    function displaySystemInfo(data) {
        const systemInfoContainer = document.getElementById('system-info-container');

        if (!systemInfoContainer) {
            console.error('System info container not found');
            return;
        }

        systemInfoContainer.innerHTML = `
            <h3>System Information</h3>
            <ul>
                <li>Available Processors: ${data.availableProcessors}</li>
                <li>System Load Average: ${data.systemLoadAverage}</li>
                <li>Total Physical Memory: ${data.totalPhysicalMemory}</li>
                <li>Free Physical Memory: ${data.freePhysicalMemory}</li>
                <li>JVM Uptime: ${data.jvmUptime} ms</li>
                <li>Used Heap Memory: ${data.usedHeapMemory}</li>
                <li>Max Heap Memory: ${data.maxHeapMemory}</li>
            </ul>
        `;
    }

    fetchSystemInfo();
    setInterval(fetchSystemInfo, 1000);
});
