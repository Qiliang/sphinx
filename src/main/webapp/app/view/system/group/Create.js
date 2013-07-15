Ext.define('sphinx.view.system.group.Create', {
    extend: 'Ext.window.Window',
    alias: 'widget.group_create',
    
    requires:['Ext.ux.form.ItemSelector'],
    layout: 'fit',
    autoShow: false,
    title:'新建组',
    initComponent: function() {
    	
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
        items: [
			{                	
	            xtype: 'hiddenfield',
	            name: 'propertyId[0]',
	            value: 'cmis:objectTypeId'
            },{                	
	            xtype: 'hiddenfield',
	            name: 'propertyValue[0]',
	            value: 'system:group'
            },{                	
	            xtype: 'hiddenfield',
	            name: 'propertyId[1]',
	            value: 'cmis:name'
            },{
	            xtype: 'textfield',
	            name: 'propertyValue[1]',
	            fieldLabel: '名称'
            },{                	
	            xtype: 'hiddenfield',
	            name: 'propertyId[2]',
	            value: 'system:users'
            },{
                xtype: 'itemselector',
                name: 'propertyValue[2]',
                height: 250,
                anchor: '100%',
                fieldLabel: '用户',
                store:  'Users',
                displayField: 'cmis:name',
                valueField: 'cmis:objectId',
                allowBlank: false,
                buttons: ['add', 'remove']
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
