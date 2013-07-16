Ext.define('sphinx.store.Users', {
    extend: 'Ext.data.Store',
    model: 'sphinx.model.User',
    autoLoad: false,
    remoteSort: true,
    pageSize: 10,
    
    constructor: function(config){
    	this.callParent([config]);
		Ext.override(this.getProxy().getReader(), {
			readRecords: function(data) {
		        var d = {};
		        d.hasMoreItems = data.hasMoreItems;
		        d.numItems = data.numItems;
		        d.results = [];
		        data.results.forEach(function(item){
		        	d.results.push(item.succinctProperties)
		        });
		        
		        return this.callParent([d]);
		    }
		});
    },
    
    proxy: {
    	type: 'ajax',
        url: 'browser/A1',
        limitParam: 'maxItems',
        startParam: 'skipCount',
        simpleSortMode: true,
        sortParam: 'orderBy',
        extraParams:{  
        	cmisselector:'query',
        	q:'select cmis:name,system:password from system:user',
        	succinct:true
        },
        reader: {
            type: 'json',
            root: 'results',
            totalProperty: 'numItems'
        }
    }
    
});