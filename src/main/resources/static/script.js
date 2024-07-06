document.addEventListener("DOMContentLoaded", function () {
    const menu = document.querySelectorAll(".menu-link");
    const menuToggle = document.querySelectorAll(".menu");


    menuToggle.forEach((toggle) => {
        toggle.addEventListener("click", function () {
            const next = toggle.nextElementSibling;
            next.classList.toggle("active");
        });
    });

    const sidebar = document.getElementById("sidebar");
    const content = document.getElementById("content");
    const toggleMenu = document.querySelector("[data-icon='menu']");

    toggleMenu.addEventListener("click", function () {
        sidebar.classList.toggle("active");
        content.classList.toggle("active");
    });

    async function fetchSystemInfo() {
        try {
            const response = await axios.get('/system/info');
            const data = response.data;

            document.getElementById('os-info').textContent = `Available Processors: ${data.availableProcessors}`;
            document.getElementById('cpu-info').textContent = `System Load Average: ${data.systemLoadAverage}`;
            document.getElementById('memory-info').textContent = `Free Memory: ${data.freeMemory}`;
            document.getElementById('disk-info').textContent = `Used Heap Memory: ${data.usedHeapMemory}`;

        } catch (error) {
        }
    }

    async function fetchEventStreams() {
        try {
            const response = await axios.get('/detect');
            const eventStreams = response.data;
            console.log('Fetching eventstreams data...');
            const eventStreamsElement = document.getElementById('event-streams');
            eventStreamsElement.innerHTML = '';
            eventStreams.forEach(event => {
                const li = document.createElement('li');
                li.textContent = `${event.timestamp}: ${event.description}`;
                eventStreamsElement.appendChild(li);
            });
        } catch (error) {
        }
    }

    async function fetchDangerousEvents() {
        try {
            const response = await axios.get('/detect/dangrous');
            const dangerousEvents = response.data;
            console.log('Fetching dangrous data...');
            const dangerousEventsElement = document.getElementById('dangerous-events');
            dangerousEventsElement.innerHTML = '';
            dangerousEvents.forEach(event => {
                const li = document.createElement('li');
                li.textContent = `${event.timestamp}: ${event.description}`;
                dangerousEventsElement.appendChild(li);
            });
        } catch (error) {
        }
    }

    function pollData() {
        fetchSystemInfo();

        fetchEventStreams();
        fetchDangerousEvents();
        
        setTimeout(pollData, 1000);
    }

    pollData();

});
