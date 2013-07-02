Ext.define('sphinx.controller.ContentGridContextmenu', {
    extend: 'Ext.app.Controller',
    
    views: ['document.ContentGridContextmenu'],
    stores: ['Contents'],
    models: ['Content'],
    
    init: function() {
    	this.control({
            'contentGridContextmenu menuitem':{
            	beforerender: this.onbeforerender
            },
            'contentGridContextmenu menuitem[itemId=properties]':{
            	click: this.onPropertiesClick
            }
        });
    },

    
    onbeforerender: function( item, eOpts ){
    	if(item.itemId=='open')
    		item.disable();
    },
    
    onPropertiesClick: function( item, e, eOpts){
    	var detail = Ext.create('sphinx.view.document.Detail', {'record': item.record});
    	detail.show();
    }
    
    
});