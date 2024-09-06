$(document).ready(function() {

    $("#download-pdf").submit(function(event) {
        var identifier = document.querySelector("#download-pdf").dataset.identifier;
        var response = document.querySelector('input[name="value"]:checked').value;
        if (identifier !== undefined && response === "true" && (identifier.length === 15 || identifier.length === 10)) {
            event.preventDefault();
            fetch(jsRoutes.controllers.ObligedEntityPdfController.getPdf(identifier).url)
              .then(function(response) {
                if (!response.ok) {
                    return response.text();
                }else{
                    return response.blob();
                }
              })
              .then(function(data) {
                  if(data instanceof Blob){
                     const url = window.URL.createObjectURL(data);
                     window.open(url)
                     window.location = jsRoutes.controllers.LogoutController.logout().url
                  }else{
                    document.body.innerHTML = data
                  }
              });
        }
    });

})

