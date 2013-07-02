Ext.define('sphinx.view.system.type.Grid', {
    extend: 'Ext.tree.Panel',
    alias: 'widget.typeGrid',
    
    useArrows: true,
//    rootVisible: false,
//    multiSelect: true,
    singleExpand: true,
    
    store: 'Types',
    
    
    initComponent: function() {
    	
        this. columns = [{
                xtype: 'treecolumn',
                text: 'id',
                flex: 2,
                sortable: true,
                dataIndex: 'id'
            },{
                text: 'Duration',
                flex: 1,
                sortable: true,
                dataIndex: 'localName',
                align: 'center',
            },{
                text: 'Assigned To',
                flex: 1,
                dataIndex: 'displayName',
                sortable: true
            }];
        
        this.callParent(arguments);
    }
});