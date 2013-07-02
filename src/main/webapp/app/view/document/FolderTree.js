Ext.define('sphinx.view.document.FolderTree' ,{
    extend: 'Ext.tree.Panel',
    
    alias: 'widget.folderTree',

    title: 'FolderTree',

   store: 'FolderTree',
    
    
    initComponent: function() {
    	this.columns= [{ xtype: 'treecolumn', header: 'Name', dataIndex: 'cmis:name', flex: 1 }];
    	
    	this.callParent(arguments);
    	
    }
    
    
    
});