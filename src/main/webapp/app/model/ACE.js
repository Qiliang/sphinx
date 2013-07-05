Ext.define('sphinx.model.ACE', {
    extend: 'Ext.data.Model',
    
    fields: [
		{ name: 'principal' ,type: 'auto'},
		{ name: 'permissions' ,type: 'auto'},
		{ name: 'isDirect' ,type: 'boolean'}
    ],



});