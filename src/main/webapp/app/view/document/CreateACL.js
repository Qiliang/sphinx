Ext.define('sphinx.view.document.CreateACL', {
    extend: 'Ext.window.Window',
    alias: 'widget.documentCreateACL',

    layout: 'fit',
    autoShow: false,
    title:'新建ACL',
    initComponent: function() {
    	
    	//this.formPanel.items[0].value= this.objectId;
    	this.items= [this.formPanel];
    	
        this.callParent(arguments);
    },
    formPanel : {
    	xtype:'form',
    	//layout: 'form',
        width: 340,
        bodyPadding: 5,
        fieldDefaults: {
            labelAlign: 'left',
            labelWidth: 90,
            anchor: '100%'
        },
        //<input name="objectId" type="hidden" value="1234-abcd-5678" />
        items: [
			{
	            xtype: 'combobox',
	            store:'Groups',
	            name: 'addACEPrincipal[0]',
	            displayField: "cmis:name",
                valueField: "cmis:name",
                forceSelection: true,
	            fieldLabel: '名称'
            } ,{
	            xtype: 'combobox',
	            store:["cmis:read", "cmis:write", "cmis:all"],
	            forceSelection:true,
	            name: 'addACEPermission[0][0]',
	            fieldLabel: '操作',
	            value: 'cmis:read'
            },{
	            xtype: 'combobox',
	            store:["objectonly", "propagate", "repositorydetermined"],
	            forceSelection:true,
	            name: 'ACLPropagation',
	            fieldLabel: '遗传',
	            value: 'objectonly'
            } ,{
            	xtype:'hidden',
            	name: 'removeACEPrincipal[0]',
            	value: 'anyone'
            },{
            	xtype:'hidden',
            	name: 'removeACEPermission[0][0]',
            	value: 'cmis:all'
            }
            ],
        buttons: [{
	        text: 'Save',
	        action: 'save'
	    },{
	        text: 'Cancel',
	        handler: function() {
	            this.up('window').close();
	        }
	    }]
    }
    
    
});

