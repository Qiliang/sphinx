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
            },
            'folderTree': {
            	select: this.onFolderTreeSelect
            },
            'contentGrid actioncolumn':{
            	click: this.viewDetail
            },
            'contentGrid': {
            	itemcontextmenu:this.itemcontextmenu,
            	itemdblclick:this.onItemdblclick
            },
            'contentGrid toolbar #newDocument':{
            	click:this.onNewDocument
            },
            'contentGrid toolbar #newFolder':{
            	click:this.onNewFolder
            },
            'createFolder button[action=save]':{
            	click:this.onCreateFolder
            },
            'create-document button[action=save]':{
            	click:this.onCreateDocument
            }
        });
    	
    },

    onItemdblclick:function( widget, record, item, index, e, eOpts ){
    	
    	var baseTypeId = record.get('cmis:baseTypeId');
    	var grid = Ext.ComponentQuery.query('#centerPanel contentGrid')[0];
    	//var tree = Ext.ComponentQuery.query('#west-panel folderTree')[0];
    	Ext.query();
    	if(baseTypeId==='cmis:document')
    		this.openDocument(record.get('cmis:objectId'));
    	else if(baseTypeId==='cmis:folder')
    		this.openFolder(grid,record);
    },
    
    openDocument:function(objectId){
    	Ext.Ajax.request({
    		method: 'GET',
    	    url: 'browser/A1/root',
    	    params :{
    	    	objectId: objectId
    	    },
    	    success: function(response, opts) {
    	        Ext.Msg.alert(objectId,response.responseText);
    	    },
    	    failure: function(response, opts) {
    	        console.log('server-side failure with status code ' + response.status);
    	    }
    	});
    },
    
    openFolder:function(grid , record){
    	var store = grid.getStore(); 
    	var proxy = store.getProxy();
    	proxy.extraParams.objectId=record.get('cmis:objectId');
    	store.reload({
    	    params: {
    	        page: 1,
    	}});
    	grid.setFolder(record);
    	//tree.expandNode( record );
    },
    
    onNewDocument:function(widget, event ){
    	var view = Ext.create('sphinx.view.document.CreateDocument');
    	view.show();
    },
    
    onCreateDocument:function(button, e, eOpts){
    	var me= this;
    	var grid = Ext.ComponentQuery.query('#centerPanel contentGrid')[0];
    	var folderId = grid.getFolder().get('cmis:objectId');
    	
    	var form = button.up('form').getForm();
    	form.submit({
    	    clientValidation: true,
    	    url: 'browser/A1/root',
    	    params: {
    	    	objectId: folderId,
    	    	cmisaction : 'createDocument',
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
    
    onNewFolder:function(widget, event ){
    	var view = Ext.create('sphinx.view.document.CreateFolder');
    	view.show();
    },
    onCreateFolder:function(button, e, eOpts){
    	var me= this;
    	var grid = Ext.ComponentQuery.query('#centerPanel contentGrid')[0];
    	var folderId = grid.getFolder().get('cmis:objectId');
    	var form = button.up('form').getForm();
    	form.submit({
    	    clientValidation: true,
    	    url: 'browser/A1/root',
    	    params: {
    	    	objectId: folderId,
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
    
    onFolderTreeSelect: function( widget, record, index, eOpts ){
    	//var tree = Ext.ComponentQuery.query('#west-panel folderTree')[0];
    	var grid = Ext.ComponentQuery.query('#centerPanel contentGrid')[0];
    	var centerPanel = Ext.getCmp('centerPanel');
    	if(!grid){
    		grid =centerPanel.add({xtype:'contentGrid',title: '数据',closable: true});
    	}
    	centerPanel.setActiveTab(grid);
    	
    	this.openFolder(grid,record);
    	
    }
    
    
    
});