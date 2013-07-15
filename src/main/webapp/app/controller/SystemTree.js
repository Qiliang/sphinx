Ext.define('sphinx.controller.SystemTree', {
    extend: 'Ext.app.Controller',
    
    views: ['system.SystemTree'],
    
    init: function() {
    	this.control({
            'systemTree': {
            	select: this.onSelected
            }
        });
    	
    },

    onSelected:function( treeModel, record, index, eOpts ){
    }
    
    
    
});