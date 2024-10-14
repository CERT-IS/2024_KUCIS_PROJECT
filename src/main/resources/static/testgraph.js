document.addEventListener('DOMContentLoaded', function() {
    const labels = ['02:43:00', '02:44:00', '02:45:00', '02:46:00', '02:47:00', '02:48:00', 
        '02:49:00', '02:50:00', '02:51:00', '02:52:00', '02:53:00', '02:54:00', 
        '02:55:00', '02:56:00', '02:57:00'];

    // Gradient for the Bar and Line charts
    const ctxBar = document.getElementById('barChart').getContext('2d');
    const gradientBar = ctxBar.createLinearGradient(0, 0, 0, 400);
    gradientBar.addColorStop(0, 'rgba(0, 123, 255, 0.5)');
    gradientBar.addColorStop(1, 'rgba(0, 123, 255, 0)');

    const ctxLine = document.getElementById('lineChart').getContext('2d');
    const gradientLine = ctxLine.createLinearGradient(0, 0, 0, 400);
    gradientLine.addColorStop(0, 'rgba(0, 123, 255, 0.5)');
    gradientLine.addColorStop(1, 'rgba(0, 123, 255, 0)');

    // Bar chart data
    const data1 = {
        labels: labels,
        datasets: [{
            label: 'Count',
            data: [5, 12, 8, 15, 10, 7, 5, 9, 20, 14, 11, 6, 13, 17, 10],
            backgroundColor: gradientBar,
            borderColor: '#007BFF',
            borderWidth: 2,
            fill: true
        }]
    };

    // Line chart data
    const data2 = {
        labels: labels,
        datasets: [{
            label: 'Count 1',
            data: [15, 20, 25, 18, 22, 17, 13, 28, 32, 23, 19, 21, 25, 29, 16],
            backgroundColor: gradientLine,
            borderColor: '#007BFF',
            borderWidth: 2,
            fill: true,
            pointBackgroundColor: '#FFFFFF',
            pointBorderColor: '#007BFF',
            pointBorderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6
        }, {
            label: 'Count 2',
            data: [10, 18, 12, 20, 15, 22, 9, 25, 30, 20, 14, 18, 23, 27, 19],
            backgroundColor: 'rgba(252, 59, 86, 0.5)',
            borderColor: 'rgba(252, 59, 86, 1)',
            borderWidth: 2,
            fill: true,
            pointBackgroundColor: '#FFFFFF',
            pointBorderColor: 'rgba(252, 59, 86, 1)',
            pointBorderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6
        }]
    };

    // Pie chart data
    const data3 = {
        labels: ['02:50:00', '02:51:00', '02:52:00', '02:53:00'],
        datasets: [{
            label: 'Count',
            data: [35, 25, 15, 10],
            backgroundColor: [
                'rgba(23, 117, 241, 0.5)',
                'rgba(255, 99, 132, 0.5)',
                'rgba(54, 162, 235, 0.5)',
                'rgba(255, 206, 86, 0.5)'
            ],
            borderColor: '#007BFF',
            borderWidth: 2
        }]
    };

    // Bar chart configuration
    new Chart(ctxBar, {
        type: 'bar',
        data: data1,
        options: {
            maintainAspectRatio: false,
            responsive: true,
            scales: {
                x: { grid: { display: false } },
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(200, 200, 200, 0.3)' }
                }
            },
            plugins: {
                legend: { display: false },
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

    // Line chart configuration
    new Chart(ctxLine, {
        type: 'line',
        data: data2,
        options: {
            maintainAspectRatio: false,
            responsive: true,
            scales: {
                x: { grid: { display: false } },
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(200, 200, 200, 0.3)' }
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

    // Pie chart configuration
    const ctxPie = document.getElementById('circleChart').getContext('2d');
    new Chart(ctxPie, {
        type: 'pie',
        data: data3,
        options: {
            maintainAspectRatio: false,
            responsive: true,
            cutout: '60%',
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
});
