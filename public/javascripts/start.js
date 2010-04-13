contacts.validate = function(form){
    alert('validating '+form.email.value+' ...');
    window.location.href = 'authenticate?email='+form.email.value;
};

contacts.localInit = function(){
    contacts.init.functions.push(function(){
        $('#startForm').submit(function(ev){
            contacts.validate(this);
            ev.preventDefault();
        });
    });
}

$(document).ready(contacts.localInit);