Ext.define('sphinx.view.document.ContentGridContextmenu' ,{
    extend: 'Ext.menu.Menu',
    
    alias: 'widget.contentGridContextmenu',
    
    initComponent: function() {
    	var me = this;
    	this.items.forEach(function(item){
    		item.record =  me.record;
    	});
    	
    	this.callParent(arguments);
    },
    
    items: [{
    	itemId: 'open',
        text: '打开'
    },{
        text: '复制'
    },{
        text: '剪切'
    },{
    	itemId: 'delete',
        text: '删除'
    },{
        text: '重命名'
    },{
    	itemId: 'acl',
        text: '权限'
    },{
    	itemId: 'properties',
        text: '属性'
    }]
    
});