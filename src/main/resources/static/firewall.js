import wsManager from './websocket.js';
import {createEventHTML} from "./utils.js";

let lastEventTimestamp = new Date(0); // Epoch (1970-01-01T00:00:00Z)
let lastEventOffset = 0;
const MAX_EVENTS_DISPLAYED = 100;

document.querySelectorAll('.event-container').forEach(container => {
    container.addEventListener('click', function() {
        toggleLogs(container);
    });
});


document.addEventListener("DOMContentLoaded", function() {

    function searchFunction() {
        let input = document.querySelector('.search-input').value.toUpperCase();
        let category = document.getElementById('search-category').value;
        let eventContainers = document.querySelectorAll('.event-container');

        eventContainers.forEach(container => {
            let searchValue = '';

            if (category === 'id') {
                searchValue = container.querySelector('.event-header strong:nth-of-type(1)').nextSibling.nodeValue.trim().toUpperCase();
            } else if (category === 'name') {
                searchValue = container.querySelector('.event-header strong:nth-of-type(2)').nextSibling.nodeValue.trim().toUpperCase();
            } else if (category === 'type') {
                searchValue = container.querySelector('.event-header strong:nth-of-type(3)').nextSibling.nodeValue.trim().toUpperCase();
            } else if (category === 'timestamp') {
                searchValue = container.querySelector('.event-header strong:nth-of-type(4)').nextSibling.nodeValue.trim().toUpperCase();
            }

            if (searchValue.indexOf(input) > -1) {
                container.style.display = "";
            } else {
                container.style.display = "none";
            }
        });
    }

    window.searchFunction = searchFunction;

    wsManager.connect();

    wsManager.onMessage((event) => {
        const message = JSON.parse(event.data);

        const isHandmade = window.location.href.includes('/handmade');
        const action = isHandmade ? "getHandmadeEvents" : "getWAFEvents";

        if(message.action == action){
            const events = JSON.parse(message.data);
            handleEvents(events);
        }
        else console.warn("Unknown action:", message.action);
    });


    setInterval(() => {

        const isHandmade = window.location.href.includes('/handmade');
        const action = isHandmade ? "getHandmadeEvents" : "getWAFEvents";

        const request = {
            action: action,
            size : 20,
            offset : lastEventOffset,
            lastTimestamp: lastEventTimestamp
        };
        if (wsManager.isConnected) {
            wsManager.send(request);
        }
    }, 1000);

    function handleEvents(events) {
        const eventsElement = document.getElementById('event-streams');
        const currentEventCount = eventsElement.querySelectorAll('.event-container').length;

        if (currentEventCount >= MAX_EVENTS_DISPLAYED) {
            console.log(`events fulled.`);
            return;
        }


        if (events.length > 0) {
            const newTimestamp = new Date(events[events.length - 1].timestamp);

            if (newTimestamp.getTime() === lastEventTimestamp.getTime()) {
                lastEventOffset += 1;
            } else {
                lastEventTimestamp = newTimestamp;
                lastEventOffset = 0;
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
            console.log(`No events in events.`);
        }
    }
});