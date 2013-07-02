Ext.define('sphinx.view.search.SearchForm', {
    extend: 'Ext.window.Window',
    alias: 'widget.searchForm',

    layout: 'fit',
    autoShow: false,
    title: '查询',
    initComponent: function() {
    	this.items= [this.formPanel];
    	

        this.callParent(arguments);
    },    
    
    formPanel: {
    	xtype:'form',
    	autoHeight: true,
    	width   : 600,
    	bodyPadding: 10,
    	defaults: {
    		anchor: '100%',
    		labelWidth: 100
    	},
    	items   : [
    	           {
    	        	   xtype     : 'textfield',
    	        	   name      : 'email',
    	        	   fieldLabel: 'Email Address',
    	        	   vtype: 'email',
    	        	   msgTarget: 'side',
    	        	   allowBlank: false
    	           },
    	           {
    	        	   xtype: 'fieldcontainer',
    	        	   fieldLabel: 'Date Range',
    	        	   combineErrors: true,
    	        	   msgTarget : 'side',
    	        	   layout: 'hbox',
    	        	   defaults: {
    	        		   flex: 1,
    	        		   hideLabel: true
    	        	   },
    	        	   items: [
    	        	           {
    	        	        	   xtype     : 'datefield',
    	        	        	   name      : 'startDate',
    	        	        	   fieldLabel: 'Start',
    	        	        	   margin: '0 5 0 0',
    	        	        	   allowBlank: false
    	        	           },
    	        	           {
    	        	        	   xtype     : 'datefield',
    	        	        	   name      : 'endDate',
    	        	        	   fieldLabel: 'End',
    	        	        	   allowBlank: false
    	        	           }
    	        	           ]
    	           },
    	           {
    	        	   xtype: 'fieldset',
    	        	   title: 'Details',
    	        	   collapsible: true,
    	        	   defaults: {
    	        		   labelWidth: 89,
    	        		   anchor: '100%',
    	        		   layout: {
    	        			   type: 'hbox',
    	        			   defaultMargins: {top: 0, right: 5, bottom: 0, left: 0}
    	        		   }
    	        	   },
    	        	   items: [
    	        	           {
    	        	        	   xtype: 'fieldcontainer',
    	        	        	   fieldLabel: 'Phone',
    	        	        	   combineErrors: true,
    	        	        	   msgTarget: 'under',
    	        	        	   defaults: {
    	        	        		   hideLabel: true
    	        	        	   },
    	        	        	   items: [
    	        	        	           {xtype: 'displayfield', value: '('},
    	        	        	           {xtype: 'textfield',    fieldLabel: 'Phone 1', name: 'phone-1', width: 35, allowBlank: false},
    	        	        	           {xtype: 'displayfield', value: ')'},
    	        	        	           {xtype: 'textfield',    fieldLabel: 'Phone 2', name: 'phone-2', width: 35, allowBlank: false, margins: '0 5 0 0'},
    	        	        	           {xtype: 'displayfield', value: '-'},
    	        	        	           {xtype: 'textfield',    fieldLabel: 'Phone 3', name: 'phone-3', width: 48, allowBlank: false}
    	        	        	           ]
    	        	           },
    	        	           {
    	        	        	   xtype: 'fieldcontainer',
    	        	        	   fieldLabel: 'Time worked',
    	        	        	   combineErrors: false,
    	        	        	   defaults: {
    	        	        		   hideLabel: true
    	        	        	   },
    	        	        	   items: [
    	        	        	           {
    	        	        	        	   name : 'hours',
    	        	        	        	   xtype: 'numberfield',
    	        	        	        	   width: 50,
    	        	        	        	   allowBlank: false
    	        	        	           },
    	        	        	           {
    	        	        	        	   xtype: 'displayfield',
    	        	        	        	   value: 'hours'
    	        	        	           },
    	        	        	           {
    	        	        	        	   name : 'minutes',
    	        	        	        	   xtype: 'numberfield',
    	        	        	        	   width: 50,
    	        	        	        	   allowBlank: false
    	        	        	           },
    	        	        	           {
    	        	        	        	   xtype: 'displayfield',
    	        	        	        	   value: 'mins'
    	        	        	           }
    	        	        	           ]
    	        	           },
    	        	           {
    	        	        	   xtype : 'fieldcontainer',
    	        	        	   combineErrors: true,
    	        	        	   msgTarget: 'side',
    	        	        	   fieldLabel: 'Full Name',
    	        	        	   defaults: {
    	        	        		   hideLabel: true
    	        	        	   },
    	        	        	   items : [
    	        	        	            {
    	        	        	            	//the width of this field in the HBox layout is set directly
    	        	        	            	//the other 2 items are given flex: 1, so will share the rest of the space
    	        	        	            	width:          65,
    	        	        	            	
    	        	        	            	xtype:          'combo',
    	        	        	            	queryMode:      'local',
    	        	        	            	value:          'mrs',
    	        	        	            	triggerAction:  'all',
    	        	        	            	forceSelection: true,
    	        	        	            	editable:       false,
    	        	        	            	fieldLabel:     'Title',
    	        	        	            	name:           'title',
    	        	        	            	displayField:   'name',
    	        	        	            	valueField:     'value',
    	        	        	            	store:          Ext.create('Ext.data.Store', {
    	        	        	            		fields : ['name', 'value'],
    	        	        	            		data   : [
    	        	        	            		          {name : 'Mr',   value: 'mr'},
    	        	        	            		          {name : 'Mrs',  value: 'mrs'},
    	        	        	            		          {name : 'Miss', value: 'miss'}
    	        	        	            		          ]
    	        	        	            	})
    	        	        	            },
    	        	        	            {
    	        	        	            	xtype: 'textfield',
    	        	        	            	flex : 1,
    	        	        	            	name : 'firstName',
    	        	        	            	fieldLabel: 'First',
    	        	        	            	allowBlank: false
    	        	        	            },
    	        	        	            {
    	        	        	            	xtype: 'textfield',
    	        	        	            	flex : 1,
    	        	        	            	name : 'lastName',
    	        	        	            	fieldLabel: 'Last',
    	        	        	            	allowBlank: false
    	        	        	            }
    	        	        	            ]
    	        	           }
    	        	           ]
    	           }
    	           ],
    	           buttons: [
    	                     {
    	                    	 text   : 'Save',
    	                    	 handler: function() {
    	                    	 }
    	                     },
    	                     
    	                     {
    	                    	 text   : 'Reset',
    	                    	 handler: function() {
    	                    		 this.up('form').getForm().reset();
    	                    	 }
    	                     }
    	                     ]
    }
});
