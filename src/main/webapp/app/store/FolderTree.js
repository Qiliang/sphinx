Ext.define('sphinx.store.FolderTree', {
    extend: 'Ext.data.TreeStore',
    model: 'sphinx.model.Object',
    autoLoad: false,
    root: {
    	'cmis:objectId':100,
    	'cmis:name':'文档库',
        expanded: true
    },
    
    constructor: function(config){
    	this.callParent([config]);
		Ext.override(this.getProxy().getReader(), {
			readRecords: function(data) {
				var d = [];
				data.forEach(function(item){
					d.push(item.object.object.succinctProperties);
				});
		        return this.callParent([d]);
		    }
		});
    },
    nodeParam:'objectId',
    
    proxy: {
        type: 'ajax',
        url: 'browser/A1/root',
        idParam:'cmis:objectId',
        extraParams:{
        	cmisselector:'folder',
        	succinct:true,
        	depth:1
        },
        reader: {
            type: 'json',
            root: 'objects',
            totalProperty: 'numItems'
        }
    }
    

});