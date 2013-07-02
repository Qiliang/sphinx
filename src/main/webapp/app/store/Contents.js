Ext.define('sphinx.store.Contents', {
    extend: 'Ext.data.Store',
    model: 'sphinx.model.Content',
    autoLoad: true,
    pageSize: 10,
    proxy: {
    	idParam: 'id',
    	limitParam : 'pageSize',
    	pageParam: 'pageIndex',
        type: 'ajax',
        url: 'data/contents.json',
//        extraParams:{  
//        	folderOnly : false
//        },
        reader: {
            type: 'json',
            root: 'rows',
            totalProperty: 'records'
        }
    },
    
});