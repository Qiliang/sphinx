Ext.define('sphinx.view.search.Result' ,{
    extend: 'Ext.grid.Panel',
    
    alias: 'widget.searchResult',
    title: 'ContentGrid',
    store: 'Query',
    
    initComponent: function() {
    	var me = this;
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
							xtype: 'panel',
							autoEl: {
				                tag: 'span',
				                html:'<span>路径：搜索</span>'
				                	}
							
						},{
				            xtype: 'toolbar',
				            items: [
				                Ext.create('Ext.Action', { text: '删除'}),
				                {text:'剪切板',menu: this.chipboardMenu}
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
    	this.getStore().on('metachange',function(store, meta, eOpts){
    		var columns=[];
    		meta.fields.forEach(function(f){
    			columns.push({ text: f , dataIndex:f});
    		});
    		me.reconfigure(store, columns);
    	});
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
                iconAlign: 'left'
            },
            items: [{
                text:'剪切板',
                width: '100%'
            },{
            	text:'粘贴',
                width: 60
            },{
                colspan: 2,
                width: '100%',
                text: '剪切',
                scale: 'small'
            },{
                colspan: 2,
                width: '100%',
                text: '复制',
                scale: 'small'
            }]
        }
    }
    
    
    
});
