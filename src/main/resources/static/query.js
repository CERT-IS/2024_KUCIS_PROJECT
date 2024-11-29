import {createEventHTML} from "./utils.js";

document.addEventListener("DOMContentLoaded", function() {
    const searchForm = document.getElementById('searchForm');
    const searchInput = document.getElementById('searchInput');
    const events = document.getElementById('events');

    searchForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const searchValue = searchInput.value.trim();
        if (searchValue === '') {
            alert('검색어를 입력하세요.');
            return;
        }

        try {
            const response = await fetch('/api/opensearch/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ query: searchValue })
            });

            if (!response.ok) {
                throw new Error('Error fetching data from server');
            }

            const list = await response.json();
            console.log(list);

            events.innerHTML = '';

            if (list && list.length > 0) {
                list.forEach(item => {
                    const eventElemnt = createEventHTML(item);
                    events.appendChild(eventElemnt);
                });
            } else {
                const noResultItem = document.createElement('li');
                noResultItem.textContent = '검색 결과가 없습니다.';
                events.appendChild(noResultItem);
            }

        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred while processing your request');
        }

        searchInput.value = '';
    });

});