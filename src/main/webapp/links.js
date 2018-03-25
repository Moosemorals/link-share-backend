


var eventSource = new EventSource("backend");

eventSource.onmessage = function (e) {

    console.log(e.data);

}