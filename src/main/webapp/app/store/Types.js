Ext.define('sphinx.store.Types', {
    extend: 'Ext.data.TreeStore',
    model: 'sphinx.model.Type',
    autoLoad: false,
    
    constructor: function(config){
    	this.callParent([config]);
		
		Ext.override(this.getProxy(), {
		    buildUrl: function(request) {
		    	if(request.params.typeId==='root'){
		    		delete request.params.typeId;
		    	}
		        return this.callParent([request]);
		    }
		});
    },
    

    
    nodeParam:'typeId',
    proxy: {
    	type: 'ajax',
        url: 'browser/A1',
        extraParams:{  
        	cmisselector: 'typeChildren'
        },
        reader: {
        	root:'types',
            type: 'json'
        }
    }
    
});