/**
 * PrimeFaces Carousel Widget
 */
PrimeFaces.widget.Carousel = function(cfg) {
    this.cfg = cfg;
    this.id = this.cfg.id;
    this.jqId = PrimeFaces.escapeClientId(this.id);
    this.jq = $(this.jqId);
    this.viewport = this.jq.children('.ui-carousel-viewport');
    this.header = this.jq.children('.ui-carousel-header'),
    this.list = this.viewport.children('ul');
    this.items = this.list.children('.ui-carousel-item');
    this.prevButton = this.header.children('.ui-carousel-prev-button');
    this.nextButton = this.header.children('.ui-carousel-next-button');
    this.pageLinks = this.header.find('.ui-carousel-page-links .ui-carousel-page-link');
    this.dropdown = this.header.children('.ui-carousel-dropdown');
    this.state = $(this.jqId + '_first');
    
    //configuration
    this.cfg.numVisible = this.cfg.numVisible||3;
    this.cfg.pageLinks = this.cfg.pageLinks||3;
    this.cfg.effect = this.cfg.effect||'slide';
    this.cfg.effectDuration = this.cfg.effectDuration||500;
    this.cfg.easing = this.cfg.easing||'easeInOutCirc';
    this.cfg.pageCount = Math.ceil(this.items.length / this.cfg.numVisible);
    this.cfg.firstVisible = (this.cfg.firstVisible||0) % this.items.length;
    this.cfg.page = (this.cfg.firstVisible / this.cfg.numVisible) + 1;
    this.animating = false;
    
    var firstItem = this.items.filter(':first').get(0);
    this.cfg.itemOuterWidth = parseInt(this.getProperty(firstItem, 'width')) + parseInt(this.getProperty(firstItem, 'margin-Left')) + parseInt(this.getProperty(firstItem, 'margin-Right')) +  ((parseInt(this.getProperty(firstItem, 'border-Left-Width')) + parseInt(this.getProperty(firstItem, 'border-Right-Width'))));
    this.cfg.itemOuterHeight = parseInt(this.getProperty(firstItem, 'height')) + Math.max(parseInt(this.getProperty(firstItem, 'margin-Top')), parseInt(this.getProperty(firstItem, 'margin-Bottom'))) + ((parseInt(this.getProperty(firstItem, 'border-Top-Width')) + parseInt(this.getProperty(firstItem, 'border-Bottom-Width'))));

    //viewport width/height
    if(this.cfg.vertical) {
        this.viewport.width(this.cfg.itemOuterWidth);
        this.viewport.height(this.cfg.numVisible * this.cfg.itemOuterHeight);
    }
    else{
        this.viewport.width(this.cfg.numVisible * this.cfg.itemOuterWidth);
        this.viewport.height(this.cfg.itemOuterHeight);
    }
    this.jq.width(this.viewport.outerWidth(true));
  
    //set offset position
    this.setOffset(this.getItemPosition(this.cfg.firstVisible));

    this.checkButtons();

    this.bindEvents();
    
    this.jq.css({
        visibility: 'visible'
    });
    
    if(this.cfg.autoPlayInterval) {
        this.startAutoPlay();
    }
    
    this.postConstruct();
}

PrimeFaces.extend(PrimeFaces.widget.Carousel, PrimeFaces.widget.BaseWidget);

/**
 * Returns browser specific computed style property value.
 */
PrimeFaces.widget.Carousel.prototype.getProperty = function(item, prop){
    return $.browser.msie ? item.currentStyle.getAttribute(prop.replace(/-/g, "")) : document.defaultView.getComputedStyle(item, "").getPropertyValue(prop.toLowerCase());
}

/**
 * Autoplay startup.
 */
PrimeFaces.widget.Carousel.prototype.startAutoPlay = function(){
    var _self = this;
    if(this.cfg.autoPlayInterval){
        setInterval(function() {
            _self.next();
        }, this.cfg.autoPlayInterval);
    }
}

/**
 * Binds related mouse/key events.
 */
PrimeFaces.widget.Carousel.prototype.bindEvents = function(){
    var _self = this;
  
    this.pageLinks.click(function(e) {
        if(!_self.animating) {
            _self.setPage($(this).index() + 1);
        }
        
        e.preventDefault();
    });
    
    this.dropdown.change(function(e) {
        if(!_self.animating)
            _self.setPage(parseInt($(this).val()));
    });
  
    this.prevButton.click(function(e) {
        if(!_self.prevButton.hasClass('ui-state-disabled') && !_self.animating)
            _self.prev();
    });
  
    this.nextButton.click(function(){
        if(!_self.nextButton.hasClass('ui-state-disabled') && !_self.animating)
            _self.next();
    });
}

/**
 * Calculates position of list for a page index.
 */
PrimeFaces.widget.Carousel.prototype.getPagePosition = function(index) {
    return -((index - 1) * (this.cfg.vertical ? this.cfg.itemOuterHeight : this.cfg.itemOuterWidth) * this.cfg.numVisible);
}

/**
 * Calculates position of a given indexed item.
 */
PrimeFaces.widget.Carousel.prototype.getItemPosition = function(index){
    return -(index * (this.cfg.vertical ? this.cfg.itemOuterHeight : this.cfg.itemOuterWidth));
}

/**
 * Returns instant position of list.
 */
PrimeFaces.widget.Carousel.prototype.getPosition = function(){
    return parseInt(this.list.css(this.cfg.vertical ? 'top' : 'left'));
};

/**
 * Sets instant position of list.
 */
PrimeFaces.widget.Carousel.prototype.setOffset = function(val) {
    this.list.css(this.cfg.vertical ? {
        'top' : val
    } : {
        'left' : val
    });
};

/**
 * Fade animation for list transition.
 */
PrimeFaces.widget.Carousel.prototype.fade = function(val){
    var _self = this;
    this.list.animate(
    {
        opacity: 0
    }, 
    {
        duration: this.cfg.effectDuration / 2,
        specialEasing: {
            opacity : this.cfg.easing
        },
        complete: function() {
            _self.setOffset(val);
            $(this).animate( 
            {
                opacity: 1
            }, 
            {
                duration: _self.cfg.effectDuration / 2,
                specialEasing: {
                    opacity : _self.cfg.easing
                },
                complete: function() {
                    _self.animating = false;
                }
            });
        }
    });
}

PrimeFaces.widget.Carousel.prototype.slide = function(val){
    var _self = this,
    animateOption = this.cfg.vertical ? {
        top : val
    } : {
        left : val
    };
  
    this.list.animate( 
        animateOption, 
        {
            duration: this.cfg.effectDuration,
            easing: this.cfg.easing,
            complete: function() {
                _self.animating = false;
            }
        });
}

/**
 * Go next page
 */
PrimeFaces.widget.Carousel.prototype.next = function(){
   this.setPage(this.cfg.page + 1);
}

/**
 * Go previous page
 */
PrimeFaces.widget.Carousel.prototype.prev = function(){
   this.setPage(this.cfg.page - 1);
}

/**
 * Navigation to a given page index.
 */
PrimeFaces.widget.Carousel.prototype.setPage = function(index) {    
    if(this.cfg.isCircular)
        this.cfg.page = index > this.cfg.pageCount ? 1 : index < 1 ? this.cfg.pageCount : index;
    else
        this.cfg.page  = index;
  
    this.checkButtons();
    
    this.state.val((this.cfg.page - 1) * this.cfg.numVisible);
    
    var newPosition = this.getPagePosition(this.cfg.page);
  
    if(this.getPosition() == newPosition) {
        this.animating = false;
        return;
    }
    
    if(this.cfg.effect == 'fade')
        this.fade(newPosition);
    else
        this.slide(newPosition);
}

/**
 * Enables/Disables navigation controls
 */
PrimeFaces.widget.Carousel.prototype.checkButtons = function() {
    this.pageLinks.filter('.ui-icon-radio-on').removeClass('ui-icon-radio-on');
    this.pageLinks.eq(this.cfg.page - 1).addClass('ui-icon-radio-on');
    
    this.dropdown.val(this.cfg.page);
  
    //no bound
    if(this.cfg.isCircular)
        return;
  
    //lower bound
    if(this.cfg.page == 1){
        this.prevButton.addClass('ui-state-disabled');
    }
    else{
        this.prevButton.removeClass('ui-state-disabled');
    }
  
    //upper bound
    if(this.cfg.page >= this.cfg.pageCount){
        this.nextButton.addClass('ui-state-disabled');
    }
    else{
        this.nextButton.removeClass('ui-state-disabled');
    }
};