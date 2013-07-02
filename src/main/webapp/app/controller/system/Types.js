Ext.define('sphinx.controller.system.Types', {
    extend: 'Ext.app.Controller',
    
    views: ['system.SystemTree','system.type.Grid'],
    stores: ['Types'],
    
    init: function() {
    	this.control({
            'systemTree': {
            	select: this.onSelected,
            }
        });
    	
    },

    onSelected:function( treeModel, record, index, eOpts ){
    	if(record.get('text')!=='类型管理'){
    		return;
    	}
    	
    	var grid = Ext.ComponentQuery.query('#centerPanel typeGrid')[0];
    	var centerPanel = Ext.getCmp('centerPanel');
    	if(!grid){
    		grid =centerPanel.add({xtype:'typeGrid',title: '类型',closable: true});
    	}
    	centerPanel.setActiveTab(grid);
    	
    	var store = grid.getStore(); 
    	var proxy = store.getProxy();
    	proxy.extraParams.objectId=100;
    	store.reload({
    	    params: {
    	        page: 1,
    	}});
    }
    
    
    
});