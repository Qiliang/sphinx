Ext.define('sphinx.view.document.ACL', {
    extend: 'Ext.window.Window',
    alias: 'widget.documentACL',

    title: '<em>权限</em>',
    header: {
        titlePosition: 2,
        titleAlign: 'center'
        	
    },
    closable: true,
    closeAction: 'hide',
    width: 600,
    minWidth: 350,
    height: 350,
    tools: [{type: 'pin'}],
    layout: {
        type: 'border',
        padding: 5
    },

    aclGrid: {
    	 title: '基本权限',
    	xtype:'grid',
    	selType: 'checkboxmodel',
        columns: [{
        	xtype: 'templatecolumn',
            text: 'principal',
            flex: 50,
            dataIndex: 'principal',
            tpl: '{principal.principalId}',
        },{
            text: 'permissions',
            xtype: 'templatecolumn',
            flex: 35,
            dataIndex: 'permissions',
            tpl: '{permissions}'
        },{
            text: 'isDirect',
            dataIndex: 'isDirect',
            flex: 15,
        }],dockedItems:[{
            xtype: 'toolbar',
            items: [
                Ext.create('Ext.Action', { text: '新建', itemId:"create"}),
                Ext.create('Ext.Action', { text: '删除', itemId:'delete'})
            ]
         }]
        
    },
    allowableActionsProperties:{
    	title: '扩展权限',
    	xtype: 'propertygrid',
    	nameColumnWidth: 200,
    },
    
    initComponent: function() {
    	
    	this.items= [{
            region: 'center',
            xtype: 'tabpanel',
            items: [
            this.aclGrid,
            this.allowableActionsProperties
            ]
        }],
        this.callParent(arguments);
    	this.down('grid').reconfigure(this.acl);
    	this.down('propertygrid').setSource(this.allowableActions.data);
    },
});

