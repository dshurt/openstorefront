<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="../../../../client/layout/adminlayout.jsp">
    <stripes:layout-component name="contents">

		<script type="text/javascript">
			/* global Ext, CoreUtil */
			Ext.onReady(function () {


				var jobStore = Ext.create('Ext.data.Store', {
					storeId: 'jobStore',
					autoLoad: true,
					proxy: {
						type: 'ajax',
						url: '/openstorefront/api/v1/service/jobs'
					}
				});

				var jobGrid = Ext.create('Ext.grid.Panel', {
					title: 'Jobs',
					id: 'jobGrid',
					store: jobStore,
					columnLines: true,
					columns: [
						{
							text: 'Job Name',
							dataIndex: 'jobName',
							flex: 3,
							cellWrap: true
						},
						{
							text: 'Group Name',
							dataIndex: 'groupName',
							flex: 1
						},
						{
							text: 'Description', 
							dataIndex: 'description',
							flex: 4
						},
						{
							text: 'Status', 
							dataIndex: 'status',
							flex: 1
						},
						{
							text: 'Previous Fire Time', 
							dataIndex: 'perviousFiredTime',
							flex: 2,
							xtype: 'datecolumn',
							format: 'm/d/y H:i:s'
						},
						{
							text: 'Next Fire Time',
							dataIndex: 'nextFiredTime',
							flex: 2,
							xtype: 'datecolumn',
							format: 'm/d/y H:i:s'
						},
						{
							text: 'Job Data', 
							dataIndex: 'jobData',
							flex: 6,
							cellWrap: true
						}
					],
					dockedItems: [
						{
							dock: 'top',
							xtype: 'toolbar',
							items: [
								{
									scale: 'medium',
									id: 'jobGrid-jobPause',
									iconCls: 'fa fa-2x fa-pause',
									text: 'Pause',
									tooltip: 'Pause the selected job',
									name: 'individualJobControl',
									disabled: true,
									handler: function () {
									}
								},
								{
									scale: 'medium',
									id: 'jobGrid-jobResume',
									iconCls: 'fa fa-2x fa-play',
									text: 'Resume',
									tooltip: 'Resume the selected job',
									name: 'individualJobControl',
									disabled: true,
									handler: function () {
									}
								},
								{
									scale: 'medium',
									id: 'jobGrid-jobExecute',
									iconCls: 'fa fa-2x fa-bolt icon-vertical-correction',
									text: 'Run',
									tooltip: 'Execute the selected job',
									name: 'individualJobControl',
									disabled: true,
									handler: function () {
									}
								},
								{ 
									xtype: 'tbfill'
								},
								{ 
									xtype: 'label',
									text: 'Show Integration Jobs:'
								},
								{
									xtype: 'segmentedbutton',
									scale: 'medium',
									items: [  
										{
											enableToggle: true,
											scale: 'medium',
											toggleGroup: 'intJobs',
											id: 'jobGrid-showIntegrationBox',
											text: 'Yes',
											pressed: true,
											name: 'showIntegration',
											handler: function () {
												jobStore.clearFilter();
											}
										},
										{
											enableToggle: true,
											scale: 'medium',
											toggleGroup: 'intJobs',
											id: 'jobGrid-noshowIntegrationBox',
											text: 'No',
											name: 'showIntegration',
											handler: function () {
												jobStore.filterBy(function(record) {
													return record.data.jobName.indexOf('ComponentJob');
												});
											}
										}
									]
								},
								{ 
									xtype: 'tbseparator'
								},
								{
									xtype: 'label',
									html: '<ext title="The scheduler state is not persistent. When the web application is restarted, the scheduler state will be reset.">System Job Scheduler:',
									style: {
										fontWeight: 'bold'
									}
								},
								{
									xtype: 'label',
									id: 'schedulerStatusLabel',
									text: 'Running',
									style: {
										color: 'green',
										fontWeight: 'bold'
									}
								},
								{
									scale: 'medium',
									toggleGroup: 'scheduler',
									id: 'jobGrid-schedulerToggleButton',
									iconCls: 'fa fa-2x fa-pause',
									text: 'Pause',
									tooltip: 'Toggle the scheduler status',
									name: 'schedulerControl',
									handler: function () {
										toggleScheduler();
									}
								}
							]
						}
					],
					listeners: {
						itemdblclick: function (grid, record, item, index, e, opts) {
						},
						selectionchange: function (grid, record, eOpts) {
							if (Ext.getCmp('jobGrid').getSelectionModel().hasSelection()) {


							} else {

							}
						}
					}
				});

				var taskStore = Ext.create('Ext.data.Store', {
					storeId: 'taskStore',
					autoLoad: true,
					proxy: {
						type: 'ajax',
						url: '/openstorefront/api/v1/service/jobs/tasks/status'
					}
				});


				var taskGrid = Ext.create('Ext.grid.Panel', {
					title: 'Tasks',
					id: 'taskGrid',
					store: taskStore,
					columnLines: true,
					columns: [
						{text: 'Task Name', dataIndex: 'taskName', flex: 1}
					],
					dockedItems: [
					],
					listeners: {
						itemdblclick: function (grid, record, item, index, e, opts) {
						},
						selectionchange: function (grid, record, eOpts) {
							if (Ext.getCmp('taskGrid').getSelectionModel().hasSelection()) {


							} else {

							}
						}
					}
				});

				var jobsMainPanel = Ext.create('Ext.tab.Panel', {
					title: 'Manage Jobs <i class="fa fa-question-circle"  data-qtip="Control and view scheduled jobs and background tasks."></i>',
					width: 400,
					height: 400,
					items: [jobGrid, taskGrid]
				});

				Ext.create('Ext.container.Viewport', {
					layout: 'fit',
					items: [
						jobsMainPanel
					]
				});


				var toggleScheduler = function toggleScheduler() {
					if (Ext.getCmp('schedulerStatusLabel').text === 'Running'){
						var what = 'pause';
						var url = '/openstorefront/api/v1/service/jobs/pause';
						var method = 'POST';
					}
					else {
						var what = 'resume';
						var url = '/openstorefront/api/v1/service/jobs/resume';
						var method = 'POST';
					}
					Ext.Ajax.request({
						url: url,
						method: method,
						success: function (response, opts) {
							var message = 'Successfully ' + what + 'd job scheduler';
							Ext.toast(message, '', 'tr');
							updateSchedulerStatus();
						},
						failure: function (response, opts) {
							Ext.MessageBox.alert( "Error: Could not " + what + ' job scheduler');
						}
					});	
				};


				var updateSchedulerStatus = function updateSchedulerStatus() {
					Ext.Ajax.request({
						url: '/openstorefront/api/v1/service/jobs/status',
						success: function (response, opts) {
							var rsp = Ext.decode(response.responseText);
							var label = Ext.getCmp('schedulerStatusLabel');
							var button = Ext.getCmp('jobGrid-schedulerToggleButton');
							if (rsp.status === 'Running') {
								label.setText('Running');
								label.setStyle({color: 'green'});
								button.setText('Pause');
								button.setIconCls('fa fa-pause fa-2x');
							}
							else {
								label.setText('Paused');
								label.setStyle({color: 'red'});
								button.setText('Resume');
								button.setIconCls('fa fa-play fa-2x');
							}
						},
						failure: function (response, opts) {
							Ext.MessageBox.alert('Error', 'Unable to update system scheduler status.');
						}
					});		
				};
				
				updateSchedulerStatus();


			});

		</script>
    </stripes:layout-component>
</stripes:layout-render>