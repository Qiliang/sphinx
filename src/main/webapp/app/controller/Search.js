Ext.define('sphinx.controller.Search', {
    extend: 'Ext.app.Controller',
    
    views: ['search.SearchTemplate'],
    stores: ['Search'],
    
    init: function() {
    	this.control({
            'searchTemplate toolbar #newSearch': {
                click: this.onActionClick,
            }
        });
    },
    
    onActionClick: function(){
    	var searchForm= Ext.create('sphinx.view.search.SearchForm');
    	searchForm.show();
    }

    
    
    
    
});
