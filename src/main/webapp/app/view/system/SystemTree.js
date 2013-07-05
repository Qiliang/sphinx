Ext.define('sphinx.view.system.SystemTree' ,{
    extend: 'Ext.tree.Panel',
    
    alias: 'widget.systemTree',

    rootVisible:false,
    
    root: {
        text: "Root node",
        expanded: true,
        children: [
            { text: "用户管理", itemId:'UserAdmin', leaf: true },
            { text: "组管理", itemId:'GroupAdmin', leaf: true },
            { text: "类型管理", itemId:'TypeAdmin', leaf: true }

        ]
    },
    
    initComponent: function() {
    	this.callParent(arguments);
    	
    }
    
    
    
});