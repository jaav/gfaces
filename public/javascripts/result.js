
google.load('search', '1');
contacts.imgWidth = 0;
contacts.imgHeight = 0;

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

contacts.getImageUrl = function(url){
    if(url === '') return '/public/images/contact.gif';
    else return url.replace('%3F', '?').replace('%3D', '=');
}


contacts.showPreview = function (coords){
    var rx = 96 / coords.w;
    var ry = 96 / coords.h;

    $('#preview').css({
        width: Math.round(rx * contacts.imgWidth) + 'px',
        height: Math.round(ry * contacts.imgHeight) + 'px',
        marginLeft: '-' + Math.round(rx * coords.x) + 'px',
        marginTop: '-' + Math.round(ry * coords.y) + 'px'
    });
};

contacts.pasteProfilePic = function(){

    var test = contacts.searcher.results;
    if(contacts.searcher.results.length >0){
        $($('.resultImage')[contacts.imageCounter]).children('.resultImageContent').attr('src', contacts.getImageUrl(contacts.searcher.results[0].url+"#0"));
        $($('.resultImageRaw')[contacts.imageCounter]).append("<img src='"+contacts.getImageUrl(contacts.searcher.results[0].url)+"#0'/>");
        for(var i = 0; i<contacts.searcher.results.length; i++){
            $($('.resultStock')[contacts.imageCounter]).append("<img src='"+contacts.getImageUrl(contacts.searcher.results[i].url)+"#"+i+"'/>");
        }
        $($('.resultStock')[contacts.imageCounter]).append("<img src='/public/images/contact.gif'/>");
        if($($('.resultImageOriginal')[contacts.imageCounter]).children('img'))
            $($('.resultStock')[contacts.imageCounter]).append("<img src='"+$($('.resultImageOriginal')[contacts.imageCounter]).children('img').attr('src')+"'/>"); 
        
        var container = $($('.resultActions')[contacts.imageCounter]).append("<a class='imageEditingLink' href='#'>Edit</a>");
        container.children().click(function(ev){
            ev.preventDefault();
            $('#imageEditorContainer').css('display', 'block');
            var selectedImage = $(this).parents('.resultImageContainer').find('.resultImageRaw > img');
            contacts.imgWidth = selectedImage.width();
            contacts.imgHeight = selectedImage.height();
            $('#result').empty();
            $('#result').append("<img src='"+selectedImage.attr('src')+"' id='mainImage'/>");
            $('#preview_container').append("<img src='"+selectedImage.attr('src')+"' id='preview'/>");
            $(function() {
                $('#mainImage').Jcrop({
                    onChange: contacts.showPreview,
                    onSelect: contacts.showPreview,
                    aspectRatio: 1
                });
            });
        });
        $($('.resultImage')[contacts.imageCounter]).children('.resultImageContent').click(function(){
            var test = $(this).parent();
            test = $(this).parent().nextAll('.resultStock');
            var stock = $(this).parent().nextAll('.resultStock').children('img');
            for(var i = 0; i<stock.length; i++){
                if($(this).attr('src') === $(stock[i]).attr('src')){
                    if(stock[i+1]) $(this).attr('src', $(stock[i+1]).attr('src'));
                    else $(this).attr('src', $(stock[0]).attr('src'));
                    $(this).parent().next('.resultImageRaw').children('img').attr('src', $(this).attr('src'));
                    break;
                }
            }
        });
    }
    var block = $('.resultImageContainer')[contacts.imageCounter]
    contacts.imageCounter++;
    if(contacts.imageCounter < contacts.totalImages) contacts.doSearch();
}

contacts.imageCounter = 0;

contacts.initSearch = function(form){
    contacts.searcher = new google.search.ImageSearch();
    var params = $($(form).serializeArray());
    params.each(function(index, item){
        if(this.name === 'dimension'){
            if(this.value === 'small')
                contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,["small"]);
            else if(this.value === 'medium')
                contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,["medium"]);
            else
                contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,["small", "medium"]);
        }
        if(this.name === 'imageType'){
            if(this.value === 'faces')
                contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGETYPE,
                            google.search.ImageSearch.IMAGETYPE_FACES);
        }
        if(this.name === 'searchbase'){
            if(this.value === 'name')
                contacts.nameSearch = true;
            if(this.value === 'mail')
                contacts.mailSearch = true;
        }
        if(this.name === 'phrase'){
            contacts.phraseExtension = this.value;
        }
        if(this.name === 'resultSet'){
            if(this.value === 'normal')
                contacts.searcher.setResultSetSize(google.search.Search.SMALL_RESULTSET);
            else if(this.value === 'large')
                contacts.searcher.setResultSetSize(google.search.Search.LARGE_RESULTSET);
        }

    });
    /*contacts.searcher.setRestriction(google.search.Search.RESTRICT_SAFESEARCH,
            google.search.Search.SAFESEARCH_STRICT);*/
    /*contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,
            google.search.ImageSearch.IMAGESIZE_SMALL);*/
    /*contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,
            google.search.ImageSearch.IMAGESIZE_MEDIUM);*/
}

contacts.executeSearch = function(phrase){
    if($($('.resultFixer')[contacts.imageCounter]).children('input').attr('checked')){
        contacts.imageCounter++;
        contacts.doSearch()
    }
    else contacts.searcher.execute(phrase);
}

contacts.doSearch = function(){
    contacts.searcher.setSearchCompleteCallback(this, contacts.pasteProfilePic);
    var phrase = '';
    if(contacts.nameSearch) phrase += $($('.resultImageContainer')[contacts.imageCounter]).children('.resultText').text();
    if(contacts.mailSearch) phrase += ' '+$($('.resultImageContainer')[contacts.imageCounter]).children('.resultMail').text();
    phrase += ' '+contacts.phraseExtension;
    contacts.executeSearch(phrase);
}


//empty all dynamically generated html blocks, except those of the fixed images
contacts.pageReset = function(){
    contacts.imageCounter = 0;
    $('.resultImageContainer').each(function(){
        var test = $(this).find('input');
        if(!$(this).find('input').attr('checked')){
            $(this).children('.resultStock').empty();
            $(this).children('.resultActions').empty();
        }
    });
}

contacts.localInit = function(){
    contacts.totalImages = $('.resultBlock').length;    
    contacts.init.functions.push(function(){
        $(".imageEditingLink").click(function(ev){contacts.modifyProfilePic(ev)});
    });
    /*contacts.init.functions.push(function(){
        $('#imageEditorContainer').css({
            "left": ($(window).width()-$('#imageEditorContainer').width())/2,
            "top": ($(window).height()-$('#imageEditorContainer').height())/2
        });
    });*/
    contacts.init.functions.push(function() {
        $('#searchform').submit(function(ev){
            ev.preventDefault();
            //setting counter back to 0. Needed for the next time the user clicks the search button
            contacts.pageReset();
            contacts.initSearch(this);
            contacts.doSearch();
        })
    });
}




contacts.OnSearchComplete = function() {
    if (contacts.results &&  contacts.results.length > 0) {
        var test = contacts.results;
        test = 1;

        for(var i = 0; i<contacts.results.length; i++){
            $('#result').append("<img src='"+contacts.results[i].url+"' class='gResult'/>");
        }
        $("#result img")

    }
}



$(document).ready(contacts.localInit);