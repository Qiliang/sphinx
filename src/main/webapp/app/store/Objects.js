Ext.define('sphinx.store.Objects', {
    extend: 'Ext.data.Store',
    model: 'sphinx.model.Object',
    autoLoad: true,
    remoteSort: true,
    pageSize: 10,
    
    constructor: function(config){
    	this.callParent([config]);
		Ext.override(this.getProxy().getReader(), {
			readRecords: function(data) {
		        var d = {};
		        d.hasMoreItems = data.hasMoreItems;
		        d.numItems = data.numItems;
		        d.objects = [];
		        data.objects.forEach(function(item){
		        	d.objects.push(item.object.succinctProperties)
		        });
		        
		        return this.callParent([d]);
		    }
		});
    },
    
    proxy: {
    	type: 'ajax',
        url: 'browser/A1/root',
        limitParam: 'maxItems',
        startParam: 'skipCount',
        simpleSortMode: true,
        sortParam: 'orderBy',
        extraParams:{  
        	cmisselector:'children',
        	includeAllowableActions:true,
        	includePathSegment:true,
        	succinct:true
        },
        reader: {
            type: 'json',
            root: 'objects',
            totalProperty: 'numItems'
        }
    }
    
});