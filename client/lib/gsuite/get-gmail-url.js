/**
 * Generates an url pointing to Gmail for the specified user.
 *
 * @param {string} email - email
 * @returns {string} - the corresponding url
 */
export function getGmailUrl( email ) {
	return (
		'https://accounts.google.com/AccountChooser?' +
		`Email=${ encodeURIComponent( email ) }` +
		`&service=mail` +
		`&continue=${ encodeURIComponent( `https://mail.google.com/mail/` ) }`
	);
}
