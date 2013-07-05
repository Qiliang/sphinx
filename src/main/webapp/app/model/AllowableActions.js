Ext.define('sphinx.model.AllowableActions', {
    extend: 'Ext.data.Model',
    
    fields: [
		{ name: 'canDeleteObject' ,type: 'boolean'},
		{ name: 'canUpdateProperties' ,type: 'boolean'},
		{ name: 'canGetFolderTree' ,type: 'boolean'},
		{ name: 'canGetProperties' ,type: 'boolean'},
		{ name: 'canGetObjectRelationships' ,type: 'boolean'},
		{ name: 'canGetObjectParents' ,type: 'boolean'},
		{ name: 'canGetFolderParent' ,type: 'boolean'},
		{ name: 'canGetDescendants' ,type: 'boolean'},
		{ name: 'canMoveObject' ,type: 'boolean'},
		{ name: 'canDeleteContentStream' ,type: 'boolean'},
		{ name: 'canCheckOut' ,type: 'boolean'},
		{ name: 'canCancelCheckOut' ,type: 'boolean'},
		{ name: 'canCheckIn' ,type: 'boolean'},
		{ name: 'canSetContentStream' ,type: 'boolean'},
		{ name: 'canGetAllVersions' ,type: 'boolean'},
		{ name: 'canAddObjectToFolder' ,type: 'boolean'},
		{ name: 'canRemoveObjectFromFolder' ,type: 'boolean'},
		{ name: 'canGetContentStream' ,type: 'boolean'},
		{ name: 'canApplyPolicy' ,type: 'boolean'},
		{ name: 'canGetAppliedPolicies' ,type: 'boolean'},
		{ name: 'canRemovePolicy' ,type: 'boolean'},
		{ name: 'canGetChildren' ,type: 'boolean'},
		{ name: 'canCreateDocument' ,type: 'boolean'},
		{ name: 'canCreateFolder' ,type: 'boolean'},
		{ name: 'canCreateRelationship' ,type: 'boolean'},
		{ name: 'canCreateItem' ,type: 'boolean'},
		{ name: 'canDeleteTree' ,type: 'boolean'},
		{ name: 'canGetRenditions' ,type: 'boolean'},
		{ name: 'canGetACL' ,type: 'boolean'},
		{ name: 'canApplyACL' ,type: 'boolean'}
    ],

    proxy: {
        type: 'ajax',
        url: 'browser/A1/root',
        extraParams:{
        	cmisselector:'allowableActions',
        },reader: {
            type: 'json'
        }
    }

});

