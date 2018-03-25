
const dayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];

function buildElement(tag, args) {

    const el = document.createElement(tag);

    for (key in args) {
        el.setAttribute(key, args[key]);
    }

    for (let i = 2; i < arguments.length; i += 1) {
        const content = arguments[i];

        switch (typeof content) {
            case "string":
            case "number":
                el.appendChild(document.createTextNode(content));
                break;
            default:
                el.appendChild(content);
        }
    }

    return el;
}

function $(selector) {
    return document.querySelector(selector);
}

function zeroPad(number, length) {
    let result = number.toString();

    while (result.length < length) {
        result = "0" + result;
    }

    return result;
}

function formatDate(timestamp) {
    const date = new Date(timestamp);

    let result = "";

    result += dayNames[date.getDay()] + ", "
    result += date.getDate() + " "
    result += monthNames[date.getMonth()] + " "
    result += date.getFullYear() + " "
    result += zeroPad(date.getHours()) + ":"
    result += zeroPad(date.getMinutes())

    return result;
}

var eventSource = new EventSource("backend");

function addLink(link) {
    $("#links").appendChild(
        buildElement("tr", undefined,
            buildElement("td", {"class": "date"}, formatDate(link["created"])),
            buildElement("td", {"class": "link"},
                buildElement("a", {"href": link["link"]}, link["link"])
            ),
            buildElement("td", {"class": "desc"}, link["description"] || '[No description]')
        )
    )
}

function addLinks(list) {
    for (let i = 0; i < list.length; i += 1) {
        addLink(list[i]);
    }
}

eventSource.addEventListener("message", e => addLink(JSON.parse(e.data)) )

fetch("links")
    .then(response => response.json())
    .then(json => addLinks(json))

$("#form").addEventListener("submit", e => {
    e.preventDefault();

    if ($("#linkInput").checkValidity()) {
        const form = e.target;
        form.submit();
        form.reset();
    } else {
        showMessage("Please enter a valid link");
    }
})

