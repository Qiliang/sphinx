Ext.define('sphinx.model.type.Policy', {
    extend: 'Ext.data.Model',
    
    idProperty:'id',
    fields: [
		{ name: 'id' ,type: 'string'},
		{ name: 'localName' ,type: 'string'},
		{ name: 'localNamespace' ,type: 'string'},
		{ name: 'displayName' ,type: 'string'},
        { name: 'queryName' ,type: 'string'},
        { name: 'description' ,type: 'string'},
        { name: 'baseId' ,type: 'string'},
        { name: 'creatable' ,type: 'boolean'},
        { name: 'fileable' ,type: 'boolean'},
        { name: 'queryable' ,type: 'boolean'},
        { name: 'fulltextIndexed' ,type: 'boolean'},
        { name: 'includedInSupertypeQuery' ,type: 'boolean'},
        { name: 'controllablePolicy' ,type: 'string'},
        { name: 'controllableACL' ,type: 'string'},
        { name: 'typeMutability' ,type: 'auto'},
        { name: 'propertyDefinitions' ,type: 'auto'},
        
    ]


});