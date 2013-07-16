Ext.define('sphinx.controller.object.ACL', {
    extend: 'Ext.app.Controller',
    
    views: ['document.ACL','document.CreateACL'],
    //stores: ['Tests'],
    
    init: function() {
    	this.control({
            'documentACL toolbar *[itemId=create]': {
                click: this.onCreateClick
            },'documentACL toolbar *[itemId=delete]': {
                click: this.onDeleteClick
            },'documentCreateACL button[action=save]': {
                click: this.onSaveClick
            }
        });
    },
    
    onCreateClick: function(button){
    	var objectId = button.up('documentACL').objectId;
    	Ext.create('sphinx.view.document.CreateACL',{'objectId' : objectId}).show();
    },
    onSaveClick: function(button){
    	
    	//var grid = Ext.ComponentQuery.query('#centerPanel contentGrid')[0];
    	//var tree = Ext.ComponentQuery.query('#west-panel folderTree')[0];
    	var contentGrid = this.getController( 'ContentGrid' );
    	var objectId = button.up('documentCreateACL').objectId;
    	var form = button.up('form').getForm();
    	form.submit({
    	    clientValidation: true,
    	    url: 'browser/A1/root',
    	    params: {
    	    	objectId: objectId,
    	    	cmisaction : 'applyACL',
    	    	succinct:true
    	    },
    	    success: function(form, action) {
    	       Ext.app.msg('Success', action.response.responseText);
    	       button.up('window').close();
    	       contentGrid.getObjectsStore().reload();
    	       contentGrid.getFolderTreeStore().reload();
    	    },
    	    failure: function(form, action) {
    	    	Ext.Msg.alert("info",action.response.responseText);
    	    }
    	});
    },
    
    
    onDeleteClick: function(button){
    	var objectId = button.up('documentACL').objectId;
    	Ext.Ajax.request({
    		method :'POST',
    	    url: 'browser/A1/root',
    	    params: {
    	    	objectId: objectId,
    	    	cmisaction : 'applyACL',
    	    	'removeACEPrincipal[0]' : 'anyone',
    	    	'removeACEPermission[0][0]' : 'cmis:all' 
    	    },
    	    success: function(response){
    	        var text = response.responseText;
    	        Ext.Msg.alert("info",text);
    	        // process server response here
    	    }
    	});
    	
    	
    	
    }
    
    
    
    
    
});
