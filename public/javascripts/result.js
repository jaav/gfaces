
google.load('search', '1');
contacts.imgWidth = 0;
contacts.imgHeight = 0;



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

contacts.modifyProfilePic = function(ev){
    var container = $(ev.target).parents('.resultImageContainer');
    $('#imageEditorContainer').css({
        "left": ($(window).width()-$('#imageEditorContainer').width())/2,
        "top": ($(window).height()-$('#imageEditorContainer').height())/2
    });
    $('#imageEditorContainer').fadeIn();
    var selectedImage = container.find('.resultImageContent');
    contacts.imgWidth = selectedImage.width();
    contacts.imgHeight = selectedImage.height();
    $('#result').empty();
    $('#preview_container').empty();
    $('#result').append("<img src='"+selectedImage.attr('src')+"' id='mainImage'/>");
    $('#preview_container').append("<img src='"+selectedImage.attr('src')+"' id='preview'/>");
    var confirm = $('<button>Confirm</button>').click(function(ev){
        selectedImage.attr('style', $(ev.target).parents('#imageEditorContainer').find('#preview').attr('style'));
        $('#imageEditorContainer').fadeOut();
    });
    $('.confirmActions').empty().append(confirm);
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


contacts.pasteProfilePic = function(){

    var test = contacts.searcher;
    if(contacts.searcher.results.length >0){
        $($('.resultImage')[contacts.imageCounter]).children('.resultImageContent').attr('src', contacts.getImageUrl(contacts.searcher.results[0].url+"#0"));
        $($('.resultImageRaw')[contacts.imageCounter]).append("<img src='"+contacts.getImageUrl(contacts.searcher.results[0].url)+"#0'/>");
        for(var i = 0; i<contacts.searcher.results.length; i++){
            $($('.resultStock')[contacts.imageCounter]).append("<img src='"+contacts.getImageUrl(contacts.searcher.results[i].url)+"#"+i+"'/>");
        }
        $($('.resultStock')[contacts.imageCounter]).append("<img src='/public/images/contact.gif'/>");
        if($($('.resultImageOriginal')[contacts.imageCounter]).children('img'))
            $($('.resultStock')[contacts.imageCounter]).append("<img src='"+$($('.resultImageOriginal')[contacts.imageCounter]).children('img').attr('src')+"'/>"); 
        var edit = $("<a class='imageEditingLink' href='#'>Edit</a>").click(function(ev){
            contacts.modifyProfilePic(ev)
        });
        $($('.resultActions')[contacts.imageCounter]).append(edit);

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
    if(!contacts.searcher) contacts.searcher = new google.search.ImageSearch();
    contacts.resetSearcherObject();
    var params = $($(form).serializeArray());
    params.each(function(index, item){
        if(this.name === 'dimension'){
            if(this.value === 'small')
                contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,["small"]);
            else if(this.value === 'medium')
                contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,["medium"]);
            else if(this.value === 'large')
                contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,["large"]);
            else
                contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGESIZE,["icon", "small", "medium", "large"]);
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
        if(this.name === 'domain'){
            contacts.domainSearch = this.value;
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
    var betweener = '';
    if(contacts.nameSearch && contacts.mailSearch) betweener = ' OR ';
    if(contacts.nameSearch) phrase += jQuery.trim($($('.resultImageContainer')[contacts.imageCounter]).children('.resultText').text());
    phrase += betweener;
    if(contacts.mailSearch) phrase += jQuery.trim($($('.resultImageContainer')[contacts.imageCounter]).children('.resultMail').text());
    //if(jQuery.trim(contacts.phraseExtension)) phrase += ' '+jQuery.trim(contacts.phraseExtension);
    //contacts.executeSearch(phrase.replace(' ', '%20'));
    if(contacts.domainSearch) contacts.searcher.setSiteRestriction(contacts.domainSearch);
    contacts.executeSearch(contacts.cleanSearchObject(phrase));
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

contacts.selectAllWithImages = function(){
    $(".resultImageContent").each(function(){
        if($(this).attr('src') === '/public/images/contact.gif') $(this).parents(".resultImageContainer").find("input").attr('checked', false);
        else $(this).parents(".resultImageContainer").find("input").attr('checked', true);
    })
}

contacts.selectAll = function(){
    $(".resultImageContent").each(function(){
        $(this).parents(".resultImageContainer").find("input").attr('checked', true);
    })
}

contacts.unselectAll = function(){
    $(".resultImageContent").each(function(){
        $(this).parents(".resultImageContainer").find("input").attr('checked', false);
    })
}

contacts.cleanSearchObject = function(phrase){
    return phrase.replace(/[^a-zA-Z 0-9@\\.]+/g,'');

}

contacts.resetSearcherObject = function(){
    contacts.nameSearch = false;
    contacts.mailSearch = false;
    contacts.searcher.setRestriction(google.search.ImageSearch.RESTRICT_IMAGETYPE);
    contacts.searcher.setSiteRestriction(null);
}

contacts.doSubmit = function(){
    var imagePoster = $('#imagePoster');
    $('.resultImageContainer').each(function(index){
        imagePoster.append($('<input type="hidden" name="image_'+index+'" value="'+$(this).find('.resultImage > img').attr('src')+'" />'));
        imagePoster.append($('<input type="hidden" name="id_'+index+'" value="'+$(this).find('.resultId').text()+'" />'));
        imagePoster.append($('<input type="hidden" name="style_'+index+'" value="'+$(this).find('.resultImage > img').attr('style')+'" />'));

    });
    imagePoster.submit();
}

contacts.hide = function(id){
    $(id).fadeOut();
}

contacts.localInit = function(){
    contacts.totalImages = $('.resultBlock').length;
    /*contacts.init.functions.push(function(){
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



$(document).ready(contacts.localInit);