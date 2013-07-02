Ext.define('sphinx.controller.ContentGrid', {
    extend: 'Ext.app.Controller',
    
    views: ['document.ContentGrid','document.FolderTree'
            ,'document.ContentGridContextmenu'
            ,'document.CreateFolder'],
    stores: ['Objects','FolderTree'],
    models: ['Object'],
    
    init: function() {
    	this.control({
            'contentGrid': {
                itemdblclick: this.viewContent2,
                itemclick: this.viewContent
            },
            'folderTree': {
            	select: this.onFolderTreeSelect
            },
            'contentGrid actioncolumn':{
            	click: this.viewDetail
            },
            'contentGrid': {
            	itemcontextmenu:this.itemcontextmenu
            },
            'contentGrid toolbar #newDocument':{
            	click:this.onNewDocument
            },
            'contentGrid toolbar #newFolder':{
            	click:this.onNewFolder
            },
            'createFolder button[action=save]':{
            	click:this.onCreateFolder
            }
        });
    	
    },

    onPanelRendered: function() {
        //console.log('The ContentGrid was rendered');
    },
    
    onNewDocument:function(widget, event ){
    	var view = Ext.create('sphinx.view.document.CreateDocument');
    	view.show();
    },
    
    onNewFolder:function(widget, event ){
    	var view = Ext.create('sphinx.view.document.CreateFolder');
    	view.show();
    },
    onCreateFolder:function(button, e, eOpts){
    	var me= this;
    	var folderTree = Ext.ComponentQuery.query('folderTree')[0];
    	var selModel = folderTree.getSelectionModel();
    	var form = button.up('form').getForm();
    	form.submit({
    	    clientValidation: true,
    	    url: 'browser/A1/root',
    	    params: {
    	    	objectId: selModel.selectionStart.internalId,
    	    	cmisaction : 'createFolder',
    	    	succinct:true
    	    },
    	    success: function(form, action) {
    	       Ext.app.msg('Success', action.response.responseText);
    	       me.getObjectsStore().reload();
    	       me.getFolderTreeStore().reload();
    	    },
    	    failure: function(form, action) {
    	    	Ext.Msg.alert("info",action.response.responseText);
    	    	
    	    }
    	});
    },
    
    itemcontextmenu:function(grid, record, item, index, e, eOpts ){
    	e.preventDefault();
    	var cxtMenu = Ext.create('widget.contentGridContextmenu',{'record' : record});
    	cxtMenu.showAt(e.getXY());
    },
    
    viewDetail: function(column, action, grid, rowIndex, colIndex, record, node){
    	var documentDetail = Ext.create('sphinx.view.document.detail');
    	documentDetail.show();
    },
    
    viewContent: function( grid, record, item, index, e, eOpts ){
    	var view = Ext.getCmp('properties');
    	view.setSource(record.raw);
    },
    
    
    onFolderTreeSelect: function( cmp, record, index, eOpts ){
    	var grid = Ext.ComponentQuery.query('#centerPanel contentGrid')[0];
    	//var grid = Ext.getCmp('contentGrid');
    	var centerPanel = Ext.getCmp('centerPanel');
    	if(!grid){
    		grid =centerPanel.add({xtype:'contentGrid',title: '数据',closable: true});
    	}
    	centerPanel.setActiveTab(grid);
    	
    	var store = grid.getStore(); 
    	var proxy = store.getProxy();
    	proxy.extraParams.objectId=record.internalId;
    	store.reload({
    	    params: {
    	        page: 1,
    	}});
    }
    
    
    
});