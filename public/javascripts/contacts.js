var contacts = {};

/*contacts.preInit = function(){
    var functions = [];
    for(var i = 0; i<functions.length; i++){
        functions[i]();
    }
}*/

contacts.init = {
    functions : [],
    start: function(){
        for(var i = 0; i<this.functions.length; i++){   
            this.functions[i].apply();
        }
    }
}

/*
contacts.postInit = function(){
    var functions = [];
    for(var i = 0; i<functions.length; i++){
        functions[i]();
    }
}*/
