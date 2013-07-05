Ext.define('sphinx.store.ACL', {
	 extend: 'Ext.data.Store',
	 model: 'sphinx.model.ACE',
	 
    proxy: {
        type: 'ajax',
        api: {
            create  : 'browser/A1/root?cmisselector=applyACL',
            read    : 'browser/A1/root?cmisselector=acl',
            destroy :  'browser/A1/root?cmisselector=applyACL',
        },
      reader: {
            type: 'json',
            root: 'aces'
        }
    }

});