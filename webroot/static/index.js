reFetch()

function insertRow(){
    let table = document.querySelector('#myTable');
    let row = table.insertRow();

    let urlCell = row.insertCell();
    urlCell.innerHTML = '<input id = "url" type="text"  class="form-control" placeholder="Type url"  />';

    let nameCell = row.insertCell();
    nameCell.innerHTML = '<input id = "name" type="text" class="form-control" placeholder="Type name"  />';

    row.insertCell();
    row.insertCell();

    let actionsCell = row.insertCell();
    actionsCell.innerHTML = '<button id = "save"  class="btn"  onclick= "saveService(this)">Save</button><button id = "delete" onclick="deleteService(this)">Delete</button>';

    return row;
}


function saveService(element){

    let row = element.closest('tr');
    let urlInput = row.cells[0].firstChild;
    let nameInput = row.cells[1].firstChild;
    let url = urlInput.value;
    let name = nameInput.value;
    if (!isUrlValid(url)) {
        alert('Please enter a valid url');
    }
    else {
        let saveResponse = postEndpoint('/service',
            JSON.stringify({url: url, name: name}));
        saveResponse.then(function (response) {
            return response.text().then(function (text) {
                if (text == "OK") {
                    element.innerHTML = "Saved";
                    element.disabled = true;
                    let statusText = row.cells[3];
                    statusText.innerHTML = "Waiting";
                } else
                    alert("Save error")
            });
        });
    }

}

function deleteService(element){
    let table = document.querySelector("#myTable");
    let row = element.closest('tr');
    let urlInput = row.cells[0].firstChild;
    let url = urlInput.value;
    let deleteResponse = postEndpoint('/delete', JSON.stringify({url:url}));
    deleteResponse.then(function (response) {
        return response.text().then(function (text) {
            if (text == "OK"){
                table.deleteRow(row.rowIndex);
            } else {
                alert("delete error")
            }
        });
    });
}

function fetchServices(){
    let servicesRequest = new Request('/service');
    fetch(servicesRequest)
    .then(function(response) {
        return response.json();
    })
    .then(function(serviceList) {
        serviceList.forEach(service => {
            updateStatus(service.url, service.createdAt, service.status);
        })
    })
}

function reFetch(){
    let servicesRequest = new Request('/service');
    fetch(servicesRequest)
    .then(function(response) {
        return response.json();
    })
    .then(function(serviceList) {
        serviceList.forEach(service => {
             let row = insertRow();
            let urlInput = row.cells[0].firstChild;
            let nameInput = row.cells[1].firstChild;
            let dateText = row.cells[2];
            let statusText = row.cells[3];
            let saveButton = row.cells[4].firstChild;

            urlInput.value = service.url;
            nameInput.value = service.name;
            dateText.innerHTML = service.createdAt;
            statusText.innerHTML = service.status;
            statusColoring(statusText, service.status);
            urlInput.disabled = true;
            nameInput.disabled = true;
            saveButton.disabled = true;
            saveButton.innerHTML = "Saved";
        })
    })
}

function updateStatus(url, date, status){
    let table = document.querySelector('#myTable');
    for (let i = 1; i < table.rows.length; i++){
        let row = table.rows[i];
        let urlInput = row.cells[0].firstChild;
        let dateText = row.cells[2];
        let statusText = row.cells[3];
        dateText.innerHTML = date;
        if (url === urlInput.value){
            statusText.innerHTML = status;
            statusColoringAnimation(statusText, status);
            break;
        }
    }
}

setInterval(function(){
    fetchServices();
}, 2000);

function isUrlValid(url) {
    var regexp = /^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:/?#[\]@!\$&'\(\)\*\+,;=.]+$/;

    if(regexp.test(url)) {
        return true;
    }

    return false;
}

async function postEndpoint(endpoint, body) {
    return  fetch(endpoint, {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: body
    });
}

function statusColoring(status, value){
    if(value == "OK")
        status.style.color = "green";
    else {
        if (value == "FAIL")
            status.style.color = "red";
        else
            status.style.color = "blue";
    }
}

function statusColoringAnimation(status, value){
    if(value == "OK") {
        status.style.color = "black";
        setTimeout(() => { status.style.color = "green";}, 500);
    }
    else {
        if (value == "FAIL") {
            status.style.color = "black";
            setTimeout(() => {
                status.style.color = "red";
            }, 500);
        } else {
            status.style.color = "black";
            setTimeout(() => {
                status.style.color = "blue";}, 500);
        }
    }
}

