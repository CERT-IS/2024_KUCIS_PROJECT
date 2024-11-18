document.addEventListener('DOMContentLoaded', function() {
    const lineGraphCtx = document.getElementById('lineGraph').getContext('2d');

    let labels = ['16:00', '16:05', '16:10', '16:15', '16:20', '16:25', '16:30', '16:35', '16:40', '16:45'];
    let dataS1 = [45, 50, 55, 60, 65, 70, 75, 80, 85, 90];

    const gradient = lineGraphCtx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(0, 123, 255, 0.5)');
    gradient.addColorStop(1, 'rgba(0, 123, 255, 0)');

    const lineGraph = new Chart(lineGraphCtx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'S1',
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

    function updateChart() {
        const lastTime = labels[labels.length - 1];
        const newTime = getNextTime(lastTime);
        const newValue = Math.floor(Math.random() * (2700 - 100 + 1)) + 100;

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

    setInterval(updateChart, 2000);
});