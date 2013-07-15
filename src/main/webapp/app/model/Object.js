Ext.define('sphinx.model.Object', {
    extend: 'Ext.data.Model',
    
    idProperty:'cmis:objectId',
    fields: [
		{ name: 'cmis:objectId' ,type: 'string'},
		{ name: 'cmis:baseTypeId' ,type: 'string'},
		{ name: 'cmis:description' ,type: 'string'},
		{ name: 'cmis:path' ,type: 'string'},
        { name: 'cmis:name' ,type: 'string'},
        { name: 'cmis:lastModificationDate' ,type: 'date', dateFormat:'timestamp'},
        { name: 'cmis:lastModifiedBy' ,type: 'string'},
        { name: 'cmis:objectTypeId' ,type: 'string'}
    ]
    //associations: [{type: 'hasMany', model: 'sphinx.model.Property', name: 'properties'}]



});