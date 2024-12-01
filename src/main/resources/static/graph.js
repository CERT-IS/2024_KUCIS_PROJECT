let labelsBar = [];
let dataBar = [];
let labelsLine = [];
let dataLine1 = [];
let dataLine2 = [];
let dataPie = [0, 0, 0, 0];

let labelsPie = [];
let chartBar, chartLine, chartPie;

let timeseries = 60; // 60 sec 단위로 구분. 나중에 커스텀화

document.addEventListener('DOMContentLoaded', function() {
    const ctxBar = document.getElementById('barChart').getContext('2d');
    const ctxLine = document.getElementById('lineChart').getContext('2d');
    const ctxPie = document.getElementById('circleChart').getContext('2d');

    chartBar = new Chart(ctxBar, {
        type: 'bar',
        data: {
            labels: labelsBar,
            datasets: [{
                label: 'Collected Logs',
                data: dataBar,
                backgroundColor: 'rgba(0, 123, 255, 0.5)',
                borderColor: '#007BFF',
                borderWidth: 2,
                fill: true
            }]
        },
        options: {
            maintainAspectRatio: false,
            responsive: true,
            scales: {
                x: { grid: { display: false } },
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(200, 200, 200, 0.3)' },
                    max: Math.max(...dataBar, 1)
                }
            },
            plugins: {
                legend: { display: true },
                tooltip: {
                    backgroundColor: 'rgba(0, 123, 255, 0.9)',
                    titleColor: '#FFFFFF',
                    bodyColor: '#FFFFFF',
                    cornerRadius: 5,
                    padding: 10,
                    displayColors: false
                }
            }
        }
    });

    chartLine = new Chart(ctxLine, {
        type: 'line',
        data: {
            labels: labelsLine,
            datasets: [
                {
                    label: 'Group A Count',
                    data: dataLine1,
                    backgroundColor: 'rgba(0, 123, 255, 0.5)',
                    borderColor: '#007BFF',
                    borderWidth: 2,
                    fill: true
                },
                {
                    label: 'Group B Count',
                    data: dataLine2,
                    backgroundColor: 'rgba(252, 59, 86, 0.5)',
                    borderColor: 'rgba(252, 59, 86, 1)',
                    borderWidth: 2,
                    fill: true
                }
            ]
        },
        options: {
            maintainAspectRatio: false,
            responsive: true,
            scales: {
                x: { grid: { display: false } },
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(200, 200, 200, 0.3)' },
                    max: Math.max(...dataLine1.concat(dataLine2), 1)
                }
            },
            plugins: {
                legend: { display: true },
                tooltip: {
                    backgroundColor: 'rgba(0, 123, 255, 0.9)',
                    titleColor: '#FFFFFF',
                    bodyColor: '#FFFFFF',
                    cornerRadius: 5,
                    padding: 10,
                    displayColors: false
                }
            }
        }
    });

    chartPie = new Chart(ctxPie, {
        type: 'pie',
        data: {
            labels: ['SQL Injection', 'XSS', 'CSRF', 'File Inclusion'],
            datasets: [{
                label: 'Vulnerability Types',
                data: dataPie,
                backgroundColor: [
                    'rgba(23, 117, 241, 0.5)',
                    'rgba(255, 99, 132, 0.5)',
                    'rgba(54, 162, 235, 0.5)',
                    'rgba(255, 206, 86, 0.5)'
                ],
                borderColor: '#FFFFFF',
                borderWidth: 2
            }]
        },
        options: {
            maintainAspectRatio: false,
            responsive: true,
            plugins: {
                legend: { display: true },
                tooltip: {
                    backgroundColor: 'rgba(0, 123, 255, 0.9)',
                    titleColor: '#FFFFFF',
                    bodyColor: '#FFFFFF',
                    cornerRadius: 5,
                    padding: 10,
                    displayColors: true
                }
            }
        }
    });
});

// main 페이지인 경우
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


export function updateCharts(events) {
    if (events.length > 0) {
        // Bar 차트 업데이트 (취약점 종류)
        updateBarChart(events);
        // Line 차트 업데이트 (로그 그룹별 취약점 수)
        updateLineChart(events);
        // Pie 차트 업데이트 (취약점 종류)
        updatePieChart(events);
    }
}

function updateBarChart(events){
    const dateCounts = {};
    events.forEach(event => {
        const eventDate = new Date(event.timestamp).toISOString().slice(0, 10); // 'YYYY-MM-DD' 형식으로 변환
        if (!dateCounts[eventDate]) {
            dateCounts[eventDate] = 0;
        }
        dateCounts[eventDate]++;
    });

    labelsBar = Object.keys(dateCounts);
    dataBar = Object.values(dateCounts);

    if (labelsBar.length > timeseries) {
        labelsBar = labelsBar.slice(-timeseries);
        dataBar = dataBar.slice(-timeseries);
    }

    chartBar.data.labels = labelsBar;
    chartBar.data.datasets[0].data = dataBar;
    chartBar.options.scales.y.max = Math.max(...dataBar, 1);
    chartBar.update();

    chartBar.data.labels = labelsBar;
    chartBar.data.datasets[0].data = dataBar;
    chartBar.options.scales.y.max = Math.max(...dataBar, 1);
    chartBar.update();
}

function updateLineChart(events){
    const currentTime = new Date();
    const timeLabel = currentTime.toISOString().slice(0, 16);

    const groupAData = events.filter(event => event.group === 'A').length;
    const groupBData = events.filter(event => event.group === 'B').length;

    labelsLine.push(timeLabel);
    dataLine1.push(groupAData);
    dataLine2.push(groupBData);

    if (labelsLine.length > timeseries) {
        labelsLine.shift();
        dataLine1.shift();
        dataLine2.shift();
    }

    chartLine.data.labels = labelsLine;
    chartLine.data.datasets[0].data = dataLine1;
    chartLine.data.datasets[1].data = dataLine2;
    chartLine.options.scales.y.max = Math.max(...dataLine1.concat(dataLine2), 1);
    chartLine.update();
}
function updatePieChart(events){
    const eventCounts = {};

    events.forEach(event => {
        if (eventCounts.hasOwnProperty(event.eventType)) {
            eventCounts[event.eventType] += 1;
        } else {
            eventCounts[event.eventType] = 1;
        }
    });

    labelsPie = Object.keys(eventCounts);
    dataPie = Object.values(eventCounts);

    chartPie.data.labels = labelsPie;
    chartPie.data.datasets[0].data = dataPie;
    chartPie.update();
}