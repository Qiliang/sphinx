Ext.define('sphinx.controller.FolderTree', {
    extend: 'Ext.app.Controller',
    
    views: ['document.FolderTree'],
    stores: ['FolderTree'],
    
    init: function() {
    	this.control({
            'folderTree': {
                itemclick: this.viewContent
            }
        });
    },

    onPanelRendered: function() {
       // console.log('The folderTree was rendered');
    },
    
    viewContent: function( tree, record, item, index, e, eOpts ){
    	//var view = Ext.getCmp('properties');
    	//view.setSource(record.raw);
    	
    }
    
    
    
    
});