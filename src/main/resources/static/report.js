document.addEventListener("DOMContentLoaded", function() {
    const form = document.querySelector("form");

    form.addEventListener("submit", function(event) {
        event.preventDefault();

        const formData = {
            name: document.getElementById("name").value,
            description: document.getElementById("description").value,
            reportSource: document.querySelector("input[name='report_source']:checked").value,
            notebook: document.getElementById("notebook").value,
            fileFormat: document.querySelector("input[name='file_format']:checked").value,
            reportTrigger: document.querySelector("input[name='report_trigger']:checked").value,
            requestTime: document.querySelector("input[name='request_time']:checked").value,
            frequency: document.getElementById("frequency").value,
            every: document.getElementById("every").value,
            timeUnit: document.getElementById("time_unit").value,
            startTime: document.getElementById("start_time").value
        };

        fetch("/pdf/report", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(formData)
        })
            .then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    throw new Error("Network response was not ok " + response.statusText);
                }
            })
            .then(data => {
                alert(data);
                window.location.href = "/";
            })
            .catch(error => {
                alert("Error: " + error.message);
            });
    });
});