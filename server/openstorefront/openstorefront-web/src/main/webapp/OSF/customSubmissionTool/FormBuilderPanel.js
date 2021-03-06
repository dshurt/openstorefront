/* 
 * Copyright 2018 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /* Author: cyearsley */
/* global Ext, CoreUtil, CoreService */

Ext.define('OSF.customSubmissionTool.FormBuilderPanel', {
	extend: 'Ext.panel.Panel',
	requires: [
		'OSF.customSubmissionTool.FormBuilderItem',
		'OSF.customSubmissionTool.FormTemplateInfoPanel',
		'OSF.customSubmissionTool.SectionNavPanel',
		'OSF.customSubmissionTool.TemplateProgressPanel',
		'OSF.customSubmissionTool.FieldDisplayPanel',
		'OSF.customSubmissionTool.AttributeProgressPanel'
	],

	width: '100%',		
	style: 'background: #6c6c6c',
	itemId: 'formBuilderPanel',
	activeItem: null,
	activeSection: null,
	layout: {
		type: 'border'
	},
	items: [
		{
			width: 450,
			xtype: 'tabpanel',
			region: 'west',
			title: 'Template Information',
			itemId: 'tools',
			titleCollapse: true,
			collapsible: true,		
			split: true,
			style: 'background: #fff;',
			dockedItems: [
				{
					xtype: 'panel',
					itemId: 'lastSaved',
					dock: 'bottom',
					margin: '0 0 0 10',
					tpl: '<b>Last Saved: </b> {[Ext.Date.format(values.updateDts, "F j, Y, g:i a")]}'+
					 ' <tpl if="unsavedChanges"><span class="text-danger"><i class="fa fa-lg fa-exclamation-triangle"></i> Unsaved Changes</span></tpl> '	
				}
			]
		},
		{
			xtype: 'osf-csf-displaypanel',
			itemId: 'fieldDisplayPanel',
			flex: 0.75,			
			region: 'center'
		}
	],

	fieldTypeMapping: [
		{
			fieldType: 'shortAnswer',
			mapping: []
		},
		{
			fieldType: 'longAnswer',
			mapping: []
		},
		{
			fieldType: 'radio',
			mapping: []
		},
		{
			fieldType: 'checkbox',
			mapping: []
		},
		{
			fieldType: 'combo',
			mapping: []
		},
		{
			fieldType: 'grid',
			mapping: []
		},
		{
			fieldType: 'form',
			mapping: []
		}
	],

	/**
	 * When adding section item field types... do it here.
	 */
	validSectionItems: [
		'question',
		'fieldType',
		'labelCode',
		'mappedTo'
	],

	templateRecord: undefined,
	changed: false,
	hasChanges: function(){
		var formBuilderPanel = this;		
		return formBuilderPanel.changed;
	},
	
	markAsChanged: function(){
		var formBuilderPanel = this;
		//skip initial load
		if (!formBuilderPanel.initialLoad) {
			formBuilderPanel.changed = true;
					
			formBuilderPanel.queryById('lastSaved').update(Ext.apply({
				unsavedChanges: formBuilderPanel.changed
			}, formBuilderPanel.templateRecord));		
		
		}
	},
	markAsSaved: function(){
		var formBuilderPanel = this;
		formBuilderPanel.changed = false;
		
		formBuilderPanel.queryById('lastSaved').update(Ext.apply({
			unsavedChanges: formBuilderPanel.changed
		}, formBuilderPanel.templateRecord));	
	}, 
	
	initComponent: function () {

		this.callParent();
		var formBuilderPanel = this;

		formBuilderPanel.initialLoad = true;

		formBuilderPanel.queryById('tools').add(
			[
				{
					xtype: 'osf-csf-sectionnavpanel',					
					width: '100%',					
					itemId: 'sectionPanel',
					scrollable: true,
					formBuilderPanel: formBuilderPanel,
					templateRecord: formBuilderPanel.templateRecord
				},
				{
					xtype: 'tabpanel',
					width: '100%',					
					title: 'Mappings',
					items: [
						{
							xtype: 'osf-csf-templateprogresspanel',
							itemId: 'templateProgress',
							width: '100%',
							templateRecord: formBuilderPanel.templateRecord
						},
						{
							xtype: 'osf-csf-attributeprogresspanel',
							itemId: 'requiredAttributesProgress',
							templateRecord: formBuilderPanel.templateRecord,
							displayRequiredAttributes: true
						},
						{
							xtype: 'osf-csf-attributeprogresspanel',
							itemId: 'optionalAttributesProgress',
							templateRecord: formBuilderPanel.templateRecord,
							displayRequiredAttributes: false
						}
					]
				},
				{
					xtype: 'osf-form-templateinfo-panel',
					itemId: 'infoPanel',
					title: 'Form',					
					formBuilderPanel: formBuilderPanel,
					templateRecord: formBuilderPanel.templateRecord
				}				
			]
		);
		formBuilderPanel.queryById('tools').setActiveItem(0);

		formBuilderPanel.displayPanel = formBuilderPanel.queryById('fieldDisplayPanel');
		formBuilderPanel.itemContainer = formBuilderPanel.queryById('itemContainer');
		formBuilderPanel.floatingMenu = formBuilderPanel.queryById('floatingMenu');
		formBuilderPanel.sectionPanel = formBuilderPanel.queryById('sectionPanel');
		formBuilderPanel.templateProgressPanel = formBuilderPanel.queryById('templateProgress');
		formBuilderPanel.requiredAttrProgressPanel = formBuilderPanel.queryById('requiredAttributesProgress');
		formBuilderPanel.optionalAttrProgressPanel = formBuilderPanel.queryById('optionalAttributesProgress');

		formBuilderPanel.displayPanel.formBuilderPanel = formBuilderPanel;

		// if no template record section has been defined, define it
		if (!formBuilderPanel.templateRecord.sections) {
			formBuilderPanel.templateRecord.sections = [];
		}

		// loads the first section in the tempateRecord
		//formBuilderPanel.displayPanel.loadSection(formBuilderPanel.templateRecord.sections[0]);

		formBuilderPanel.changed = false;		
		formBuilderPanel.sectionPanel.updateTemplate();
		Ext.defer(function(){
			formBuilderPanel.updateTemplate();
			formBuilderPanel.initialLoad = false;
		}, 500);
		
		
	},
	reloadAttributes: function(newEntryType) {
		var formBuilderPanel = this;
		formBuilderPanel.requiredAttrProgressPanel.reloadStore(newEntryType);
		formBuilderPanel.optionalAttrProgressPanel.reloadStore(newEntryType);
	},

	updateTemplate: function() {
		var formBuilderPanel = this;
		formBuilderPanel.queryById('lastSaved').update(Ext.apply({
			unsavedChanges: formBuilderPanel.changed
		}, formBuilderPanel.templateRecord));		
	},

	updateSection: function (section) {
		var formBuilderPanel = this;
		
		formBuilderPanel.sectionPanel.updateSection(section);
	},

	/**
	 * Saves the section by:
	 * 	1) updates the section's name and instructions
	 * 	2) retreiving all physical items from the display panel
	 * 	3) then updates the active section's fieldItems
	 * @param refreshNav - bool to indicate whether or not the nav needs to be refreshed.
	 */
	saveSection: function (refreshNav) {

		// typeof refreshNav === 'undefined' ? formBuilderPanel.activeSection.name !== sectionFormValues.name : refreshNav;
		var formBuilderPanel = this;
		var displayPanel = this.query('osf-csf-displaypanel')[0];
		var sectionFormValues = displayPanel.queryById('sectionContainer').getForm().getFieldValues();

		if (formBuilderPanel.activeSection) {

			refreshNav = typeof refreshNav === 'undefined' ? formBuilderPanel.activeSection.name !== sectionFormValues.name : refreshNav;

			formBuilderPanel.activeSection.name = sectionFormValues.name || displayPanel.untitledSectionName;
			formBuilderPanel.activeSection.instructions = sectionFormValues.instructions || '';

			if (refreshNav) {

				formBuilderPanel.sectionPanel.updateNavList();
			}

			// update the activeSection (this saves the section in the templateRecord)
			formBuilderPanel.activeSection.fieldItems = formBuilderPanel.getSection();
		}
	},

	/**
	 * Get a specific section or the currently active section.
	 * @param sectionId - provide to specify the section
	 */
	getSection: function (sectionId) {

		var formBuilderPanel = this;
		if (sectionId) {
			// look up specific section...
		}
		else {
			var items = formBuilderPanel.queryById('itemContainer').getRefItems();
			var dataItems = [];

			
			Ext.Array.forEach(items, function (el, index) {
				
				dataItems.push(formBuilderPanel.generateSectionObject(el));
			});

			return dataItems;
		}
	},

	/**
	 * Given a complex or simple object, will convert it into an object that only contains the fields needed for a section.
	 * @param sectionItem - complex/simple object representation of a section item
	 */
	generateSectionObject: function (sectionItem) {

		var resultSection = {};
		var formBuilderPanel = this;
		Ext.Array.forEach(formBuilderPanel.validSectionItems, function (item, index) {
			resultSection[item] = sectionItem[item];
		});

		return resultSection;
	}
});
