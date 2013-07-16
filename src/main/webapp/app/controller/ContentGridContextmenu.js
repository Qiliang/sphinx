Ext.define('sphinx.controller.ContentGridContextmenu', {
    extend: 'Ext.app.Controller',
    
    views: ['document.ContentGridContextmenu'],
    stores: ['ACL','Objects','FolderTree'],
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
            },'contentGridContextmenu menuitem[itemId=delete]':{
            	click: this.onDeleteClick
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
    
    onDeleteClick: function(item, e, eOpts){
    	var me = this;
    	var objectId=item.record.get('cmis:objectId');
    	Ext.Ajax.request({
    		method :'POST',
    	    url: 'browser/A1/root',
    	    params: {
    	    	objectId: objectId,
    	    	cmisaction : 'delete'
    	    },
    	    success: function(response){
    	    	Ext.app.msg('Success', response.responseText);
    	        me.getObjectsStore().reload();
     	       	me.getFolderTreeStore().reload();
    	    },failure: function(response) {
    	    	Ext.Msg.alert("info",response.responseText);
    	    }
    	});
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