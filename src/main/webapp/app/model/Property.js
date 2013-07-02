Ext.define('sphinx.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id'},
        {name: 'localName'},
        {name: 'displayName'},
        {name: 'queryName'},
        {name: 'type'},
        {name: 'cardinality'},
        {name: 'value'}
    ]

});