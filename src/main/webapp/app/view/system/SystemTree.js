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
            { text: "类型管理", itemId:'TypeAdmin', leaf: false,children:[
                                                                                { text: "文档类型管理",  leaf: true },
                                                                                { text: "文件夹类型管理",  leaf: true },
                                                                                { text: "策略类型管理",  leaf: true },
                                                                                { text: "关系类型管理",  leaf: true },
                                                                                { text: "项类型管理",  leaf: true },
                                                                                { text: "次要类型管理",  leaf: true }
                                                                                ] }

        ]
    },
    
    initComponent: function() {
    	this.callParent(arguments);
    	
    }
    
    
    
});