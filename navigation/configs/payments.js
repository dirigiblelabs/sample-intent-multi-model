/*
 * Navigation group for the shared application shell. Defined once here and referenced by id
 * (group: payments) from the domain projects' entities, so the group is not declared multiple times
 * (the shell drops duplicate group ids).
 */
exports.getPerspectiveGroup = () => ({
	id: 'payments',
	label: 'Payments',
	expanded: true,
	order: 30,
	icon: '/services/web/resources/unicons/wallet.svg',
	items: []
});
