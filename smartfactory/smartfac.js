document.addEventListener('DOMContentLoaded', function() {
    // time
    function updateTime() {
        const now = new Date();
        const offset = 9 * 60;
        const localTime = new Date(now.getTime() + offset * 60 * 1000);
        document.getElementById('current-time').textContent = localTime.toISOString().slice(0, 19).replace('T', ' ');
    }
    
    updateTime();
    setInterval(updateTime, 1000);


    // circleChart
    const ctx = document.getElementById('circleChart').getContext('2d');
    const circleChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['완료', '미완료'],
            datasets: [{
                data: [34.7, 65.3],
                backgroundColor: ['#007BFF', '#e0e0e0'],
                borderColor: ['#fff', '#fff'],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            cutout: '60%',
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });

    function getRandomPercentage() {
        return (Math.random() * (50 - 30) + 30).toFixed(1);
    }

    function updatePercentages() {
        const randomValue = getRandomPercentage();
        const completedValue = parseFloat(randomValue);
        const uncompletedValue = (100 - completedValue).toFixed(1);

        circleChart.data.datasets[0].data = [completedValue, uncompletedValue];
        circleChart.update();

        document.querySelectorAll('.percent').forEach(element => {
            element.textContent = `${completedValue}%`;
        });
    }

    updatePercentages();
    setInterval(updatePercentages, 2000);


    // list
    // function getRandomValue() {
    //     return Math.floor(Math.random() * (1500 - 800 + 1)) + 800;
    // }

    // function updateTotal() {
    //     let aLine = getRandomValue();
    //     let bLine = getRandomValue();
    //     let cLine = getRandomValue();
    //     let total = aLine + bLine + cLine;
    
    //     document.getElementById('a-line').innerText = aLine;
    //     document.getElementById('b-line').innerText = bLine;
    //     document.getElementById('c-line').innerText = cLine;
    //     document.getElementById('lineTotal').innerText = total;
    // }

    // updateTotal();
    // setInterval(updateTotal, 2000);


    // barChart
    const ctx2 = [
        document.getElementById('barChart1').getContext('2d'),
        document.getElementById('barChart2').getContext('2d'),
        document.getElementById('barChart3').getContext('2d'),
        document.getElementById('barChart4').getContext('2d'),
        document.getElementById('barChart5').getContext('2d'),
        document.getElementById('barChart6').getContext('2d')
    ];

    const charts = [];
    
    function createCharts(progressValues) {
        progressValues.forEach((progress, index) => {
            const datasets = [
                {
                    label: 'Progress',
                    data: [progress],
                    backgroundColor: '#007BFF',
                    borderColor: '#fff',
                    borderWidth: 2,
                    barThickness: 20
                },
                {
                    label: 'Total Goal',
                    data: [100],
                    backgroundColor: '#e0e0e0',
                    borderColor: '#fff',
                    borderWidth: 2,
                    barThickness: 20
                }
            ];

            const chart = new Chart(ctx2[index], {
                type: 'bar',
                data: {
                    labels: [''],
                    datasets: datasets
                },
                options: {
                    indexAxis: 'y',
                    responsive: true,
                    scales: {
                        x: { display: false },
                        y: { display: false, stacked: true }
                    },
                    plugins: { legend: { display: false } }
                }
            });

            charts.push(chart);
        });
    }

    function updateCharts() {
        const progressValues = [
            Math.floor(Math.random() * (40 - 30 + 1)) + 30,
            Math.floor(Math.random() * (70 - 60 + 1)) + 60,
            Math.floor(Math.random() * (30 - 20 + 1)) + 20,
            Math.floor(Math.random() * (50 - 40 + 1)) + 40,
            Math.floor(Math.random() * (90 - 80 + 1)) + 80,
            Math.floor(Math.random() * (20 - 10 + 1)) + 10,
            Math.floor(Math.random() * (10000 - 8000 + 1)) + 8000
        ];

        charts.forEach((chart, index) => {
            chart.data.datasets[0].data[0] = progressValues[index];
            chart.update();
        });

        document.getElementById('progress1').innerText = `${progressValues[0]}%`;
        document.getElementById('progress2').innerText = `${progressValues[1]}%`;
        document.getElementById('progress3').innerText = `${progressValues[2]}%`;
        document.getElementById('progress4').innerText = `${progressValues[3]}%`;
        document.getElementById('progress5').innerText = `${progressValues[4]}%`;
        document.getElementById('progress6').innerText = `${progressValues[5]}%`;
        document.getElementById('barTotal').innerText = `${progressValues[6]}`;
    }

    createCharts([30, 60, 20, 40, 80, 10]);
    updateCharts();
    setInterval(updateCharts, 2000);



    // cctv
    let currentIndex = 1;
    const totalImages = 6;
    const cctvImages = document.querySelectorAll('.cctvImage');
    const cameraTexts = document.querySelectorAll('.cameraText');
    
    setInterval(() => {
        currentIndex = (currentIndex % totalImages) + 1;
    
        cctvImages.forEach((image) => {
            image.src = `cctv${currentIndex}.png`;
        });
    
        cameraTexts.forEach((text) => {
            text.textContent = `Camera ${currentIndex}`;
        });
    }, 2000);


    // machine
    function updateCharts2() {
        const progressValues = [
            Math.floor(Math.random() * (50 - 40 + 1)) + 40,
            Math.floor(Math.random() * (20 - 10 + 1)) + 10,
            Math.floor(Math.random() * (-50 - -60 + 1)) + -50,
            Math.floor(Math.random() * (170 - 150 + 1)) + 150,
            Math.floor(Math.random() * (160 - 140 + 1)) + 140
        ];

        document.getElementById('capacityValue1').innerText = `${progressValues[0]}%`;
        document.getElementById('capacityValue2').innerText = `${progressValues[1]}%`;
        document.getElementById('capacityValue3').innerText = `${progressValues[2]}℃`;
        document.getElementById('capacityValue4').innerText = `${progressValues[3]}℃`;
        document.getElementById('capacityValue5').innerText = `${progressValues[4]}℃`;
    }

    updateCharts2();
    setInterval(updateCharts2, 2000);


    // lineGraph
    const lineGraphCtx = document.getElementById('lineGraph').getContext('2d');

    let labels = ['16:00', '16:05', '16:10', '16:15', '16:20', '16:25', '16:30', '16:35', '16:40', '16:45'];
    let dataS1 = [45, 50, 55, 60, 65, 70, 75, 80, 85, 90];
    
    const lineGraph = new Chart(lineGraphCtx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'S1',
                    data: dataS1,
                    borderColor: '#007BFF',
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                x: {
                    title: {
                        display: false
                    }
                },
                y: {
                    title: {
                        display: false
                    },
                    min: 30,
                    max: 100
                }
            },
            plugins: {
                legend: {
                    display: false
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
        const newValue = Math.floor(Math.random() * (90 - 40 + 1)) + 40;
    
        labels.push(newTime);
        labels.shift();
    
        dataS1.push(newValue);
        dataS1.shift();
    
        lineGraph.data.labels = labels;
        lineGraph.data.datasets[0].data = dataS1;
    
        lineGraph.update();
    }
    
    setInterval(updateChart, 2000);
    
    
    




});