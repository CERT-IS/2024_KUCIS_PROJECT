import wsManager from "./websocket.js";
import { toggleMenu, setProgressBar } from "./utils.js";

document.addEventListener("DOMContentLoaded", () => {
    toggleMenu('main .head .menu2');

    wsManager.connect();
    wsManager.onMessage((event) => {
        const message = JSON.parse(event.data);

        if (message.action === "sendSystemInfo") {
            const data = JSON.parse(message.data);
            updateSystemInfo(data);
        }
    });

    setInterval(() => {
        wsManager.send({ action: "getSystemInfo" });
    }, 1000);

    function updateSystemInfo(data) {
        const osInfo = document.getElementById('os-info');
        const cpuInfo = document.getElementById('cpu-info');
        if (osInfo) osInfo.textContent = `Available Processors: ${data.availableProcessors}`;
        if (cpuInfo) cpuInfo.textContent = `실행 대기중인 Task의 평균 수: ${data.systemLoadAverage}`;

        const memoryInfo = document.getElementById('memory-info');
        const memoryInfoFree = document.getElementById('memory-info-free');
        const memoryInfoTotal = document.getElementById('memory-info-total');
        const memoryInfoPercentage = document.getElementById('memory-info-percentage');
        if (memoryInfo) memoryInfo.textContent = `Free Memory: ${data.freeMemory} / ${data.totalMemory}`;
        if (memoryInfoFree) memoryInfoFree.textContent = `${data.freeMemory}`;
        if (memoryInfoTotal) memoryInfoTotal.textContent = `${data.totalMemory}`;
        if (memoryInfoPercentage) memoryInfoPercentage.textContent = `${data.freeMemoryPercentage}%`;

        const diskInfo = document.getElementById('disk-info');
        const diskInfoUsed = document.getElementById('disk-info-used');
        const diskInfoMax = document.getElementById('disk-info-max');
        const diskInfoPercentage = document.getElementById('disk-info-percentage');
        if (diskInfo) diskInfo.textContent = `Used Heap : ${data.usedHeapMemory} / ${data.maxHeapMemory}`;
        if (diskInfoUsed) diskInfoUsed.textContent = `${data.usedHeapMemory}`;
        if (diskInfoMax) diskInfoMax.textContent = `${data.maxHeapMemory}`;
        if (diskInfoPercentage) diskInfoPercentage.textContent = `${data.usedHeapPercentage}%`;

        const memoryProgress = document.querySelector('.progress[data-type="memory"]');
        const diskProgress = document.querySelector('.progress[data-type="disk"]');

        console.log(data);

        if (memoryProgress) setProgressBar(memoryProgress, data.freeMemoryPercentage);
        if (diskProgress) setProgressBar(diskProgress, data.usedHeapPercentage);

    }
});
