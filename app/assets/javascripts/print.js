$(document).ready(function() {

    function supportsPrint() {
        return (typeof window.print === 'function');
    }

    if(supportsPrint()) {
        window.print();
    }

});