Ext.define('sphinx.view.document.CreateDocument', {
    extend: 'Ext.window.Window',
    alias: 'widget.create-document',

    layout: 'fit',
    autoShow: false,
    title:'新建文档',
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
	            value: 'cmis:document'
            },{                	
	            xtype: 'hiddenfield',
	            name: 'propertyId[1]',
	            value: 'cmis:name'
            },{
	            xtype: 'textfield',
	            name: 'propertyValue[1]',
	            fieldLabel: '名称',
	            value: 'Text field value'
            } ,{
	            xtype: 'filefield',
	            name: 'content',
	            fieldLabel: '内容',
            } 
            ],
        buttons: [{
	        text: 'Save',
	        action: 'save',
	    },{
	        text: 'Cancel',
	        handler: function() {
	            this.up('form').getForm().reset();
	        }
	    }]
    }
	
	    
	

});
