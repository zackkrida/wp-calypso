/**
 * Generates an url pointing to Google Admin for the specified user.
 *
 * @param {string} email - email
 * @returns {string} - the corresponding url
 */
export function getGoogleAdminUrl( email ) {
	return (
		'https://accounts.google.com/AccountChooser?' +
		`Email=${ encodeURIComponent( email ) }` +
		`&service=CPanel` +
		`&continue=${ encodeURIComponent( `https://admin.google.com/` ) }`
	);
}

/**
 * Generates an url pointing to Google Admin and its Reseller ToS page for the specified user.
 *
 * @param {string} email - email
 * @param {string} domain_name - domain name
 * @returns {string} - the corresponding url
 */
export function getGoogleAdminWithTosUrl( email, domain_name ) {
	return (
		'https://accounts.google.com/AccountChooser?' +
		`Email=${ encodeURIComponent( email ) }` +
		`&service=CPanel` +
		`&continue=${ encodeURIComponent( `https://admin.google.com/${ domain_name }/AcceptTermsOfService` ) }`
	);
}
