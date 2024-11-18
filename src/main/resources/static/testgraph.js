document.addEventListener('DOMContentLoaded', function () {
    const ctxBar = document.getElementById('barChart').getContext('2d');
    const ctxLine = document.getElementById('lineChart').getContext('2d');
    const ctxPie = document.getElementById('pieChart').getContext('2d');

    const MAX_GRAPH_POINTS = 20;

    const barChart = new Chart(ctxBar, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Event Count',
                data: [],
                backgroundColor: 'rgba(75, 192, 192, 0.5)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1,
            }],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
        },
    });

    const lineChart = new Chart(ctxLine, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Critical Events',
                data: [],
                borderColor: 'rgba(255, 99, 132, 1)',
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                fill: true,
            }],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
        },
    });

    const pieChart = new Chart(ctxPie, {
        type: 'pie',
        data: {
            labels: [],
            datasets: [{
                label: 'Event Types',
                data: [],
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'],
            }],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
        },
    });


    wsManager.connect();
    wsManager.onMessage((event) => {
        const message = JSON.parse(event.data);
        switch (message.action) {
            case "getWAFEvents":
                const wafEvent = JSON.parse(message.data);
                handleWAFEvents(wafEvent);
                break;
            
            case "askResponse":
                handleAskResponse(message.data);
                break;

            default:
                console.warn("Unknown action:", message.action);
        }
    });
    setInterval(() => {
        const request = {
            action: "getWAFEvents",
            size : 20,
            offset : lastWAFEventOffset,
            lastTimestamp: lastWAFEventTimestamp
        };
        if (wsManager.isConnected) {
            wsManager.send(request);
        }
    }, 1000);

    function handleWAFEvents(events) {
        const eventsElement = document.getElementById('waf-events');
        const currentEventCount = eventsElement.querySelectorAll('.event-container').length;
        if (currentEventCount >= MAX_EVENTS_DISPLAYED) {
            console.log(`waf-events fulled.`);
            return;
        }

        if (events.length > 0) {
            const newTimestamp = new Date(events[events.length - 1].timestamp);

            if (newTimestamp.getTime() === lastWAFEventTimestamp.getTime()) {
                lastWAFEventOffset += 1;
            } else {
                lastWAFEventTimestamp = newTimestamp;
                lastWAFEventOffset = 0;
            }

            let existingEventCount = eventsElement.querySelectorAll('.event-container').length;
            const totalEventCount = existingEventCount + events.length;

            const fragment = document.createDocumentFragment();
            events.forEach(event => {
                if (existingEventCount < MAX_EVENTS_DISPLAYED) {
                    const eventLi = document.createElement('li');
                    eventLi.innerHTML = createEventHTML(event);
                    fragment.appendChild(eventLi);
                    existingEventCount++;
                }

                updateCharts(event);
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
            console.log(`No events in waf-events.`);
        }
    }


    function updateCharts(event) {
        const timestamp = new Date(event.timestamp).toLocaleTimeString();

        if (barChart.data.labels.length >= MAX_GRAPH_POINTS) {
            barChart.data.labels.shift();
            barChart.data.datasets[0].data.shift();
        }
        barChart.data.labels.push(timestamp);
        barChart.data.datasets[0].data.push(1);
        barChart.update();

        if (event.eventType === 'Critical') {
            if (lineChart.data.labels.length >= MAX_GRAPH_POINTS) {
                lineChart.data.labels.shift();
                lineChart.data.datasets[0].data.shift();
            }
            lineChart.data.labels.push(timestamp);
            lineChart.data.datasets[0].data.push(
                (lineChart.data.datasets[0].data.slice(-1)[0] || 0) + 1
            );
            lineChart.update();
        }
        
        const eventType = event.eventType || 'Unknown';
        const typeIndex = pieChart.data.labels.indexOf(eventType);
        if (typeIndex === -1) {
            pieChart.data.labels.push(eventType);
            pieChart.data.datasets[0].data.push(1);
        } else {
            pieChart.data.datasets[0].data[typeIndex] += 1;
        }
        pieChart.update();
    }
});
