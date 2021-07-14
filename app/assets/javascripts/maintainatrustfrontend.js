$(document).ready(function () {

    $("#download-pdf").submit(function(event) {
        var identifier = document.querySelector("#download-pdf").dataset.identifier;
        var response = document.querySelector('input[name="value"]:checked').value;
        if (identifier !== undefined && response === "true" && (identifier.length === 15 || identifier.length === 10)) {
            event.preventDefault();
            window.open(jsRoutes.controllers.ObligedEntityPdfController.getPdf(identifier).url);
            window.location = jsRoutes.controllers.LogoutController.logout().url;
        }
    })

}
