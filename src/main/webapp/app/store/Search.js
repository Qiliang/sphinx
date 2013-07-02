Ext.define('sphinx.store.Search', {
    extend: 'Ext.data.ArrayStore',
    autoLoad: true,
    fields: [
             'name',
             {name: 'price', type: 'float'},
             {name: 'change', type: 'float'},
             {name: 'pctChange', type: 'float'},
             {name: 'lastChange', type: 'date', dateFormat: 'n/j h:ia'}
          ],
    data: [
           ['3m Co',71.72,0.02,0.03,'9/1 12:00am'],
    ['Alcoa Inc',29.01,0.42,1.47,'9/1 12:00am'],
    ['Boeing Co.',75.43,0.53,0.71,'9/1 12:00am'],
    ['Hewlett-Packard Co.',36.53,-0.03,-0.08,'9/1 12:00am'],
    ['Wal-Mart Stores, Inc.',45.45,0.73,1.63,'9/1 12:00am']
           ]
    
});