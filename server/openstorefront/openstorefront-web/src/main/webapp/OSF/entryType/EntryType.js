/*
 * Copyright 2018 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * See NOTICE.txt for more information.
 */


/*
 * Make a store of entries. This needs to be done for each grid panel,
 * because if grids share a store they'll also share a filter.
 */
var leftEntryStore = Ext.create('Ext.data.Store', {
	storeId: 'LeftEntryStore',
	fields: [
		// The griddragdrop plugin needs a type in order to operate!
		{
			name: 'name',
			type: 'string'
		}
	],
	sorters: new Ext.util.Sorter({
		property: 'name',
		direction: 'ASC'
	}),
	proxy: {
		id: 'entryStoreProxy',
		type: 'ajax',
		url: 'api/v1/resource/components/lookup'
	},
	autoLoad: true
});

/*
 * Create a store full of all the entry types.
 */
var entryTypeStore = Ext.create('Ext.data.Store', {
	fields: [
		'componentType',
		'updateUser',
		{
			name: 'updateDts',
			type: 'date',
			dateFormat: 'c'
		},
		'activeStatus',
		'label',
		'description',
		'componentTypeTemplate'
	],
	autoLoad: true,
	proxy: {
		type: 'ajax',
		url: 'api/v1/resource/componenttypes/lookup',
		extraParams: {
			all: true
		}
	},
	listeners: {
		// Add the "All entries" items at load time, because inserting
		// into a store after Ext factories it doesn't change it and/or breaks it.
		// Factory pattern FTW?
		load: function (store, records) {
			//Entry type is a required attribute so no "NONE" field is not necessary
//				var noEntryType = {
//					code: "NONE",
//					description: "(No entry type)",
//					id: "extModel56-999",
//					updateDts: null
//				};
			var allEntries = {
				code: "ALL",
				description: "(Show all)",
				id: "extModel56-9999",
				updateDts: null
			};
//				store.insert(0, noEntryType);
			store.insert(0, allEntries);
		}
	}
});


Ext.define('OSF.entryType.EntryType', {
	extend: 'Ext.grid.Panel',
	id: 'LeftEntryAssignment',
	alias: 'widget.entryType.LeftEntryType',
	height: "100%",
	flex: 1,
	columnWidth: .5,
	margin: 5,
	//border: 1,
	store: leftEntryStore,
	columnLines: true,

	columns: [
		{text: 'Name', dataIndex: 'description', flex: 5},
		{text: 'Type', align: 'center', dataIndex: 'componentType', flex: 1}
	],
	viewConfig: {
		plugins: {
			ptype: 'gridviewdragdrop',
			dragText: 'Drag and drop to change entry type'
		}
	},

	dockedItems: [
		{
			xtype: 'toolbar',
			dock: 'top',
			items: [
				Ext.create('OSF.component.StandardComboBox', {
					id: 'LeftEntryTypeFilter',
					emptyText: '(Show all)',
					value: 'ALL',
					fieldLabel: 'Filter by Entry Type',
					name: 'LeftEntryTypeFilter',
					typeAhead: false,
					editable: false,
					width: 200,
					listeners: {
						expand: function (e, opts) {
							//Reload to catch newly created types
							entryTypeStore.reload();
						},
						change: function (filter, newValue, oldValue, opts) {

							//console.log("From " + oldValue + " to " + newValue);

							var grid = Ext.getCmp('LeftEntryAssignment');

							// Clear any existing filter
							grid.store.clearFilter(true);

							if (newValue !== ('ALL')) {
								// Apply new filter
								grid.store.filter({
									property: 'componentType',
									value: newValue,
									exactMatch: true,
									caseSensitive: true
								});
							}

							// Reload the store to apply the new filtering.
							grid.store.reload();

						}
					},
					//emptyOptionAdded: true,

					store: entryTypeStore
				})
			]
		}
	],

	listeners: {
		beforedrop: function (node, data, overModel, dropPosition, dropHandlers) {
			// Don't do anything if the drop isn't over a node in the grid
//			if (node == null)
//				dropHandlers.processDrop();

			var filterBox = Ext.getCmp('LeftEntryTypeFilter');

			var code = filterBox.getValue();

			//console.log(data.records[0].data.componentType);

			// Don't let the user try to assign 'All' to an entry's type.
			if (code === 'ALL') {
				dropHandlers.cancelDrop();
				Ext.toast("Cannot assign entry type while filter is set to Show All");
				return false;
			}
			// Cancel the drop if the dragged entry
			// is already of the entry type in the target grid
			else if (data.records[0].data.componentType === code) {
				dropHandlers.cancelDrop();
				Ext.toast("Entry is already assigned to this type");
				return false;
			}


		},
		drop: function (node, overModel, dropPosisiont) {

			var filterBox = Ext.getCmp('LeftEntryTypeFilter');

			var code = filterBox.getValue();

			var compID = overModel.records[0].data.code;

			//console.log("Changing " + compID + " to " + code);

			Ext.Ajax.request({
				url: 'api/v1/resource/components/' + compID + "/changeComponentType?newType=" + code,
				method: 'PUT',
				headers: {
					'Accept': 'application/json'
				},
				success: function (response, opts) {
					var obj = Ext.decode(response.responseText);
					//console.dir(obj);
				},

				failure: function (response, opts) {
//						console.log("Failed to change " + compID + " to type " + code);
//						console.log('server-side failure with status code ' + response.status);
				}

			});

			var leftGrid = Ext.getCmp('LeftEntryAssignment');
			leftGrid.store.reload();
			var rightGrid = Ext.getCmp('RightEntryAssignment');
			rightGrid.store.reload();
		}
	}
});






var rightEntryStore = Ext.create('Ext.data.Store', {
	storeId: 'LeftEntryStore',
	fields: [
		// The griddragdrop plugin needs a type in order to operate!
		{
			name: 'name',
			type: 'string'
		}
	],
	sorters: new Ext.util.Sorter({
		property: 'name',
		direction: 'ASC'
	}),
	proxy: {
		id: 'entryStoreProxy',
		type: 'ajax',
		url: 'api/v1/resource/components/lookup'
	},
	autoLoad: true
});

/*
 * Create a store full of all the entry types.
 */
var entryTypeStore = Ext.create('Ext.data.Store', {
	fields: [
		'componentType',
		'updateUser',
		{
			name: 'updateDts',
			type: 'date',
			dateFormat: 'c'
		},
		'activeStatus',
		'label',
		'description',
		'componentTypeTemplate'
	],
	autoLoad: true,
	proxy: {
		type: 'ajax',
		url: 'api/v1/resource/componenttypes/lookup',
		extraParams: {
			all: true
		}
	},
	listeners: {
		// Add the "All entries" items at load time, because inserting
		// into a store after Ext factories it doesn't change it and/or breaks it.
		// Factory pattern FTW?
		load: function (store, records) {
			//Entry type is a required attribute so no "NONE" field is not necessary
//				var noEntryType = {
//					code: "NONE",
//					description: "(No entry type)",
//					id: "extModel56-999",
//					updateDts: null
//				};
			var allEntries = {
				code: "ALL",
				description: "(Show all)",
				id: "extModel56-9999",
				updateDts: null
			};
//				store.insert(0, noEntryType);
			store.insert(0, allEntries);
		}
	}
});


Ext.define('OSF.entryType.RightEntryType', {
	extend: 'Ext.grid.Panel',
	id: 'RightEntryAssignment',
	alias: 'widget.entryType.RightEntryType',
	height: "100%",
	flex: 1,
	columnWidth: .5,
	margin: 5,
	//border: 1,
	store: rightEntryStore,
	columnLines: true,

	columns: [
		{text: 'Name', dataIndex: 'description', flex: 5},
		{text: 'Type', align: 'center', dataIndex: 'componentType', flex: 1}
	],
	viewConfig: {
		plugins: {
			ptype: 'gridviewdragdrop',
			dragText: 'Drag and drop to change entry type'
		}
	},

	dockedItems: [
		{
			xtype: 'toolbar',
			dock: 'top',
			items: [
				Ext.create('OSF.component.StandardComboBox', {
					id: 'RightEntryTypeFilter',
					emptyText: '(Show all)',
					value: 'ALL',
					fieldLabel: 'Filter by Entry Type',
					name: 'RightEntryTypeFilter',
					typeAhead: false,
					editable: false,
					width: 200,
					listeners: {
						expand: function (e, opts) {
							//Reload to catch newly created types
							entryTypeStore.reload();
						},
						change: function (filter, newValue, oldValue, opts) {

							//console.log("From " + oldValue + " to " + newValue);

							var grid = Ext.getCmp('RightEntryAssignment');

							// Clear any existing filter
							grid.store.clearFilter(true);

							if (newValue !== ('ALL')) {
								// Apply new filter
								//grid.store.filter('componentType', newValue);
								grid.store.filter({
									property: 'componentType',
									value: newValue,
									exactMatch: true,
									caseSensitive: true
								});
							}

							// Reload the store to apply the new filtering.
							grid.store.reload();

						}
					},
					//emptyOptionAdded: true,

					store: entryTypeStore
				})
			]
		}
	],

	listeners: {
		beforedrop: function (node, data, overModel, dropPosition, dropHandlers) {
			// Don't do anything if the drop isn't over a node in the grid
//			if (node == null)
//				dropHandlers.processDrop();

			var filterBox = Ext.getCmp('RightEntryTypeFilter');

			var code = filterBox.getValue();

			//console.log(data.records[0].data.componentType);

			// Don't let the user try to assign 'All' to an entry's type.
			if (code === 'ALL') {
				Ext.toast("Cannot assign entry type while filter is set to Show All");
				dropHandlers.cancelDrop();
				return false;
			}
			// Cancel the drop if the dragged entry
			// is already of the entry type in the target grid
			else if (data.records[0].data.componentType === code) {
				dropHandlers.cancelDrop();
				Ext.toast("Entry is already assigned to this type");
				return false;
			}


		},
		drop: function (node, overModel, dropPosisiont) {

			var filterBox = Ext.getCmp('RightEntryTypeFilter');

			var code = filterBox.getValue();

			var compID = overModel.records[0].data.code;

			//console.log("Changing " + compID + " to " + code);

			Ext.Ajax.request({
				url: 'api/v1/resource/components/' + compID + "/changeComponentType?newType=" + code,
				method: 'PUT',
				headers: {
					'Accept': 'application/json'
				},
				success: function (response, opts) {
					var obj = Ext.decode(response.responseText);
					//console.dir(obj);
				},

				failure: function (response, opts) {
//						console.log("Failed to change " + compID + " to type " + code);
//						console.log('server-side failure with status code ' + response.status);
				}

			});

			var leftGrid = Ext.getCmp('LeftEntryAssignment');
			leftGrid.store.reload();
			var rightGrid = Ext.getCmp('RightEntryAssignment');
			rightGrid.store.reload();
		}
	}
});

