document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("report-form");
    const uploadTemplateForm = document.getElementById("upload-template-form");
    const deleteTemplateForm = document.getElementById("delete-template-form");
    const templateList = document.getElementById("template-list");
    const refreshTemplatesButton = document.getElementById("refresh-templates");

    form.addEventListener("submit", function (event) {
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
            startTime: document.getElementById("start_time").value,
            template: document.getElementById("template-list").value
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
                    return response.blob(); // PDF 파일로 응답을 받음
                } else {
                    throw new Error("Network response was not ok " + response.statusText);
                }
            })
            .then(blob => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = `${formData.name}.pdf`; // 다운로드할 파일 이름 설정
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(url); // 메모리 해제
            })
            .catch(error => {
                alert("Error: " + error.message);
            });
    });

    function loadTemplates() {
        fetch("/pdf/list")
            .then(response => response.json())
            .then(data => {
                const templateSelect = document.getElementById("template-list");
                templateSelect.innerHTML = ''; // 기존 옵션 제거

                if (data && data.templates && data.templates.length > 0) {
                    data.templates.forEach(template => {
                        const option = document.createElement("option");
                        option.value = template;
                        option.textContent = template;
                        templateSelect.appendChild(option);
                    });
                } else {
                    console.error("No templates found or the data is null");
                }
            })
            .catch(error => alert("Failed to load templates: " + error.message));
    }

    loadTemplates(); // 페이지 로드 시 템플릿 로드

    uploadTemplateForm.addEventListener("submit", function (event) {
        event.preventDefault();
        const formData = new FormData(uploadTemplateForm);

        fetch("/pdf/upload", {
            method: "POST",
            body: formData
        })
            .then(response => {
                if (response.ok) {
                    alert("Template uploaded successfully.");
                    loadTemplates();
                } else {
                    throw new Error("Upload failed");
                }
            })
            .catch(error => alert("Error: " + error.message));
    });

    deleteTemplateForm.addEventListener("submit", function (event) {
        event.preventDefault();
        const filename = templateList.value;

        if (!filename) {
            alert("Please select a template to delete.");
            return;
        }

        fetch(`/pdf/delete?filename=${filename}`, {
            method: "POST"
        })
            .then(response => {
                if (response.ok) {
                    alert("Template deleted successfully.");
                    loadTemplates();
                } else {
                    throw new Error("Delete failed");
                }
            })
            .catch(error => alert("Error: " + error.message));
    });

    refreshTemplatesButton.addEventListener("click", loadTemplates);
});
