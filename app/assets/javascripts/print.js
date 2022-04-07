$(document).ready(function() {

    const printButton = document.getElementById("print-button")
    printButton.addEventListener("click", print)

    // required because using just "window.print()" in the above event listener causes it to fire on page load
    function print(){
        window.print()
    }

    function beforePrintCall(){
        printButton.removeEventListener("click", print)
        if($('.no-details').length > 0){
            // store current focussed element to return focus to later
            var fe = document.activeElement;
            // store scroll position
            var scrollPos = window.pageYOffset;
            $('details').not('.open').each(function(){
                $(this).find('summary').trigger('click');
            });
            // blur focus off current element in case original cannot take focus back
            $(document.activeElement).blur();
            // return focus if possible
            $(fe).focus();
            // return to scroll pos
            window.scrollTo(0,scrollPos);
        } else {
            $('details').attr("open","open");
        }
    }

    function afterPrintCall(){
        if($('.no-details').length > 0){
            // store current focussed element to return focus to later
            var fe = document.activeElement;
            // store scroll position
            var scrollPos = window.pageYOffset;
            $('details').each(function(){
                $(this).find('summary').trigger('click');
            });
            // blur focus off current element in case original cannot take focus back
            $(document.activeElement).blur();
            // return focus if possible
            $(fe).focus();
            // return to scroll pos
            window.scrollTo(0,scrollPos);
        } else {
            $('details').removeAttr("open");
        }
        printButton.addEventListener("click", print)
    }

    //Chrome
    if(typeof window.matchMedia != 'undefined'){
        mediaQueryList = window.matchMedia('print');
        mediaQueryList.addListener(function(mql) {
            if (mql.matches) {
                beforePrintCall();
            };
            if (!mql.matches) {
                afterPrintCall();
            };
        });
    }

    //Firefox and IE (above does not work)
    window.onbeforeprint = function(){
        beforePrintCall();
    }
    window.onafterprint = function(){
        afterPrintCall();
    }

});


