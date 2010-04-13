contacts.showEditor = function(){


}

contacts.showPreview = function (coords){
    var rx = 100 / coords.w;
    var ry = 100 / coords.h;

    $('#preview').css({
        width: Math.round(rx * 240) + 'px',
        height: Math.round(ry * 240) + 'px',
        marginLeft: '-' + Math.round(rx * coords.x) + 'px',
        marginTop: '-' + Math.round(ry * coords.y) + 'px'
    });
};


contacts.modifyProfilePic = function(ev){
    $('#imageEditorContainer').fadeIn();
    var test = $(ev.target).parent();
    test = $(ev.target).parent().first();
    var selectedImage = $(ev.target).parent().children().first();
    $('#result').empty();
    $('#preview_container').empty();
    $('#result').append("<img src='/public/images/bigger.jpg' id='mainImage'/>");
    $('#preview_container').append("<img src='"+selectedImage.attr('src')+"' id='preview'/>");
    $(function() {
        $('#mainImage').Jcrop({
            onChange: contacts.showPreview,
            onSelect: contacts.showPreview,
            aspectRatio: 1
        });
    });
}

contacts.pasteProfilePic = function(){

    var test = contacts.searcher.results;
    if(contacts.searcher.results.length >0){
        $($('.resultImageContainer')[contacts.imageCounter]).children('.resultImage').attr('src', contacts.searcher.results[0].url);
        for(var i = 0; i<contacts.searcher.results.length; i++){
            $($('.resultStock')[contacts.imageCounter]).append("<img src='"+contacts.searcher.results[i].url+"'/>");
        }
        $($('.resultActions')[contacts.imageCounter]).append("<a class='imageEditingLink'>Edit</a><a class='imageReplacerLink'>Replace</a>");
        $($('.resultActions')[contacts.imageCounter]).children('.imageEditingLink').click(function(){
            var message = $(this).parent().prev().text();
            alert(message);
        });
    }
    var block = $('.resultImageContainer')[contacts.imageCounter]
    contacts.imageCounter++;
    if(contacts.imageCounter < 32) contacts.doSearch();
}

contacts.imageCounter = 0;

contacts.initSearch = function(){
    contacts.searcher = new google.search.ImageSearch();
    contacts.searcher.setRestriction(google.search.Search.RESTRICT_SAFESEARCH,
            google.search.Search.SAFESEARCH_STRICT);
    contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,
            google.search.ImageSearch.IMAGESIZE_MEDIUM);
    /*contacts.setRestriction(google.search.ImageSearch.RESTRICT_IMAGETYPE,
                            google.search.ImageSearch.IMAGETYPE_FACES);*/
}

contacts.doSearch = function(){
    contacts.searcher.setSearchCompleteCallback(this, contacts.pasteProfilePic);
    var test = contacts.imageCounter;
    var tteesstt = $($('.resultImageContainer')[contacts.imageCounter]);
    var test2 = $($('.resultImageContainer')[contacts.imageCounter]).children('.resultText'); 
    var phrase = $($('.resultImageContainer')[contacts.imageCounter]).children('.resultText').text();
    phrase += " ";
    phrase += $($('.resultImageContainer')[contacts.imageCounter]).children('.resultMail').text();
    contacts.searcher.execute(phrase);
}

contacts.localInit = function(){
    contacts.init.functions.push(function(){
        $(".imageEditingLink").click(function(ev){contacts.modifyProfilePic(ev)});
    });
    contacts.init.functions.push(function(){
        $('#imageEditorContainer').css({
            "left": ($(window).width()-$('#imageEditorContainer').width())/2,
            "top": ($(window).height()-$('#imageEditorContainer').height())/2
        });
    });
    contacts.init.functions.push(function() {
        contacts.initSearch()   ;
    });
    contacts.init.functions.push(function() {
        $('#doSearch').click(contacts.doSearch);
    });
}




google.load('search', '1');
contacts.imgWidth = 0;
contacts.imgHeight = 0;
contacts.reset = function(){
    $('#result').empty();
    $('#preview_container').empty();
}
contacts.showPreview = function (coords){
    var rx = 100 / coords.w;
    var ry = 100 / coords.h;

    $('#preview').css({
        width: Math.round(rx * contacts.imgWidth) + 'px',
        height: Math.round(ry * contacts.imgHeight) + 'px',
        marginLeft: '-' + Math.round(rx * coords.x) + 'px',
        marginTop: '-' + Math.round(ry * coords.y) + 'px'
    });
};


contacts.OnSearchComplete = function() {
    if (contacts.results &&  contacts.results.length > 0) {
        var test = contacts.results;
        test = 1;

        for(var i = 0; i<contacts.results.length; i++){
            $('#result').append("<img src='"+contacts.results[i].url+"' class='gResult'/>");
        }
        $("#result img").click(function(ev){
            var selectedImage = $(ev.target);
            imgWidth = selectedImage.width();
            imgHeight = selectedImage.height();
            $('#result').empty();
            $('#result').append("<img src='"+selectedImage.attr('src')+"' id='mainImage'/>");
            $('#preview_container').append("<img src='"+selectedImage.attr('src')+"' id='preview'/>");
            $(function() {
                $('#mainImage').Jcrop({
                    onChange: showPreview,
                    onSelect: showPreview,
                    aspectRatio: 1
                });
            });
        });

    }
}



$(document).ready(contacts.localInit);