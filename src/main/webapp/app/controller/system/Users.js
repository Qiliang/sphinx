Ext.define('sphinx.controller.system.Users', {
    extend: 'Ext.app.Controller',
    
    views: ['system.SystemTree','system.user.Grid','system.user.CreateUser'],
    stores: ['Users'],
    
    init: function() {
    	this.control({
            'systemTree': {
            	select: this.onSelected,
            },
            'userGrid toolbar *[itemId=create]': {
            	click:this.onOpenCreateUser
            },'createUser button[action=save]': {
            	click:this.onSaveUser
            }
        });
    	
    },

    onSelected:function( treeModel, record, index, eOpts ){
    	if(record.get('text')!=='用户管理'){
    		return;
    	}
    	
    	var grid = Ext.ComponentQuery.query('#centerPanel userGrid')[0];
    	var centerPanel = Ext.getCmp('centerPanel');
    	if(!grid){
    		grid =centerPanel.add({xtype:'userGrid',title: '用户',closable: true});
    	}
    	centerPanel.setActiveTab(grid);
    	
    	var store = grid.getStore(); 
    	var proxy = store.getProxy();
    	proxy.extraParams.objectId=100;
    	store.reload({
    	    params: {
    	        page: 1,
    	}});
    },
    
    onOpenCreateUser: function(){
    	var view = Ext.create('sphinx.view.system.user.CreateUser');
    	view.show();
    },
    onSaveUser: function(button, e, eOpts){
    	var me= this;
    	var form = button.up('form').getForm();
    	form.submit({
    	    clientValidation: true,
    	    url: 'browser/A1',
    	    params: {
    	    	cmisaction : 'createItem',
    	    	succinct:true
    	    },
    	    success: function(form, action) {
    	       Ext.app.msg('Success', action.response.responseText);
    	       button.up('window').close();
    	       me.getUsersStore().reload();
    	    },
    	    failure: function(form, action) {
    	    	Ext.Msg.alert("info",action.response.responseText);
    	    }
    	});
    }
    
    
    
});