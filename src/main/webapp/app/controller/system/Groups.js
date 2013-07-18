Ext.define('sphinx.controller.system.Groups', {
    extend: 'Ext.app.Controller',
    
    views: ['system.SystemTree','system.group.Grid','system.group.Create'],
    stores: ['Groups'],
    
    init: function() {
    	this.control({
            'systemTree': {
            	select: this.onSelected
            },
            'group_grid toolbar *[itemId=create]': {
            	click:this.onOpenCreateUser
            },'group_create button[action=save]': {
            	click:this.onSave
            }
        });
    	
    },

    onSelected:function( treeModel, record, index, eOpts ){
    	if(record.get('text')!=='组管理'){
    		return;
    	}
    	
    	var grid = Ext.ComponentQuery.query('#centerPanel group_grid')[0];
    	var centerPanel = Ext.getCmp('centerPanel');
    	if(!grid){
    		grid =centerPanel.add({xtype:'group_grid',title: '组',closable: true});
    	}
    	centerPanel.setActiveTab(grid);
    	
    	var store = grid.getStore(); 
    	var proxy = store.getProxy();
    	proxy.extraParams.objectId=100;
    	store.reload({
    	    params: {
    	        page: 1
    	}});
    },
    
    onOpenCreateUser: function(){
    	var view = Ext.create('sphinx.view.system.group.Create');
    	view.show();
    },
    //Object {propertyId[0]: "cmis:objectTypeId", propertyValue[0]: "system:group", propertyId[1]: "cmis:name", propertyValue[1]: "g4", userselector: "102,103"}
    onSave: function(button, e, eOpts){
    	var me= this;
    	var form = button.up('form').getForm();
    	var itemselector=button.up('form').down('itemselector');
    	var values = form.getValues();
    	var users = itemselector.getValue();
    	
    	var params={
    			cmisaction : 'createItem',
    	    	succinct:true
    	};
    	users.forEach(function(userId,index){
    		var key = 'propertyValue[2]['+index+']' ;
    		params[key]=userId;
    	});
    	
    	form.submit({
    	    clientValidation: true,
    	    url: 'browser/A1',
    	    params: params,
    	    success: function(form, action) {
    	       Ext.app.msg('Success', action.response.responseText);
    	       button.up('window').close();
    	       me.getGroupsStore().reload();
    	    },
    	    failure: function(form, action) {
    	    	Ext.Msg.alert("info",action.response.responseText);
    	    }
    	});
    }
    
    
    
});