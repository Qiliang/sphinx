Ext.define('sphinx.view.document.ContentGrid' ,{
    extend: 'Ext.grid.Panel',
    
    alias: 'widget.contentGrid',

    id:'view_document_ContentGrid',
    title: 'ContentGrid',

    store: 'Objects',
    
    
    initComponent: function() {
    	this.selModel=Ext.create('Ext.selection.CheckboxModel');
    	this.columns= [
                  {
                      text: 'objectId',
                      width: '17%',
                      sortable: false,
                      hideable: false,
                      dataIndex: 'cmis:objectId'
                  },
                  {
                      text: 'name',
                      width: '17%',
                      dataIndex: 'cmis:name',
                      hidden: false
                  },
                  {
                      text: 'lastModificationDate',
                      width: '20%',
                      dataIndex: 'cmis:lastModificationDate'
                  },
                  {
                      text: 'lastModifiedBy',
                      width: '17%',
                      dataIndex: 'cmis:lastModifiedBy'
                  },
                  {
                      text: 'objectTypeId',
                      width: '17%',
                      dataIndex: 'cmis:objectTypeId'
                  }
              ];
    	this.dockedItems = [{
        	xtype: 'panel',
        	dock: 'top',
            items:[
						{
							itemId:'path',
							xtype: 'component',
							autoEl: {
				                tag: 'div',
				                html:'路径：</span><span style="font-size: 12px;font-weight: bolder;">文档库/目录1/目录2'
				                	}
							
						},{
				            xtype: 'toolbar',
				            items: [
				                {text:'新建', menu: this.newMenu },
				                Ext.create('Ext.Action', { text: '删除'}),
				                {text:'剪切板',menu: this.chipboardMenu},
				                Ext.create('Ext.form.field.Text', { emptyText: '按名称搜索',width: 135})
				            ]
				        }
                     ]
            
        },{
		    xtype: 'pagingtoolbar',
		    store: this.getStore(),
		    dock: 'bottom',
		    displayInfo: true
		}];
    	
    	this.callParent(arguments);
    	
    },
    
    setFolder : function(record){
    	this.folder = record;
    	var component =this.down('component[itemId=path]');
    	component.update(record.get('cmis:path'));
    },
    
    getFolder : function(record){
    	return this.folder;
    },
    
    newMenu: {
    	xtype: 'menu',
    	plain: true,
        items: [
            {
            	itemId:'newDocument',
               text: '新建文档'
            }, '-',{
               text: '新建报销单'
           },{
        	   itemId:'newFolder',
               text: '新建文件夹'
           }
        ]
    },
    chipboardMenu: {
        xtype: 'menu',
        plain: true,
        items: {
            xtype: 'buttongroup',
            title: '剪切板',
            columns: 2,
            defaults: {
                xtype: 'button',
                scale: 'large',
                iconAlign: 'left',
            },
            items: [{
                text:'剪切板',
                width: '100%',
            },{
            	text:'粘贴',
                width: 60,
            },{
                colspan: 2,
                width: '100%',
                text: '剪切',
                scale: 'small',
            },{
                colspan: 2,
                width: '100%',
                text: '复制',
                scale: 'small',
            }]
        }
    }
    
    
    
});
