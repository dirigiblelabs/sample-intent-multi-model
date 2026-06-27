/*
 * Navigation group for the shared application shell. Defined once here and referenced by id
 * (group: settings) from the domain projects' entities, so the group is not declared multiple times
 * (the shell drops duplicate group ids).
 */
exports.getPerspectiveGroup = () => ({
	id: 'settings',
	label: 'Settings',
	expanded: true,
	order: 40,
	icon: '/services/web/resources/unicons/setting.svg',
	items: []
});
