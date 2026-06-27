/*
 * Navigation group for the shared application shell. Defined once here and referenced by id
 * (group: sales) from the domain projects' entities, so the group is not declared multiple times
 * (the shell drops duplicate group ids).
 */
exports.getPerspectiveGroup = () => ({
	id: 'sales',
	label: 'Sales',
	expanded: true,
	order: 20,
	icon: '/services/web/resources/unicons/receipt.svg',
	items: []
});
