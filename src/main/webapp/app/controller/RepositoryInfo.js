Ext.define('sphinx.controller.RepositoryInfo', {
    extend: 'Ext.app.Controller',
    
    models: ['RepositoryInfo'],
    
    init: function() {
    	this.control({
        });
    	
    	sphinx.model.RepositoryInfo.load(null, {
    	    scope: this,
    	    failure: function(record, operation) {
    	    	console.log(record);
    	        //record is null
    	    },
    	    success: function(record, operation) {
    	    	console.log(record);
    	    	
    	        //do something if the load succeeded
    	    },
    	    callback: function(record, operation, success) {
    	    	console.log(record);
    	        //if operation is unsuccessful, record is null
    	    }
    	});
    }
    
});
