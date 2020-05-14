/**
 * Internal dependencies
 */
import config from 'config';
import { makeLayout } from 'controller';
import { details, fetchThemeDetailsData, notFoundError } from './controller';
import { setupLocale } from 'my-sites/themes';
import { getLanguageRouteParam } from 'lib/i18n-utils';

export default function ( router ) {
	if ( config.isEnabled( 'manage/themes/details' ) ) {
		const langParam = getLanguageRouteParam();

		router( '/theme', ( { res } ) => res.redirect( '/themes' ) );
		router(
			`/${ langParam }/theme/:slug/:section(setup|support)?/:site_id?`,
			setupLocale,
			fetchThemeDetailsData,
			details,
			makeLayout
		);
		router( notFoundError );
	}
}
