Ext.define('sphinx.controller.DocumentType', {
    extend: 'Ext.app.Controller',
    
    views: ['system.documenttype.Grid'],
    stores: ['DocumentType'],
    
    init: function() {
    	this.control({
            'systemTree': {
            	select: this.onSelected,
            }
        });
    },
    
    onSelected: function(cmp,record, index, eOpts) {
    	if(record.raw.itemId!='DocumentTypeAdmin')
    		return;
    	this.openDocumentTypeAdminTab();
    },

    
    
    
    
});
