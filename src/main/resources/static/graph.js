document.addEventListener('DOMContentLoaded', function () {
    const lineGraphCtx = document.getElementById('lineGraph').getContext('2d');

    let labels = ['16:00', '16:05', '16:10', '16:15', '16:20', '16:25', '16:30', '16:35', '16:40', '16:45'];
    let dataS1 = Array(10).fill(0);

    const gradient = lineGraphCtx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(0, 123, 255, 0.5)');
    gradient.addColorStop(1, 'rgba(0, 123, 255, 0)');

    const lineGraph = new Chart(lineGraphCtx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Network Traffic (Tx + Rx)',
                    data: dataS1,
                    borderColor: '#007BFF',
                    backgroundColor: gradient,
                    fill: true,
                    pointBackgroundColor: '#FFFFFF',
                    pointBorderColor: '#007BFF',
                    pointBorderWidth: 2,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    tension: 0.3
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: {
                duration: 0,
                easing: 'linear',
                lazy: true
            },
            scales: {
                x: {
                    title: {
                        display: false
                    },
                    grid: {
                        display: false
                    }
                },
                y: {
                    title: {
                        display: false
                    },
                    min: 0,
                    max: 3000,
                    grid: {
                        color: 'rgba(200, 200, 200, 0.3)',
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 123, 255, 0.9)',
                    titleColor: '#FFFFFF',
                    bodyColor: '#FFFFFF',
                    titleFont: { weight: 'bold' },
                    padding: 10,
                    cornerRadius: 5,
                    displayColors: false,
                }
            }
        }
    });

    function getNextTime(currentTime) {
        const [hours, minutes] = currentTime.split(':').map(Number);
        let newMinutes = minutes + 5;
        let newHours = hours;

        if (newMinutes >= 60) {
            newMinutes -= 60;
            newHours = (hours + 1) % 24;
        }

        return `${newHours < 10 ? '0' : ''}${newHours}:${newMinutes < 10 ? '0' : ''}${newMinutes}`;
    }

    async function fetchTrafficData() {
        try {
            const response = await fetch('/system/traffic');
            if (response.ok) {
                const trafficData = await response.json();

                let totalTraffic = 0;
                for (const [key, value] of Object.entries(trafficData)) {
                    const trafficValue = parseFloat(value.split(' ')[0]);
                    const unit = value.split(' ')[1];

                    const multiplier = unit === 'KB' ? 1024 : unit === 'MB' ? 1024 ** 2 : 1024 ** 3;
                    totalTraffic += trafficValue * multiplier;
                }

                return Math.floor(totalTraffic / (1024 ** 2));
            } else {
                console.error('Failed to fetch traffic data:', response.status);
                return 0;
            }
        } catch (error) {
            console.error('Error fetching traffic data:', error);
            return 0;
        }
    }

    async function updateChart() {
        const lastTime = labels[labels.length - 1];
        const newTime = getNextTime(lastTime);
        const newValue = await fetchTrafficData();

        labels.push(newTime);
        labels.shift();

        dataS1.push(newValue);
        dataS1.shift();

        lineGraph.data.labels = labels;
        lineGraph.data.datasets[0].data = dataS1;

        lineGraph.update({
            duration: 0,
            easing: 'linear'
        });
    }

    setInterval(updateChart, 5000);
    updateChart();
});
