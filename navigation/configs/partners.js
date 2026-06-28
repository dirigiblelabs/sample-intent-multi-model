/*
 * Navigation group for the shared application shell. Defined once here and referenced by id
 * (group: master-data) from the domain projects' entities, so the group is not declared multiple times
 * (the shell drops duplicate group ids).
 */
exports.getPerspectiveGroup = () => ({
	id: 'partners',
	label: 'Partners',
	expanded: true,
	order: 10,
	icon: '/services/web/resources/unicons/database.svg',
	items: []
});
