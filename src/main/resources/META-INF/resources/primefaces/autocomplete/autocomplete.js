                /**
 * PrimeFaces AutoComplete Widget
 */
PrimeFaces.widget.AutoComplete = PrimeFaces.widget.BaseWidget.extend({
    
    init: function(cfg) {
        this._super(cfg);
        
        this.panelId = this.jqId + '_panel';
        this.input = $(this.jqId + '_input');
        this.hinput = $(this.jqId + '_hinput');
        this.panel = this.jq.children(this.panelId);
        this.dropdown = this.jq.children('.ui-button');
        this.disabled = this.input.is(':disabled');
        this.active = true;
        this.cfg.pojo = this.hinput.length == 1;
        this.cfg.minLength = this.cfg.minLength != undefined ? this.cfg.minLength : 1;
        this.cfg.delay = this.cfg.delay != undefined ? this.cfg.delay : 300;
        this.cfg.cache = this.cfg.cache||false;
        
        if(this.cfg.cache) {
            this.initCache();
        }
        
        //pfs metadata
        this.input.data(PrimeFaces.CLIENT_ID_DATA, this.id);
        this.hinput.data(PrimeFaces.CLIENT_ID_DATA, this.id);
        
        if(!this.disabled) {
            if(this.cfg.multiple) {
                this.setupMultipleMode();

                this.multiItemContainer.data('primefaces-overlay-target', true).find('*').data('primefaces-overlay-target', true);
            } 
            else {
                //visuals
                PrimeFaces.skinInput(this.input);

                this.input.data('primefaces-overlay-target', true).find('*').data('primefaces-overlay-target', true);
                this.dropdown.data('primefaces-overlay-target', true).find('*').data('primefaces-overlay-target', true);
            }
            
            //core events
            this.bindStaticEvents();

            //client Behaviors
            if(this.cfg.behaviors) {
                PrimeFaces.attachBehaviors(this.input, this.cfg.behaviors);
            }
            
            //force selection
            if(this.cfg.forceSelection) {
                this.setupForceSelection();
            }

            //Panel management
            this.appendPanel();

            //itemtip
            if(this.cfg.itemtip) {
                this.itemtip = $('<div id="' + this.id + '_itemtip" class="ui-autocomplete-itemtip ui-state-highlight ui-widget ui-corner-all ui-shadow"></div>').appendTo(document.body);
                this.cfg.itemtipMyPosition = this.cfg.itemtipMyPosition||'left top';
                this.cfg.itemtipAtPosition = this.cfg.itemtipAtPosition||'right bottom';
                this.cfg.checkForScrollbar = (this.cfg.itemtipAtPosition.indexOf('right') !== -1);
            }

            //dialog support
            this.setupDialogSupport();
        }
        
    },
    
    appendPanel: function() {
        if(this.cfg.appendTo) {
            if(this.cfg.appendTo !== this.id) {
                this.panel.appendTo(PrimeFaces.expressions.SearchExpressionFacade.resolveComponentsAsSelector(this.cfg.appendTo));
            }
        }
        else {
            $(document.body).children(this.panelId).remove();
            this.panel.appendTo(document.body);
        }
    },
        
    initCache: function() {
        this.cache = {};
        var $this=this;
        
        this.cacheTimeout = setInterval(function(){
            $this.clearCache();
        }, this.cfg.cacheTimeout);        
    }, 
            
    clearCache: function() {
        this.cache = {};      
    },
    
    /**
     * Binds events for multiple selection mode
     */
    setupMultipleMode: function() {
        var $this = this;
        this.multiItemContainer = this.jq.children('ul');
        this.inputContainer = this.multiItemContainer.children('.ui-autocomplete-input-token');

        this.multiItemContainer.hover(function() {
                $(this).addClass('ui-state-hover');
            },
            function() {
                $(this).removeClass('ui-state-hover');
            }
        ).click(function() {
            $this.input.focus();
        });

        //delegate events to container
        this.input.focus(function() {
            $this.multiItemContainer.addClass('ui-state-focus');
        }).blur(function(e) {
            $this.multiItemContainer.removeClass('ui-state-focus');
        });

        var closeSelector = '> li.ui-autocomplete-token > .ui-autocomplete-token-icon';
        this.multiItemContainer.off('click', closeSelector).on('click', closeSelector, null, function(event) {
            $this.removeItem(event, $(this).parent());
        });
    },
    
    setupDialogSupport: function() {
        var dialog = this.jq.parents('.ui-dialog:first');

        if(dialog.length == 1) {
            this.panel.css('position', 'fixed');
            
            if(this.cfg.itemtip) {
                this.itemtip.css('position', 'fixed');
            }
        }
    },
    
    bindStaticEvents: function() {
        var _self = this;
 
        this.bindKeyEvents();

        this.dropdown.mouseover(function() {
            if(!_self.disabled) {
                $(this).addClass('ui-state-hover');
            }
        }).mouseout(function() {
            if(!_self.disabled) {
                $(this).removeClass('ui-state-hover');
            }
        }).mousedown(function() {
            if(!_self.disabled && _self.active) {
                $(this).addClass('ui-state-active');
            }
        }).mouseup(function() {
            if(!_self.disabled && _self.active) {
                $(this).removeClass('ui-state-active');

                _self.search('');
                _self.input.focus();
            }
        }).focus(function() {
            $(this).addClass('ui-state-focus');
        }).blur(function() {
            $(this).removeClass('ui-state-focus');
        }).keydown(function(e) {
            var keyCode = $.ui.keyCode;
            
            if(e.which == keyCode.ENTER || e.which == keyCode.NUMPAD_ENTER) {
                _self.search('');
                _self.input.focus();
                
                e.preventDefault();
            }
        });

        //hide overlay when outside is clicked
        var offset;
        $(document.body).bind('mousedown.ui-autocomplete', function (e) {
            if(_self.panel.is(":hidden")) {
                return;
            }
            offset = _self.panel.offset();
            if(e.target === _self.input.get(0)) {
                return;
            }
            if (e.pageX < offset.left ||
                e.pageX > offset.left + _self.panel.width() ||
                e.pageY < offset.top ||
                e.pageY > offset.top + _self.panel.height()) {
                _self.hide();
            }
        });
        
        this.resizeNS = 'resize.' + this.id;
        this.unbindResize();
        this.bindResize();
    },
    
    bindResize: function() {
        var _self = this;

        $(window).bind(this.resizeNS, function(e) {
            if(_self.panel.is(':visible')) {
                _self.alignPanel();
            }
        });
    },
    
    unbindResize: function() {
        $(window).unbind(this.resizeNS);
    },
    
    bindKeyEvents: function() {
        var _self = this;
        
        //bind keyup handler
        this.input.keyup(function(e) {
            var keyCode = $.ui.keyCode,
            key = e.which,
            shouldSearch = true;

            if (key == keyCode.ESCAPE) {
                _self.hide();
                shouldSearch = false;
            }
            else if(key == keyCode.UP 
                || key == keyCode.LEFT 
                || key == keyCode.DOWN 
                || key == keyCode.RIGHT 
                || key == keyCode.TAB 
                || key == keyCode.SHIFT 
                || key == keyCode.ENTER
                || key == keyCode.NUMPAD_ENTER) {
                shouldSearch = false;
            } 
            else if(_self.cfg.pojo && !_self.cfg.multiple) {
                _self.hinput.val($(this).val());
            }

            if(shouldSearch) {
                var value = _self.input.val();

                if(!value.length) {
                    _self.hide();
                }

                if(value.length >= _self.cfg.minLength) {

                    //Cancel the search request if user types within the timeout
                    if(_self.timeout) {
                        clearTimeout(_self.timeout);
                    }

                    _self.timeout = setTimeout(function() {
                        _self.search(value);
                    }, 
                    _self.cfg.delay);
                }
            }
            
        }).keydown(function(e) {
            if(_self.panel.is(':visible')) {
                var keyCode = $.ui.keyCode,
                highlightedItem = _self.items.filter('.ui-state-highlight');

                switch(e.which) {
                    case keyCode.UP:
                        var prev = highlightedItem.length == 0 ? _self.items.eq(0) : highlightedItem.prevAll('.ui-autocomplete-item:first');
                        
                        if(prev.length == 1) {
                            highlightedItem.removeClass('ui-state-highlight');
                            prev.addClass('ui-state-highlight');
                            
                            if(_self.cfg.scrollHeight) {
                                PrimeFaces.scrollInView(_self.panel, prev);
                            }
                            
                            if(_self.cfg.itemtip) {
                                _self.showItemtip(prev);
                            }
                        }
 
                        e.preventDefault();
                        break;

                    case keyCode.DOWN:
                        var next = highlightedItem.length == 0 ? _self.items.eq(0) : highlightedItem.nextAll('.ui-autocomplete-item:first');
                        
                        if(next.length == 1) {
                            highlightedItem.removeClass('ui-state-highlight');
                            next.addClass('ui-state-highlight');
                            
                            if(_self.cfg.scrollHeight) {
                                PrimeFaces.scrollInView(_self.panel, next);
                            }
                            
                            if(_self.cfg.itemtip) {
                                _self.showItemtip(next);
                            }
                        }
                        
                        e.preventDefault();
                        break;

                    case keyCode.ENTER:
                    case keyCode.NUMPAD_ENTER:
                        highlightedItem.click();

                        e.preventDefault();
                        break;

                    case keyCode.ALT: 
                    case 224:
                        break;

                    case keyCode.TAB:
                        highlightedItem.trigger('click');
                        _self.hide();
                        break;
                }
            }

        });
    },
    
    bindDynamicEvents: function() {
        var _self = this;

        //visuals and click handler for items
        this.items.bind('mouseover', function() {
            var item = $(this);
            
            if(!item.hasClass('ui-state-highlight')) {
                _self.items.filter('.ui-state-highlight').removeClass('ui-state-highlight');
                item.addClass('ui-state-highlight');
                
                if(_self.cfg.itemtip) {
                    _self.showItemtip(item);
                }
            }
        })
        .bind('click', function(event) {
            var item = $(this),
            itemValue = item.attr('data-item-value');

            if(_self.cfg.multiple) {
                var itemDisplayMarkup = '<li data-token-value="' + item.attr('data-item-value') + '"class="ui-autocomplete-token ui-state-active ui-corner-all ui-helper-hidden">';
                itemDisplayMarkup += '<span class="ui-autocomplete-token-icon ui-icon ui-icon-close" />';
                itemDisplayMarkup += '<span class="ui-autocomplete-token-label">' + item.attr('data-item-label') + '</span></li>';

                _self.inputContainer.before(itemDisplayMarkup);
                _self.multiItemContainer.children('.ui-helper-hidden').fadeIn();
                _self.input.val('').focus();
             
                _self.hinput.append('<option value="' + itemValue + '" selected="selected"></option>');
            }
            else {
                _self.input.val(item.attr('data-item-label')).focus();

                if(_self.cfg.pojo) {
                    _self.hinput.val(itemValue);            
                }
            }

            _self.invokeItemSelectBehavior(event, itemValue);

            _self.hide();
        });
    },
    
    showItemtip: function(item) {
        var content = item.is('li') ? item.next('.ui-autocomplete-itemtip-content') : item.children('td:last');
            
        this.itemtip.html(content.html())
                    .css({
                        'left':'', 
                        'top':'', 
                        'z-index': ++PrimeFaces.zindex,
                        'width': content.outerWidth()
                    })
                    .position({
                        my: this.cfg.itemtipMyPosition
                        ,at: this.cfg.itemtipAtPosition
                        ,of: item
                    });
                    
        //scrollbar offset
        if(this.cfg.checkForScrollbar) {
            if(this.panel.innerHeight() < this.panel.children('.ui-autocomplete-items').outerHeight(true)) {
                var panelOffset = this.panel.offset();
                this.itemtip.css('left', panelOffset.left + this.panel.outerWidth());
            }
        }
                    
        this.itemtip.show();
    },
            
    showSuggestions: function(query) {
        this.items = this.panel.find('.ui-autocomplete-item');                   
        this.bindDynamicEvents();
        
        var $this=this,
        hidden = this.panel.is(':hidden');
        
        if(hidden) {
            this.show();
        }
        else {
            this.alignPanel();
        }
        
        if(this.items.length > 0) {
            var firstItem = this.items.eq(0);                    
        
            //highlight first item
            firstItem.addClass('ui-state-highlight');                    
        
            //highlight query string
            if(this.panel.children().is('ul') && query.length > 0) {
                this.items.each(function() {
                    var item = $(this),
                    text = item.html(),
                    re = new RegExp(PrimeFaces.escapeRegExp(query), 'gi'),
                    highlighedText = text.replace(re, '<span class="ui-autocomplete-query">$&</span>');

                    item.html(highlighedText);
                });
            }
        
            if(this.cfg.forceSelection) {
                this.currentItems = [];
                this.items.each(function(i, item) {
                    $this.currentItems.push($(item).attr('data-item-label'));
                });
            }

            //show itemtip if defined
            if(this.cfg.itemtip && firstItem.length === 1) {
                this.showItemtip(firstItem);
            }
        }
        else {
            if(this.cfg.emptyMessage) { 
                var emptyText = '<div class="ui-autocomplete-emptyMessage ui-widget">'+this.cfg.emptyMessage+'</div>';
                this.panel.html(emptyText);
            }
            else {
                this.panel.hide();
            }
        }     
    },
            
    search: function(query) {
        //allow empty string but not undefined or null
        if(query === undefined || query === null) {
            return;
        }
        
        if(this.cfg.cache && this.cache[query]) {
            this.panel.html(this.cache[query]);
            this.showSuggestions(query);
            return;            
        }
        
        if(!this.active) {
            return;
        }

        var _self = this;
        
        if(this.cfg.itemtip) {
            this.itemtip.hide();
        }
        
        var options = {
            source: this.id,
            process: this.id,
            update: this.id,
            formId: this.cfg.formId,
            onsuccess: function(responseXML, status, xhr) {
                PrimeFaces.ajax.Response.handle(responseXML, status, xhr, {
                    widget: _self,
                    handle: function(content) {
                        this.panel.html(content);

                        if(this.cfg.cache) {
                            this.cache[query] = content;
                        }

                        this.showSuggestions(query);
                    }
                });
                
                return true;
            }
        };

        options.params = [
          {name: this.id + '_query', value: query}  
        ];
        
        if(this.hasBehavior('query')) {
            var queryBehavior = this.cfg.behaviors['query'];
            queryBehavior.call(this, options);
        } 
        else {
            PrimeFaces.ajax.AjaxRequest(options); 
        }
    },
    
    show: function() {
        this.alignPanel();

        if(this.cfg.effect)
            this.panel.show(this.cfg.effect, {}, this.cfg.effectDuration);
        else
            this.panel.show();
    },
    
    hide: function() {        
        this.panel.hide();
        this.panel.css('height', 'auto');
       
        if(this.cfg.itemtip) {
            this.itemtip.hide();
        }
    },
    
    invokeItemSelectBehavior: function(event, itemValue) {
        if(this.cfg.behaviors) {
            var itemSelectBehavior = this.cfg.behaviors['itemSelect'];

            if(itemSelectBehavior) {
                var ext = {
                    params : [
                        {name: this.id + '_itemSelect', value: itemValue}
                    ]
                };

                itemSelectBehavior.call(this, ext);
            }
        }
    },
    
    invokeItemUnselectBehavior: function(event, itemValue) {
        if(this.cfg.behaviors) {
            var itemUnselectBehavior = this.cfg.behaviors['itemUnselect'];

            if(itemUnselectBehavior) {
                var ext = {
                    params : [
                        {name: this.id + '_itemUnselect', value: itemValue}
                    ]
                };
                
                itemUnselectBehavior.call(this, ext);
            }
        }
    },
    
    removeItem: function(event, item) {
        var itemValue = item.attr('data-token-value'),
        _self = this;
        
        //remove from options
        this.hinput.children('option').filter('[value="' + itemValue + '"]').remove();
        
        //remove from items
        item.fadeOut('fast', function() {
            var token = $(this);

            token.remove();

            _self.invokeItemUnselectBehavior(event, itemValue);
        });
    },
    
    setupForceSelection: function() {
        this.currentItems = [this.input.val()];
        var $this = this;
        
        this.input.blur(function() {
            var value = $(this).val(),
            valid = false;

            for(var i = 0; i < $this.currentItems.length; i++) {
                if($this.currentItems[i] === value) {
                    valid = true;
                    break;
                }
            }
                
            if(!valid) {
                if($this.cfg.multiple) {
                    $this.input.val('');
                }
                else {
                    $this.input.val('');
                    $this.hinput.val('');
                }
            }
        });
    },
            
    disable: function() {
        this.disabled = true;
        this.input.addClass('ui-state-disabled').attr('disabled', 'disabled');
    },
    
    enable: function() {
        this.disabled = false;
        this.input.removeClass('ui-state-disabled').removeAttr('disabled');
    },
    
    close: function() {
        this.hide();
    },
    
    deactivate: function() {
        this.active = false;
    },
    
    activate: function() {
        this.active = true;
    },
    
    hasBehavior: function(event) {
        if(this.cfg.behaviors) {
            return this.cfg.behaviors[event] != undefined;
        }
    
        return false;
    },
    
    alignPanel: function() {
        var fixedPosition = this.panel.css('position') == 'fixed',
        win = $(window),
        positionOffset = fixedPosition ? '-' + win.scrollLeft() + ' -' + win.scrollTop() : null,
        panelWidth = null;

        if(this.cfg.multiple) {
            panelWidth = this.multiItemContainer.innerWidth() - (this.input.position().left - this.multiItemContainer.position().left);
        }
        else {
            if(this.panel.is(':visible')) {
                panelWidth = this.panel.children('.ui-autocomplete-items').outerWidth();
            }
            else {
                this.panel.css({'visibility':'hidden','display':'block'});
                panelWidth = this.panel.children('.ui-autocomplete-items').outerWidth();
                this.panel.css({'visibility':'visible','display':'none'});
            }
            
            var inputWidth = this.input.outerWidth();
            if(panelWidth < inputWidth) {
                panelWidth = inputWidth;
            }
        }
        
        if(this.cfg.scrollHeight) {
            var heightConstraint = this.panel.is(':hidden') ? this.panel.height() : this.panel.children().height();
            if(heightConstraint > this.cfg.scrollHeight)
                this.panel.height(this.cfg.scrollHeight);
            else
                this.panel.css('height', 'auto');                                               
        }
        
        this.panel.css({
                        'left':'',
                        'top':'',
                        'width': panelWidth,
                        'z-index': ++PrimeFaces.zindex
                });
        
        if(this.panel.parent().attr('id') === this.id) {
            this.panel.css({
                left: 0,
                top: this.jq.innerHeight()
            });
        }
        else {
            this.panel.position({
                    my: 'left top'
                    ,at: 'left bottom'
                    ,of: this.input,
                    offset : positionOffset
                });
        }
    }
    
}); 