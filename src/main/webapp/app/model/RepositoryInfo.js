Ext.define('sphinx.model.RepositoryInfo', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'repositoryId', type: 'string' },
        { name: 'repositoryName', type: 'string' },
        { name: 'repositoryDescription', type: 'string' },
        { name: 'vendorName', type: 'string' },
        { name: 'productName', type: 'string' },
        { name: 'productVersion', type: 'string' },
        { name: 'rootFolderId', type: 'string' },
        { name: 'capabilities', type: 'auto' },
        { name: 'aclCapabilities', type: 'auto' },
        { name: 'latestChangeLogToken', type: 'string' },
        { name: 'cmisVersionSupported', type: 'string' },
        { name: 'thinClientURI', type: 'string' },
        { name: 'changesIncomplete', type: 'string' },
        { name: 'changesOnType', type: 'auto' },
        { name: 'principalIdAnonymous', type: 'string' },
        { name: 'principalIdAnyone', type: 'string' },
        { name: 'repositoryUrl', type: 'string' },
        { name: 'rootFolderUrl', type: 'string' }
    ],
    
    proxy: {
        type: 'ajax',
        url: 'browser',
        reader: {
        	record:'A1',
            type: 'json'
        }
    }

});