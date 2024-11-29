import wsManager from "./websocket.js";
import { toggleMenu, setProgressBar } from "./utils.js";

document.addEventListener("DOMContentLoaded", () => {
    toggleMenu('main .head .menu2');

    const memoryProgress = document.querySelector('.progress[data-type="memory"]');
    const diskProgress = document.querySelector('.progress[data-type="disk"]');

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
        if (memoryProgress) {
            setProgressBar(memoryProgress, data.freeMemoryPercentage);
        }

        if (diskProgress) {
            setProgressBar(diskProgress, data.usedHeapPercentage);
        }
    }
});
