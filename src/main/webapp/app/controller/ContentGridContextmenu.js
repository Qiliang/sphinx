Ext.define('sphinx.controller.ContentGridContextmenu', {
    extend: 'Ext.app.Controller',
    
    views: ['document.ContentGridContextmenu'],
    stores: ['ACL'],
    models: ['ACE','AllowableActions'],
    
    init: function() {
    	this.control({
            'contentGridContextmenu menuitem':{
            	beforerender: this.onbeforerender
            },
            'contentGridContextmenu menuitem[itemId=properties]':{
            	click: this.onPropertiesClick
            },'contentGridContextmenu menuitem[itemId=acl]':{
            	click: this.onAclClick
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
    },
    
    onAclClick: function( item, e, eOpts){
    	
    	var objectId=item.record.get('cmis:objectId');
    	
    	this.getACLStore().load({
    		scope: this,
    		params:{'objectId':objectId }
    	});
    	
    	sphinx.model.AllowableActions.load(null,{
    		scope: this,
    		params:{ 'objectId' : objectId},
    	    callback: function(record, operation, success) {
    	    	Ext.create('sphinx.view.document.ACL', 
    	        		{
    	        			'acl': this.getACLStore(),
    	        			'allowableActions': record,
    	        			'objectId' : objectId
    	        		}
    	        	).show();
    	    }
    	});
    	
    }
    
    
});