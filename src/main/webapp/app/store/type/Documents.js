Ext.define('sphinx.store.type.Documents', {
    extend: 'Ext.data.TreeStore',
    model: 'sphinx.model.type.Document',
    autoLoad: true,
    
    constructor: function(config){
    	this.callParent([config]);
		Ext.override(this.getProxy().getReader(), {
			readRecords: function(data) {
		        var d = {};
		        d.results = [];
		        data.results.forEach(function(item){
		        	d.results.push(item.succinctProperties)
		        });
		        
		        return this.callParent([d]);
		    }
		});
    },
    //http://localhost:8080/sphinx/browser/A1?cmisselector=typeDescendants&typeId=cmis:document
    proxy: {
    	type: 'ajax',
        url: 'browser/A1',
        extraParams:{  
        	cmisselector: 'typeDescendants',
        	//typeId: 'cmis:document',
        },
        reader: {
            type: 'json',
            root: 'results'
        }
    }
    
});