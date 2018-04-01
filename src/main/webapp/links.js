
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
            buildElement("td", {"class": "favIcon"},
                buildElement("img", {"src": link["favIconURL"] || 'about:blank', "alt": link["title"] || ""})
            ),
            buildElement("td", {"class": "link"},
                buildElement("a", {"href": link["url"]}, link["title"] || link["url"])
            ),
            buildElement("td", {"class": "date"}, formatDate(link["created"]))
        )
    )
}

function addLinks(list) {
    for (let i = 0; i < list.length; i += 1) {
        addLink(list[i]);
    }
}

eventSource.addEventListener("message", e => addLink(JSON.parse(e.data)) )

fetch("links", {
    credentials: "include",
})
    .then(response => response.json())
    .then(json => addLinks(json))

$("#form").addEventListener("submit", e => {
    e.preventDefault();

    if ($("#linkInput").checkValidity()) {
        const form = e.target;
        const body = new URLSearchParams();
        const param = form.elements;
        for (let i = 0; i < param.length; i += 1) {
            body.append(param[i].name, param[i].value);
        }
        form.reset();
        fetch(form.action, {
            credentials: "include",
            method: "POST",
            body: body
        });
    } else {
        showMessage("Please enter a valid link");
    }
})