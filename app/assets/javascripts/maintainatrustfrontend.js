$(document).ready(function() {

    $("#download-pdf").submit(function(event) {

        var identifier = document.querySelector("#download-pdf").dataset.identifier;
        var response = document.querySelector('input[name="value"]:checked').value;

        if (identifier !== undefined && response === "true" && (identifier.length === 15 || identifier.length === 10)) {

            event.preventDefault();

            var canLoadPdf = false;
            var pdfUrl = jsRoutes.controllers.ObligedEntityPdfController.getPdf(identifier).url

            /**
             *  ajax is used to check if we can download a pdf for the given identifier
             *    - on success, open a new window for downloading pdf and log user out.
             *    - on failure, show the error page in the same window.
             *
             *  some browsers stop calls to window.open within an ajax call,
             *  so we defer the download until after the ajax call has completed.
             */
            $.ajax({
                url: pdfUrl,
                async: false, // wait for this call to complete before 'canLoadPdf' condition check
                success: function() {
                    canLoadPdf = true;
                },
                error: function(response, status, errorMsg) {
                    window.location.href = pdfUrl
                }
            });

            if(canLoadPdf) {
                window.open(pdfUrl);
                window.location = jsRoutes.controllers.LogoutController.logout().url;
            }
        }
    });

})

