Ext.define('sphinx.view.search.SearchTemplate' ,{
    extend: 'Ext.grid.Panel',
    alias: 'widget.searchTemplate',
    
    store: 'Search',
    
    initComponent: function() {
    	this.columns= [
                      {
                          text: '名称',
                          width: '45%',
                          sortable: false,
                          hideable: false,
                          dataIndex: 'name'
                      },
                      {
                          text: '时间',
                          width: '45%',
                          dataIndex: 'lastChange',
                          hidden: false
                      }
                  ];

    	this.dockedItems = [{
            xtype: 'toolbar',
            items: [
                Ext.create('Ext.Action', {itemId:'newSearch', text: '新建查询'}),
            ]
        }];
    	
        this.callParent(arguments);
    }
    
    
    
});