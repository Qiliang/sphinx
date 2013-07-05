Ext.define('sphinx.view.system.user.Grid' ,{
    extend: 'Ext.grid.Panel',
    
    alias: 'widget.userGrid',
    store: 'Users',
    
    
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
                      text: '密码',
                      width: '45%',
                      dataIndex: 'system:password',
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
                Ext.create('Ext.Action', { text: '新建用户', itemId:"create"}),
                Ext.create('Ext.Action', { text: '删除用户', itemId:'delete'}),
                Ext.create('Ext.form.field.Text', { emptyText: '按名称搜索',width: 135})
            ]
        }];
    	
    	
    	this.callParent(arguments);
    	
    	
    	
    }
    
    
    
});
