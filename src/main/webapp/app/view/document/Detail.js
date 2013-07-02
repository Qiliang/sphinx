Ext.define('sphinx.view.document.Detail', {
    extend: 'Ext.window.Window',
    alias: 'widget.documentDetail',

    title: '<em>属性</em>',
    header: {
        titlePosition: 2,
        titleAlign: 'center'
        	
    },
    closable: true,
    closeAction: 'hide',
    width: 600,
    minWidth: 350,
    height: 350,
    tools: [{type: 'pin'}],
    layout: {
        type: 'border',
        padding: 5
    },

    required : '<span style="color:red;font-weight:bold" data-qtip="Required">*</span>',
  //cmis:allowedChildObjectTypeIds: Array[1]
  //cmis:baseTypeId: "cmis:folder"
  //cmis:changeToken: "1372641185482"
  //cmis:createdBy: "unknown"
  //cmis:creationDate: 1372641185482
  //cmis:description: null
  //cmis:lastModificationDate: 1372641185482
  //cmis:lastModifiedBy: "unknown"
  //cmis:name: "Text field value"
  //cmis:objectId: "136"
  //cmis:objectTypeId: "cmis:folder"
  //cmis:parentId: "100"
  //cmis:path: "/Text field value"
  //cmis:secondaryObjectTypeIds: null
    simple : {
        xtype: 'form',
        layout: 'form',
        title: '基本属性',
        bodyPadding: '5 5 0',
        width: 350,
        readOnly:true,
        fieldDefaults: {
            msgTarget: 'side',
            labelWidth: 125
        },
        defaultType: 'displayfield',
        items: [{
            fieldLabel: 'ObjectId',
            name: 'cmis:objectId'
        },{
            fieldLabel: 'Object Type Id',
            name: 'cmis:objectTypeId'
        },{
            fieldLabel: 'Base Type Id',
            name: 'cmis:baseTypeId'
        },{
            fieldLabel: 'Name',
            name: 'cmis:name'
        },{
            fieldLabel: 'Description',
            name: 'cmis:description'
        },{
            fieldLabel: 'Last Modification Date',
            name: 'cmis:lastModificationDate'
        }],

        buttons: [{
            text: 'Save',
            handler: function() {
            	Ext.app.msg('尼玛','尼玛啊');
                this.up('form').getForm().isValid();
            }
        },{
            text: 'Cancel',
            handler: function() {
                this.up('window').close( );
            }
        }]
    },
    
    initComponent: function() {
    	this.items= [{
            region: 'center',
            xtype: 'tabpanel',
            items: [
            this.simple, 
            {
                title: '扩展属性',
                html: 'Hello world 2'
            }, {
                title: '附件信息',
                html: 'Hello world 3'
            }]
        }],
        this.callParent(arguments);
    	this.down('form').loadRecord(this.record);
    },
});

