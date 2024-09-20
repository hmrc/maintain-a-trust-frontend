$(document).ready(function() {

    $("#download-pdf").submit(function(event) {
        var identifier = document.querySelector("#download-pdf").dataset.identifier;
        var response = document.querySelector('input[name="value"]:checked').value;
        if (identifier !== undefined && response === "true" && (identifier.length === 15 || identifier.length === 10)) {
            event.preventDefault();
            var pdfUrl = jsRoutes.controllers.ObligedEntityPdfController.getPdf(identifier).url
            /**
            * using ajax for downloading pdf
            * if response is ok, then it opens new window for downloading pdf and logs out user from system
            * if response is not ok, then shows the error page in the same window
            **/
            $.ajax({
                url:pdfUrl,
                success : function(){
                    window.open(pdfUrl)
                    window.location = jsRoutes.controllers.LogoutController.logout().url
                },
                error: function(response, status, errorMsg){
                    window.location.href = pdfUrl
                }
            });

        }
    });

})

