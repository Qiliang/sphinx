Ext.define('sphinx.view.system.group.Grid' ,{
    extend: 'Ext.grid.Panel',
    
    alias: 'widget.group_grid',
    store: 'Groups',
    
    
    initComponent: function() {
    	this.selModel=Ext.create('Ext.selection.CheckboxModel');
    	this.columns= [
    	           {xtype: 'rownumberer'},
                  {
                      text: '名称',
                      width: '45%',
                      sortable: false,
                      hideable: false,
                      dataIndex: 'cmis:name'
                  },
                  {
                      text: '用户',
                      width: '45%',
                      dataIndex: 'system:users',
                      hidden: false
                  }
              ];
    	this.dockedItems = [{
            xtype: 'pagingtoolbar',
            store: this.getStore(),
            dock: 'bottom',
            displayInfo: true
        },{
            xtype: 'toolbar',
            items: [
                Ext.create('Ext.Action', { text: '新建', itemId:"create"}),
                Ext.create('Ext.Action', { text: '删除', itemId:'delete'}),
                Ext.create('Ext.form.field.Text', { emptyText: '按名称搜索',width: 135})
            ]
        }];
    	
    	
    	this.callParent(arguments);
    	
    	
    	
    }
    
    
    
});
