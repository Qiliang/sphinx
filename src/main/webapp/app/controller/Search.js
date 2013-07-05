Ext.define('sphinx.controller.Search', {
    extend: 'Ext.app.Controller',
    
    views: ['search.SearchForm','search.Result'],
    stores: ['Query'],
    
    init: function() {
    	this.control({
            'searchTemplate toolbar #newSearch': {
                click: this.onActionClick,
            },'searchForm button[action=search]':{
            	click:this.onSearchClick
            }
        });
    },
    
    onActionClick: function(){
    	var searchForm= Ext.create('sphinx.view.search.SearchForm');
    	searchForm.show();
    },
    
    onSearchClick: function(button){
    	var statement =button.up('form').down('textarea').getValue() ;
    	
    	var grid = Ext.ComponentQuery.query('#centerPanel searchResult')[0];
    	var centerPanel = Ext.getCmp('centerPanel');
    	if(!grid){
    		grid =centerPanel.add({xtype:'searchResult',title: '查询',closable: true});
    	}
    	centerPanel.setActiveTab(grid);
    	
    	
    	grid.getStore().getProxy().extraParams.q= statement;
    	grid.getStore().reload();
    	
    }

    
    
    
    
});
