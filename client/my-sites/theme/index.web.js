/**
 * Internal dependencies
 */

import config from 'config';
import { makeLayout, redirectLoggedOut } from 'controller';
import { details, fetchThemeDetailsData } from './controller';
import { siteSelection } from 'my-sites/controller';
import { getLanguageRouteParam } from 'lib/i18n-utils';

function redirectToLoginIfSiteRequested( context, next ) {
	if ( context.params.site_id ) {
		redirectLoggedOut( context, next );
		return;
	}

	next();
}

export default function ( router ) {
	if ( config.isEnabled( 'manage/themes/details' ) ) {
		const langParam = getLanguageRouteParam();

		router(
			`/${ langParam }/theme/:slug/:section(setup|support)?/:site_id?`,
			redirectToLoginIfSiteRequested,
			siteSelection,
			fetchThemeDetailsData,
			details,
			makeLayout
		);
	}
}
