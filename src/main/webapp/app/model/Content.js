Ext.define('sphinx.model.Content', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'string' },
        { name: 'name', type: 'string' },
        { name: 'createdBy', type: 'string' },
        { name: 'creationDate', type: 'string' },
        { name: 'prepath', type: 'string' }
        
    ]

});